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

public enum UserCrud {
    INSTANCE;

    private final Validator validator = new Validator();

    public User create(String str) throws InvalidUserException {
        User user = split(str);
        for (User u : UserDB.INSTANCE.users) {
            if (u.getName().equals(user.getName())) {
                throw new InvalidUserException("User already exists");
            }
        }
        UserDB.INSTANCE.users.add(user);
        return user;

    }

    public void delete(User user) throws IOException {
        UserDB.INSTANCE.users.remove(user);
        reloadUsers();
    }

    public void deleteAll() throws IOException {
        UserDB.INSTANCE.users.clear();
        reloadUsers();
    }

    public User update(User oldUser, String newUser) throws InvalidUserException {

        User newuser = split(newUser);
        for (User u : UserDB.INSTANCE.users) {
            if (u.equals(newuser)) {
                throw new InvalidUserException("User already exists");
            }
        }
        User user1 = findByName(oldUser.getName());
        user1.setName(newuser.getName());
        user1.setPassword(newuser.getPassword());
        return user1;
    }

    public User findByName(String name) throws InvalidUserException {
        for (User user : UserDB.INSTANCE.users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        throw new InvalidUserException("User not found");
    }

    public void setUserStatus(User user) {
        if (UserDB.INSTANCE.users.contains(user)) {
            for (User u : UserDB.INSTANCE.users) {
                if (user.equals(u)) {
                    u.setStatus(user.getStatus());
                }
            }
        }
    }

    public Set<User> findAll() {
        return UserDB.INSTANCE.users;
    }

    public void reloadUsers() throws IOException {
        UserDB.INSTANCE.save();

    }

    private User split(String str) throws InvalidUserException {
        String[] userstr = str.split(",");
        if (userstr.length == 2) {
            User user = new User(userstr[0], userstr[1]);
            if (!validator.checkPersonData(user.getName())) {
                throw new InvalidUserException(
                    "Incorrect user name. Name should has from 3 to 15 characters and contains only letters and numerals");
            } else if (!validator.checkPersonData(user.getPassword())) {
                throw new InvalidUserException(
                    "Incorrect user password. Password should has from 3 to 15 characters and contains only letters and numerals");
            }
            if (user.getName().equalsIgnoreCase("admin")) {
                user.setRole("ADMIN");
            }
            return user;
        } else {
            throw new InvalidUserException("Incorrect user data");
        }
    }

    public User findOne(String namePass) throws InvalidUserException {
        User user = split(namePass);
        if (UserDB.INSTANCE.users.contains(user)) {
            for (User u : UserDB.INSTANCE.users) {
                if (user.equals(u)) {
                    user = u;
                }
            }
            return user;
        } else {
            throw new InvalidUserException("User not exists");
        }

    }

}
