package alchemystar.core.handler.factory;

import alchemystar.core.codec.RedisRespDecoder;
import alchemystar.core.handler.backend.BackendConnection;
import alchemystar.core.handler.backend.BackendConnectionFactory;
import alchemystar.core.handler.backend.BackendRespHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * BackendHandlerFactory
 *
 * @Author lizhuyang
 */
public class BackendHandlerFactory extends ChannelInitializer<SocketChannel> {

    private BackendConnectionFactory factory;

    public BackendHandlerFactory(BackendConnectionFactory factory) {
        this.factory = factory;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        BackendConnection connection = factory.getConnection();
        // 暂时不需要对结果集有所改动
        // ch.pipeline().addLast(new RedisRespDecoder());
        ch.pipeline().addLast(BackendRespHandler.HANDLER_NAME, new BackendRespHandler(connection));
    }
}
