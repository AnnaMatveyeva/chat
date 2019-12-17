package matveyeva.chat.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import matveyeva.chat.Message;
import matveyeva.chat.PublicMessages;
import matveyeva.chat.Room;
import matveyeva.chat.Rooms;
import matveyeva.chat.User;
import matveyeva.chat.User.Status;
import matveyeva.chat.UserCrud;
import java.net.Socket;

public class SideServer extends Thread {

    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private UserCrud crud;
    private User user;
    private List<Message> publicMessagesList;
    private volatile List<Message> privateMessages;
    private List<Room> roomsList;

    public SideServer(Socket socket) {
        this.socket = socket;
        this.crud = new UserCrud();
        publicMessagesList = PublicMessages.INSTANCE.getPublicMessages();
        privateMessages = new ArrayList<>();
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
                loginMenu();
                if (this.user.getRole().equalsIgnoreCase("admin")) {
                    showAdminMenu();
                    if(this.isInterrupted()){
                        break;
                    }
                } else {
                    if(this.isInterrupted()){
                        break;
                    }
                    boolean check = false;

                    while (!check) {
                        send(
                            "to public chat | to rooms | find user | see connected users | send message to.. | check private messages | logoff | exit");

                        String answer = input.readLine();
                        switch (Integer.parseInt(answer)) {
                            case 1:
                                send("Public chat:");
                                showPublicChat();
                                break;
                            case 2:
                                send("Redirect to rooms");
                                showRoomMenu();
                                break;
                            case 3:
                                findUser();
                                break;
                            case 4:
                                showConnectedUsers();
                                break;
                            case 5:
                                send("Enter username");
                                String username = input.readLine();
                                User friend;
                                if ((friend = crud.findByName(username)) != null && friend
                                    .getStatus().equals(User.Status.ONLINE)) {
                                    toPrivateChat(friend);
                                } else {
                                    send("User " + username + " not found or is not online");
                                }

                                break;
                            case 6:
                                send("Redirect to private messages");
                                showPrivateMessages();
                                break;
                            case 7:
                                exit("You logged off");
                                check = true;
                                break;
                            case 8:
                                exit("Exit from application");
                                check = true;
                                break;
                        }
                    }

                }
            }
        } catch (IOException ex) {
            this.shutdown();
        }
    }

    private void findUser() throws IOException {
        send("Enter username");
        String name = input.readLine();
        User user;
        if ((user = crud.findByName(name)) != null) {
            send(user.getName() + " is " + user.getStatus());
        } else {
            send("User " + name + " not found");
        }
    }

    private void showPrivateMessages() throws IOException {
        if (!privateMessages.isEmpty()) {
            Map<String, User> userMess = new HashMap<String, User>();
            StringBuilder str = new StringBuilder();

            for (int i = 0; i < privateMessages.size(); i++) {
                userMess.put(String.valueOf(i + 1), privateMessages.get(i).getUser());
                if (!str.toString().contains(privateMessages.get(i).getUser().getName())) {
                    str.append(privateMessages.get(i).getUser().getName() + " | ");
                }
            }

            send("You have private chats with :");
            send(str.toString());
            String answer = input.readLine();

            if (answer.equalsIgnoreCase("exit")) {
                send("Exit from private messages");
            } else if (userMess.containsKey(answer)) {
                User friend = userMess.get(answer);
                if (!friend.getStatus().equals(User.Status.ONLINE)) {
                    send("User " + friend.getName() + "is not online. You  can't answer");
                } else {
                    toPrivateChat(friend);
                }
            }
        } else {
            send("You don't have private chats");
        }
    }

    private void toPrivateChat(User friend) throws IOException {
        send("Chat with " + friend.getName());
        List<Message> messages = new ArrayList<Message>(privateMessages);
        for (Message mess : messages) {
            if (mess.getUser().equals(friend)) {
                send(mess.toString());
            }
        }
        while (true) {
            if (privateMessages.size() != messages.size()) {
                for (int i = messages.size(); i < privateMessages.size(); i++) {
                    Message mess = privateMessages.get(i);
                    messages.add(mess);
                    if (mess.getUser().equals(friend)) {
                        send(mess.toString());
                    }
                }
            }
            if (input.ready()) {
                String str = input.readLine();
                if (!str.equalsIgnoreCase("exit")) {
                    Message newMess = new Message(this.user, str);
                    sendTo(friend, newMess);
                    send(newMess.toString());
                } else {
                    send("Exit from chat with " + friend.getName());

                    break;
                }
            }
        }
    }

    private void showConnectedUsers() {
        List<SideServer> servers = Server.serverList;
        if (servers.size() > 1) {
            send("All connected users:");
            for (SideServer server : servers) {
                if (server.user != null && !server.user.equals(this.user)) {
                    send(server.user.getName());
                }
            }
        } else {
            send("There is nobody but you");
        }

    }

    private void send(String msg) {
        try {
            output.write(msg + "\n");
            output.flush();
        } catch (IOException ex) {

        }
    }

    private void showPublicChat() throws IOException {
        List<Message> pubMessages = new ArrayList<>(publicMessagesList);
        for (Message mess : pubMessages) {
            send(mess.toString());
        }
        while (true) {
            if (publicMessagesList.size() != pubMessages.size()) {
                for (int i = pubMessages.size(); i < publicMessagesList.size(); i++) {
                    Message mess = publicMessagesList.get(i);
                    pubMessages.add(mess);
                    send(mess.toString());
                }
            }
            if (input.ready()) {
                String str = input.readLine();
                if (!str.equalsIgnoreCase("exit")) {
                    Message newMess = new Message(this.user, str);
                    publicMessagesList.add(newMess);

                } else {
                    send("Exit from public chat");
                    break;
                }
            }
        }
    }

    private void exit(String message) throws IOException {
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

    private void shutdown() {
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

    private boolean login(String namePass) {
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
        } else return false;

    }

    private boolean registration(String newUser) {
        return (user = crud.create(newUser)) != null;
    }

    private void loginMenu() throws IOException {
        boolean check = false;
        while (!check) {
            try {
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
                            check = true;
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
                            check = true;
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

    private void showAdminMenu() {
        try {
            while (true) {

                boolean check = false;

                if(this.isInterrupted()){
                    break;
                }
                while (!check) {
                    send("to public chat | to rooms | find user | see connected users | "
                        + "see all users | send message to.. | check private messages| ban/delete/update user | create/delete room |logoff | exit ");

                    String answer = input.readLine();
                    switch (Integer.parseInt(answer)) {
                        case 1:
                            send("Public chat:");
                            showPublicChat();
                            break;
                        case 2:
                            send("Redirect to rooms");
                            showRoomMenu();
                            break;
                        case 3:
                            findUser();
                            break;
                        case 4:
                            showConnectedUsers();
                            break;
                        case 5:
                            showAllUsers();
                            break;
                        case 6:
                            send("Enter username");
                            String username = input.readLine();
                            User friend;
                            if ((friend = crud.findByName(username)) != null && friend.getStatus()
                                .equals(User.Status.ONLINE)) {
                                toPrivateChat(friend);
                            } else {
                                send("User " + username + " not found or is not online");
                            }

                            break;
                        case 7:
                            send("Redirect to private messages");
                            showPrivateMessages();
                            break;
                        case 8:
                            userChangesMenu();
                            break;
                        case 9:
                            adminRoomMenu();
                            break;
                        case 10:
                            exit("You logged off");
                            check = true;
                            break;
                        case 11:
                            exit("Exit from application");
                            check = true;
                            break;
                    }
                }

            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void showAllUsers() {
        Set<User> allUsers = crud.users;
        if (allUsers.size() > 1) {
            send("All users:");
            for (User u : allUsers) {
                if (!u.equals(this.user)) {
                    send(u.getName() + "; role: " + u.getRole() + "; satus: " + u.getStatus());
                }
            }
        } else {
            send("There is nobody but you");
        }
    }

    private void adminRoomMenu() {
        boolean check = false;
        try {
            while (!check) {
                send("delete room | update room | see all rooms | create room |exit");
                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        deleteRoom();
                        break;
                    case 2:
                        updateRoom();
                        break;
                    case 3:
                        showAllRooms();
                        break;
                    case 4:
                        createRoom();
                        break;
                    case 5:
                        check = true;
                        break;
                }

            }
        } catch (NumberFormatException ex) {

        } catch (Exception e) {
            e.printStackTrace();
            send("Something went wrong");
        }
    }

    private void createRoom() throws IOException {
        send("Enter room title");
        String title = input.readLine();
        Room r = null;
        for(Room room : roomsList){
            if(room.getTitle().equals(title)){
                r = room;
                break;
            }
        }
        if(r == null){
            r = new Room(title);
            roomsList.add(r);
            send("Room \"" + title + "\" was created");
        }else send("Room with title \"" + title + "\" already exists");
    }

    private void showAllRooms() {
        if(!roomsList.isEmpty()) {
            send("All rooms:");
            for (Room room : roomsList) {
                send(room.getTitle() + ";");
            }
        }else send("There are no rooms");
    }

    private void updateRoom() throws IOException {
        send("Enter room title");
        String title = input.readLine();
        Room r = null;
        send("Enter new room title");
        String str = input.readLine();

        for(Room room : roomsList){
            if(room.getTitle().equals(title)){
                room.setTitle(str);
                r = room;
                break;
            }
        }
        if(r == null){
            send("Room not found");
        }
    }

    private void deleteRoom() throws IOException {
        send("Enter room title");
        String title = input.readLine();
        Room r = null;
        for(Room room : roomsList){
            if(room.getTitle().equals(title)){
                r = room;
                break;
            }
        }
        if(r != null) {
            send("Do you want to delete room " + r.getTitle() + "which has " + r.getUsers().size()
                + " users?");
            send("Yes | No");

            String answer = input.readLine();
            if(answer.equals("1")){
                r.getMessages().add(new Message(this.user, "DELETE"));
                roomsList.remove(r);
            }
            send("room was deleted");
        }else send("Room not found");

    }


    private void userChangesMenu() throws IOException {
        boolean check = false;
        try {
            while (!check) {
                send("delete user | update user | ban user | make user admin |exit");
                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        deleteUser();
                        break;
                    case 2:
                        updateUser();
                        break;
                    case 3:
                        banUser();
                        break;
                    case 4:
                        makeUserAdmin();
                    case 5:
                        check = true;
                        break;
                }

            }
        } catch (NumberFormatException ex) {

        } catch (Exception e) {
            e.printStackTrace();
            send("Something went wrong");
        }
    }

    private void makeUserAdmin() throws Exception {
        send("Enter user name");
        String username = input.readLine();
        User userToAdmin;
        if ((userToAdmin = crud.findByName(username)) != null && !userToAdmin.getStatus().equals(Status.BANNED)) {
            send("Do you want to make admin " + userToAdmin.getName() +
                " with status " + userToAdmin.getStatus());
            send("Yes | No");
            String str = input.readLine();

            if(str.equals("1")){
                userToAdmin.setRole("ADMIN");
                for (User u : UserCrud.users) {
                    if (u.equals(userToAdmin)) {
                        u.setRole(userToAdmin.getRole());
                    }
                }
                if (userToAdmin.getStatus().equals(User.Status.ONLINE)) {
                    SideServer serverToShut = null;
                    for (SideServer server : Server.serverList) {
                        if (server.user.equals(userToAdmin)) {
                            serverToShut = server;
                        }
                    }
                    serverToShut.exit("You are admin now");
                }
            }

        } else send("user not found or banned");

    }

    private void banUser() throws Exception {
        send("Enter user name");
        String username = input.readLine();
        User usertoBan;
        if ((usertoBan = crud.findByName(username)) != null && !usertoBan.getStatus().equals(Status.BANNED)) {
            send("Do you want to ban " + usertoBan.getName() + " with role " + usertoBan.getRole());
            send("Yes | No");
            String str = input.readLine();
            switch (Integer.parseInt(str)) {
                case 1:
                    Status oldStatus = usertoBan.getStatus();
                    usertoBan.setStatus(User.Status.BANNED);
                    for (User u : UserCrud.users) {
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
                        serverToShut.exit("You are banned");
                    }
                    break;
                case 2:
                    break;
            }
        }else send("User not found or already banned");
    }

    private void deleteUser() throws Exception {
        send("Enter user name");
        String username = input.readLine();
        User usertoDelete;
        if ((usertoDelete = crud.findByName(username)) != null) {
            send("Do you want to delete " + usertoDelete.getName() + " with role " + usertoDelete
                .getRole());
            send("Yes | No");
            String str = input.readLine();
            switch (Integer.parseInt(str)) {
                case 1:
                    if (usertoDelete.getStatus().equals(User.Status.ONLINE)) {
                        SideServer serverToShut = null;
                        for (SideServer server : Server.serverList) {
                            if (server.user.equals(usertoDelete)) {
                                serverToShut = server;
                            }
                        }
                        serverToShut.exit("You are deleted");
                    }
                    crud.delete(usertoDelete);
                    break;
                case 2:
                    break;
            }
        }else send("User not found");
    }

    private void updateUser() throws Exception {
        send("Enter user name");
        String username = input.readLine();
        User usertoUpdate;
        if ((usertoUpdate = crud.findByName(username)) != null && !usertoUpdate.getStatus().equals(Status.BANNED)) {
            send("Enter new username,password");
            String str = input.readLine();
            crud.update(usertoUpdate, str);
            if (usertoUpdate.getStatus().equals(User.Status.ONLINE)) {
                SideServer serverToShut = null;
                for (SideServer server : Server.serverList) {
                    if (server.user.equals(usertoUpdate)) {
                        serverToShut = server;
                    }
                }
                serverToShut.exit("Your credentials updated");
            }
        } else send("User no found or banned");
    }

    private void sendTo(User friend, Message message) {
        for (SideServer ss : Server.serverList) {
            if (ss.user.equals(friend)) {
                ss.privateMessages.add(message);
            }
        }
    }

    private void showRoomMenu(){
        boolean check = false;
        try {
            while (!check) {
                send("choose room | invite user to room | exit");
                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        showRooms();
                        break;
                    case 2:
                        inviteUser();
                        break;
                    case 3:
                        check = true;
                        break;

                }

            }
        } catch (NumberFormatException ex) {

        } catch (Exception e) {
            e.printStackTrace();
            send("Something went wrong");
        }
    }

    private void inviteUser() {
    }

    private void showRooms() {
        if (!roomsList.isEmpty()) {
            boolean check = false;
            try {
                while (!check) {
                    Map<String, Room> rMap = new HashMap<String, Room>();
                    StringBuilder str = new StringBuilder();
                    if(!roomsList.isEmpty()) {
                        for (int i = 0; i < roomsList.size(); i++) {
                            rMap.put(String.valueOf(i + 1), roomsList.get(i));
                            str.append(roomsList.get(i).getTitle() + " | ");
                        }
                        str.append(" wite \"exit\" to return");
                        send(str.toString());
                    }else check = true;

                    String answer = input.readLine();
                    if (rMap.containsKey(answer)) {
                        Room room = rMap.get(answer);
                        room.getUsers().add(this.user);
                        toRoom(room);
                    } else if (answer.equals("exit")) {
                        check = true;
                    }
                }
            } catch (NumberFormatException | IOException ex) {
            }
        }else send("There are no rooms");
    }

    private void toRoom(Room room) throws IOException {
        send("You are in " + room.getTitle() + " room, write \"exit\" to return");
        List<Message> messages = new ArrayList<Message>(room.getMessages());
        if(!messages.isEmpty()){
            for(Message mess : messages){
                send(mess.toString());
            }
        }else send("There are no messages");
        boolean check = false;
        while (!check) {
            if (room.getMessages().size() != messages.size()) {
                for (int i = messages.size(); i < room.getMessages().size(); i++) {

                    Message mess = room.getMessages().get(i);
                    messages.add(mess);
                    send(mess.toString());
                    if(mess.getUser().getRole().equals("ADMIN") && mess.getText().equals("DELETE")){
                        send("This room was deleted by admin");
                        check = true;
                    }
                }
            }
            if (input.ready()) {
                String str = input.readLine();
                if (!str.equalsIgnoreCase("exit")) {
                    Message newMess = new Message(this.user, str);
                    room.getMessages().add(newMess);
                } else {
                    send("Exit from " + room.getTitle() + " room");
                    room.getUsers().remove(this.user);
                    check = true;
                }
            }
        }
    }



}
