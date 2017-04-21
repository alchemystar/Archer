package alchemystar.core.protocol.cmd;

import alchemystar.core.protocol.cmd.RedisCmd;

/**
 * SetCmd
 *
 * @Author lizhuyang
 */
public class SetCmd extends RedisCmd {

    public static final byte[] SET = "set".getBytes();

    public SetCmd(String... args) {
        super(SET, args);
    }
}
