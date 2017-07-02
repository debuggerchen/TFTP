package tftp.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by 4P on 2017/7/1.
 */
public class Write extends Packet{
    private String filename;
    private String mode;

    public Write(String filename, String mode){
        OP_CODE = Packet.WRQ;
        this.filename = filename;
        this.mode = mode;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public byte[] getBytes() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(2+filename.getBytes().length+3+mode.getBytes().length+3);
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeChar(OP_CODE);
            dos.write(filename.getBytes());
            dos.writeByte(0);
            dos.write(mode.getBytes());
            dos.writeByte(0);
            dos.flush();

            return baos.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
        }

        return new byte[0];
    }
}
