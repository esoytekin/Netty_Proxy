package com.company.discard.client;


import com.company.discard.server.DiscardServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.log4j.Logger;

/**
 * Created by emrahsoytekin on 03/06/2017.
 */
public class DiscardClient {
    int remotePort;
    String remoteHost;

    Logger logger = Logger.getLogger (DiscardClient.class);
    public DiscardClient(String remoteHost, int remotePort){
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public void run() throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap ();
            b.group (workerGroup);
            b.channel (NioSocketChannel.class);
            b.option (ChannelOption.SO_KEEPALIVE,true);
            b.handler (new ChannelInitializer<SocketChannel> () {
                protected void initChannel(SocketChannel socketChannel) throws Exception {

                    socketChannel.pipeline ().addLast (new StringEncoder (),new StringDecoder (),new DiscardClientHandler ());

                }
            });

            final ChannelFuture f = b.connect(remoteHost,remotePort).sync ();
            logger.info("connected to "+ remoteHost + " at " + remotePort);

            f.channel ().writeAndFlush ("asdf");

            f.addListener (new ChannelFutureListener () {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    channelFuture.channel ().writeAndFlush ("sending message...");

                }
            });
        } finally {
            workerGroup.shutdownGracefully ();

        }


    }

    public static void main(String[] args) throws InterruptedException {
        new DiscardClient ("localhost",5000).run ();
    }
}
