package alchemystar.core.handler.frontend;

import alchemystar.core.handler.AbstractConnection;

/**
 * 前端连接
 *
 * @Author lizhuyang
 */
public class FrontendConnection extends AbstractConnection {

    public void write(Object msg) {
        ctx.writeAndFlush(msg);
    }
}
