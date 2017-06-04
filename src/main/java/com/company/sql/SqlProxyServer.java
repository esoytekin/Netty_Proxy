package com.company.sql;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.log4j.Logger;

/**
 * Created by emrahsoytekin on 03/06/2017.
 */
public class SqlProxyServer {
    private int localPort;
    private int remotePort;
    private String remoteHost;

    public SqlProxyServer(int localPort, String remoteHost, int remotePort){
        this.localPort = localPort;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }
    private static final Logger logger = Logger.getLogger (SqlProxyServer.class);
    public void run() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup ();
        EventLoopGroup workerGroup = new NioEventLoopGroup ();

        try {
            logger.debug ("listening on port:"+localPort);
            ServerBootstrap sb = new ServerBootstrap ();
            sb.group(bossGroup, workerGroup);
            sb.channel(NioServerSocketChannel.class);
            sb.childHandler(new ChannelInitializer<SocketChannel> () {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline ().addLast (new StringEncoder ());
//                    socketChannel.pipeline ().addLast (new StringDecoder ());
                    socketChannel.pipeline().addLast(new SqlProxyHandler (remoteHost, remotePort));
                }
            });
            sb.option(ChannelOption.SO_BACKLOG, 128);
            sb.childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = sb.bind(localPort).sync();

            f.channel ().closeFuture ().sync ();


        } finally {

            bossGroup.shutdownGracefully ();
            workerGroup.shutdownGracefully ();
        }


    }

    public static void main(String[] args) throws InterruptedException {
        new SqlProxyServer (5000,"192.168.1.103",3306).run ();
    }
}
