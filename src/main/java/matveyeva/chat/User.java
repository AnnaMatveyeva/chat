package matveyeva.chat;

import matveyeva.chat.exception.InvalidUserException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User {

    private String name;
    private String password;
    private String role;
    private Status status;

    enum Status{
        BANNED,
        ONLINE,
        OFFLINE;
    }
    public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.role = "USER";
        this.status = Status.OFFLINE;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public boolean isUserValid() throws InvalidUserException {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9-]{3,15}$");
        Matcher matcherN = pattern.matcher(name);
        Matcher matcherP = pattern.matcher(password);
        if(matcherN.matches() && matcherP.matches()){
            return true;
        }else throw new InvalidUserException("incorrect user data");
    }
}
