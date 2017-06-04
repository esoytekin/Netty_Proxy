package com.company.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;

/**
 * Created by emrahsoytekin on 03/06/2017.
 */
public class HexDumpProxyFrontendHandler  extends ChannelInboundHandlerAdapter{

    private int remotePort;
    private String remoteHost;
    private Channel outboundChannel;

    public HexDumpProxyFrontendHandler(int remotePort, String remoteHost){
        this.remotePort = remotePort;
        this.remoteHost = remoteHost;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel inboundChannel = ctx.channel();
        Bootstrap clientBootstrap = new Bootstrap();

        clientBootstrap.group(inboundChannel.eventLoop())
                .channel(inboundChannel.getClass())
//                .handler()
                .option(ChannelOption.AUTO_READ,false);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
