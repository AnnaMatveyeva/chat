package matveyeva.chat;

import matveyeva.chat.Entity.User;
import matveyeva.chat.exception.InvalidUserException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class UserCrud {
    public static Set<User> users;

    public UserCrud(){
        try {
            loadUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User create(String str){
        try{
            User user = split(str);
            if(users.add(user))
                return user;
            else return null;
        }catch (InvalidUserException | IllegalArgumentException  ex){
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public void delete(User user) throws Exception{
        users.remove(user);
        reloadUsers();
    }

    public void deleteAll() throws Exception{
        ArrayList<User> arr = new ArrayList<User>(users);
        users.removeAll(arr);
        reloadUsers();
    }

    public User update(User oldUser, String newUser){
        try{
            User user = split(newUser);
            users.remove(oldUser);
            users.add(user);
//            reloadUsers();
            return user;
        }catch(InvalidUserException ex){
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public User findByName(String name) {
        for(User user : users){
            if(user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    public void setUserStatus(User user){
        if(users.contains(user)) {
            for(User u : users) {
                if(user.equals(u)) {
                    u.setStatus(user.getStatus());
                }
            }
        }
    }

    public Set<User> findAll(){
        return users;
    }

    private void loadUsers() throws Exception {
        if(users == null){
            users = new HashSet<User>();
            try(FileInputStream fileInputStream = new FileInputStream(new File("users.ser"))){
                while (fileInputStream.available() > 0) {
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                    User user = (User) objectInputStream.readObject();
                    users.add(user);
                }
            }
        }
    }

    public void reloadUsers() throws IOException{
        try(FileOutputStream fileOutputStream = new FileOutputStream(new File("users.ser"))) {
            for(User user : users) {
                ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
                out.writeObject(user);
            }
        }

    }

    private User split(String str) throws InvalidUserException {
        String[] userstr = str.split(",");
        if(userstr.length == 2){
            User user = new User(userstr[0], userstr[1]);
            user.isUserValid();
            if(user.getName().equalsIgnoreCase("admin")){
                user.setRole("ADMIN");
            }
            return user;
        }else throw new InvalidUserException("Incorrect user data");
    }

    public User findOne(String namePass) {
        try{
            User user = split(namePass);
            if(users.contains(user)){
                for(User u : users) {
                    if(user.equals(u)) {
                        user = u;
                    }
                }
                return user;
            }else return null;
        }catch (InvalidUserException ex){
            return null;
        }
    }

}
