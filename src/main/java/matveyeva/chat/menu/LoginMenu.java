package matveyeva.chat.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import matveyeva.chat.Entity.Message;
import matveyeva.chat.Entity.User;
import matveyeva.chat.enums.PublicMessages;
import matveyeva.chat.server.SideServer;
import matveyeva.chat.service.AuthorizationService;
import org.apache.log4j.Logger;

public class LoginMenu implements Menu {

    protected BufferedReader input;
    protected BufferedWriter output;
    protected volatile List<Message> privateMessages;
    protected SideServer thisSide;
    private static final org.apache.log4j.Logger logger = Logger.getLogger(LoginMenu.class);
    private AuthorizationService authService = AuthorizationService.getInstance();


    public LoginMenu(BufferedReader input, BufferedWriter output,
        List<Message> privateMessages,
        SideServer thisSide) {
        this.input = input;
        this.output = output;
        this.privateMessages = privateMessages;
        this.thisSide = thisSide;
    }

    public void showMenu(User user) {
        boolean check = false;
        while (!check) {
            try {
                if (thisSide.isInterrupted()) {
                    break;
                }
                authService.send("login | registration | exit", output);
                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        authService.login(input, output, user,
                            PublicMessages.INSTANCE.getPublicMessages(), privateMessages,
                            thisSide);
                        break;
                    case 2:
                        authService.registration(input, output, user, privateMessages, thisSide);
                        break;
                    case 3:
                        authService.exit("Exit from application", user, thisSide, output);
                        check = true;
                        break;
                }
            } catch (NumberFormatException ex) {

            } catch (IOException ex) {
            }
        }
    }

}
