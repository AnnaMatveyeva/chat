package matveyeva.chat.enums;

import java.util.ArrayList;
import java.util.List;
import matveyeva.chat.Entity.Message;

public enum PublicMessages {
    INSTANCE;
    private volatile List<Message> publicMessages = new ArrayList<Message>();

    public List<Message> getPublicMessages() {
        return publicMessages;
    }
    public void setPublicMessages(List<Message> publicMessages) {
        this.publicMessages = publicMessages;
    }
}

