package matveyeva.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import matveyeva.chat.Entity.User;

public class Validator {
    private final Pattern pattern = Pattern.compile("^[a-zA-Z0-9-]{3,15}$");

    public boolean checkRoomTitle(String title){
        Matcher matcher = pattern.matcher(title);
        return matcher.matches();
    }
    public boolean checkPersonData(String data){
        Matcher matcher = pattern.matcher(data);
        return matcher.matches();
    }
    public boolean checkUser(User user){
        Matcher name = pattern.matcher(user.getName());
        Matcher pass = pattern.matcher(user.getPassword());
        return name.matches() && pass.matches();
    }
}
