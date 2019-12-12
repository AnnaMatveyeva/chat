package matveyeva.chat.client;

import java.io.IOException;
import java.net.Socket;

public class Client {

    public static String ipAddr = "localhost";
    public static int port = 8080;

    public static void main(String[] args) {
        try{
            Socket socket = new Socket(ipAddr,port);
            new SideClient(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}