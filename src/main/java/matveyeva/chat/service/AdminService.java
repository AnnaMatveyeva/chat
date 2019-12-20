package matveyeva.chat.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;
import matveyeva.chat.Entity.User;
import matveyeva.chat.menu.RoomChangesMenu;
import matveyeva.chat.menu.UserChangesMenu;
import org.apache.log4j.Logger;

public class AdminService extends UserService {

    private static final Logger logger = Logger.getLogger(AdminService.class);

    private static AdminService instance;

    public static AdminService getInstance() {
        if (instance == null) {
            instance = new AdminService();
        }
        return instance;
    }

    public void showAllUsers(BufferedWriter output, User user) {
        Set<User> allUsers = crud.findAll();
        if (allUsers.size() > 1) {
            send("All users:", output);
            for (User u : allUsers) {
                if (!u.equals(user)) {
                    send(u.getName() + "; role: " + u.getRole() + "; satus: " + u.getStatus(),
                        output);
                }
            }
        } else {
            send("There is nobody but you", output);
        }
    }

    public void userChangesMenu(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        UserChangesMenu userChangesMenu = new UserChangesMenu(output,input);
        userChangesMenu.showMenu(user);
    }


    public void adminRoomMenu(BufferedWriter output, BufferedReader input, User user) {
        RoomChangesMenu roomChangesMenu = new RoomChangesMenu(output,input);
        roomChangesMenu.showMenu(user);
    }

}
