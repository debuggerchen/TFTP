package tftp.client;

import tftp.packet.Acknowledge;
import tftp.packet.Packet;
import tftp.packet.Write;

import java.net.DatagramPacket;
import java.util.Scanner;

/**
 * Created by 4P on 2017/7/1.
 */
public class Main {
    public static void main(String[] args){
        boolean con = true;
        int opt ;
        Scanner in = new Scanner(System.in);
        while(con){
            System.out.println();System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("************************************");
            System.out.println("Welcome to use TFTP!");
            System.out.println("************************************");
            System.out.println("Please input the option:");
            System.out.println("1. Send Files");
            System.out.println("2. Receive Files");
            System.out.println("3. Quit");
            opt=in.nextInt();
            if(opt==3)break;

            System.out.println("Please input the host ip:");
            String ip_addr = in.next();

            in.nextLine();
            System.out.println("Please input the file addr:");
            String local_file = in.nextLine();



            System.out.println("Please input the remote file addr:");
            String remote_file = in.nextLine();

            System.out.println("Please input the sending mode(netascii:octet:mail)");
            String mode = in.nextLine();

//            System.out.println();


            if(opt==1){
                //Send

                Session session = new Session(ip_addr,mode,local_file,remote_file,Session.SEND_FILE);
//                Session session = new Session("127.0.0.10","nanana","client.rar","server.rar",Session.SEND_FILE);
//                new Thread(session).start();
                session.run();
            }else {
                //Receive
                Session session = new Session(ip_addr,mode,local_file,remote_file,Session.RECEIVE_FILE);
//                Session session = new Session("127.0.0.10","nanana","haha.rar","lingyi.rar",Session.RECEIVE_FILE);
//                new Thread(session).start();
                session.run();
            }


        }

        System.out.println("Bye~");


    }
}
