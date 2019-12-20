package matveyeva.chat.service;

import com.sun.org.apache.bcel.internal.generic.BREAKPOINT;
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
import matveyeva.chat.exception.InvalidUserException;
import matveyeva.chat.server.Server;
import matveyeva.chat.server.SideServer;
import org.apache.log4j.Logger;

public class UserService extends DefaultService {

    private static final Logger logger = Logger.getLogger(UserService.class);

    private static UserService instance;

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public void showPublicChat(BufferedWriter output, BufferedReader input, User user,
        List<Message> publicMessagesList) throws IOException {
        logger.info("User " + user.getName() + " opened public chat");
        List<Message> pubMessages = new ArrayList<>(publicMessagesList);
        for (Message mess : pubMessages) {
            send(mess.toString(), output);
        }
        while (true) {
            if (publicMessagesList.size() != pubMessages.size()) {
                for (int i = pubMessages.size(); i < publicMessagesList.size(); i++) {
                    Message mess = publicMessagesList.get(i);
                    pubMessages.add(mess);
                    send(mess.toString(), output);
                }
            }
            if (input.ready()) {
                String str = input.readLine();
                if (!str.equalsIgnoreCase("exit")) {
                    Message newMess = new Message(user, str);
                    publicMessagesList.add(newMess);

                } else {
                    send("Exit from public chat", output);
                    break;
                }
            }
        }
        logger.info("User " + user.getName() + " closed public chat");
    }

    public void showRoomMenu(BufferedWriter output, BufferedReader input, User user,
        List<Room> roomsList) {
        boolean check = false;
        if (!roomsList.isEmpty()) {
            try {
                while (!check) {
                    send("choose room | invite user to room | exit", output);
                    String answer = input.readLine();
                    switch (Integer.parseInt(answer)) {
                        case 1:
                            showRooms(user, roomsList, input, output);
                            break;
                        case 2:
                            inviteUser(output, input, roomsList, user);
                            break;
                        case 3:
                            check = true;
                            break;
                    }
                }
            } catch (NumberFormatException ex) {

            } catch (Exception e) {
                e.printStackTrace();
                send("Something went wrong", output);
            }
        } else {
            send("There is nothing to show", output);
        }
    }

    public void showRooms(User user, List<Room> roomsList, BufferedReader input,
        BufferedWriter output) {
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
                        send(str.toString(), output);
                    } else {
                        break;
                    }

                    String answer = input.readLine();
                    if (rMap.containsKey(answer)) {
                        Room room = rMap.get(answer);
                        toRoom(room, user, output, input);

                        check = true;
                    } else if (answer.equals("exit")) {
                        check = true;
                    }
                }
            } catch (NumberFormatException | IOException ex) {
            }
        } else {
            send("There are no rooms", output);
        }
    }

    public void toRoom(Room room, User user, BufferedWriter output, BufferedReader input)
        throws IOException {
        room.getUsers().add(user);

        logger.info("User " + user.getName() + " opened room" + room.getTitle());
        send("You are in " + room.getTitle() + " room, write \"exit\" to return", output);

        List<Message> messages = new ArrayList<Message>(room.getMessages());
        if (!messages.isEmpty()) {
            for (Message mess : messages) {
                send(mess.toString(), output);
            }
        } else {
            send("There are no messages", output);
        }
        boolean check = false;
        while (!check) {
            if (room.getMessages().size() != messages.size()) {
                for (int i = messages.size(); i < room.getMessages().size(); i++) {

                    Message mess = room.getMessages().get(i);
                    messages.add(mess);
                    send(mess.toString(), output);
                    if (mess.getUser().getRole().equals("ADMIN") && mess.getText()
                        .equals("DELETE")) {
                        send("This room was deleted by admin", output);

                        check = true;
                        break;
                    }
                }
            }
            if (input.ready()) {
                String str = input.readLine();
                if (!str.equalsIgnoreCase("exit")) {
                    Message newMess = new Message(user, str);
                    room.getMessages().add(newMess);
                } else {
                    send("Exit from " + room.getTitle() + " room", output);
                    room.getUsers().remove(user);
                    check = true;
                }
            }
        }
        logger.info("User " + user.getName() + " exit from " + room.getTitle() + " room");
    }

    public void inviteUser(BufferedWriter output, BufferedReader input, List<Room> roomsList,
        User user) throws IOException {
        send("Enter user name", output);
        String username = input.readLine();
        User usertoInvite;
        try {
            usertoInvite = crud.findByName(username);
            if (usertoInvite.getStatus().equals(Status.ONLINE)) {
                Room r = null;
                send("Enter room title", output);
                String title = input.readLine();
                for (Room room : roomsList) {
                    if (room.getTitle().equals(title)) {
                        r = room;
                        sendInvitation(user, usertoInvite, r);
                        send("User was invited", output);
                        break;
                    }
                }
                if (r == null) {
                    send("Room not found", output);
                }
            } else {
                send("User are not online", output);
            }
        } catch (InvalidUserException ex) {
            send(ex.getMessage(), output);
        }
    }

    public void sendInvitation(User fromWho, User toWho, Room room) {
        for (SideServer ss : Server.serverList) {
            if (ss.user.equals(toWho)) {
                logger.info("User " + fromWho.getName() + " sent invitation to " + toWho.getName()
                    + "in room " + room.getTitle());
                ss.invitations.add(new Invitation(fromWho, toWho, room));
                break;
            }
        }
    }

    public void showInvitations(List<Invitation> invitations, BufferedWriter output,
        BufferedReader input, User user) throws IOException {
        boolean check = false;
        if (!invitations.isEmpty()) {
            while (!check) {
                if (invitations.isEmpty()) {
                    check = true;
                }
                StringBuilder str = new StringBuilder();
                Map<String, Invitation> iMap = new HashMap<>();
                for (int i = 0; i < invitations.size(); i++) {
                    str.append(
                        "to " + invitations.get(i).getRoom().getTitle() + " from " + invitations
                            .get(i)
                            .getFromWho().getName() + "; ");
                    iMap.put(String.valueOf(i + 1), invitations.get(i));
                }
                send(str.toString(), output);
                String answer = input.readLine();

                if (answer.equalsIgnoreCase("exit")) {
                    check = true;
                }

                if (iMap.containsKey(answer)) {
                    Invitation invite = iMap.get(answer);
                    send(invite.toString(), output);
                    send("to room | delete invitation | return", output);
                    String choice = input.readLine();
                    switch (Integer.parseInt(choice)) {
                        case 1:
                            toRoom(invite.getRoom(), user, output, input);
                            invitations.remove(invite);
                            check = true;
                            break;
                        case 2:
                            invitations.remove(invite);
                            logger.info(
                                "User " + user.getName() + "deleted invitation from " + invite
                                    .getFromWho().getName() + " in room " + invite.getRoom()
                                    .getTitle());
                            send("Invitation was deleted", output);
                            break;
                    }
                }
            }
        } else {
            send("Nothing to show", output);
        }
    }

    public void findUser(BufferedWriter output, BufferedReader input) throws IOException {
        send("Enter username", output);
        String name = input.readLine();
        User user;
        try {
            user = crud.findByName(name);
            send(user.getName() + " is " + user.getStatus(), output);

        } catch (InvalidUserException e) {
            send(e.getMessage(), output);
        }
    }

    public void showConnectedUsers(BufferedWriter output, User user) {
        List<SideServer> servers = Server.serverList;
        if (servers.size() > 1) {
            send("All connected users:", output);
            for (SideServer server : servers) {
                if (server.user != null && !server.user.equals(user)) {
                    send(server.user.getName(), output);
                }
            }
        } else {
            send("There is nobody but you", output);
        }
    }

    public void sendMessageTo(BufferedWriter output, BufferedReader input, User user, List<Message> privateMessages){
        send("Enter username", output);
        User friend;
        try {
            String username = input.readLine();
            friend = crud.findByName(username);
            if (friend.getStatus().equals(User.Status.ONLINE)) {
                toPrivateChat(friend, user, output, input, privateMessages);
            }else send(friend.getName() + " is not online", output);
        } catch (InvalidUserException ex) {
            send(ex.getMessage(), output);
        } catch (IOException e) {
        }
    }


    public void toPrivateChat(User friend, User user, BufferedWriter output,
        BufferedReader input, List<Message> privateMessages) throws IOException {

        logger.info("User " + user.getName() + " opened private chat with " + friend.getName());
        send("Chat with " + friend.getName(), output);
        List<Message> messages = new ArrayList<Message>(privateMessages);
        for (Message mess : messages) {
            if (mess.getUser().equals(friend)) {
                send(mess.toString(), output);
            }
        }
        while (true) {
            if (privateMessages.size() != messages.size()) {
                for (int i = messages.size(); i < privateMessages.size(); i++) {
                    Message mess = privateMessages.get(i);
                    messages.add(mess);
                    if (mess.getUser().equals(friend)) {
                        send(mess.toString(), output);
                    }
                }
            }
            if (input.ready()) {
                String str = input.readLine();
                if (!str.equalsIgnoreCase("exit")) {
                    Message newMess = new Message(user, str);
                    sendTo(friend, newMess);
                    send(newMess.toString(), output);
                } else {
                    send("Exit from chat with " + friend.getName(), output);

                    break;
                }
            }
        }
        logger.info("User " + user.getName() + " exit from private chat with " + friend.getName());
    }

    public void sendTo(User friend, Message message) {
        for (SideServer ss : Server.serverList) {
            if (ss.user.equals(friend)) {
                ss.privateMessages.add(message);
            }
        }
    }

    public void showPrivateMessages(User user, List<Message> privateMessages,
        BufferedWriter output, BufferedReader input) throws IOException {
        if (!privateMessages.isEmpty()) {
            Map<String, User> userMess = new HashMap<String, User>();
            StringBuilder str = new StringBuilder();

            for (int i = 0; i < privateMessages.size(); i++) {
                userMess.put(String.valueOf(i + 1), privateMessages.get(i).getUser());
                if (!str.toString().contains(privateMessages.get(i).getUser().getName())) {
                    str.append(privateMessages.get(i).getUser().getName() + " | ");
                }
            }

            send("You have private chats with :", output);
            send(str.toString(), output);
            String answer = input.readLine();

            if (answer.equalsIgnoreCase("exit")) {
                send("Exit from private messages", output);
            } else if (userMess.containsKey(answer)) {
                User friend = userMess.get(answer);
                if (!friend.getStatus().equals(User.Status.ONLINE)) {
                    send("User " + friend.getName() + " is not online. You  can't answer", output);
                } else {
                    toPrivateChat(friend, user, output, input, privateMessages);
                }
            }
        } else {
            send("You don't have private chats", output);
        }
    }

}
