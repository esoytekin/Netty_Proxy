package com.company.discard.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

/**
 * Created by emrahsoytekin on 03/06/2017.
 */
public class DiscardClientHandler extends ChannelInboundHandlerAdapter{
    Logger logger = Logger.getLogger (DiscardClientHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug ("client received: " + (String)msg);
/*        ByteBuf in = (ByteBuf) msg;
        StringBuffer message = new StringBuffer ();
        try {
            while (in.isReadable()){
                message.append ((char)in.readByte ());
            }
            logger.debug ("client received: " + message.toString ());


        } finally {
            ReferenceCountUtil.release(msg);
        }*/
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug ("closing channel.");
        super.channelInactive (ctx);
    }
}
