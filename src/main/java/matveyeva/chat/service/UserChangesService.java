package matveyeva.chat.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import matveyeva.chat.Entity.User;
import matveyeva.chat.Entity.User.Status;
import matveyeva.chat.exception.InvalidUserException;
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
        try {
            userToAdmin = crud.findByName(username);
            if (!userToAdmin.getStatus()
                .equals(Status.BANNED)) {
                send("Do you want to make admin " + userToAdmin.getName() +
                    " with status " + userToAdmin.getStatus(), output);
                send("Yes | No", output);
                String str = input.readLine();

                if (str.equals("1")) {
                    logger.info(
                        "Admin " + user.getName() + " made admin user " + userToAdmin.getName());
                    userToAdmin.setRole("ADMIN");
                    for (User u : crud.findAll()) {
                        if (u.equals(userToAdmin)) {
                            u.setRole(userToAdmin.getRole());
                        }
                    }
                    send("User are admin now", output);
                    if (userToAdmin.getStatus().equals(User.Status.ONLINE)) {
                        SideServer serverToShut = null;
                        for (SideServer server : Server.serverList) {
                            if (server.user.equals(userToAdmin)) {
                                serverToShut = server;
                            }
                        }
                        exit("You are admin now", serverToShut.user, serverToShut, output);
                    }
                }

            } else {
                send("user banned", output);
            }
        } catch (InvalidUserException ex) {
            ex.getMessage();
        }

    }

    public void banUser(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        send("Enter user name", output);
        String username = input.readLine();
        User usertoBan;
        try {
            usertoBan = crud.findByName(username);
            if (!usertoBan.getStatus()
                .equals(Status.BANNED)) {
                send("Do you want to ban " + usertoBan.getName() + " with role " + usertoBan
                    .getRole(), output);
                send("Yes | No", output);
                String str = input.readLine();
                switch (Integer.parseInt(str)) {
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
                            SideServer serverToShut = null;
                            for (SideServer server : Server.serverList) {
                                if (server.user.equals(usertoBan)) {
                                    serverToShut = server;
                                }
                            }
                            exit("You are banned", serverToShut.user, serverToShut, output);
                        }
                        logger.info(
                            "Admin " + user.getName() + " banned user " + usertoBan.getName()
                                + " with role " + usertoBan.getRole());
                        break;
                    case 2:
                        break;
                }
            } else {
                send("User not found or already banned", output);
            }
        } catch (InvalidUserException ex) {
            send(ex.getMessage(), output);
        }
    }

    public void deleteUser(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        send("Enter user name", output);
        String username = input.readLine();
        User usertoDelete;
        try {
            usertoDelete = crud.findByName(username);
            send("Do you want to delete " + usertoDelete.getName() + " with role " + usertoDelete
                .getRole(), output);
            send("Yes | No", output);
            String str = input.readLine();

            if (str.equals("1")) {
                if (usertoDelete.getStatus().equals(User.Status.ONLINE)) {
                    SideServer serverToShut = null;
                    for (SideServer server : Server.serverList) {
                        if (server.user.equals(usertoDelete)) {
                            serverToShut = server;
                        }
                    }
                    exit("You are deleted", serverToShut.user, serverToShut, output);
                }
                logger.info(
                    "Admin " + user.getName() + " deleted user " + usertoDelete.getName()
                        + " with role " + usertoDelete.getRole());
                crud.delete(usertoDelete);
                send("User deleted", output);
            }
        } catch (InvalidUserException ex) {
            send(ex.getMessage(), output);
        }

    }

    public void updateUser(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        send("Enter user name", output);
        String username = input.readLine();
        User usertoUpdate;
        try {
            usertoUpdate = crud.findByName(username);
            if (!usertoUpdate.getStatus().equals(Status.BANNED)) {
                send("Enter new username,password", output);
                String str = input.readLine();
                crud.update(usertoUpdate, str);
                send("User updated", output);
                logger.info("Admin " + user.getName() + " updated user " + usertoUpdate.getName()
                    + " with role " + usertoUpdate.getRole());
                if (usertoUpdate.getStatus().equals(User.Status.ONLINE)) {
                    SideServer serverToShut = null;
                    for (SideServer server : Server.serverList) {
                        if (server.user.equals(usertoUpdate)) {
                            serverToShut = server;
                        }
                    }
                    exit("Your credentials updated", serverToShut.user, serverToShut, output);
                }
            } else {
                send("User was banned", output);
            }
        } catch (InvalidUserException ex) {
            send(ex.getMessage(), output);
        }
    }

}
