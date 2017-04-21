package alchemystar.core.protocol.resp;

import java.util.ArrayList;
import java.util.List;

import alchemystar.core.exception.ArcherException;
import alchemystar.core.protocol.RedisProtoConst;
import alchemystar.core.util.ByteUtil;
import io.netty.buffer.ByteBuf;

/**
 * @Author lizhuyang
 */
public class RedisResp {

    public static Object readResp(ByteBuf byteBuf) {
        byte b = byteBuf.readByte();
        switch (b) {
            case RedisProtoConst.ERROR_REPLAY:
                return readError(byteBuf);
            case RedisProtoConst.MULTI_BULK_REPLAY:
                return readMultiBulkReply(byteBuf);
            case RedisProtoConst.INTEGER_REPLY:
                return readInteger(byteBuf);
            case RedisProtoConst.BULK_REPLY:
                return readBulkReply(byteBuf);
            case RedisProtoConst.STATUS_REPLAY:
                return readStatusCodeReply(byteBuf);
            default:
                throw new ArcherException("Error reply: " + (char) b);
        }
    }

    private static String readError(ByteBuf byteBuf) {
        return ByteUtil.readLine(byteBuf);
    }

    private static Long readInteger(ByteBuf byteBuf) {
        return Long.valueOf(ByteUtil.readLine(byteBuf));
    }

    private static String readStatusCodeReply(ByteBuf byteBuf) {
        return ByteUtil.readLine(byteBuf);
    }

    // 可能是各种结构,所以用byte代替
    private static byte[] readBulkReply(ByteBuf byteBuf) {
        // $后紧接这就是len
        int length = Integer.parseInt(ByteUtil.readLine(byteBuf));
        // 如果为-1,表明值不存在
        if (length == -1) {
            return null;
        }
        byte[] body = new byte[length];
        // 读出body
        byteBuf.readBytes(body, 0, length);
        // skip CR LF
        byteBuf.readBytes(2);
        return body;
    }

    private static List<Object> readMultiBulkReply(ByteBuf byteBuf) {
        int replyCount = Integer.parseInt(ByteUtil.readLine(byteBuf));
        // 表明不存在
        if (replyCount == -1) {
            return null;
        }
        List<Object> result = new ArrayList<Object>();
        for (int i = 0; i < replyCount; i++) {
            result.add(readResp(byteBuf));
        }
        return result;
    }

}
