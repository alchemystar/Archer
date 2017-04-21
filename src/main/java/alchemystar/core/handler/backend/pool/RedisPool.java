package alchemystar.core.handler.backend.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.core.config.SocketConfig;
import alchemystar.core.config.SystemConfig;
import alchemystar.core.exception.RetryConnectFailException;
import alchemystar.core.handler.backend.BackendConnection;
import alchemystar.core.handler.backend.BackendConnectionFactory;
import alchemystar.core.handler.backend.BackendRespHandler;
import alchemystar.core.handler.factory.BackendHandlerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * RedisPool
 *
 * @Author lizhuyang
 */
public class RedisPool {

    private static final Logger logger = LoggerFactory.getLogger(RedisPool.class);

    private static RedisPool redisPool = null;

    // 当前连接池中空闲的连接数
    private int idleCount;
    // 最大连接数
    private final int maxPoolSize;
    // 初始化连接数
    private int initSize;
    // 连接池
    private final BackendConnection[] items;
    // Backend Loop Group
    private EventLoopGroup backendGroup;
    // Backend Bootstrap
    private Bootstrap b;
    // Backend Connection Factory
    private BackendConnectionFactory factory;
    // 线程间同步的闩锁
    private CountDownLatch latch;
    // get/put的锁
    private final ReentrantLock lock;
    // 当前连接池是否被初始化成功的标识
    private final AtomicBoolean initialized;
    // data pool的command allocator
    private ByteBufAllocator allocator;

    public RedisPool(int initSize, int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        this.initSize = initSize;
        this.idleCount = 0;
        items = new BackendConnection[maxPoolSize];
        backendGroup = new NioEventLoopGroup();
        b = new Bootstrap();
        latch = new CountDownLatch(initSize);
        lock = new ReentrantLock();
        initialized = new AtomicBoolean(false);
        allocator = new UnpooledByteBufAllocator(false);
    }

    public void init() {
        factory = new BackendConnectionFactory(this);
        // todo 采用PooledBuf来减少GC
        b.group(backendGroup).channel(NioSocketChannel.class).handler(new BackendHandlerFactory(factory));
        setOption(b);
        initBackends();
        markInit();
    }

    /**
     * 初始化后端连接
     */
    private void initBackends() {
        List<ChannelFuture> futureList = new ArrayList<ChannelFuture>();
        for (int i = 0; i < initSize; i++) {
            ChannelFuture future = b.connect(SystemConfig.REDIS_HOST, SystemConfig.REDIS_PORT);
            futureList.add(future);
        }
        try {
            // awit with time out
            latch.await(SystemConfig.BackendInitialWaitTime, TimeUnit.SECONDS);
            // if reach here,the backend has been initialized
            for (ChannelFuture future : futureList) {
                future.sync();
                recycle(getInitBackendFromFuture(future));
            }
            // for gc
            latch = null;
            logger.info("the data pool start up");
        } catch (Exception e) {
            logger.error("latch fail", e);
        }
    }

    private BackendConnection createNewConnection() {
        for (int i = 0; i < SystemConfig.BackendConnectRetryTimes; i++) {
            ChannelFuture future = b.connect(SystemConfig.REDIS_HOST, SystemConfig.REDIS_PORT);
            BackendConnection backend = getBackendFromFuture(future);
            if (backend != null) {
                return backend;
            }
        }
        throw new RetryConnectFailException("Retry Connect Error Host:" + SystemConfig.REDIS_HOST + " "
                + "Port:" + SystemConfig.REDIS_PORT);
    }

    private BackendConnection getBackendFromFuture(ChannelFuture future) {
        try {
            // must wait to init the channel
            future.sync();
            BackendRespHandler respHandler =
                    (BackendRespHandler) future.channel().pipeline().get(BackendRespHandler.HANDLER_NAME);
            respHandler.getSource().syncLatch.await();
            return respHandler.getSource();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BackendConnection getInitBackendFromFuture(ChannelFuture future) {
        // beacuse the init latch in the outside,we don't need latch await here
        try {
            future.sync();
            BackendRespHandler handler =
                    (BackendRespHandler) future.channel().pipeline().get(BackendRespHandler.HANDLER_NAME);
            return handler.getSource();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void recycle(BackendConnection backend) {
        putBackend(backend);
    }

    public void discard(BackendConnection backend) {
        backend.close();
        logger.info("backendConnection discard it");
    }

    public BackendConnection getBackend() {
        BackendConnection backend = null;
        lock.lock();
        try {
            // idleCount 初始为0
            if (idleCount >= 1 && items[idleCount - 1] != null) {
                backend = items[idleCount - 1];
                idleCount--;
                return backend;
            }
        } finally {
            lock.unlock();
        }
        // must create new connection
        logger.info("create new conneciton");
        backend = createNewConnection();
        return backend;
    }

    public void putBackend(BackendConnection backend) {
        lock.lock();
        try {
            if (backend.isAlive()) {
                if (idleCount < maxPoolSize) {
                    items[idleCount] = backend;
                    idleCount++;
                } else {
                    backend.close();
                    logger.info("backendConnection too much,so close it");
                }
            } else {
                logger.info("backendConnection not alive,so discard it");
            }

        } finally {
            lock.unlock();
        }
    }

    private void markInit() {
        if (initialized.compareAndSet(false, true)) {
            initialized.set(true);
        }
    }

    public void countDown() {
        latch.countDown();
    }

    private void setOption(Bootstrap bootstrap) {

        bootstrap.option(ChannelOption.SO_RCVBUF, SocketConfig.Backend_Socket_Recv_Buf);
        bootstrap.option(ChannelOption.SO_SNDBUF, SocketConfig.Backend_Socket_Send_Buf);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SocketConfig.CONNECT_TIMEOUT_MILLIS);
        bootstrap.option(ChannelOption.SO_TIMEOUT, SocketConfig.SO_TIMEOUT);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK,
                1024 * 1024);
    }

    public boolean isInited() {
        return initialized.get();
    }

}
