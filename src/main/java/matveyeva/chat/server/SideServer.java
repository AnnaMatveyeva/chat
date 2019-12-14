package matveyeva.chat.server;

import matveyeva.chat.User;
import matveyeva.chat.UserCrud;
import matveyeva.chat.exception.InvalidUserException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SideServer extends Thread{

    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private UserCrud crud;
    private User user;

    public SideServer(Socket socket) {
        this.socket = socket;
        this.crud = new UserCrud();
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
                    send("to public chat | too rooms | find user | see all users | send message to.. | logoff | exit");

                    String answer = input.readLine();
                    switch (Integer.parseInt(answer)) {
                        case 1:
                            send("Redirect to chat");
                            //переходит к чату
                            check = true;
                            break;
                        case 2:
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
                            this.user.setStatus(User.Status.OFFLINE);
                            crud.reloadUsers();
                            check = true;
                            break start;
                        case 7:
                            this.user.setStatus(User.Status.OFFLINE);
                            System.out.println(this.user.getStatus());
                            send("exit");
                            crud.reloadUsers();
                            this.shutdown();
                            check = true;
                    }
                }

            }
        }catch (IOException ex){
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
            System.out.println("wrote " + msg);
            output.flush();
        } catch (IOException ex) {

        }
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
                            System.out.println(this.user.getStatus());
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
                            System.out.println(this.user.getStatus());
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
