package matveyeva.chat.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import matveyeva.chat.entity.User;
import matveyeva.chat.enums.Rooms;
import matveyeva.chat.service.InvitationService;
import matveyeva.chat.service.RoomService;
import org.apache.log4j.Logger;

public class RoomMenu implements Menu{

    protected BufferedReader input;
    protected BufferedWriter output;
    private static final org.apache.log4j.Logger logger = Logger.getLogger(RoomChangesMenu.class);

    private RoomService roomService = RoomService.getInstance();
    private InvitationService invitationService = InvitationService.getInstance();
    public RoomMenu(BufferedWriter output, BufferedReader input) {
        this.output = output;
        this.input = input;
    }

    @Override
    public void showMenu(User user) {
        boolean checkIfExit = false;
        if (!Rooms.INSTANCE.getRoomsList().isEmpty()) {
            try {
                while (!checkIfExit) {
                    roomService.send("choose room | invite user to room | exit", output);
                    String answer = input.readLine();
                    switch (Integer.parseInt(answer)) {
                        case 1:
                            roomService.showRooms(user, input, output);
                            break;
                        case 2:
                            invitationService.inviteUser(output, input, user);
                            break;
                        case 3:
                            checkIfExit = true;
                            break;
                    }
                }
            } catch (NumberFormatException ex) {
                roomService.send("Wrong input, try again", output);
            } catch (Exception e) {
                e.printStackTrace();
                roomService.send("Something went wrong", output);
            }
        } else {
            roomService.send("There is nothing to show", output);
        }
    }
}
