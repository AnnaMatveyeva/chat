package matveyeva.chat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

