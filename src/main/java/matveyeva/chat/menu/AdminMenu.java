package matveyeva.chat.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import matveyeva.chat.entity.Message;
import matveyeva.chat.entity.User;
import matveyeva.chat.enums.Invitations;
import matveyeva.chat.server.SideServer;
import matveyeva.chat.service.AdminService;
import org.apache.log4j.Logger;

public class AdminMenu implements Menu{

    private static final org.apache.log4j.Logger logger = Logger.getLogger(LoginMenu.class);
    private AdminService adminService = AdminService.getInstance();
    protected BufferedReader input;
    protected BufferedWriter output;
    protected volatile List<Message> privateMessages;
    protected SideServer thisSide;

    public AdminMenu(BufferedReader input, BufferedWriter output,
        List<Message> privateMessages, SideServer thisSide) {
        this.input = input;
        this.output = output;
        this.privateMessages = privateMessages;
        this.thisSide = thisSide;
    }

    @Override
    public void showMenu(User user) {
        try {
            boolean checkIfExit = false;

            while (!checkIfExit) {
                if (thisSide.isInterrupted()) {
                    break;
                }

                adminService.send("to public chat | to rooms | find user | see connected users | "
                    + "see all users | send message to.. | check private messages| invitations "
                    + Invitations.INSTANCE.checkInvitationsByUser(user)
                    + " | ban/delete/update user | create/delete room | logoff | exit ", output);

                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        adminService.send("Public chat:", output);
                        adminService.showPublicChat(output, input, user);
                        break;
                    case 2:
                        adminService.send("Redirect to rooms", output);
                        adminService.showRoomMenu(output, input, user);
                        break;
                    case 3:
                        adminService.findUser(output, input);
                        break;
                    case 4:
                        adminService.showConnectedUsers(output, user);
                        break;
                    case 5:
                        adminService.showAllUsers(output, user);
                        break;
                    case 6:
                        adminService.sendMessageTo(output, input, user, privateMessages);
                        break;
                    case 7:
                        adminService.send("Redirect to private messages", output);
                        adminService.showPrivateMessages(user, privateMessages, output, input);
                        break;
                    case 8:
                        adminService.showInvitations(output, input, user);
                        break;
                    case 9:
                        adminService.userChangesMenu(output, input, user);
                        break;
                    case 10:
                        adminService.adminRoomMenu(output, input, user);
                        break;
                    case 11:
                        adminService.exit("You logged off", thisSide, output);
                        checkIfExit = true;
                        break;
                    case 12:
                        adminService.exit("Exit from application", thisSide, output);
                        checkIfExit = true;
                        break;
                }
            }


        } catch (IOException e) {
            adminService.send("Something went wrong, try again", output);
        }catch (NumberFormatException ex){
            adminService.send("Wrong input, try again", output);
        }
    }

}
