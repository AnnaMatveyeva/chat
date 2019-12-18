package matveyeva.chat.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import matveyeva.chat.Entity.Invitation;
import matveyeva.chat.Entity.Message;
import matveyeva.chat.Entity.Room;
import matveyeva.chat.Entity.User;
import matveyeva.chat.Entity.User.Status;
import matveyeva.chat.UserCrud;
import matveyeva.chat.client.Client;
import matveyeva.chat.server.Server;
import matveyeva.chat.server.SideServer;
import org.apache.log4j.Logger;

public class LoginMenu {

    protected BufferedReader input;
    protected BufferedWriter output;
    protected UserCrud crud;
    protected User user;
    protected List<Message> publicMessagesList;
    protected volatile List<Message> privateMessages;
    protected List<Room> roomsList;
    protected List<Invitation> invitations;
    protected SideServer thisSide;
    private static final org.apache.log4j.Logger logger  = Logger.getLogger(LoginMenu.class);

    public LoginMenu(BufferedReader input, BufferedWriter output, UserCrud crud,
        User user, List<Message> publicMessagesList,
        List<Message> privateMessages, List<Room> roomsList,
        List<Invitation> invitations, SideServer thisSide) {
        this.input = input;
        this.output = output;
        this.crud = crud;
        this.user = user;
        this.publicMessagesList = publicMessagesList;
        this.privateMessages = privateMessages;
        this.roomsList = roomsList;
        this.invitations = invitations;
        this.thisSide = thisSide;
    }

    public void loginMenu() throws IOException {
        boolean check = false;
        while (!check) {
            try {
                if (thisSide.isInterrupted()) {
                    break;
                }
                send("login | registration | exit");
                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        send("Enter username,password");
                        String namePass = input.readLine();
                        if (!login(namePass)) {
                            send("Incorrect user data");
                        } else {
                            this.user.setStatus(User.Status.ONLINE);
                            crud.setUserStatus(this.user);
                            if(user.getRole().equalsIgnoreCase("USER")){
                                logger.info("User " + this.user.getName() + " logged in");
                                UserMenu userMenu = new UserMenu(input,output,crud,user,publicMessagesList,privateMessages,roomsList,invitations, thisSide);
                                userMenu.showMainMenu();
                            }else if(user.getRole().equalsIgnoreCase("ADMIN")){
                                logger.info("Admin " + this.user.getName() + " logged in");
                                AdminMenu adminMenu = new AdminMenu(input,output,crud,user,publicMessagesList,privateMessages,roomsList,invitations,thisSide);
                                adminMenu.showMainMenu();
                            }
                        }
                        break;
                    case 2:
                        send("Create new  username,password");
                        String newUser = input.readLine();
                        if (!registration(newUser)) {
                            send("Incorrect user data");
                        } else {
                            this.user.setStatus(User.Status.ONLINE);
                            crud.setUserStatus(this.user);
                            if(user.getRole().equalsIgnoreCase("USER")){
                                logger.info("User " + this.user.getName() + " was created and logged in");
                                UserMenu userMenu = new UserMenu(input,output,crud,user,publicMessagesList,privateMessages,roomsList,invitations, thisSide);
                                userMenu.showMainMenu();
                            }else if(user.getRole().equalsIgnoreCase("ADMIN")){
                                logger.info("Admin " + this.user.getName() + "was created and logged in");
                                AdminMenu adminMenu = new AdminMenu(input,output,crud,user,publicMessagesList,privateMessages,roomsList,invitations,thisSide);
                                adminMenu.showMainMenu();
                            }
                        }
                        break;
                    case 3:
                        exit("Exit from application");
                        check = true;
                        break;
                }
            } catch (NumberFormatException ex) {

            }
        }
    }

    protected void send(String msg) {
        try {
            output.write(msg + "\n");
            output.flush();
        } catch (IOException ex) {

        }
    }
    protected boolean login(String namePass) {
        if ((user = crud.findOne(namePass)) != null && this.user.getStatus() != Status.BANNED) {
            try {
                for (SideServer server : Server.serverList) {
                    if (Server.serverList.size() > 1 && !server.equals(this) && server.user
                        .equals(this.user)) {
                        return false;
                    }
                }
            } catch (NullPointerException ex) {
                return true;
            }
            return true;
        } else {
            return false;
        }
    }

    protected boolean registration(String newUser) {
        return (user = crud.create(newUser)) != null;
    }
    protected void exit(String message) throws IOException {
        if (this.user != null && !this.user.getStatus().equals(Status.BANNED)) {
            this.user.setStatus(User.Status.OFFLINE);
            crud.setUserStatus(this.user);
            logger.info("User " + user.getName() + " logged off");
        }
        crud.reloadUsers();
        this.user = null;
        if (message.equalsIgnoreCase("Exit from application") || message.contains("deleted")
            || message.contains("admin") || message.contains("banned") || message
            .contains("updated")) {
            send("Exit from application");
            thisSide.shutdown();
        }
    }
}
