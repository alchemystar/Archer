package alchemystar.core.codec;

import java.util.List;

import alchemystar.core.protocol.resp.RedisResp;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

/**
 * RedisRespDecoder
 *
 * @Author lizhuyang
 */
public class RedisRespDecoder extends ReplayingDecoder {

    // Redis这种以分隔符为界定的解析,用relayingDecoder正合适
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        out.add(RedisResp.readResp(in));
    }
}
