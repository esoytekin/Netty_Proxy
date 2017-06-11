package com.company.sql;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.util.LinkedList;

/**
 * Created by emrahsoytekin on 03/06/2017.
 */
public class SqlProxyHandler extends ChannelInboundHandlerAdapter{

    private static final Logger logger = Logger.getLogger (SqlProxyHandler.class);

    private final int remotePort;
    private final String remoteHost;
    private Channel inboundChannel;
    private Channel outboundChannel;
    private SqlProxyBackendHandler  backendHandler;
    private final LinkedList<Object> inboundMsgBuffer = new LinkedList<Object> ();

    enum ConnectionStatus{
        init,
        outBoundChnnlConnecting,      //inbound connected and outbound connecting
        outBoundChnnlReady,           //inbound connected and outbound connected
        closing                       //closing inbound and outbound connection
    }

    private ConnectionStatus connectStatus = ConnectionStatus.init;



    SqlProxyHandler(String remoteHost, int remotePort){
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        inboundChannel = ctx.channel ();

        Bootstrap b = new Bootstrap ();
        b.group (inboundChannel.eventLoop ())
                .channel (inboundChannel.getClass ())
                .handler (new ChannelInitializer<SocketChannel> () {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        backendHandler = new SqlProxyBackendHandler (SqlProxyHandler.this);
                        socketChannel.pipeline ().addLast (new StringEncoder ());
//                        socketChannel.pipeline ().addLast (new StringDecoder ());
                        socketChannel.pipeline ().addLast (backendHandler);
                    }
                })
//                .handler (new SqlProxyBackendHandler (inboundChannel))

                .option (ChannelOption.AUTO_READ,false);

        ChannelFuture cf = b.connect (remoteHost,remotePort);
        connectStatus = ConnectionStatus.outBoundChnnlConnecting;

        outboundChannel = cf.channel ();

        cf.addListener (new ChannelFutureListener () {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if ( channelFuture.isSuccess () ) {
                    inboundChannel.read ();
                } else {
                    inboundChannel.close ();
                }

            }
        });


    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        switch(connectStatus){
            case outBoundChnnlReady:
                outboundChannel.writeAndFlush (msg).addListener (new ChannelFutureListener () {
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess ()) {
                            ctx.channel ().read ();
                        } else {
                            channelFuture.channel ().close ();
                        }
                    }
                });
                break;
            case closing:
                release(msg);
                break;
            case init:
                logger.error("Bad connectStatus.");
                close();
                break;
            case outBoundChnnlConnecting:
            default:
                inboundMsgBuffer.add(msg);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (outboundChannel != null) {
            closeOnFlush (outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace ();
        closeOnFlush (ctx.channel ());
    }

    static void closeOnFlush(Channel ch) {
        if (ch.isActive ()) {
            ch.writeAndFlush (Unpooled.EMPTY_BUFFER).addListener (ChannelFutureListener.CLOSE);
        }
    }

    void outBoundChannelReady() {
        inboundChannel.config().setAutoRead(true);

        connectStatus = ConnectionStatus.outBoundChnnlReady;
        for(Object obj : inboundMsgBuffer){
            outboundChannel.writeAndFlush(obj);
        }
        inboundMsgBuffer.clear();
    }

    Channel getInboundChannel() {
        return inboundChannel;
    }

    private void release(Object obj){
        if(obj instanceof ByteBuf){
            ((ByteBuf)obj).release();
        }
    }
    private void close() {
        connectStatus = ConnectionStatus.closing;
        for(Object obj : inboundMsgBuffer){
            release(obj);
        }
        inboundMsgBuffer.clear();
        closeOnFlush(inboundChannel);
        closeOnFlush(outboundChannel);
    }


    static String readMessage(ByteBuf byteBuf){
        return byteBuf.toString (Charset.defaultCharset ());
    }
}
