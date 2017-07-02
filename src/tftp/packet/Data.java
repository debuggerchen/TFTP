package tftp.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by 4P on 2017/6/30.
 */
public class Data extends Packet {
    public static final int MAX_SIZE = 512;

    private char block_num;

    private byte[] data;

    private int length;

    public Data(char block_num, byte[] data, int length){
        OP_CODE = Packet.DATA;
        this.block_num = block_num;
        this.data = data;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    public char getBlock_num() {
        return block_num;
    }

    @Override
    public byte[] getBytes() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4+data.length);
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeChar(OP_CODE);
            dos.writeChar(block_num);
            dos.write(data,0,length);
            dos.flush();

            return baos.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
        }

        return new byte[0];
    }
}
