package matveyeva.chat.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import matveyeva.chat.entity.User;
import matveyeva.chat.entity.User.Status;
import matveyeva.chat.exception.InvalidUserException;
import matveyeva.chat.exception.UserExistsException;
import matveyeva.chat.server.Server;
import matveyeva.chat.server.SideServer;
import org.apache.log4j.Logger;

public class UserChangesService extends DefaultService {

    private static final Logger logger = Logger.getLogger(UserChangesService.class);

    private static UserChangesService instance;

    public static UserChangesService getInstance() {
        if (instance == null) {
            instance = new UserChangesService();
        }
        return instance;
    }

    public void makeUserAdmin(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        send("Enter user name", output);
        String username = input.readLine();
        User userToAdmin;

        userToAdmin = crud.findByName(username);
        if (userToAdmin == null) {
            send("User not found", output);
        } else if (!userToAdmin.getStatus().equals(Status.BANNED)) {
            send("Do you want to make admin " + userToAdmin.getName() +
                " with status " + userToAdmin.getStatus(), output);
            send("Yes | No", output);
            String answer = input.readLine();

            if (answer.equals("1")) {
                logger.info(
                    "Admin " + user.getName() + " made admin user " + userToAdmin.getName());
                userToAdmin.setRole("ADMIN");
                for (User u : crud.findAll()) {
                    if (u.equals(userToAdmin)) {
                        u.setRole(userToAdmin.getRole());
                    }
                }
                send("User is admin now", output);
                if (userToAdmin.getStatus().equals(User.Status.ONLINE)) {
                    for (SideServer server : Server.serverList) {
                        if (server.user.equals(userToAdmin)) {
                            exit("You are admin now", server, server.output);
                        }
                    }
                }
            }

        } else {
            send("User is banned", output);
        }


    }

    public void banUser(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        send("Enter user name", output);
        String username = input.readLine();
        User usertoBan;

        usertoBan = crud.findByName(username);
        if (usertoBan == null) {
            send("User not found", output);
        } else if (!usertoBan.getStatus()
            .equals(Status.BANNED)) {
            send("Do you want to ban " + usertoBan.getName() + " with role " + usertoBan
                .getRole(), output);
            send("Yes | No", output);
            String answer = input.readLine();
            switch (Integer.parseInt(answer)) {
                case 1:
                    Status oldStatus = usertoBan.getStatus();
                    usertoBan.setStatus(User.Status.BANNED);
                    send("User banned", output);
                    for (User u : crud.findAll()) {
                        if (u.equals(usertoBan)) {
                            u.setStatus(usertoBan.getStatus());
                        }
                    }
                    if (oldStatus.equals(User.Status.ONLINE)) {
                        for (SideServer server : Server.serverList) {
                            if (server.user.equals(usertoBan)) {
                                exit("You are banned", server, server.output);
                            }
                        }
                    }
                    logger.info(
                        "Admin " + user.getName() + " banned user " + usertoBan.getName()
                            + " with role " + usertoBan.getRole());
                    break;
                case 2:
                    break;
            }
        } else {
            send("User is already banned", output);
        }

    }

    public void deleteUser(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        send("Enter user name", output);
        String username = input.readLine();
        User usertoDelete;

        usertoDelete = crud.findByName(username);
        if (usertoDelete == null) {
            send("User not found", output);
        } else {
            send(
                "Do you want to delete " + usertoDelete.getName() + " with role " + usertoDelete
                    .getRole(), output);
            send("Yes | No", output);
            String str = input.readLine();

            if (str.equals("1")) {
                if (usertoDelete.getStatus().equals(User.Status.ONLINE)) {

                    for (SideServer server : Server.serverList) {
                        if (server.user.equals(usertoDelete)) {
                            exit("You are deleted", server, server.output);
                        }
                    }
                }
                logger.info(
                    "Admin " + user.getName() + " deleted user " + usertoDelete.getName()
                        + " with role " + usertoDelete.getRole());
                crud.delete(usertoDelete);
                send("User deleted", output);
            }
        }

    }

    public void updateUser(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        send("Enter user name", output);
        String username = input.readLine();
        User usertoUpdate;
        try {
            usertoUpdate = crud.findByName(username);
            if (usertoUpdate == null) {
                send("User not found", output);
            } else if (!usertoUpdate.getStatus().equals(Status.BANNED)) {
                send("Create new username", output);
                String newUserName = input.readLine();
                send("Create new password", output);
                String newUserPass = input.readLine();
                send("Confirm password", output);
                String newConfirmPass = input.readLine();
                if (newUserPass.equals(newConfirmPass)) {
                    crud.update(usertoUpdate, newUserName, newUserPass);
                } else {
                    throw new InvalidUserException("Passwords don' match");
                }

                send("User updated", output);

                logger.info("Admin " + user.getName() + " updated user " + usertoUpdate.getName()
                    + " with role " + usertoUpdate.getRole());

                if (usertoUpdate.getStatus().equals(User.Status.ONLINE)) {
                    for (SideServer server : Server.serverList) {
                        if (server.user.equals(usertoUpdate)) {
                            exit("Your credentials updated", server, server.output);
                        }
                    }
                }
            } else {
                send("User was banned", output);
            }
        } catch (InvalidUserException | UserExistsException ex) {
            send(ex.getMessage(), output);
        }
    }

}
