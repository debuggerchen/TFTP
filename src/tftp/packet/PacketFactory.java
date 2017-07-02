package tftp.packet;

import tftp.server.Session;

import java.io.*;
import java.net.DatagramPacket;

/**
 * Created by 4P on 2017/7/1.
 */
public class PacketFactory {

    public static Packet RQ(DatagramPacket dp){
        byte[] ba = dp.getData();
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        try{
            char Opcode = dis.readChar();
            String Filename = dis.readUTF();
            dis.readByte();
            String mode = dis.readUTF();
            dis.readByte();

            if(dis.available()==0){
                switch (Opcode){
                    case Packet.RRQ:
                        return new Read(Filename,mode);
                    case Packet.WRQ:
                        return new Write(Filename,mode);
                    default:
                        return new Error(Error.ILLEGAL_OPERATION,"Received Illegal Packet");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Error(Error.ILLEGAL_OPERATION,"Packet transmission error");
    }
    public static byte[] Readfully(DataInputStream dis){
//        String s="";
        try {
            byte[] b = new byte[dis.available()];
            int i = 0;
            while (dis.available()!=0){
                b[i++] = dis.readByte();
            }
            return b;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
    public static Packet byte2Packet(byte[] data,int length){
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);

        try {
            char opcode = dis.readChar();
            switch(opcode){
                case Packet.RRQ:
                    byte[] rrq_origin = Readfully(dis);
                    String filename = null,mode = null;
                    for(int i = 0;i<rrq_origin.length;i++){
                        if(rrq_origin[i]==0){
                            filename = new String(rrq_origin,0,i);
                            mode = new String(rrq_origin,i+1,rrq_origin.length-i-2);
                            break;
                        }
                    }

                    return new Read(filename,mode);
                case Packet.WRQ:
                    byte[] wrq_origin = Readfully(dis);
                    String filename2=null,mode2=null;
                    for(int i = 0;i<wrq_origin.length;i++){
                        if(wrq_origin[i]==0){
                            filename2 = new String(wrq_origin,0,i);
                            mode2 = new String(wrq_origin,i+1,wrq_origin.length-i-2);
                            break;
                        }
                    }
                    return new Write(filename2,mode2);
                case Packet.DATA:
                    char block_num = dis.readChar();
                    Data data_packet = new Data(block_num,Readfully(dis),length-4);
                    return data_packet;
                case Packet.ACK:
                    Acknowledge ack = new Acknowledge(dis.readChar());
                    return ack;
                case Packet.ERROR:
                    char error_code = dis.readChar();
                    byte[] msg = Readfully(dis);
                    return new Error(error_code,new String(msg,0,msg.length-1));
                default:
                    return new Error(Error.OTHER,"Are you OK?");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Packet WRQ(DatagramPacket dp){
        byte[] ba = dp.getData();
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        try{
            int Opcode = dis.readChar();
            String Filename = dis.readUTF();
            dis.readByte();
            String mode = dis.readUTF();
            dis.readByte();

            if(dis.available()==0){
                return new Write(Filename,mode);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Error(Error.ILLEGAL_OPERATION,"Packet transmission error");
    }

    public static void main(String[] args){
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
            DataOutputStream dos = new DataOutputStream(baos);
            String a = "abcc";
            dos.write(a.getBytes());

            byte[] b = baos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(b);
            DataInputStream dis = new DataInputStream(bis);

            byte[] c = Readfully(dis);
            String d = new String(c);
            System.out.println(d);


            //dis.readInt();
        }catch (Exception e){
            e.printStackTrace();

        }


    }
}
