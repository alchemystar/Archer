package alchemystar.core.handler.factory;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.core.config.SystemConfig;
import alchemystar.core.handler.frontend.FrontendConnection;

/**
 * FrontendConnection 工厂类
 *
 * @Author lizhuyang
 */
public class FrontConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(FrontConnectionFactory.class);

    /**
     * Redis ThreadId Generator
     */
    private static final AtomicInteger ACCEPT_SEQ = new AtomicInteger(0);

    public FrontendConnection getConnection() {
        FrontendConnection connection = new FrontendConnection();
        connection.setConnectionId(ACCEPT_SEQ.getAndIncrement());
        logger.info("connection Id=" + connection.getConnectionId());
        connection.setCharset(SystemConfig.DEFAULT_CHARSET);
        return connection;
    }
}
