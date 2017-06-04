package com.company.sql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.apache.log4j.Logger;

/**
 * Created by emrahsoytekin on 03/06/2017.
 */
public class SqlProxyBackendHandler extends ChannelInboundHandlerAdapter{
    private final Channel inboundChannel;
    public static final Logger logger = Logger.getLogger (SqlProxyBackendHandler.class);

    public SqlProxyBackendHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.read ();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf copiedMsg = ((ByteBuf)msg).copy ();
        logger.debug ("received msg from server.." + SqlProxyHandler.readMessage (copiedMsg));
        inboundChannel.writeAndFlush (msg).addListener (new ChannelFutureListener () {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess ()){
                    ctx.channel ().read ();
                } else {
                    channelFuture.channel().close ();
                }

            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SqlProxyHandler.closeOnFlush (inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace ();
        SqlProxyHandler.closeOnFlush (ctx.channel ());
    }
}
