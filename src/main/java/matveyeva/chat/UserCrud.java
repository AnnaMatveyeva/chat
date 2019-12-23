package matveyeva.chat;

import java.io.IOException;
import java.util.Set;
import matveyeva.chat.entity.User;
import matveyeva.chat.exception.InvalidUserException;
import matveyeva.chat.exception.UserExistsException;

public enum UserCrud {
    INSTANCE;

    private final Validator validator = new Validator();

    public User create(String userName, String userPassword) throws InvalidUserException, UserExistsException {

        if (!validator.checkPersonData(userName)) {
            throw new InvalidUserException(
                "Incorrect user name. Name should has from 3 to 15 characters and contains only letters and numerals");
        } else if (!validator.checkPersonData(userPassword)) {
            throw new InvalidUserException(
                "Incorrect user password. Password should has from 3 to 15 characters and contains only letters and numerals");
        }
        User newUser = new User(userName, userPassword);
        if (newUser.getName().equalsIgnoreCase("admin")) {
            newUser.setRole("ADMIN");
        }else newUser.setRole("USER");

        for (User u : UserDB.INSTANCE.users) {
            if (u.getName().equals(newUser.getName())) {
                throw new UserExistsException();
            }
        }
        UserDB.INSTANCE.users.add(newUser);
        return newUser;

    }

    public void delete(User user) throws IOException {
        UserDB.INSTANCE.users.remove(user);
        reloadUsers();
    }

    public void deleteAll() throws IOException {
        UserDB.INSTANCE.users.clear();
        reloadUsers();
    }

    public User update(User oldUser, String newUserName, String newUserPass)
        throws InvalidUserException, UserExistsException {

        if (!validator.checkPersonData(newUserName)) {
            throw new InvalidUserException(
                "Incorrect user name. Name should has from 3 to 15 characters and contains only letters and numerals");
        } else if (!validator.checkPersonData(newUserPass)) {
            throw new InvalidUserException(
                "Incorrect user password. Password should has from 3 to 15 characters and contains only letters and numerals");
        }

        User newUser = new User(newUserName, newUserPass);
        if(!newUserName.equals(oldUser.getName())){
            for (User u : UserDB.INSTANCE.users) {
                if (u.getName().equals(newUser.getName())) {
                    throw new UserExistsException();
                }
            }
        }

        User userToUpdate = findByName(oldUser.getName());
        userToUpdate.setName(newUser.getName());
        userToUpdate.setPassword(newUser.getPassword());
        return userToUpdate;
    }

    public User findByName(String name) {
        for (User user : UserDB.INSTANCE.users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
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

}
