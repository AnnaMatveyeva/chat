package matveyeva.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import matveyeva.chat.Entity.User;

public enum UserDB {
    INSTANCE;

    public Set<User> users = new HashSet<>();

    UserDB() {
        try {
            loadUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() throws Exception {
        try (FileInputStream fileInputStream = new FileInputStream(new File("users.ser"))) {
            while (fileInputStream.available() > 0) {
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                users = (Set<User>) objectInputStream.readObject();
            }
        }

    }

    public void save() throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File("users.ser"))) {
            ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
            out.writeObject(users);
        }

    }
}
