package tools;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientConnection implements Runnable {
    private boolean closed;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public static Map<String, ClientConnection> onlineUsers = new ConcurrentHashMap<>();

    public ClientConnection(Socket socket) {
        closed = true;
        try {
            this.socket = socket;

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            this.bufferedWriter = new BufferedWriter(outputStreamWriter);
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            this.bufferedReader = new BufferedReader(inputStreamReader);
        } catch (IOException e) {
            System.out.println("[!] ERROR OCCURRED WITH NEW CLIENT.");
            e.printStackTrace();
            close();
        }
    }

    @Override
    public void run() {
        closed = false;
        initialize();

        while (!closed) {
            connectUser();
            connectionLoop();
        }
    }

    public void initialize() {
        send(this, "[SERVER]: Type 'signup' to register or 'login' to log in.");
        String action = read();

        if ("signup".equalsIgnoreCase(action)) {
            send(this, "[SERVER]: Choose a username.");
            String newUsername = read();
            send(this, "[SERVER]: Choose a password.");
            String newPassword = read();

            if (Database.signUp(newUsername, newPassword)) {
                send(this, "[SERVER]: Signup successful! Please restart and log in.");
            } else {
                send(this, "[SERVER]: Username already exists.");
            }
            close();
            return;
        }

        send(this, "[SERVER]: Enter your username.");
        username = read();
        send(this, "[SERVER]: Enter your password.");
        String password = read();

        if (Database.signIn(username, password)) {
            if (onlineUsers.containsKey(username)) {
                send(this, "[SERVER]: That user is already signed in.");
                close();
                return;
            }

            onlineUsers.put(username, this);
            send(this, "[SERVER]: Login successful!");
            System.out.println(username + " logged in.");
        } else {
            send(this, "[SERVER]: Invalid credentials.");
            close();
        }
    }

    public void connectUser() {
        // TODO
    }

    public void connectionLoop() {
        // TODO
    }

    public String read() {
        while (socket.isConnected() && !closed) {
            try {
                return bufferedReader.readLine();
            } catch (IOException e) {
                System.out.println("[!] ERROR OCCURRED READING FROM CLIENT");
                e.printStackTrace();
                close();
            }
        }
        return null;
    }

    public void send(ClientConnection recipient, String message) {
        try {
            recipient.bufferedWriter.write(message);
            recipient.bufferedWriter.newLine();
            recipient.bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("[!] ERROR OCCURRED SENDING TO CLIENT");
            e.printStackTrace();
            close();
        }
    }

    public void close() {
        if (username != null) {
            onlineUsers.remove(username);
        }

        System.out.println("[!] Closing " + (username != null ? username : "unknown user") + "'s connection.");
        closed = true;
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
