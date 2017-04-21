package alchemystar.core.handler.backend;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.core.config.SystemConfig;
import alchemystar.core.handler.backend.pool.RedisPool;

/**
 * BackendConnectionFactory
 *
 * @Author lizhuyang
 */
public class BackendConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(BackendConnectionFactory.class);

    private RedisPool redisPool;

    public BackendConnectionFactory(RedisPool pool) {
        this.redisPool = pool;
    }

    /**
     * Redis ThreadId Generator
     */
    private static final AtomicInteger ACCEPT_SEQ = new AtomicInteger(0);

    public BackendConnection getConnection() {
        BackendConnection connection = new BackendConnection();
        connection.setConnectionId(ACCEPT_SEQ.getAndIncrement());
        logger.info("connection Id=" + connection.getConnectionId());
        connection.setRedisPool(redisPool);
        connection.setCharset(SystemConfig.DEFAULT_CHARSET);
        return connection;
    }

}