package matveyeva.chat.enums;

import java.util.ArrayList;
import java.util.List;
import matveyeva.chat.entity.Invitation;
import matveyeva.chat.entity.User;

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
