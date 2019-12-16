package matveyeva.chat;

import java.time.LocalDate;
import java.time.LocalTime;

public class Message {
    private User user;
    private LocalDate date;
    private String text;
    private LocalTime time;

    public Message(User user, String text) {
        this.user = user;
        this.text = text;
        this.date = LocalDate.now();
        this.time = LocalTime.now();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString(){
        return  date.toString() + " " + time.getHour()+ ":" + time.getMinute() + " " + user.getName() + ": " + text;
    }
}
