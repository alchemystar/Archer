package alchemystar.core.handler.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * BackendRespHandler
 *
 * @Author lizhuyang
 */
public class BackendRespHandler extends ChannelHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BackendRespHandler.class);

    public static final String HANDLER_NAME = "BACKEND_RESP_HANDLER";

    private BackendConnection source;

    public BackendRespHandler(BackendConnection source) {
        this.source = source;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        source.setCtx(ctx);
        logger.info("redis connection okay");
        // 连接池同步
        source.countDown();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 写回前端
        source.getFrontend().write(msg);
        logger.info("msg write back");
    }

    public BackendConnection getSource() {
        return source;
    }
}


