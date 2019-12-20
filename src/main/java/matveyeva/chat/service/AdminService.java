package matveyeva.chat.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import matveyeva.chat.Entity.Message;
import matveyeva.chat.Entity.Room;
import matveyeva.chat.Entity.User;
import matveyeva.chat.Entity.User.Status;
import matveyeva.chat.exception.InvalidUserException;
import matveyeva.chat.server.Server;
import matveyeva.chat.server.SideServer;
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
        boolean check = false;
        try {
            while (!check) {
                send("delete user | update user | ban user | make user admin |exit", output);
                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        deleteUser(output, input, user);
                        break;
                    case 2:
                        updateUser(output, input, user);
                        break;
                    case 3:
                        banUser(output, input, user);
                        break;
                    case 4:
                        makeUserAdmin(output, input, user);
                    case 5:
                        check = true;
                        break;
                }

            }
        } catch (NumberFormatException ex) {

        } catch (IOException e) {
            e.printStackTrace();
            send("Something went wrong", output);
        }
    }

    private void makeUserAdmin(BufferedWriter output, BufferedReader input, User user)
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
                        exit("You are admin now", serverToShut.user, serverToShut,output);
                    }
                }

            } else {
                send("user banned", output);
            }
        } catch (InvalidUserException ex) {
            ex.getMessage();
        }

    }

    private void banUser(BufferedWriter output, BufferedReader input, User user)
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
                            exit("You are banned", serverToShut.user, serverToShut,output);
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

    private void deleteUser(BufferedWriter output, BufferedReader input, User user)
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
                    exit("You are deleted", serverToShut.user, serverToShut,output);
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

    private void updateUser(BufferedWriter output, BufferedReader input, User user)
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
                    exit("Your credentials updated", serverToShut.user, serverToShut,output);
                }
            } else {
                send("User was banned", output);
            }
        } catch (InvalidUserException ex) {
            send(ex.getMessage(), output);
        }
    }

    public void adminRoomMenu(BufferedWriter output, BufferedReader input, User user,
        List<Room> roomsList) {
        boolean check = false;
        try {
            while (!check) {
                send("delete room | update room | see all rooms | create room |exit", output);
                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        deleteRoom(output, input, user, roomsList);
                        break;
                    case 2:
                        updateRoom(output, input, user, roomsList);
                        break;
                    case 3:
                        showAllRooms(output, input, user, roomsList);
                        break;
                    case 4:
                        createRoom(output, input, user, roomsList);
                        break;
                    case 5:
                        check = true;
                        break;
                }

            }
        } catch (NumberFormatException ex) {

        } catch (Exception e) {
            e.printStackTrace();
            send("Something went wrong", output);
        }
    }

    private void createRoom(BufferedWriter output, BufferedReader input, User user,
        List<Room> roomsList) throws IOException {
        send("Enter room title", output);
        String title = input.readLine();
        Room r = null;
        for (Room room : roomsList) {
            if (room.getTitle().equals(title)) {
                r = room;
                break;
            }
        }
        if (r == null) {
            r = new Room(title);
            roomsList.add(r);
            logger.info("Admin " + user.getName() + " created room " + r.getTitle());
            send("Room \"" + title + "\" was created", output);
        } else {
            send("Room with title \"" + title + "\" already exists", output);
        }
    }

    private void showAllRooms(BufferedWriter output, BufferedReader input, User user,
        List<Room> roomsList) {
        if (!roomsList.isEmpty()) {
            send("All rooms:", output);
            for (Room room : roomsList) {
                send(room.getTitle() + ", users: " + room.getUsers().size() + ";", output);
            }
        } else {
            send("There are no rooms", output);
        }
    }

    private void updateRoom(BufferedWriter output, BufferedReader input, User user,
        List<Room> roomsList) throws IOException {
        send("Enter room title", output);
        String title = input.readLine();
        Room r = null;
        send("Enter new room title", output);
        String str = input.readLine();

        for (Room room : roomsList) {
            if (room.getTitle().equals(title)) {
                room.setTitle(str);
                r = room;
                logger.info(
                    "Admin " + user.getName() + " change room title from " + title + " to " + r
                        .getTitle());
                break;
            }
        }
        if (r == null) {
            send("Room not found", output);
        }
    }

    private void deleteRoom(BufferedWriter output, BufferedReader input, User user,
        List<Room> roomsList) throws IOException {
        send("Enter room title", output);
        String title = input.readLine();
        Room r = null;
        for (Room room : roomsList) {
            if (room.getTitle().equals(title)) {
                r = room;
                break;
            }
        }
        if (r != null) {
            send("Do you want to delete room " + r.getTitle() + " which has " + r.getUsers().size()
                + " users?", output);
            send("Yes | No", output);

            String answer = input.readLine();
            if (answer.equals("1")) {
                logger.info("Admin " + user.getName() + " deleted room " + r.getTitle());
                r.getMessages().add(new Message(user, "DELETE"));
                roomsList.remove(r);
                send("room was deleted", output);
            }
        } else {
            send("Room not found", output);
        }

    }


}
