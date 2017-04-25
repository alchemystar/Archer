package alchemystar.core.protocol.cmd;

import java.util.ArrayList;
import java.util.List;

import alchemystar.core.protocol.RedisProtoConst;
import alchemystar.core.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

/**
 * RedisCmd
 *
 * @Author lizhuyang
 */
public class RedisCmd {

    public static final Integer INIT_SIZE = 512;

    private String commandStr;
    private List<String> argsStr;

    private byte[] command;
    private byte[][] args;


    //Jedis Code
    private final static int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999,
            9999999, 99999999, 999999999, Integer.MAX_VALUE };

    private final static byte[] DigitTens = { '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3',
            '3', '3', '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4',
            '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5',
            '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8',
            '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9',
            '9', '9', '9', };

    private final static byte[] DigitOnes = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1',
            '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', };

    private final static byte[] digits = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'x', 'y', 'z' };

    public RedisCmd(String command, List<String> args) {
        commandStr = command;
        argsStr = args;
        this.command = ByteUtil.encode(command);
        if (args == null || args.size() == 0) {
            return;
        }
        this.args = new byte[args.size()][];
        for (int i = 0; i < args.size(); i++) {
            this.args[i] = ByteUtil.encode(args.get(i));
        }
    }

    public RedisCmd(byte[] command, String... args) {
        this.command = command;
        this.args = new byte[args.length][];
        for (int i = 0; i < args.length; i++) {
            this.args[i] = ByteUtil.encode(args[i]);
        }
    }

    public static RedisCmd readCmd(ByteBuf byteBuf) {
        // skip *
        byteBuf.readByte();
        int size = Integer.parseInt(ByteUtil.readLine(byteBuf));
        // skip $(command.length)\r\n
        ByteUtil.readLine(byteBuf);
        // 当前先假定Redis的Command都是String
        String commandStr = ByteUtil.readLine(byteBuf);
        List<String> argsStr = new ArrayList<String>();
        if (size <= 1) {
            return new RedisCmd(commandStr, null);
        }
        for (int i = 0; i < size - 1; i++) {
            // skip $(command.length)\r\n
            ByteUtil.readLine(byteBuf);
            argsStr.add(ByteUtil.readLine(byteBuf));
        }
        return new RedisCmd(commandStr, argsStr);
    }

    public void write(ChannelHandlerContext ctx) {
        ByteBuf byteBuf = getBuffer(ctx.alloc());
        ctx.writeAndFlush(byteBuf);
    }

    public ByteBuf getBuffer(ByteBufAllocator alloc) {
        ByteBuf byteBuf = alloc.buffer(INIT_SIZE);
        // 写入*(size)\r\n
        byteBuf.writeByte(RedisProtoConst.PARAM_COUNT);
        writeSizeCRLF(args == null ? 1 : args.length + 1, byteBuf);
        // 写入$(command.length)\r\n
        byteBuf.writeByte(RedisProtoConst.PARAM_LEN);
        writeSizeCRLF(command.length, byteBuf);
        // 写入command\r\n
        byteBuf.writeBytes(command);
        writeCRLF(byteBuf);
        // 写入args
        if (args != null) {
            for (byte[] arg : args) {
                // 写入$(size)\r\n
                byteBuf.writeByte(RedisProtoConst.PARAM_LEN);
                writeSizeCRLF(arg.length, byteBuf);
                // 写入(command)\r\n
                byteBuf.writeBytes(arg);
                writeCRLF(byteBuf);
            }
        }
        return byteBuf;
    }

    public static void writeSizeCRLF(int size, ByteBuf byteBuf) {
        // 写入args+1
        writeInt(size, byteBuf);
        // 写入CRLF
        writeCRLF(byteBuf);
    }

    // From Jedis Code
    public static void writeInt(int value, ByteBuf byteBuf) {
        // 如果<0,需要写入-|value|
        if (value < 0) {
            byteBuf.writeByte(RedisProtoConst.MINUS);
            value = -value;
        }

        int size = 0;
        while (value > sizeTable[size])
            size++;

        size++;

        int q, r;
        int charPos = size;
        byte[] buf = new byte[charPos];

        while (value >= 65536) {
            q = value / 100;
            r = value - ((q << 6) + (q << 5) + (q << 2));
            value = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        for (;;) {
            q = (value * 52429) >>> (16 + 3);
            r = value - ((q << 3) + (q << 1));
            buf[--charPos] = digits[r];
            value = q;
            if (value == 0)
                break;
        }
        byteBuf.writeBytes(buf);
    }

    public static void writeCRLF(ByteBuf byteBuf) {
        byteBuf.writeBytes(RedisProtoConst.CRLF);
    }

    @Override
    public String toString() {
        String result = commandStr + " ";
        if (argsStr != null) {
            for (String temp : argsStr) {
                result += temp + " ";
            }
        }
        return result;
    }
}
