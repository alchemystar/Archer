package alchemystar.core.protocol.resp;

import alchemystar.core.protocol.RedisProtoConst;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * OKResp
 *
 * @Author lizhuyang
 */
public class OKResp {

    private static final byte[] OK = "OK".getBytes();

    public static void write(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeByte(RedisProtoConst.STATUS_REPLAY);
        buffer.writeBytes(OK);
    }
}
