package matveyeva.chat.server;

import matveyeva.chat.Message;
import matveyeva.chat.PublicMessages;
import matveyeva.chat.User;
import matveyeva.chat.UserCrud;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class SideServer extends Thread{

    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private UserCrud crud;
    private User user;
    private PublicMessages instance;
    private volatile List<Message> privateMessages;

    public SideServer(Socket socket) {
        this.socket = socket;
        this.crud = new UserCrud();
        instance = PublicMessages.getInstance();
        privateMessages = new ArrayList<>();
        try {
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Side server created");
    }

    @Override
    public void run() {
        try {
            System.out.println("running...");
            start:
            while (true) {
                startMenu();

                boolean check = false;

                while(!check) {
                    send("to public chat | too rooms | find user | see all users | send message to.. | check private messages | logoff | exit");

                    String answer = input.readLine();
                    switch (Integer.parseInt(answer)) {
                        case 1:
                            send("Public chat:");
                            showPublicChat();
                            break;
                        case 2:
                            send("Redirect to rooms");
                            //переходит к RoomMenu
                            break;
                        case 3:
                            findUser();
                            break;
                        case 4:
                            showUsers();
                            break;
                        case 5:
                            send("Enter username");
                            String username = input.readLine();
                            User friend;
                            if((friend = crud.findByName(username)) != null && friend.getStatus().equals(User.Status.ONLINE)) {
                                toPrivateChat(friend);
                            } else send("User " + username + " not found or is not online");

                            break;
                        case 6:
                            send("Redirect to private messages");
                            showPrivateMessages();
                            break;
                        case 7:
                            exit("You logged off");
                            check = true;
                            break;
                        case 8:
                            exit("Exit from application");
                            check = true;
                            break;
                    }
                }

            }
        }catch (IOException ex ){
            this.shutdown();
        }
    }

    private void findUser() throws IOException {
        send("Enter username");
        String name = input.readLine();
        User user;
        if((user = crud.findByName(name)) != null) {
            send(user.getName() + " is " + user.getStatus());
        } else send("User " + name + " not found");
    }

    private void showPrivateMessages() throws IOException {
        if(!privateMessages.isEmpty()) {
            Map<String, User> userMess = new HashMap<String,User>();
            StringBuilder str = new StringBuilder();

            for(int i = 0; i< privateMessages.size(); i++) {
                userMess.put(String.valueOf(i + 1),privateMessages.get(i).getUser());
                if(!str.toString().contains(privateMessages.get(i).getUser().getName()))
                    str.append(privateMessages.get(i).getUser().getName() + " | ");
            }

            send("You have private chats with :");
            send(str.toString());
            String answer = input.readLine();

            if(answer.equalsIgnoreCase("exit")){
                send("Exit from private messages");
            }else if(userMess.containsKey(answer)){
                User friend = userMess.get(answer);
                if(!friend.getStatus().equals(User.Status.ONLINE)){
                    send("User " + friend.getName() + "is not online. You  can't answer");
                }else toPrivateChat(friend);
            }
        }else send("You don't have private chats");
    }

    private void toPrivateChat(User friend) throws IOException {
        send("Chat with " + friend.getName());
        List<Message> messages = new ArrayList<Message>(privateMessages);
        for(Message mess : messages){
            if(mess.getUser().equals(friend)){
                send(mess.toString());
            }
        }
        while(true){
            if(privateMessages.size() != messages.size()){
                for(int i = messages.size(); i < privateMessages.size(); i++){
                    Message mess = privateMessages.get(i);
                    messages.add(mess);
                    if(mess.getUser().equals(friend)){
                        send(mess.toString());
                    }
                }
            }
            if(input.ready()){
                String str = input.readLine();
                if(!str.equalsIgnoreCase("exit")) {
                    Message newMess = new Message(this.user,str);
                    sendTo(friend,newMess);
                    send(newMess.toString());
                }else {
                    send("Exit from chat with " + friend.getName());

                    break;
                }
            }
        }
    }

    private void showUsers() {
        List<SideServer> servers = Server.serverList;
        if(servers.size() > 1){
            send("All connected users:");
            for(SideServer server : servers){
                if(server.user !=null && !server.user.equals(this.user)){
                    send(server.user.getName());
                }
            }
        }else send("There is nobody but you");

    }

    private void send(String msg) {
        try {
            output.write(msg + "\n");
            output.flush();
        } catch (IOException ex) {

        }
    }

    private void showPublicChat() throws IOException {
        List<Message> pubMessages = new ArrayList<>(instance.getPublicMessages());
        for(Message mess : pubMessages){
            send(mess.toString());
        }
        while(true){
            if(instance.getPublicMessages().size() != pubMessages.size()){
                for(int i = pubMessages.size(); i < instance.getPublicMessages().size(); i++){
                    Message mess = instance.getPublicMessages().get(i);
                    pubMessages.add(mess);
                    send(mess.toString());
                }
            }
            if(input.ready()){
                String str = input.readLine();
                if(!str.equalsIgnoreCase("exit")) {
                    Message newMess = new Message(this.user,str);
                    instance.getPublicMessages().add(newMess);

                }else {
                    send("Exit from public chat");
                    break;
                }
            }
        }
    }

    private void exit(String message) throws IOException{
        send(message);
        this.user.setStatus(User.Status.OFFLINE);
        crud.setUserStatus(this.user);
        crud.reloadUsers();
        this.user = null;
        if(message.equalsIgnoreCase("exit"))
            this.shutdown();
    }

    private void shutdown() {
        try {
            if(!socket.isClosed()) {
                socket.close();
                input.close();
                output.close();
                List<SideServer> list = new ArrayList<SideServer>();
                for (SideServer ss : Server.serverList) {
                    if(ss.equals(this)){
                        ss.interrupt();
                        list.add(ss);
                    }
                }
                Server.serverList.removeAll(list);
            }
        } catch (IOException ignored) {}
    }

    private boolean login(String namePass){
        if((user = crud.findOne(namePass)) != null && user.getStatus() != User.Status.BANNED) {
            try{
                for(SideServer server : Server.serverList) {
                    if(Server.serverList.size() > 1 && !server.equals(this) && server.user.equals(this.user)) {
                        return false;
                    }
                }
            }catch (NullPointerException ex){
                return true;
            }
            return true;
        }else return false;
    }

    private boolean registration(String newUser){
        return (user = crud.create(newUser)) != null;
    }

    private void startMenu() throws IOException{
        boolean check = false;
        while(!check) {
            try {
                send("login | registration | exit");
                String answer = input.readLine();
                switch (Integer.parseInt(answer)) {
                    case 1:
                        send("Enter username,password");
                        String namePass = input.readLine();
                        if(!login(namePass)) {
                            send("Incorrect user data");
                        } else {
                            this.user.setStatus(User.Status.ONLINE);
                            crud.setUserStatus(this.user);
                            check = true;
                        }
                        break;
                    case 2:
                        send("Create new  username,password");
                        String newUser = input.readLine();
                        if(!registration(newUser)) {
                            send("Incorrect user data");
                        } else {
                            this.user.setStatus(User.Status.ONLINE);
                            crud.setUserStatus(this.user);
                            check = true;
                        }
                        break;
                    case 3:
                        send("exit");
                        this.shutdown();
                        check = true;
                        break;
                }
            }catch (NumberFormatException ex){

            }
        }
    }

    private void sendTo(User friend, Message message){
        for(SideServer ss : Server.serverList) {
            if(ss.user.equals(friend)){
                ss.privateMessages.add(message);
            }
        }
    }
}
