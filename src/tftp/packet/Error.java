package tftp.packet;

import java.io.*;

/**
 * Created by hhhhhh on 2017/6/30.
 */
public class Error extends Packet {
    public static final char NOT_DEFINED = 0;   //See error message
    public static final char FILE_NOT_FOUND = 1;
    public static final char ACCESS_VIOLATION = 2;
    public static final char DISK_FULL = 3;  //Insufficient Space
    public static final char ILLEGAL_OPERATION = 4;
    public static final char UNKNOWN_TID = 5;
    public static final char FILE_EXISTED = 6;
    public static final char NO_SUCH_USER = 7;  //Deprecated
    public static final char OTHER = 8;
    public static final char NONE_RECEIVED = 9;
    private char error_code;
    private String error_msg;

    public Error(char error_code, String error_msg){
        OP_CODE = Packet.ERROR;
        this.error_code = error_code;
        this.error_msg = error_msg;
    }

    public char getError_code() {
        return error_code;
    }

    public void setError_code(char error_code) {
        this.error_code = error_code;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    @Override
    public byte[] getBytes() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(5+error_msg.length());
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeChar(OP_CODE);
            dos.writeChar(error_code);
            dos.writeUTF(error_msg);
            dos.writeByte(0);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
