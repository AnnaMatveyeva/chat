package matveyeva.chat.service;

import java.io.BufferedWriter;
import java.io.IOException;
import matveyeva.chat.entity.User;
import matveyeva.chat.entity.User.Status;
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
            System.out.println("Something went wrong, try again");
        }
    }

    public void exit(String message, SideServer thisSide, BufferedWriter output)
        throws IOException {
        if (thisSide.user != null && !thisSide.user.getStatus().equals(Status.BANNED)) {
            thisSide.user.setStatus(User.Status.OFFLINE);
            crud.setUserStatus(thisSide.user);
            logger.info("User " + thisSide.user.getName() + " logged off");
        }
        crud.reloadUsers();
        thisSide.user = null;
        if (message.equalsIgnoreCase("Exit from application") || message.contains("deleted")
            || message.contains("admin") || message.contains("banned") || message
            .contains("updated")) {
            send("Exit from application", output);
            thisSide.shutdown();
        }
    }

}
