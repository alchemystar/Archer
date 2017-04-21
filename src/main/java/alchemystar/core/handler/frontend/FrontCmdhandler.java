package alchemystar.core.handler.frontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.core.handler.backend.BackendConnection;
import alchemystar.core.handler.backend.pool.RedisSource;
import alchemystar.core.protocol.cmd.RedisCmd;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * FrontCmdHandler
 *
 * @Author lizhuyang
 */
public class FrontCmdHandler extends ChannelHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ChannelHandlerAdapter.class);

    private FrontendConnection source;

    private RedisSource redisSource = RedisSource.getInstance();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        source.setCtx(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RedisCmd redisCmd = (RedisCmd) msg;
        logger.info("cmd=" + redisCmd.toString());
        BackendConnection backend = redisSource.getBackend(source);
        // 写入到后端
        // backend.getCtx().writeAndFlush(msg);
        redisCmd.write(backend.getCtx());
    }

    public FrontCmdHandler(FrontendConnection source) {
        this.source = source;
    }

    public FrontendConnection getSource() {
        return source;
    }

    public void setSource(FrontendConnection source) {
        this.source = source;
    }
}
