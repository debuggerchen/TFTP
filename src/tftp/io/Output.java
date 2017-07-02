package tftp.io;

/**
 * Created by 4P on 2017/7/2.
 */
public class Output {
    public static void ClientOutput(String s){
        System.out.println("Client: "+s);
    }
    public static void ServerOutput(String s){
        System.out.println("Server: "+s);
    }
    public static void ClientErr(String s){
        System.err.println("ClientErr: "+s);
    }
    public static void ServerErr(String s){
        System.err.println("ServerErr: "+s);
    }


}
