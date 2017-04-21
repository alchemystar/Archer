package alchemystar.core.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import alchemystar.core.exception.ArcherException;
import io.netty.buffer.ByteBuf;

/**
 * ByteUtil
 *
 * @Author lizhuyang
 */
public class ByteUtil {

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static byte[] encode(String s) {
        try {
            return s.getBytes(DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new ArcherException(e);
        }
    }

    public static byte[] encode(String s, String charset) {
        try {
            return s.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new ArcherException(e);
        }
    }

    // 读取一行
    public static String readLine(ByteBuf byteBuf) {
        int b;
        byte c;
        StringBuilder sb = new StringBuilder();

        while (true) {
            b = byteBuf.readByte();
            if (b == '\r') {
                c = byteBuf.readByte();
                if (c == '\n') {
                    break;
                }
                sb.append((char) b);
                sb.append((char) c);
            } else {
                sb.append((char) b);
            }
        }
        String reply = sb.toString();
        if (reply.length() == 0) {
            throw new ArcherException("some thing wrong");
        }
        return reply;
    }

}
