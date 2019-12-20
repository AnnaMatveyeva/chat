package matveyeva.chat.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import matveyeva.chat.Entity.Message;
import matveyeva.chat.Entity.User;
import matveyeva.chat.enums.Invitations;
import matveyeva.chat.server.SideServer;
import matveyeva.chat.service.UserService;
import org.apache.log4j.Logger;

public class UserMenu extends LoginMenu {

    private static final org.apache.log4j.Logger logger = Logger.getLogger(UserMenu.class);

    public UserMenu(BufferedReader input, BufferedWriter output,
        List<Message> privateMessages, SideServer thisSide) {
        super(input, output, privateMessages,thisSide);
    }

    private UserService userService = UserService.getInstance();

    public void showMenu(User user) {
        try {
            boolean check = false;
            while (!check) {
                if (thisSide.isInterrupted()) {
                    break;
                }
                userService.send(
                    "to public chat | to rooms | find user | see connected users | send message to.. | check private messages | invitations "
                        + Invitations.INSTANCE.checkInvitationsByUser(user) + " | logoff | exit |", output);

                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        userService.send("Public chat:", output);
                        userService.showPublicChat(output, input, user);
                        break;
                    case 2:
                        userService.showRoomMenu(output, input, user);
                        break;
                    case 3:
                        userService.findUser(output, input);
                        break;
                    case 4:
                        userService.showConnectedUsers(output, user);
                        break;
                    case 5:
                        userService.sendMessageTo(output, input, user, privateMessages);
                        break;
                    case 6:
                        userService.showPrivateMessages(user, privateMessages, output, input);
                        break;
                    case 7:
                        userService.showInvitations(output, input, user);
                        break;
                    case 8:
                        userService.exit("You logged off", user, thisSide, output);
                        check = true;
                        break;
                    case 9:
                        userService.exit("Exit from application", user, thisSide, output);
                        check = true;
                        break;
                }
            }
        }catch (IOException ex){

        }
    }
}
