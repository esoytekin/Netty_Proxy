package com.company.discard.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

/**
 * Created by emrahsoytekin on 01/06/2017.
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {

    Logger logger = Logger.getLogger (DiscardServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug ("message received from client..");
        ByteBuf copied = ((ByteBuf)msg) .copy ();
        ctx.writeAndFlush (msg);

        ByteBuf in = (ByteBuf) copied;
        StringBuffer buffer = new StringBuffer ();
        try {
            while (in.isReadable()){
                buffer.append ((char)in.readByte ());
            }
            logger.debug (buffer.toString ());


        } finally {
            ReferenceCountUtil.release(copied);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

