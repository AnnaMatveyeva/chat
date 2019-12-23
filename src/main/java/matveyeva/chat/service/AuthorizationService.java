package matveyeva.chat.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import matveyeva.chat.entity.Message;
import matveyeva.chat.entity.User;
import matveyeva.chat.entity.User.Status;
import matveyeva.chat.exception.InvalidUserException;
import matveyeva.chat.exception.UserExistsException;
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
        User user, List<Message> privateMessages, SideServer thisSide) {
        try {
            send("Enter username", output);
            String username = input.readLine();
            send("Enter password", output);
            String password = input.readLine();

            user = login(username, password, user);

            if(user != null){
                user.setStatus(User.Status.ONLINE);
                thisSide.user = user;
                crud.setUserStatus(user);
                if (user.getRole().equalsIgnoreCase("USER")) {
                    logger.info("User " + user.getName() + " logged in");
                    UserMenu userMenu = new UserMenu(input, output, privateMessages,
                        thisSide);
                    userMenu.showMenu(user);
                } else if (user.getRole().equalsIgnoreCase("ADMIN")) {
                    logger.info("Admin " + user.getName() + " logged in");
                    AdminMenu adminMenu = new AdminMenu(input, output,
                        privateMessages, thisSide);
                    adminMenu.showMenu(user);

                }
            }else send("User is banned", output);
        } catch (IOException ex) {
            ex.printStackTrace();
            send("Something went wrong, try again", output);
        } catch (InvalidUserException e) {
            send(e.getMessage(), output);
        }

    }

    public User login(String userName, String password,User user) throws InvalidUserException{

        user = crud.findByName(userName);
        if(user == null){
            throw new InvalidUserException("Wrong username");
        }
        if(!user.getPassword().equals(password)){
            throw new InvalidUserException("Wrong password");
        }
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
        User user, List<Message> privateMessages, SideServer thisSide) {
        try {
            send("Create new username", output);
            String userName = input.readLine();
            send("Create new password", output);
            String userPassword = input.readLine();
            send("Confirm password", output);
            String confirmPassword = input.readLine();

            user = registration(userName, userPassword, confirmPassword);

            user.setStatus(User.Status.ONLINE);
            crud.setUserStatus(user);
            thisSide.user = user;
            if (user.getRole().equalsIgnoreCase("USER")) {
                logger.info("User " + user.getName()
                    + " was created and logged in");
                UserMenu userMenu = new UserMenu(input, output, privateMessages, thisSide);
                userMenu.showMenu(user);
            } else if (user.getRole().equalsIgnoreCase("ADMIN")) {
                logger
                    .info("Admin " + user.getName()
                        + "was created and logged in");
                AdminMenu adminMenu = new AdminMenu(input, output, privateMessages, thisSide);
                adminMenu.showMenu(user);
            }

        } catch (IOException ex) {
            send("Something went wrong, try again", output);
        } catch (InvalidUserException | UserExistsException e) {
            send(e.getMessage(), output);
        }

    }

    public User registration(String username, String userPassword, String confirmPassword)
        throws InvalidUserException, UserExistsException {
        if (userPassword.equals(confirmPassword)) {
            return crud.create(username, userPassword);
        } else {
            throw new InvalidUserException("Passwords don't match");
        }
    }


}
