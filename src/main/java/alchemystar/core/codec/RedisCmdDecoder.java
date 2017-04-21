package alchemystar.core.codec;

import java.util.List;

import alchemystar.core.protocol.cmd.RedisCmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

/**
 * RedisCmdDecoder
 * todo singleTon
 * @Author lizhuyang
 */
public class RedisCmdDecoder extends ReplayingDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        out.add(RedisCmd.readCmd(in));
    }
}
