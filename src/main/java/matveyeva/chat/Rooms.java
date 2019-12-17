package matveyeva.chat;

import java.util.ArrayList;
import java.util.List;

public enum Rooms {
    INSTANCE;

    private volatile List<Room> rooms = new ArrayList<>();

    public List<Room> getRoomsList() {
        return rooms;
    }

    public void addToList(Room room) {
        rooms.add(room);
    }

    public void setRoomsList(List<Room> roomsList) {
        this.rooms = roomsList;
    }

}

