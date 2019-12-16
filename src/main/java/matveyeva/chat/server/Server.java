package matveyeva.chat.server;

import matveyeva.chat.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Server {

    public static final int PORT = 8080;
    public static List<SideServer> serverList = new ArrayList<>();

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