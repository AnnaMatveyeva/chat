package matveyeva.chat.server;

import matveyeva.chat.Message;
import matveyeva.chat.PublicMessages;
import matveyeva.chat.User;
import matveyeva.chat.UserCrud;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SideServer extends Thread{

    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private UserCrud crud;
    private User user;
    private PublicMessages instance;
    public SideServer(Socket socket) {
        this.socket = socket;
        this.crud = new UserCrud();
        instance = PublicMessages.getInstance();
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
                            send("Redirect to chat");
                            showPublicChat();
                            break;
                        case 2:
                            send("Redirect to rooms");
                            //переходит к RoomMenu
                            break;
                        case 3:
                            send("Enter username");
                            String name = input.readLine();
                            User user;
                            if((user = crud.findByName(name)) != null) {
                                send(user.getName() + " is " + user.getStatus());
                            } else send("User " + name + " not found");
                            break;
                        case 4:
                            showUsers();
                            break;
                        case 5:
                            send("Enter username");
                            String username = input.readLine();
                            User us;
                            if((us = crud.findByName(username)) != null) {
                                send("Enter a message");
                                //отправить сообщени найденному пользователю
                            } else send("User " + username + " not found");

                            break;
                        case 6:
                            send("Redirect to private messages");
                            //к личным сообщениям
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
        List<Message> pubMessages = new LinkedList<Message>(instance.getPublicMessages());
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
            for(SideServer server : Server.serverList) {
                if(Server.serverList.size() > 1 && !server.equals(this) && server.user.equals(this.user)) {
                    return false;
                }
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
    private void sendToAll(String message){
        for(SideServer ss : Server.serverList) {
            ss.send(message);
        }
    }

}
