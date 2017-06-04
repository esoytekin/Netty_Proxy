package com.company.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by emrahsoytekin on 03/06/2017.
 */
public class HexDumpProxy {

    private int port;
    private String remoteHost;
    private int remotePort;
    private HexDumpProxy hexDumpProxy = this;

    public HexDumpProxy(int port, String remoteHost, int remotePort){
        hexDumpProxy.port = port;
        hexDumpProxy.remoteHost = remoteHost;
        hexDumpProxy.remotePort = remotePort;
    }

    public void run() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.INFO),
                                    new HexDumpProxyFrontendHandler(hexDumpProxy.remotePort,hexDumpProxy.remoteHost));

                        }
                    })
                    .childOption(ChannelOption.AUTO_READ,false)
                    .bind(port).sync().channel().closeFuture().sync();
        } finally {

        }
    }
}
