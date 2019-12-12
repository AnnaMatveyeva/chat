package matveyeva.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {

    public static final int PORT = 8080;
    public static LinkedList<SideServer> serverList = new LinkedList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server Started");

        try {
            while (true) {
                Socket socket = server.accept();
                try {
                    SideServer sideServer = new SideServer(socket);
                    serverList.add(sideServer);
                    sideServer.start();
                } catch (Exception e) {
                    socket.close();
                }
            }
        } finally {
            server.close();
        }
    }
}