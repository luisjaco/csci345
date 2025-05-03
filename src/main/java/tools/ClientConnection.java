package tools;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import tools.Database;

public class ClientConnection implements Runnable {
    private boolean closed;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private int userId;
    private String username;
    private Database sql;
    public static Map<Integer, ClientConnection> onlineUsers = new ConcurrentHashMap<>();

    private boolean signedIn;
    private boolean inChat;

    /**
     * The ClientConnection class will handle all client-server interactions.
     * A ClientConnection can sign a user in, and relay messages to other clients.
     * It will hold state of client (active, not active, connected, attempting to connect) and query/update the sql database where necessary.
     * @param socket Socket of client connection
     */
    public ClientConnection(Socket socket, Database sql) {
        closed = true;
        signedIn = false;
        inChat = false;
        this.sql = sql; // do not close sql, Server will handle that.
        try {
            this.socket = socket;

            // Creates writer and reader for reading & sending messages.
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            this.bufferedWriter = new BufferedWriter(outputStreamWriter);
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            this.bufferedReader = new BufferedReader(inputStreamReader);

        } catch (IOException e) {
            System.out.println("[!] An error occurred connecting to the client.");
            e.printStackTrace();
            close();
        }
    }

    @Override
    public void run() {
        closed = false;
        /*
            menu structure:

            sign in, sign up
            {
                connect to user prompt
                {
                    user-to-user message
                }
            }

            user can use menus to traverse forward or backwards
            always handle closing user sequence
         */
        while (!closed) {
            startMenu();

            while (!closed && signedIn) {
                connectUserMenu();
                while (!closed && inChat) {
                    userChatLoop();
                }
            }
        }
    }

    private void startMenu() {
        System.out.println("-> [!] Attempting to sign in client."); // any print() will show up in server, while send() will go to user

        while (!closed && !signedIn) {
            send(this, """
                    x--------------------------------------------x
                                     START PAGE
                    [2] Sign in
                    [1] Sign up
                    [0] Exit
                    x--------------------------------------------x""");

            int response = readInt(0, 2);

            switch (response) {
                case 2 -> signIn();
                case 1 -> signUp();
                case 0 -> close();
            }
        }
    }

    /**
     * Will sign a user in, user may choose to retry sign in or
     * go back to previous menu if username and password combo do not match,
     * or the user is already signed in.
     */
    private void signIn() {
        while (!closed && !signedIn) {
            send(this, "[!] Please enter the username.");
            String username = read();
            send(this, "[!] Please enter the password.");
            String password = read();

            if (sql.verifyUserActive(username)) {
                send(this, "[!] This user is already active and cannot be signed in.");
            } else {
                // attempt to sign in
                if (sql.signIn(username, password)) {
                    this.username = username;
                    userId = sql.getUserId(username);
                    System.out.printf("-> [!] User [%s] ID#%d has signed in.\n", username, userId);
                    signedIn = true; // exits loop
                    break;
                } else {
                    send(this, "[!] The username or password is incorrect.");
                }
            }


            // retry message if user already active or incorrect username or password
            send(this, """
                        [1] Try again
                        [0] Go back""");
            int response = readInt(0, 1);
            if (response == 0) {
                break;
            }
        }
    }

    /**
     * Will sign a user up, user may choose to retry sign up or go back if
     * username not valid.
     */
    private void signUp() {
        while (!closed && !signedIn) {
            send(this, "[!] Please enter a username.");
            String username = read();

            // check if username exists
            if (sql.verifyUserExists(username)) {
                send(this, """
                        [!] This username is already taken.
                        [1] Try again
                        [0] Go back""");
                int response = readInt(0, 1);
                if (response == 1) {
                    continue;
                } else {
                    break; // go back
                }
            }

            send(this, "[!] Please enter a password.");
            String password = read();

            sql.signUp(username, password);
            sql.signIn(username, password);
            this.username = username;
            userId = sql.getUserId(username);
            System.out.printf("-> [!] User [%s] ID#%d has signed in.\n", username, userId);
            signedIn = true; // exits loop
        }
    }
    private void connectUserMenu() {
        send(this,"TODO.. UNDER CONSTRUCTION. DO NOT SEND OR IDK WHAT WILL HAPPEN");
        read();
    }

    private void userChatLoop() {
        // TODO
    }

    private String read() {
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

    /**
     * Will continually prompt a user for an integer value until a value within the given range is provided.
     * Range in inclusive. [min <= value <= max]
     * @param min Min integer value
     * @param max Max integer value
     * @return Integer value within range
     */
    private int readInt(int min, int max) {
        while (true) {
            String uncastedResponse = read();
            try {
                int response = Integer.parseInt(uncastedResponse);

                if ( (min <= response) && (response <= max) ) {
                    return response;
                } else {
                    send(this, "[!] Please enter a valid integer between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                send(this, "[!] Please enter a valid integer between " + min + " and " + max + ".");
            }
        }
    }

    private void send(ClientConnection recipient, String message) {
        try {
            recipient.bufferedWriter.write(message);
            recipient.bufferedWriter.newLine();
            recipient.bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("[!] An error occurred while sending a message to the client.");
            e.printStackTrace();
            close();
        }
    }

    private void close() {
        // send exitKey to clients so it can shut down properly
        // TODO, this breaks if client terminates through the terminal
        send(this, "%server_disconnect%");

        if (userId != -1) {
            onlineUsers.remove(userId);
        }

        System.out.println("[!] Closing " + (signedIn ? username : "unknown user") + "'s connection.");
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
