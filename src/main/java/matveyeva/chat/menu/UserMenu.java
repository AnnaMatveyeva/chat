package matveyeva.chat.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import matveyeva.chat.Entity.Invitation;
import matveyeva.chat.Entity.Message;
import matveyeva.chat.Entity.Room;
import matveyeva.chat.Entity.User;
import matveyeva.chat.Entity.User.Status;
import matveyeva.chat.UserCrud;
import matveyeva.chat.server.Server;
import matveyeva.chat.server.SideServer;

public class UserMenu extends LoginMenu{

    public UserMenu(BufferedReader input, BufferedWriter output, UserCrud crud,
        User user, List<Message> publicMessagesList,
        List<Message> privateMessages, List<Room> roomsList,
        List<Invitation> invitations,SideServer thisSide) {
        super(input, output, crud, user, publicMessagesList, privateMessages, roomsList,
            invitations, thisSide);
    }

    protected void showMainMenu() throws IOException {
        boolean check = false;
        while (!check) {
            if (thisSide.isInterrupted()) {
                break;
            }
            send(
                "to public chat | to rooms | find user | see connected users | send message to.. | check private messages | invitations "
                    + invitations.size() + " | logoff | exit |");

            String answer = input.readLine();
            switch (Integer.parseInt(answer)) {
                case 1:
                    send("Public chat:");
                    showPublicChat();
                    break;
                case 2:
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
                    showInvitations();
                    break;
                case 8:
                    exit("You logged off");
                    check = true;
                    break;
                case 9:
                    exit("Exit from application");
                    check = true;
                    break;
            }
        }
    }

    protected void showPublicChat() throws IOException {
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

    protected void showRoomMenu() {
        boolean check = false;
        if(!roomsList.isEmpty()){
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
        }else send("There is nothing to show");
    }

    protected void findUser() throws IOException {
        send("Enter username");
        String name = input.readLine();
        User user;
        if ((user = crud.findByName(name)) != null) {
            send(user.getName() + " is " + user.getStatus());
        } else {
            send("User " + name + " not found");
        }
    }

    protected void showConnectedUsers() {
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

    protected void toPrivateChat(User friend) throws IOException {
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

    protected void sendTo(User friend, Message message) {
        for (SideServer ss : Server.serverList) {
            if (ss.user.equals(friend)) {
                ss.privateMessages.add(message);
            }
        }
    }

    protected void showPrivateMessages() throws IOException {
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

    protected void showRooms() {
        if (!roomsList.isEmpty()) {
            boolean check = false;
            try {
                while (!check) {

                    Map<String, Room> rMap = new HashMap<String, Room>();
                    StringBuilder str = new StringBuilder();
                    if (!roomsList.isEmpty()) {
                        for (int i = 0; i < roomsList.size(); i++) {
                            rMap.put(String.valueOf(i + 1), roomsList.get(i));
                            str.append(roomsList.get(i).getTitle() + " | ");
                        }
                        str.append(" wite \"exit\" to return");
                        send(str.toString());
                    } else {
                        break;
                    }

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
        } else {
            send("There are no rooms");
        }
    }

    protected void toRoom(Room room) throws IOException {
        send("You are in " + room.getTitle() + " room, write \"exit\" to return");
        List<Message> messages = new ArrayList<Message>(room.getMessages());
        if (!messages.isEmpty()) {
            for (Message mess : messages) {
                send(mess.toString());
            }
        } else {
            send("There are no messages");
        }
        boolean check = false;
        while (!check) {
            if (room.getMessages().size() != messages.size()) {
                for (int i = messages.size(); i < room.getMessages().size(); i++) {

                    Message mess = room.getMessages().get(i);
                    messages.add(mess);
                    send(mess.toString());
                    if (mess.getUser().getRole().equals("ADMIN") && mess.getText()
                        .equals("DELETE")) {
                        send("This room was deleted by admin");
                        check = true;
                        break;
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

    protected void inviteUser() throws IOException {
        send("Enter user name");
        String username = input.readLine();
        User usertoInvite;
        if ((usertoInvite = crud.findByName(username)) != null && usertoInvite.getStatus()
            .equals(Status.ONLINE)) {
            Room r = null;
            send("Enter room title");
            String title = input.readLine();
            for (Room room : roomsList) {
                if (room.getTitle().equals(title)) {
                    r = room;
                    sendInvitation(this.user, usertoInvite, r);
                    send("User was invited");
                    break;
                }
            }
            if (r == null) {
                send("Room not found");
            }
        } else {
            send("User not found or not online");
        }
    }

    protected void sendInvitation(User fromWho, User toWho, Room room) {
        for (SideServer ss : Server.serverList) {
            if (ss.user.equals(toWho)) {
                ss.invitations.add(new Invitation(fromWho, toWho, room));
                break;
            }
        }
    }

    protected void showInvitations() throws IOException {
        if (!invitations.isEmpty()) {
            while (true) {
                if (invitations.isEmpty()) {
                    break;
                }
                StringBuilder str = new StringBuilder();
                Map<String, Invitation> iMap = new HashMap<>();
                for (int i = 0; i < invitations.size(); i++) {
                    str.append(
                        "to " + invitations.get(i).getRoom().getTitle() + "from " + invitations
                            .get(i)
                            .getFromWho().getName() + "; ");
                    iMap.put(String.valueOf(i + 1), invitations.get(i));
                }
                send(str.toString());
                String answer = input.readLine();

                if (answer.equalsIgnoreCase("exit")) {
                    break;
                }

                if (iMap.containsKey(answer)) {
                    Invitation invite = iMap.get(answer);
                    send(invite.toString());
                    send("to room | delete invitation | return");
                    String choice = input.readLine();
                    switch (Integer.parseInt(choice)) {
                        case 1:
                            toRoom(invite.getRoom());
                            break;
                        case 2:
                            invitations.remove(invite);
                            send("Invitation was deleted");
                            break;
                    }
                }
            }
        } else {
            send("Nothing to show");
        }
    }


}
