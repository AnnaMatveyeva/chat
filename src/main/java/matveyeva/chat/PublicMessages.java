package matveyeva.chat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PublicMessages {
    private volatile List<Message> publicMessages = new ArrayList<Message>();
    private static PublicMessages instance = null;


    public List<Message> getPublicMessages() {
        return publicMessages;
    }

    public void setPublicMessages(List<Message> publicMessages) {
        this.publicMessages = publicMessages;
    }

    public synchronized static PublicMessages getInstance(){
        if(instance == null){
            instance = new PublicMessages();
        }
        return instance;

    }
    private PublicMessages(){}
}

