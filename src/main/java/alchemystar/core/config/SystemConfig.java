package alchemystar.core.config;

/**
 * SystemConfig
 * todo 捞取配置
 *
 * @Author lizhuyang
 */
public interface SystemConfig {

    int SERVER_PORT = 8090;

    String DEFAULT_CHARSET = "utf8";

    int REDIS_PORT = 6379;

    String REDIS_HOST = "127.0.0.1";

    int BackendInitialWaitTime = 60;

    int BackendConnectRetryTimes=3;

    int BackendInitialSize = 10;

    int BackendMaxSize = 20;
}
