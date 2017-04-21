package alchemystar.core.handler;

import io.netty.channel.ChannelHandlerContext;

/**
 * AbstractConnection
 *
 * @Author lizhuyang
 */
public class AbstractConnection {
    
    protected ChannelHandlerContext ctx;

    protected Integer connectionId;

    protected String charset;

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public Integer getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Integer connectionId) {
        this.connectionId = connectionId;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
