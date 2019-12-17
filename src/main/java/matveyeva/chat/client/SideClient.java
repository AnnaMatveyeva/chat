package matveyeva.chat.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class SideClient {

    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private Scanner scanner;

    public SideClient(Socket socket) {

        this.socket = socket;
        scanner = new Scanner(System.in);
        try {
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            new WriteMsg().start();
            new ReadMsg().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shutdown() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                input.close();
                output.close();
                System.exit(0);
            }
        } catch (IOException ignored) {}
    }

    public class WriteMsg extends Thread {
        @Override
        public void run() {
            while (true) {
                String message;
                try {
                    message = scanner.nextLine();
                    if (!message.equals(" ") && !message.equals("")) {
                        output.write(message + "\n");
                        output.flush();
                    } else {
                        System.out.println("You wrote empty line,try again");
                    }
                } catch (IOException e) {
                    SideClient.this.shutdown();
                }
            }
        }
    }

    private class ReadMsg extends Thread {

        @Override
        public void run() {
            String message;
            try {
                while (true) {
                    message = input.readLine();
                    if(message.equalsIgnoreCase("exit from application")){
                        System.out.println("Exit");
                        SideClient.this.shutdown();
                    }
                    System.out.println(message);
                }
            } catch (IOException e) {
                SideClient.this.shutdown();
            }
        }
    }
}
