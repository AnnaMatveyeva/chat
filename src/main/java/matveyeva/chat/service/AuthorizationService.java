package matveyeva.chat.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import matveyeva.chat.Entity.Invitation;
import matveyeva.chat.Entity.Message;
import matveyeva.chat.Entity.Room;
import matveyeva.chat.Entity.User;
import matveyeva.chat.Entity.User.Status;
import matveyeva.chat.exception.InvalidUserException;
import matveyeva.chat.menu.AdminMenu;
import matveyeva.chat.menu.UserMenu;
import matveyeva.chat.server.Server;
import matveyeva.chat.server.SideServer;
import org.apache.log4j.Logger;

public class AuthorizationService extends DefaultService {

    private static final Logger logger = Logger.getLogger(AuthorizationService.class);

    private static AuthorizationService instance;

    public static AuthorizationService getInstance() {
        if (instance == null) {
            instance = new AuthorizationService();
        }
        return instance;
    }


    public void login(BufferedReader input, BufferedWriter output,
        User user, List<Message> publicMessagesList,
        List<Message> privateMessages, List<Room> roomsList,
        List<Invitation> invitations, SideServer thisSide) {
        try {
            send("Enter username,password", output);
            String namePass = input.readLine();
            user = login(namePass, user, output);
            user.setStatus(User.Status.ONLINE);
            thisSide.user = user;
            crud.setUserStatus(user);
            if (user.getRole().equalsIgnoreCase("USER")) {
                logger.info("User " + user.getName() + " logged in");
                UserMenu userMenu = new UserMenu(input, output, user,
                    publicMessagesList, privateMessages, roomsList, invitations,
                    thisSide);
                userMenu.showMenu();
            } else if (user.getRole().equalsIgnoreCase("ADMIN")) {
                logger.info("Admin " + user.getName() + " logged in");
                AdminMenu adminMenu = new AdminMenu(input, output, user,
                    publicMessagesList, privateMessages, roomsList, invitations,
                    thisSide);
                adminMenu.showMenu();

            }
        } catch (IOException ex) {

        } catch (
            InvalidUserException e) {
            send(e.getMessage(), output);
        }

    }

    public User login(String namePass, User user, BufferedWriter output)
        throws InvalidUserException {

        user = crud.findOne(namePass);
        if (user.getStatus() != Status.BANNED) {
            try {
                for (SideServer server : Server.serverList) {
                    if (Server.serverList.size() > 1 && !server.equals(this) && !server.user
                        .equals(user)) {
                        return user;
                    }
                }
            } catch (NullPointerException ex) {
                return user;
            }
            return user;
        } else {
            return null;
        }
    }

    public void registration(BufferedReader input, BufferedWriter output,
        User user, List<Message> publicMessagesList,
        List<Message> privateMessages, List<Room> roomsList,
        List<Invitation> invitations, SideServer thisSide) {
        try {
            send("Create new  username,password", output);
            String newUser = input.readLine();
            user = registration(newUser);
            user.setStatus(User.Status.ONLINE);
            crud.setUserStatus(user);
            thisSide.user = user;
            if (user.getRole().equalsIgnoreCase("USER")) {
                logger.info("User " + user.getName()
                    + " was created and logged in");
                UserMenu userMenu = new UserMenu(input, output, user,
                    publicMessagesList, privateMessages, roomsList, invitations,
                    thisSide);
                userMenu.showMenu();
            } else if (user.getRole().equalsIgnoreCase("ADMIN")) {
                logger
                    .info("Admin " + user.getName()
                        + "was created and logged in");
                AdminMenu adminMenu = new AdminMenu(input, output, user,
                    publicMessagesList, privateMessages, roomsList, invitations,
                    thisSide);
                adminMenu.showMenu();
            }

        } catch (IOException ex) {

        } catch (InvalidUserException e) {
            send(e.getMessage(), output);
        }

    }

    public User registration(String newUser) throws InvalidUserException {
        return crud.create(newUser);
    }


}
