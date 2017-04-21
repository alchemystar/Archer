package alchemystar.core.protocol.cmd;

/**
 * GetCmd
 *
 * @Author lizhuyang
 */
public class GetCmd extends RedisCmd {

    public static final byte[] GET = "get".getBytes();

    public GetCmd(String arg) {
        super(GET, arg);
    }
}
