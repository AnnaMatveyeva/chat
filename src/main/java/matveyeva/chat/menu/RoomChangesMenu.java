package matveyeva.chat.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import matveyeva.chat.entity.User;
import matveyeva.chat.service.RoomChangesService;
import org.apache.log4j.Logger;

public class RoomChangesMenu implements Menu {

    protected BufferedReader input;
    protected BufferedWriter output;
    private static final org.apache.log4j.Logger logger = Logger.getLogger(RoomChangesMenu.class);

    private RoomChangesService roomChangesService = RoomChangesService.getInstance();

    public RoomChangesMenu(BufferedWriter output, BufferedReader input) {
        this.output = output;
        this.input = input;
    }

    @Override
    public void showMenu(User user) {
        boolean checkIfExit = false;
        try {
            while (!checkIfExit) {
                roomChangesService
                    .send("delete room | update room | see all rooms | create room |exit", output);
                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        roomChangesService.deleteRoom(output, input, user);
                        break;
                    case 2:
                        roomChangesService.updateRoom(output, input, user);
                        break;
                    case 3:
                        roomChangesService.showAllRooms(output, input, user);
                        break;
                    case 4:
                        roomChangesService.createRoom(output, input, user);
                        break;
                    case 5:
                        checkIfExit = true;
                        break;
                }

            }
        } catch (NumberFormatException ex) {
            roomChangesService.send("Wrong input, try again", output);
        } catch (Exception e) {
            e.printStackTrace();
            roomChangesService.send("Something went wrong", output);
        }
    }
}
