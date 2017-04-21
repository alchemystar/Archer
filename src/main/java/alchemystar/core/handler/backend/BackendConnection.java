package alchemystar.core.handler.backend;

import java.util.concurrent.CountDownLatch;

import alchemystar.core.handler.AbstractConnection;
import alchemystar.core.handler.backend.pool.RedisPool;
import alchemystar.core.handler.frontend.FrontendConnection;

/**
 * BackendConnection
 *
 * @Author lizhuyang
 */
public class BackendConnection extends AbstractConnection {

    public FrontendConnection frontend;

    public RedisPool redisPool;

    private Boolean isAlive;

    // 后端连接同步latch
    public CountDownLatch syncLatch;

    public BackendConnection() {

        syncLatch = new CountDownLatch(1);
    }

    public Boolean isAlive() {
        // todo alive 检测
        return true;
    }

    public void close() {
        // send quit
        ctx.close();
    }

    public RedisPool getRedisPool() {
        return redisPool;
    }

    public void setRedisPool(RedisPool redisPool) {
        this.redisPool = redisPool;
    }

    public void setAlive(Boolean alive) {
        isAlive = alive;
    }

    public FrontendConnection getFrontend() {
        return frontend;
    }

    public void setFrontend(FrontendConnection frontend) {
        this.frontend = frontend;
    }

    public void countDown() {
        if (!redisPool.isInited()) {
            redisPool.countDown();
        }
        syncLatch.countDown();
    }

}
