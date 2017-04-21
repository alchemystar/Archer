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

    public static void writeInt(int value, ByteBuf byteBuf) {
        // 如果<0,需要写入-|value|
        if (value < 0) {
            byteBuf.writeByte(RedisProtoConst.MINUS);
            value = -value;
        }
        byteBuf.writeBytes(String.valueOf(value).getBytes());
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
