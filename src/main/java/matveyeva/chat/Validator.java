package matveyeva.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    private final Pattern pattern = Pattern.compile("^[a-zA-Z0-9-]{3,15}$");

    public boolean checkPersonData(String data){
        Matcher matcher = pattern.matcher(data);
        return matcher.matches();
    }
}
