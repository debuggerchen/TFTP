package tftp.client;

import tftp.io.ReadFile;
import tftp.io.WriteFile;
import tftp.packet.*;
import tftp.packet.Error;
import tftp.client.Param;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.Random;

/**
 * Created by 4P on 2017/7/1.
 */
public class Session implements Runnable{
    public static final int SEND_FILE = 0;
    public static final int RECEIVE_FILE = 1;

    DatagramSocket ds;
    InetAddress address;    //Remote Ip
    int port;   //协商后的Remote Port
    String mode;
    String local_filename;    //本地文件名
    String remote_filename;
    int operation;
    public Session(String host, String mode,String local_filename, String remote_filename,int operation){
        try {
            Random random = new Random();
            while(ds==null){
                try {
                    ds = new DatagramSocket(random.nextInt(64535)+1000);
                    ds.setSoTimeout(tftp.client.Param.WAIT_TIMEOUT);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }

            address = InetAddress.getByName(host);
            this.mode = mode;
            this.local_filename = local_filename;
            this.remote_filename = remote_filename;
            this.operation = operation;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void send(Packet p){
        byte[] data = p.getBytes();
//        System.out.println("Op Code:"+(int)data[1]);
        DatagramPacket dp = new DatagramPacket(data,data.length,address,port);
        try {
            ds.send(dp);
            System.out.println("Send to "+address+":"+port+" opcode:"+data[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Packet receive(){
        byte[] buf = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buf,buf.length);

        try {
            do{
                ds.receive(dp);
            }while(dp.getPort()!=port);

            byte[] data = dp.getData();
            Packet packet = PacketFactory.byte2Packet(data,dp.getLength());
            return packet;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Error(Error.NONE_RECEIVED,"IO Exception");
    }

    private void handleSend(){
        char block_num = 1;
        byte[] data = new byte[Data.MAX_SIZE];
        try {
            ReadFile rf = new ReadFile(tftp.client.Param.Default_dir+local_filename);
            while(true){

                int opt = rf.hasNext();
                int length = rf.Next(data);
                Data data1 = new Data(block_num,data,length);
                send(data1);
                boolean sent = false;
                int i = 0;
                while(!sent){
                    Packet packet = receive();
                    if(packet.getOP_CODE() == Packet.ERROR){
                        if(((Error)packet).getError_code()==Error.NONE_RECEIVED)
                        {
                            if(++i> tftp.server.Param.RETRY_TIMES){
                                return;
                            }
                            else{
                                send(data1);
                            }
                        }
                        else{
                            return;
                        }
                    }
                    if(packet.getOP_CODE()!=Packet.ACK)continue;
                    if(((Acknowledge)packet).getBlock_num() == block_num){
                        block_num++;
                        sent=true;
                    }else {
                        send(data1);
                    }
                }
                if(opt==0){

                    byte[] b = new byte[0];
                    Data d = new Data(block_num,b,0);
                    send(d);
                    sent = false;
                    int j =0;
                    while(!sent){
                        Packet packet = receive();
                        if(packet.getOP_CODE() == Packet.ERROR){
                            if(((Error)packet).getError_code()==Error.NONE_RECEIVED)
                            {
                                if(++j> tftp.server.Param.RETRY_TIMES){
                                    return;
                                }
                                else{
                                    send(data1);
                                }
                            }
                            else{
                                return;
                            }
                        }
                        if(packet.getOP_CODE()!=Packet.ACK) continue;
                        if(((Acknowledge)packet).getBlock_num() == block_num){
                            sent=true;
                            block_num++;
                        }else {
                            send(d);
                        }
                    }
                    break;
                }
                if(opt==1)break;
            }



        } catch (FileNotFoundException e) {
            Error error = new Error(Error.FILE_NOT_FOUND,"404 Not Found");
            send(error);

        }
    }

    private void handleReceive(){
        byte[] init_byte = new byte[1024];
        int block_num = 1;
        DatagramPacket init_packet = new DatagramPacket(init_byte,init_byte.length);
        WriteFile wf = new WriteFile(Param.Default_dir+local_filename);
        try {
            Packet packet1;
            boolean portset = false;
            boolean finish = false;
            while(!portset) {
                ds.receive(init_packet);
//                ds.re
                byte[] init_data = init_packet.getData();
                packet1 = PacketFactory.byte2Packet(init_data,init_packet.getLength());
                System.out.println(init_packet.getLength());
                if (packet1.getOP_CODE() == Packet.DATA && ((Data) packet1).getBlock_num() == block_num) {
                    port = init_packet.getPort();
                    portset = true;
//                    System.out.println(((Data)packet1).getData().length);
//                    System.out.println((int)((Data)packet1).getBlock_num());
                    wf.write(((Data)packet1).getData(),((Data)packet1).getLength());
                    if(((Data)packet1).getLength()<512) {
                        finish = true;
                    }
                    block_num++;
                    Acknowledge ack = new Acknowledge((char)1);
                    send(ack);
                }
                else if(packet1.getOP_CODE() == Packet.DATA && ((Data) packet1).getBlock_num() < block_num){
                    Acknowledge ack = new Acknowledge(((Data)packet1).getBlock_num());
                    send(ack);
                }
                else if(packet1.getOP_CODE() == Packet.ERROR)
                {
                    wf.delete();
                    System.err.println("Exception: " + ((Error)packet1).getError_msg());
                    return;
                }
            }

            while(!finish) {
                packet1 = receive();
                if (packet1.getOP_CODE() == Packet.DATA && ((Data) packet1).getBlock_num() == block_num) {
                    wf.write(((Data) packet1).getData(),((Data)packet1).getLength());
                    if(((Data)packet1).getLength()<512)
                        finish = true;
                    Acknowledge ack = new Acknowledge((char)block_num++);
                    send(ack);
                }
                else if(packet1.getOP_CODE() == Packet.DATA && ((Data) packet1).getBlock_num() < block_num){
                    Acknowledge ack = new Acknowledge(((Data)packet1).getBlock_num());
                    send(ack);
                }
                else if(packet1.getOP_CODE() == Packet.ERROR)
                {
                    wf.delete();
                    System.err.println("Exception at tftp.client.Session->handleReceive");
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Error error = new Error(Error.DISK_FULL,"IO Exception");
            send(error);
            return;
        }finally {
            wf.close();
        }

    }

    @Override
    public void run() {
        switch (operation){
            case SEND_FILE:
                Write wrq = new Write(remote_filename,mode);
                port = 69;
                send(wrq);

                //Port Detection
                byte[] init_packet = new byte[1024];
                DatagramPacket dp = new DatagramPacket(init_packet,init_packet.length);
                try {
                    while(true) {
                        ds.receive(dp);
                        byte[] init_data = dp.getData();
                        Packet packet = PacketFactory.byte2Packet(init_data,dp.getLength());

                        if (packet.getOP_CODE() == Packet.ACK && ((Acknowledge) packet).getBlock_num() == 0) {
                            port = dp.getPort();
                            handleSend();
                            break;
                        } else if(packet.getOP_CODE() == Packet.ERROR)  {
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case RECEIVE_FILE:
                Read rrq = new Read(remote_filename,mode);
                port = 69;
                send(rrq);
                handleReceive();
                break;
        }
    }
}
