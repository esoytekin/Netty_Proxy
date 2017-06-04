package com.company.sql;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.log4j.Logger;

import java.net.ServerSocket;
import java.nio.charset.Charset;

/**
 * Created by emrahsoytekin on 03/06/2017.
 */
public class SqlProxyHandler extends ChannelInboundHandlerAdapter{

    public static final Logger logger = Logger.getLogger (SqlProxyHandler.class);

    int remotePort;
    String remoteHost;
    private Channel outboundChannel;
    public SqlProxyHandler(String remoteHost, int remotePort){
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel inboundChannel = ctx.channel ();

        Bootstrap b = new Bootstrap ();
        b.group (inboundChannel.eventLoop ())
                .channel (inboundChannel.getClass ())
                .handler (new ChannelInitializer<SocketChannel> () {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline ().addLast (new StringEncoder ());
//                        socketChannel.pipeline ().addLast (new StringDecoder ());
                        socketChannel.pipeline ().addLast (new SqlProxyBackendHandler (inboundChannel));
                    }
                })
//                .handler (new SqlProxyBackendHandler (inboundChannel))

                .option (ChannelOption.AUTO_READ,false);

        ChannelFuture cf = b.connect (remoteHost,remotePort);
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
        ByteBuf copiedMsg = ((ByteBuf)msg).copy ();

        String strMessage = SqlProxyHandler.readMessage (copiedMsg);
        logger.debug ("received msg from client.." + strMessage);


        if (outboundChannel.isActive ()) {
            outboundChannel.writeAndFlush (msg).addListener (new ChannelFutureListener () {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess ()) {
                        ctx.channel ().read ();
                    } else {
                        channelFuture.channel ().close ();
                    }
                }
            });
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

    static String readMessage(ByteBuf byteBuf){
        return byteBuf.toString (Charset.defaultCharset ());
    }
}
