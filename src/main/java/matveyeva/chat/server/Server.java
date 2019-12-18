package matveyeva.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import matveyeva.chat.menu.LoginMenu;
import org.apache.log4j.Logger;

public class Server {

    public static final int PORT = 8080;
    public static List<SideServer> serverList = new ArrayList<>();
    private static final org.apache.log4j.Logger logger  = Logger.getLogger(Server.class);

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        logger.info("Server started");

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
            logger.info("Server closed");
            server.close();
        }
    }
}