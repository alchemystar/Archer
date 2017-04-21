package alchemystar.core.handler.factory;

import alchemystar.core.codec.RedisCmdDecoder;
import alchemystar.core.handler.frontend.FrontCmdHandler;
import alchemystar.core.handler.frontend.FrontendConnection;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * 前端Handler工厂
 *
 * @Author lizhuyang
 */
public class FrontHandlerFactory extends ChannelInitializer<SocketChannel> {

    private FrontConnectionFactory factory;

    public static FrontHandlerFactory getInstance() {
        return new FrontHandlerFactory();
    }

    public FrontHandlerFactory() {
        factory = new FrontConnectionFactory();
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        FrontendConnection source = factory.getConnection();
        RedisCmdDecoder cmdDecoder = new RedisCmdDecoder();
        FrontCmdHandler cmdHandler = new FrontCmdHandler(source);
        ch.pipeline().addLast(cmdDecoder);
        ch.pipeline().addLast(cmdHandler);
    }
}
