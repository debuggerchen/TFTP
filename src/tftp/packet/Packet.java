package tftp.packet;

/**
 * Created by 4P on 2017/6/30.
 */
public abstract class Packet {
    public static final int RRQ = 1;
    public static final int WRQ = 2;
    public static final int DATA = 3;
    public static final int ACK = 4;
    public static final int ERROR = 5;

    protected char OP_CODE;

    public char getOP_CODE() {
        return OP_CODE;
    }

    public abstract byte[] getBytes();
}
