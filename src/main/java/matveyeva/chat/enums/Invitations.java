package matveyeva.chat.enums;

import java.util.ArrayList;
import java.util.List;
import matveyeva.chat.Entity.Invitation;
import matveyeva.chat.Entity.User;

public enum Invitations {
    INSTANCE;

    private List<Invitation> invitations = new ArrayList<>();

    public List<Invitation> getInvitationsList(){
        return invitations;
    }

    public int checkInvitationsByUser(User user){
        int count = 0;
        for(Invitation inv : invitations){
            if(inv.getToWho().equals(user))
                count++;
        }
        return count;
    }
}
