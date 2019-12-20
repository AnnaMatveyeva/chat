package matveyeva.chat.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import matveyeva.chat.Entity.Message;
import matveyeva.chat.Entity.Room;
import matveyeva.chat.Entity.User;
import matveyeva.chat.enums.Rooms;
import org.apache.log4j.Logger;

public class RoomChangesService extends DefaultService {


    private static final Logger logger = Logger.getLogger(RoomChangesService.class);

    private static RoomChangesService instance;

    public static RoomChangesService getInstance() {
        if (instance == null) {
            instance = new RoomChangesService();
        }
        return instance;
    }

    public void createRoom(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        send("Enter room title", output);
        String title = input.readLine();
        Room r = null;
        for (Room room : Rooms.INSTANCE.getRoomsList()) {
            if (room.getTitle().equals(title)) {
                r = room;
                break;
            }
        }
        if (r == null) {
            r = new Room(title);
            Rooms.INSTANCE.getRoomsList().add(r);
            logger.info("Admin " + user.getName() + " created room " + r.getTitle());
            send("Room \"" + title + "\" was created", output);
        } else {
            send("Room with title \"" + title + "\" already exists", output);
        }
    }

    public void showAllRooms(BufferedWriter output, BufferedReader input, User user) {
        if (!Rooms.INSTANCE.getRoomsList().isEmpty()) {
            send("All rooms:", output);
            for (Room room : Rooms.INSTANCE.getRoomsList()) {
                send(room.getTitle() + ", users: " + room.getUsers().size() + ";", output);
            }
        } else {
            send("There are no rooms", output);
        }
    }

    public void updateRoom(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        send("Enter room title", output);
        String title = input.readLine();
        Room r = null;
        send("Enter new room title", output);
        String str = input.readLine();

        for (Room room : Rooms.INSTANCE.getRoomsList()) {
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

    public void deleteRoom(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        send("Enter room title", output);
        String title = input.readLine();
        Room r = null;
        for (Room room : Rooms.INSTANCE.getRoomsList()) {
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
                Rooms.INSTANCE.getRoomsList().remove(r);
                send("room was deleted", output);
            }
        } else {
            send("Room not found", output);
        }

    }

}
