package matveyeva.chat.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import matveyeva.chat.Entity.Invitation;
import matveyeva.chat.Entity.Room;
import matveyeva.chat.Entity.User;
import matveyeva.chat.Entity.User.Status;
import matveyeva.chat.enums.Invitations;
import matveyeva.chat.enums.Rooms;
import matveyeva.chat.exception.InvalidUserException;
import org.apache.log4j.Logger;

public class InvitationService extends DefaultService{

    private static final Logger logger = Logger.getLogger(InvitationService.class);

    private static InvitationService instance;

    public static InvitationService getInstance() {
        if (instance == null) {
            instance = new InvitationService();
        }
        return instance;
    }

    public void showInvitations(BufferedWriter output,
        BufferedReader input, User user) throws IOException {
        boolean check = false;
        List<Invitation> userInvitations = new ArrayList<>();
        for (Invitation inv : Invitations.INSTANCE.getInvitationsList()) {
            if (inv.getToWho().equals(user)) {
                userInvitations.add(inv);
            }
        }
        if (!Invitations.INSTANCE.getInvitationsList().isEmpty()) {
            while (!check) {
                if (userInvitations.isEmpty()) {
                    check = true;
                }
                StringBuilder str = new StringBuilder();
                Map<String, Invitation> iMap = new HashMap<>();
                for (int i = 0; i < userInvitations.size(); i++) {
                    str.append(
                        "to " + userInvitations.get(i).getRoom().getTitle() + " from "
                            + userInvitations
                            .get(i)
                            .getFromWho().getName() + "; ");
                    iMap.put(String.valueOf(i + 1), userInvitations.get(i));
                }
                send(str.toString(), output);
                String answer = input.readLine();

                if (answer.equalsIgnoreCase("exit")) {
                    check = true;
                }

                if (iMap.containsKey(answer)) {
                    Invitation invite = iMap.get(answer);
                    send(invite.toString(), output);
                    send("to room | delete invitation | return", output);
                    String choice = input.readLine();
                    switch (Integer.parseInt(choice)) {
                        case 1:
                            RoomService.getInstance().toRoom(invite.getRoom(), user, output, input);
                            userInvitations.remove(invite);
                            Invitations.INSTANCE.getInvitationsList().remove(invite);
                            check = true;
                            break;
                        case 2:
                            userInvitations.remove(invite);
                            Invitations.INSTANCE.getInvitationsList().remove(invite);
                            logger.info(
                                "User " + user.getName() + "deleted invitation from " + invite
                                    .getFromWho().getName() + " in room " + invite.getRoom()
                                    .getTitle());
                            send("Invitation was deleted", output);
                            check = true;
                            break;
                    }
                }
            }
        } else {
            send("Nothing to show", output);
        }
    }

    public void inviteUser(BufferedWriter output, BufferedReader input, User user)
        throws IOException {
        send("Enter user name", output);
        String username = input.readLine();
        User usertoInvite;
        try {
            usertoInvite = crud.findByName(username);
            if (usertoInvite.getStatus().equals(Status.ONLINE)) {
                Room r = null;
                send("Enter room title", output);
                String title = input.readLine();
                for (Room room : Rooms.INSTANCE.getRoomsList()) {
                    if (room.getTitle().equals(title)) {
                        r = room;
                        sendInvitation(user, usertoInvite, r);
                        send("User was invited", output);
                        break;
                    }
                }
                if (r == null) {
                    send("Room not found", output);
                }
            } else {
                send("User are not online", output);
            }
        } catch (InvalidUserException ex) {
            send(ex.getMessage(), output);
        }
    }

    public void sendInvitation(User fromWho, User toWho, Room room) {
        Invitations.INSTANCE.getInvitationsList().add(new Invitation(fromWho, toWho, room));

        logger.info("User " + fromWho.getName() + " sent invitation to " + toWho.getName()
            + "in room " + room.getTitle());
    }
}
