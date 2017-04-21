package alchemystar.core.handler.backend.pool;

import alchemystar.core.config.SystemConfig;
import alchemystar.core.handler.backend.BackendConnection;
import alchemystar.core.handler.frontend.FrontendConnection;

/**
 * RedisSource
 *
 * @Author lizhuyang
 */
public class RedisSource {

    private RedisPool redisPool;

    // SingleTon
    private static class LazyHolder {
        private static final RedisSource redisSource = new RedisSource();
    }

    public static final RedisSource getInstance() {
        return LazyHolder.redisSource;
    }

    private RedisSource() {
        redisPool = new RedisPool(SystemConfig.BackendInitialSize, SystemConfig.BackendMaxSize);
        redisPool.init();
    }

    public BackendConnection getBackend(FrontendConnection frontendConnection) {
        BackendConnection backend = redisPool.getBackend();
        // set the frontend
        backend.setFrontend(frontendConnection);
        return backend;
    }

    public void recycle(BackendConnection backend) {
        // clear the frontend
        backend.setFrontend(null);
        redisPool.putBackend(backend);
    }

    public RedisPool getRedisPool() {
        return redisPool;
    }

    public void setRedisPool(RedisPool redisPool) {
        this.redisPool = redisPool;
    }
}
