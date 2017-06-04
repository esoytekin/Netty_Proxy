package com.company.discard.server;

import com.company.discard.client.DiscardClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.log4j.Logger;

/**
 * Created by emrahsoytekin on 01/06/2017.
 */
public class DiscardServer {
    int port;

    Logger logger = Logger.getLogger (DiscardServer.class);
    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws InterruptedException {

        EventLoopGroup bossgroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            logger.debug ("listening on port:"+port);
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossgroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new DiscardServerHandler());
                }
            });
            b.option(ChannelOption.SO_BACKLOG, 128);
            b.childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(port).sync();

            f.addListener (new ChannelFutureListener () {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    new DiscardClient ("localhost",port).run ();

                }
            });

            f.channel().closeFuture().sync();

        } finally {
            bossgroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        new DiscardServer (5000).run ();
    }

}
