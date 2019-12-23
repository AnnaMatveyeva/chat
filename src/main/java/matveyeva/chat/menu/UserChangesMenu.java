package matveyeva.chat.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import matveyeva.chat.entity.User;
import matveyeva.chat.service.UserChangesService;
import org.apache.log4j.Logger;

public class UserChangesMenu implements Menu {

    protected BufferedReader input;
    protected BufferedWriter output;
    private static final org.apache.log4j.Logger logger = Logger.getLogger(RoomChangesMenu.class);

    private UserChangesService userChangesService = UserChangesService.getInstance();

    public UserChangesMenu(BufferedWriter output, BufferedReader input) {
        this.output = output;
        this.input = input;
    }



    @Override
    public void showMenu(User user) {
        boolean checkIfExit = false;
        try {
            while (!checkIfExit) {
                userChangesService.send("delete user | update user | ban user | make user admin |exit", output);
                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        userChangesService.deleteUser(output, input, user);
                        break;
                    case 2:
                        userChangesService.updateUser(output, input, user);
                        break;
                    case 3:
                        userChangesService.banUser(output, input, user);
                        break;
                    case 4:
                        userChangesService.makeUserAdmin(output, input, user);
                    case 5:
                        checkIfExit = true;
                        break;
                }

            }
        } catch (NumberFormatException ex) {
            userChangesService.send("Wrong input, try again", output);
        } catch (IOException e) {
            e.printStackTrace();
            userChangesService.send("Something went wrong", output);
        }
    }
}
