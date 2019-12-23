package matveyeva.chat.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import matveyeva.chat.entity.Message;
import matveyeva.chat.entity.User;
import matveyeva.chat.enums.PublicMessages;
import matveyeva.chat.menu.RoomMenu;
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

    public void showPublicChat(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        logger.info("User " + user.getName() + " opened public chat");
        List<Message> pubMessages = new ArrayList<>(PublicMessages.INSTANCE.getPublicMessages());
        for (Message mess : pubMessages) {
            send(mess.toString(), output);
        }

        while (true) {
            if (PublicMessages.INSTANCE.getPublicMessages().size() != pubMessages.size()) {
                for (int i = pubMessages.size();
                    i < PublicMessages.INSTANCE.getPublicMessages().size(); i++) {
                    Message mess = PublicMessages.INSTANCE.getPublicMessages().get(i);
                    pubMessages.add(mess);
                    send(mess.toString(), output);
                }
            }
            if (input.ready()) {
                String str = input.readLine();
                if (!str.equalsIgnoreCase("exit")) {
                    Message newMess = new Message(user, str);
                    PublicMessages.INSTANCE.getPublicMessages().add(newMess);

                } else {
                    send("Exit from public chat", output);
                    break;
                }
            }
        }
        logger.info("User " + user.getName() + " closed public chat");
    }

    public void showRoomMenu(BufferedWriter output, BufferedReader input, User user) {
        RoomMenu roomMenu = new RoomMenu(output, input);
        roomMenu.showMenu(user);
    }

    public void findUser(BufferedWriter output, BufferedReader input) throws IOException {
        send("Enter username", output);
        String name = input.readLine();
        User user;
        user = crud.findByName(name);
        if (user != null) {
            send(user.getName() + " is " + user.getStatus(), output);
        } else {
            send("User not found", output);
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

    public void sendMessageTo(BufferedWriter output, BufferedReader input, User user,
        List<Message> privateMessages) {
        send("Enter username", output);
        User friend;
        try {
            String username = input.readLine();
            friend = crud.findByName(username);
            if (friend == null) {
                send("User not found", output);
            } else if (friend.getStatus().equals(User.Status.ONLINE)) {
                toPrivateChat(friend, user, output, input, privateMessages);
            } else {
                send(friend.getName() + " is not online", output);
            }
        } catch (IOException e) {
            send("Something went wrong, try again", output);
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

    public void showInvitations(BufferedWriter output, BufferedReader input, User user) {
        try {
            InvitationService.getInstance().showInvitations(output, input, user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
