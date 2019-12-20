package matveyeva.chat.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import matveyeva.chat.Entity.Invitation;
import matveyeva.chat.Entity.Message;
import matveyeva.chat.Entity.Room;
import matveyeva.chat.Entity.User;
import matveyeva.chat.server.SideServer;
import matveyeva.chat.service.AuthorizationService;
import org.apache.log4j.Logger;

public class LoginMenu implements Menu {

    protected BufferedReader input;
    protected BufferedWriter output;
    protected User user;
    protected volatile List<Message> publicMessagesList;
    protected volatile List<Message> privateMessages;
    protected volatile List<Room> roomsList;
    protected volatile List<Invitation> invitations;
    protected SideServer thisSide;
    private static final org.apache.log4j.Logger logger = Logger.getLogger(LoginMenu.class);
    private AuthorizationService authService = AuthorizationService.getInstance();


    public LoginMenu(BufferedReader input, BufferedWriter output,
        User user, List<Message> publicMessagesList,
        List<Message> privateMessages, List<Room> roomsList,
        List<Invitation> invitations, SideServer thisSide) {
        this.input = input;
        this.output = output;
        this.user = user;
        this.publicMessagesList = publicMessagesList;
        this.privateMessages = privateMessages;
        this.roomsList = roomsList;
        this.invitations = invitations;
        this.thisSide = thisSide;
    }

    public void showMenu() {
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
                            publicMessagesList, privateMessages, roomsList, invitations,
                            thisSide);
                        break;
                    case 2:
                        authService.registration(input, output, user,
                            publicMessagesList, privateMessages, roomsList, invitations,
                            thisSide);
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
