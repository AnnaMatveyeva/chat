package matveyeva.chat.service;

import java.io.BufferedWriter;
import java.io.IOException;
import matveyeva.chat.Entity.User;
import matveyeva.chat.Entity.User.Status;
import matveyeva.chat.UserCrud;
import matveyeva.chat.server.SideServer;
import org.apache.log4j.Logger;

public class DefaultService {

    private static final Logger logger = Logger.getLogger(DefaultService.class);
    protected UserCrud crud = UserCrud.INSTANCE;

    public void send(String msg, BufferedWriter output) {
        try {
            output.write(msg + "\n");
            output.flush();
        } catch (IOException ex) {

        }
    }

    public void exit(String message, User user, SideServer thisSide, BufferedWriter output)
        throws IOException {
        if (user != null && !user.getStatus().equals(Status.BANNED)) {
            user.setStatus(User.Status.OFFLINE);
            crud.setUserStatus(user);
            logger.info("User " + user.getName() + " logged off");
        }
        crud.reloadUsers();
        user = null;
        if (message.equalsIgnoreCase("Exit from application") || message.contains("deleted")
            || message.contains("admin") || message.contains("banned") || message
            .contains("updated")) {
            send("Exit from application", output);
            thisSide.shutdown();
        }
    }

}
