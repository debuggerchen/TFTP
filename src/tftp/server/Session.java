package tftp.server;

import tftp.io.ReadFile;
import tftp.io.WriteFile;
import tftp.packet.*;
import tftp.packet.Error;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

/**
 * Created by 4P on 2017/7/1.
 */
public class Session implements Runnable{
    public static final int RRQ = 0;
    public static final int WRQ = 1;

    InetAddress address;
    int port;
    String filename;
    DatagramSocket ds;
    DatagramPacket dp;
    int operation;

    public Session(DatagramPacket dp,int length){
        this.dp = dp;
        address = dp.getAddress();
        port = dp.getPort();
        System.out.println("Client:"+address.getHostAddress()+":"+port+" Connected");
        byte[] request = dp.getData();
        Packet rwq = PacketFactory.byte2Packet(request,length);
//        Packet rwq  = new Write("serveraaa.jpg","fda");
        System.out.println("Receive packet:"+(int)rwq.getOP_CODE());
        System.out.println("Packet size:"+ length);

        Random random = new Random();
        while(ds==null){
            try {
                ds = new DatagramSocket(random.nextInt(64535)+1000);
                ds.setSoTimeout(Param.WAIT_TIMEOUT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        if(rwq.getOP_CODE()==Packet.RRQ){
            System.out.println("Read Request Got");
            filename = ((Read)rwq).getFilename();
            operation = RRQ;
        }
        else if(rwq.getOP_CODE()==Packet.WRQ){
            System.out.println("Write Request Got");
            filename = ((Write)rwq).getFilename();
            operation = WRQ;
        }
        else{
            Error error = new Error(Error.ILLEGAL_OPERATION,"Invalid Request");
            send(error);
            System.exit(0);
        }


    }

    private void send(Packet p){
        byte[] data = p.getBytes();
        DatagramPacket d = new DatagramPacket(data,data.length,address,port);
        try {
            ds.send(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Packet receive(){
        byte[] buf = new byte[1024];
        DatagramPacket p = new DatagramPacket(buf,buf.length);

        try {

            ds.receive(p);

            if(p.getPort()==port) {
                System.out.println("Receive " + p.getAddress() + ":" + p.getPort());

                byte[] data = p.getData();
                Packet packet = PacketFactory.byte2Packet(data, p.getLength());
                System.out.println("Opcode:" + (int) packet.getOP_CODE());

                return packet;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Error(Error.NONE_RECEIVED,"None Received");
    }

    private void handleRead(){
        char block_num = 1;
        byte[] data = new byte[Data.MAX_SIZE];
        try {
            ReadFile rf = new ReadFile(Param.Default_dir+filename);
            while(true){

                int opt = rf.hasNext();
                int length = rf.Next(data);
                Data data1 = new Data(block_num,data,length);
                send(data1);
                System.out.println("Block #"+(int)block_num+" Size:"+length+" has been sent");
                boolean sent = false;
                int i = 0;
                while(!sent){
                    Packet packet = receive();
                    if(packet.getOP_CODE() == Packet.ERROR){
                        if(((Error)packet).getError_code()==Error.NONE_RECEIVED)
                        {
                            if(++i>Param.RETRY_TIMES){
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
//                    if(packet.getOP_CODE() == Packet.ERROR) return;
                    if(packet.getOP_CODE()!=Packet.ACK)continue;
                    if(((Acknowledge)packet).getBlock_num() == block_num){
                        block_num++;
                        sent=true;
                    }else {
                        send(data1);
                        System.out.println("Guess where am I?");
                    }
                }
                if(opt==0){

                    byte[] b = new byte[0];
                    Data d = new Data(block_num,b,0);
                    send(d);
                    sent = false;
                    int j = 0;
                    while(!sent){
                        Packet packet = receive();
                        if(packet.getOP_CODE() == Packet.ERROR){
                            if(((Error)packet).getError_code()==Error.NONE_RECEIVED)
                            {
                                if(++j>Param.RETRY_TIMES){
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

    private void handleWrite(){

        //Ack WRQ
        Acknowledge requestReceived = new Acknowledge((char)0);
        send(requestReceived);

        WriteFile wf = new WriteFile(Param.Default_dir+filename);
        //Receive data
        int size = 0;
        char block_num = 1;
        int time = 0;
        do {
            Packet packet = receive();
            if(packet.getOP_CODE() == Packet.ERROR)
            {
                if(((Error)packet).getError_code()==Error.NONE_RECEIVED){
                    if(++time>Param.RETRY_TIMES){
                        wf.close();
                        wf.delete();
                        return;
                    }
//                    else{
//                        Acknowledge ack = new Acknowledge((char)(block_num-1));
//                        send(ack);
//                    }
                }
                else{
                    System.out.println("Delete");
                    wf.close();
                    wf.delete();
                    return;
                }
            }
            if(packet.getOP_CODE()== Packet.DATA){
                if(((Data)packet).getBlock_num() == block_num) {
                    byte[] data = ((Data) packet).getData();
                    block_num = ((Data) packet).getBlock_num();
                    size = ((Data) packet).getLength();
                    try {
                        wf.write(data, size);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Error error = new Error(Error.DISK_FULL, "IO Exception");
                        send(error);
                        return;
                    }
                    Acknowledge ack = new Acknowledge(block_num++);
                    send(ack);
                }
                else{
                    Acknowledge ack = new Acknowledge(((Data)packet).getBlock_num());
                    send(ack);
                }
            }

        }while(size==512);
        wf.close();

    }

    private void handleError(){

    }

    @Override
    public void run() {
//        Packet packet = PacketFactory.RQ(dp);
        switch (operation){
            case RRQ:
                handleRead();
                break;
            case WRQ:
                handleWrite();
                break;
            default:
                Error error = new Error(Error.ILLEGAL_OPERATION,"Illegal operation");
                send(error);
                break;
        }
    }
}
