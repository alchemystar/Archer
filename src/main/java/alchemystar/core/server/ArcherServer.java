package alchemystar.core.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.core.config.SocketConfig;
import alchemystar.core.config.SystemConfig;
import alchemystar.core.handler.backend.pool.RedisSource;
import alchemystar.core.handler.factory.FrontHandlerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * ArcherServer
 *
 * @Author lizhuyang
 */
public class ArcherServer extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ArcherServer.class);

    public static void main(String[] args) {
        ArcherServer archer = new ArcherServer();
        try {
            archer.start();
            while (true) {
                Thread.sleep(1000 * 300);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        logger.info("Start The Redis Proxy");
        startProxy();
    }

    public void startProxy() {
        // acceptor
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // worker
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 单例RedisSource的初始化
            RedisSource.getInstance();
            ServerBootstrap b = new ServerBootstrap();
            // 这边的childHandler是用来管理accept的
            // 由于线程间传递的是byte[],所以内存池okay
            // 只需要保证分配ByteBuf和write在同一个线程(函数)就行了
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new FrontHandlerFactory()).option(ChannelOption.ALLOCATOR,
                    PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SocketConfig.CONNECT_TIMEOUT_MILLIS)
                    .option(ChannelOption.SO_TIMEOUT, SocketConfig.SO_TIMEOUT);
            ChannelFuture f = b.bind(SystemConfig.SERVER_PORT).sync();
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            logger.error("监听失败" + e);
        }
    }
}
