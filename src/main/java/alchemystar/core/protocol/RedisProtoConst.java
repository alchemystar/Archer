package alchemystar.core.protocol;

/**
 * RedisProtoConst
 *
 * @Author lizhuyang
 */
public class RedisProtoConst {

    // CRLF
    public static byte[] CRLF = "\r\n".getBytes();
    // 参数数量
    public static final byte PARAM_COUNT = '*';
    // 参数字节数量
    public static final byte PARAM_LEN = '$';

    // 状态回复
    public static final byte STATUS_REPLAY = '+';
    // 错误回复
    public static final byte ERROR_REPLAY = '-';
    // 整数回复
    public static final byte INTEGER_REPLY = ':';
    // 批量回复
    public static final byte BULK_REPLY = '$';
    // 多条批量回复
    public static final byte MULTI_BULK_REPLAY = '*';

    // 负号
    public static final byte MINUS = '-';

}
