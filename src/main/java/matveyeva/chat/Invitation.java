package matveyeva.chat;

public class Invitation {
    private User fromWho;
    private User toWho;
    private Room room;

    public Invitation(User fromWho, User toWho, Room room) {
        this.fromWho = fromWho;
        this.toWho = toWho;
        this.room = room;
    }

    public User getFromWho() {
        return fromWho;
    }

    public void setFromWho(User fromWho) {
        this.fromWho = fromWho;
    }

    public User getToWho() {
        return toWho;
    }

    public void setToWho(User toWho) {
        this.toWho = toWho;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String toString(){
        return "to " + room.getTitle() + "from " + fromWho.getName();
    }
}
