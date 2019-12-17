package matveyeva.chat.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import matveyeva.chat.Entity.Invitation;
import matveyeva.chat.menu.LoginMenu;
import matveyeva.chat.Entity.Message;
import matveyeva.chat.enums.PublicMessages;
import matveyeva.chat.Entity.Room;
import matveyeva.chat.enums.Rooms;
import matveyeva.chat.Entity.User;
import matveyeva.chat.Entity.User.Status;
import matveyeva.chat.UserCrud;

public class SideServer extends Thread {

    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private UserCrud crud;
    public User user;
    private List<Message> publicMessagesList;
    public volatile List<Message> privateMessages;
    private List<Room> roomsList;
    public volatile List<Invitation> invitations;

    public SideServer(Socket socket) {
        this.socket = socket;
        this.crud = new UserCrud();
        publicMessagesList = PublicMessages.INSTANCE.getPublicMessages();
        privateMessages = new ArrayList<>();
        invitations = new ArrayList<>();
        roomsList = Rooms.INSTANCE.getRoomsList();
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
        try {
            while (true) {

                LoginMenu loginMenu = new LoginMenu(input, output,crud,user,publicMessagesList,privateMessages,roomsList,invitations,this);
                loginMenu.loginMenu();
            }
        } catch (IOException ex) {
            this.shutdown();
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
    public void exit(String message) throws IOException {
        if (this.user != null && !this.user.getStatus().equals(Status.BANNED)) {
            this.user.setStatus(User.Status.OFFLINE);
            crud.setUserStatus(this.user);
        }
        crud.reloadUsers();
        this.user = null;
        if (message.equalsIgnoreCase("Exit from application") || message.contains("deleted")
            || message.contains("admin") || message.contains("banned") || message
            .contains("updated")) {
            send("Exit from application");
            this.shutdown();
        }
    }

    protected void send(String msg) {
        try {
            output.write(msg + "\n");
            output.flush();
        } catch (IOException ex) {

        }
    }
}
