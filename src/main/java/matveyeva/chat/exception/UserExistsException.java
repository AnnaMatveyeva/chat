package matveyeva.chat.exception;

public class UserExistsException extends Throwable {
    public UserExistsException(){
        super("User already exists");
    }

}
