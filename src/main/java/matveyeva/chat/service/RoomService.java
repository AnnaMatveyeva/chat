package matveyeva.chat.service;

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
import matveyeva.chat.enums.Invitations;
import matveyeva.chat.enums.Rooms;
import matveyeva.chat.exception.InvalidUserException;
import org.apache.log4j.Logger;

public class RoomService extends DefaultService{

    private static final Logger logger = Logger.getLogger(RoomService.class);

    private static RoomService instance;

    public static RoomService getInstance() {
        if (instance == null) {
            instance = new RoomService();
        }
        return instance;
    }

    public void showRooms(User user, BufferedReader input,
        BufferedWriter output) {
        if (!Rooms.INSTANCE.getRoomsList().isEmpty()) {
            boolean check = false;
            try {
                while (!check) {

                    Map<String, Room> rMap = new HashMap<String, Room>();
                    StringBuilder str = new StringBuilder();
                    if (!Rooms.INSTANCE.getRoomsList().isEmpty()) {
                        for (int i = 0; i < Rooms.INSTANCE.getRoomsList().size(); i++) {
                            rMap.put(String.valueOf(i + 1), Rooms.INSTANCE.getRoomsList().get(i));
                            str.append(Rooms.INSTANCE.getRoomsList().get(i).getTitle() + " | ");
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


}
