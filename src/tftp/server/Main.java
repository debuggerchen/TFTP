package tftp.server;


import tftp.packet.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by 4P on 2017/6/30.
 */
public class Main {

    public static void main(String[] args){
        try{
            DatagramSocket ds = new DatagramSocket(69);
            while(true){
                byte[] buf = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buf,buf.length);
                ds.receive(dp);

                Session session = new Session(dp,dp.getLength());
                Thread thread = new Thread(session);
                thread.start();
            }
        }catch(SocketException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
