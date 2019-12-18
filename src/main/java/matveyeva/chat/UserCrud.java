package matveyeva.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import matveyeva.chat.Entity.User;
import matveyeva.chat.exception.InvalidUserException;

public class UserCrud {
    private final Validator validator = new Validator();
    public volatile static Set<User> users;

    public UserCrud(){
        try {
            loadUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User create(String str) throws InvalidUserException {
        User user = split(str);
        for(User u : users){
            if(u.getName().equals(user.getName())){
                throw new InvalidUserException("User already exists");
            }
        }
        users.add(user);
        return user;

    }

    public void delete(User user) throws IOException{
        users.remove(user);
        reloadUsers();
    }

    public void deleteAll() throws IOException{
        ArrayList<User> arr = new ArrayList<User>(users);
        users.removeAll(arr);
        reloadUsers();
    }

    public User update(User oldUser, String newUser) throws InvalidUserException{

        User user = split(newUser);
        for(User u : users){
            if(u.equals(user)){
                throw new InvalidUserException("User already exists");
            }
        }
        user.setStatus(oldUser.getStatus());
        user.setRole(oldUser.getRole());
        users.remove(oldUser);
        users.add(user);
        return user;
    }

    public User findByName(String name) throws InvalidUserException{
        for(User user : users){
            if(user.getName().equals(name)) {
                return user;
            }
        }
        throw new InvalidUserException("User not found");
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
            if(!validator.checkPersonData(user.getName())){
                throw new InvalidUserException("Incorrect user name. Name should has from 3 to 15 characters and contains only letters and numerals");
            }else if(!validator.checkPersonData(user.getPassword())){
                throw new InvalidUserException("Incorrect user password. Password should has from 3 to 15 characters and contains only letters and numerals");
            }
            if (user.getName().equalsIgnoreCase("admin")) {
                user.setRole("ADMIN");
            }
            return user;
        }else throw new InvalidUserException("Incorrect user data");
    }

    public User findOne(String namePass) throws InvalidUserException{
        User user = split(namePass);
        if(users.contains(user)){
            for(User u : users) {
                if(user.equals(u)) {
                    user = u;
                }
            }
            return user;
        }else throw new InvalidUserException("User not exists");

    }

}
