package matveyeva.chat.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import matveyeva.chat.UserDB;
import matveyeva.chat.entity.Message;
import matveyeva.chat.entity.User;
import matveyeva.chat.menu.LoginMenu;

public class SideServer extends Thread {

    private Socket socket;
    private BufferedReader input;
    public BufferedWriter output;
    public User user;
    public volatile List<Message> privateMessages;

    public SideServer(Socket socket) {
        this.socket = socket;
        privateMessages = new ArrayList<>();

        try {
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Side server created");
    }

    @Override
    public void run() {
        while (true) {
            UserDB.INSTANCE.init();
            LoginMenu loginMenu = new LoginMenu(input, output,privateMessages, this);
            loginMenu.showMenu(user);
        }
    }

    public void shutdown() {
        try {
            if (!socket.isClosed()) {
                input.close();
                output.close();
                socket.close();
                List<SideServer> list = new ArrayList<SideServer>();
                for (SideServer ss : Server.serverList) {
                    if (ss.equals(this)) {
                        ss.interrupt();
                        list.add(ss);
                    }
                }

                Server.serverList.removeAll(list);
            }
        } catch (IOException ignored) {
            System.out.println(ignored.getMessage());
        }
    }

    protected void send(String msg) {
        try {
            output.write(msg + "\n");
            output.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
