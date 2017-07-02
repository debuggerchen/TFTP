package tftp.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by 4P on 2017/6/30.
 */
public class Acknowledge extends Packet{
    private char block_num;

    //TODO: Block num mod
    public Acknowledge(char block_num){
        OP_CODE = Packet.ACK;
        this.block_num = block_num;
    }

    public char getBlock_num() {
        return block_num;
    }
    @Override
    public byte[] getBytes() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeChar(OP_CODE);
            dos.writeChar(block_num);
            return baos.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
        }
        return new byte[0];
    }
}
