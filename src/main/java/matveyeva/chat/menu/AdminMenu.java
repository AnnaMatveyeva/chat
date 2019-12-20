package matveyeva.chat.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import matveyeva.chat.Entity.Invitation;
import matveyeva.chat.Entity.Message;
import matveyeva.chat.Entity.Room;
import matveyeva.chat.Entity.User;
import matveyeva.chat.server.SideServer;
import matveyeva.chat.service.AdminService;
import org.apache.log4j.Logger;

public class AdminMenu extends LoginMenu {

    private static final org.apache.log4j.Logger logger = Logger.getLogger(LoginMenu.class);
    private AdminService adminService = AdminService.getInstance();

    public AdminMenu(BufferedReader input, BufferedWriter output,
        User user, List<Message> publicMessagesList,
        List<Message> privateMessages, List<Room> roomsList,
        List<Invitation> invitations, SideServer thisSide) {
        super(input, output, user, publicMessagesList, privateMessages, roomsList,
            invitations,
            thisSide);
    }

    @Override
    public void showMenu() {
        try {
            boolean check = false;

            while (!check) {
                if (thisSide.isInterrupted()) {
                    break;
                }

                adminService.send("to public chat | to rooms | find user | see connected users | "
                    + "see all users | send message to.. | check private messages| invitations "
                    + invitations.size()
                    + " | ban/delete/update user | create/delete room | logoff | exit ", output);

                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        adminService.send("Public chat:", output);
                        adminService.showPublicChat(output, input, user, publicMessagesList);
                        break;
                    case 2:
                        adminService.send("Redirect to rooms", output);
                        adminService.showRoomMenu(output, input, user, roomsList);
                        break;
                    case 3:
                        adminService.findUser(output, input);
                        break;
                    case 4:
                        adminService.showConnectedUsers(output, user);
                        break;
                    case 5:
                        adminService.showAllUsers(output, user);
                        break;
                    case 6:
                        adminService.sendMessageTo(output, input, user, privateMessages);
                        break;
                    case 7:
                        adminService.send("Redirect to private messages", output);
                        adminService.showPrivateMessages(user, privateMessages, output, input);
                        break;
                    case 8:
                        adminService.showInvitations(invitations, output, input, user);
                        break;
                    case 9:
                        adminService.userChangesMenu(output, input, user);
                        break;
                    case 10:
                        adminService.adminRoomMenu(output, input, user, roomsList);
                        break;
                    case 11:
                        adminService.exit("You logged off", user, thisSide, output);
                        check = true;
                        break;
                    case 12:
                        adminService.exit("Exit from application", user, thisSide, output);
                        check = true;
                        break;
                }
            }


        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

//    private void showAllUsers() {
//        Set<User> allUsers = crud.users;
//        if (allUsers.size() > 1) {
//            send("All users:");
//            for (User u : allUsers) {
//                if (!u.equals(this.user)) {
//                    send(u.getName() + "; role: " + u.getRole() + "; satus: " + u.getStatus());
//                }
//            }
//        } else {
//            send("There is nobody but you");
//        }
//    }

//    private void userChangesMenu() throws IOException {
//        boolean check = false;
//        try {
//            while (!check) {
//                send("delete user | update user | ban user | make user admin |exit");
//                String answer = input.readLine();
//                switch (Integer.parseInt(answer)) {
//                    case 1:
//                        deleteUser();
//                        break;
//                    case 2:
//                        updateUser();
//                        break;
//                    case 3:
//                        banUser();
//                        break;
//                    case 4:
//                        makeUserAdmin();
//                    case 5:
//                        check = true;
//                        break;
//                }
//
//            }
//        } catch (NumberFormatException ex) {
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            send("Something went wrong");
//        }
//    }

//    private void makeUserAdmin() throws IOException {
//        send("Enter user name");
//        String username = input.readLine();
//        User userToAdmin;
//        try {
//            if ((userToAdmin = crud.findByName(username)) != null && !userToAdmin.getStatus()
//                .equals(Status.BANNED)) {
//                send("Do you want to make admin " + userToAdmin.getName() +
//                    " with status " + userToAdmin.getStatus());
//                send("Yes | No");
//                String str = input.readLine();
//
//                if (str.equals("1")) {
//                    logger.info(
//                        "Admin " + user.getName() + " made admin user " + userToAdmin.getName());
//                    userToAdmin.setRole("ADMIN");
//                    for (User u : UserCrud.users) {
//                        if (u.equals(userToAdmin)) {
//                            u.setRole(userToAdmin.getRole());
//                        }
//                    }
//                    send("User are admin now");
//                    if (userToAdmin.getStatus().equals(User.Status.ONLINE)) {
//                        SideServer serverToShut = null;
//                        for (SideServer server : Server.serverList) {
//                            if (server.user.equals(userToAdmin)) {
//                                serverToShut = server;
//                            }
//                        }
//                        serverToShut.exit("You are admin now");
//                    }
//                }
//
//            } else {
//                send("user banned");
//            }
//        }catch (InvalidUserException ex){
//            ex.getMessage();
//        }
//
//    }
//
//    private void banUser() throws IOException {
//        send("Enter user name");
//        String username = input.readLine();
//        User usertoBan;
//        try {
//            if ((usertoBan = crud.findByName(username)) != null && !usertoBan.getStatus()
//                .equals(Status.BANNED)) {
//                send("Do you want to ban " + usertoBan.getName() + " with role " + usertoBan
//                    .getRole());
//                send("Yes | No");
//                String str = input.readLine();
//                switch (Integer.parseInt(str)) {
//                    case 1:
//                        Status oldStatus = usertoBan.getStatus();
//                        usertoBan.setStatus(User.Status.BANNED);
//                        send("User banned");
//                        for (User u : UserCrud.users) {
//                            if (u.equals(usertoBan)) {
//                                u.setStatus(usertoBan.getStatus());
//                            }
//                        }
//                        if (oldStatus.equals(User.Status.ONLINE)) {
//                            SideServer serverToShut = null;
//                            for (SideServer server : Server.serverList) {
//                                if (server.user.equals(usertoBan)) {
//                                    serverToShut = server;
//                                }
//                            }
//                            serverToShut.exit("You are banned");
//                        }
//                        logger.info(
//                            "Admin " + user.getName() + " banned user " + usertoBan.getName()
//                                + " with role " + usertoBan.getRole());
//                        break;
//                    case 2:
//                        break;
//                }
//            } else {
//                send("User not found or already banned");
//            }
//        }catch (InvalidUserException ex){
//            send(ex.getMessage());
//        }
//    }
//
//    private void deleteUser() throws IOException{
//        send("Enter user name");
//        String username = input.readLine();
//        User usertoDelete;
//        try {
//            if ((usertoDelete = crud.findByName(username)) != null) {
//                send(
//                    "Do you want to delete " + usertoDelete.getName() + " with role " + usertoDelete
//                        .getRole());
//                send("Yes | No");
//                String str = input.readLine();
//                switch (Integer.parseInt(str)) {
//                    case 1:
//                        if (usertoDelete.getStatus().equals(User.Status.ONLINE)) {
//                            SideServer serverToShut = null;
//                            for (SideServer server : Server.serverList) {
//                                if (server.user.equals(usertoDelete)) {
//                                    serverToShut = server;
//                                }
//                            }
//                            serverToShut.exit("You are deleted");
//                        }
//                        logger.info(
//                            "Admin " + user.getName() + " deleted user " + usertoDelete.getName()
//                                + " with role " + usertoDelete.getRole());
//                        crud.delete(usertoDelete);
//                        send("User deleted");
//                        break;
//                    case 2:
//                        break;
//                }
//            } else {
//                send("User not found");
//            }
//        }catch (InvalidUserException ex){
//            send(ex.getMessage());
//        }
//    }
//
//    private void updateUser() throws IOException{
//        send("Enter user name");
//        String username = input.readLine();
//        User usertoUpdate;
//        try {
//            if ((usertoUpdate = crud.findByName(username)) != null && !usertoUpdate.getStatus()
//                .equals(Status.BANNED)) {
//                send("Enter new username,password");
//                String str = input.readLine();
//                crud.update(usertoUpdate, str);
//                send("User updated");
//                logger.info("Admin " + user.getName() + " updated user " + usertoUpdate.getName()
//                    + " with role " + usertoUpdate.getRole());
//                if (usertoUpdate.getStatus().equals(User.Status.ONLINE)) {
//                    SideServer serverToShut = null;
//                    for (SideServer server : Server.serverList) {
//                        if (server.user.equals(usertoUpdate)) {
//                            serverToShut = server;
//                        }
//                    }
//                    serverToShut.exit("Your credentials updated");
//                }
//            } else {
//                send("User no found or banned");
//            }
//        }catch (InvalidUserException ex){
//            send(ex.getMessage());
//        }
//    }

//    private void adminRoomMenu() {
//        boolean check = false;
//        try {
//            while (!check) {
//                send("delete room | update room | see all rooms | create room |exit");
//                String answer = input.readLine();
//                switch (Integer.parseInt(answer)) {
//                    case 1:
//                        deleteRoom();
//                        break;
//                    case 2:
//                        updateRoom();
//                        break;
//                    case 3:
//                        showAllRooms();
//                        break;
//                    case 4:
//                        createRoom();
//                        break;
//                    case 5:
//                        check = true;
//                        break;
//                }
//
//            }
//        } catch (NumberFormatException ex) {
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            send("Something went wrong");
//        }
//    }
//
//    private void createRoom() throws IOException {
//        send("Enter room title");
//        String title = input.readLine();
//        Room r = null;
//        for (Room room : roomsList) {
//            if (room.getTitle().equals(title)) {
//                r = room;
//                break;
//            }
//        }
//        if (r == null) {
//            r = new Room(title);
//            roomsList.add(r);
//            logger.info("Admin " + user.getName() + " created room " + r.getTitle());
//            send("Room \"" + title + "\" was created");
//        } else {
//            send("Room with title \"" + title + "\" already exists");
//        }
//    }
//
//    private void showAllRooms() {
//        if (!roomsList.isEmpty()) {
//            send("All rooms:");
//            for (Room room : roomsList) {
//                send(room.getTitle() + ", users: " + room.getUsers().size() + ";");
//            }
//        } else {
//            send("There are no rooms");
//        }
//    }
//
//    private void updateRoom() throws IOException {
//        send("Enter room title");
//        String title = input.readLine();
//        Room r = null;
//        send("Enter new room title");
//        String str = input.readLine();
//
//        for (Room room : roomsList) {
//            if (room.getTitle().equals(title)) {
//                room.setTitle(str);
//                r = room;
//                logger.info("Admin " + user.getName() + " change room title from " + title + " to " + r.getTitle());
//                break;
//            }
//        }
//        if (r == null) {
//            send("Room not found");
//        }
//    }
//
//    private void deleteRoom() throws IOException {
//        send("Enter room title");
//        String title = input.readLine();
//        Room r = null;
//        for (Room room : roomsList) {
//            if (room.getTitle().equals(title)) {
//                r = room;
//                break;
//            }
//        }
//        if (r != null) {
//            send("Do you want to delete room " + r.getTitle() + " which has " + r.getUsers().size()
//                + " users?");
//            send("Yes | No");
//
//            String answer = input.readLine();
//            if (answer.equals("1")) {
//                logger.info("Admin " + user.getName() + " deleted room " + r.getTitle());
//                r.getMessages().add(new Message(this.user, "DELETE"));
//                roomsList.remove(r);
//                send("room was deleted");
//            }
//        } else {
//            send("Room not found");
//        }
//
//    }


}
