package tools;

import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientConnection implements Runnable {
    // keeps a list of all current users, so ClientConnection can access other values
    public static Map<Integer, ClientConnection> activeUsers = new ConcurrentHashMap<>();
    private boolean closed;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private final int port;
    private final Database sql;
    private int userId;
    private String username;
    private String targetUsername;
    private int targetUserId;
    private boolean signedIn;
    private boolean inChat;
    // for the wait for user sequence.
    private boolean threadRunning;
    /**
     * The ClientConnection class will handle all client-server interactions.
     * A ClientConnection can sign a user in, and relay messages to other clients.
     * It will hold state of client (active, not active, connected, attempting to connect) and query/update the sql database where necessary.
     * @param socket Socket of client connection
     */
    public ClientConnection(Socket socket, Database sql, int port) {
        closed = true;
        signedIn = false;
        inChat = false;
        userId = -1;
        this.sql = sql; // do not close sql, Server will handle that.
        this.port = port; // used when sending messages to the console.
        try {
            this.socket = socket;

            // Creates writer and reader for reading & sending messages.
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            this.bufferedWriter = new BufferedWriter(outputStreamWriter);
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            this.bufferedReader = new BufferedReader(inputStreamReader);

        } catch (IOException e) {
            sendToServer("[!] An error occurred connecting to the client.");
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
                connect to user, sign out, sign out & quit
                {
                    user-to-user messages
                }
            }

            user can use menus to traverse forward or backwards
            always handle closing user sequence
         */
        while (!closed) {
            startMenu();

            while (!closed && signedIn) {
                userHomeMenu();
                // will call the userChatMenu if they connect.
            }
        }
    }

    private void startMenu() {
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
                case 2 -> signInMenu();
                case 1 -> signUpMenu();
                case 0 -> {
                    send(this, "[!] Goodbye!");
                    send(this, "%server_disconnect%");
                    close();
                }
            }
        }
    }

    /**
     * Will sign a user in, user may choose to retry sign in or
     * go back to previous menu if username and password combo do not match,
     * or the user is already signed in.
     */
    private void signInMenu() {
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
                    signIn(username);
                    break; // exits loop
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
    private void signUpMenu() {
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
            sendToServer("[!] New user [" + username + "] created.");
            // sign in through sql before setting up on signIn()
            sql.signIn(username, password);
            signIn(username);
        }
    }

    /**
     * Will sign in a user and set all class variables.
     * @param username User username
     */
    private void signIn(String username) {
        this.username = username;
        userId = sql.getUserId(username);
        sendToServer("[!] User [%s] ID#%d has signed in.".formatted(username, userId));
        signedIn = true;
        // add to active users list
        activeUsers.put(userId, this);
    }

    /**
     * Will sign a user out and set class variables.
     */
    private void signOut() {
        sendToServer("[!] User [%s] ID#%d has signed out.".formatted(username, userId));
        // remove from active users
        activeUsers.remove(userId);
        sql.signOut(userId);
        this.username = null;
        userId = -1;
        signedIn = false;
        inChat = false; // just in case
    }

// SHOULD display message history with desired user from historyWithWho method
    private void displayMsgHistory()
    {
        // Fetch and print the chat history
        send(this, "\nChat history between " + username + " and " + targetUsername + ":");
        ResultSet rs = sql.getMessageHistory(userId, targetUserId);
        if (rs == null) {
            send(this, "[!] No result set returned (possible DB error).");
            return;
        }


        try {
            while (rs != null && rs.next()) {
                String sender = rs.getString("sender_name");
                String receiver = rs.getString("receiver_name");
                String content = rs.getString("message_content");
                String timestamp = rs.getString("timestamp");

                send(this, "[" + timestamp + "] " + sender + " ➡ " + receiver + ": " + content);
            }
        } catch (SQLException e) {
            System.out.println("[!] Error reading chat history.");
            e.printStackTrace();
        }
    }
    // asks user who they want to view history with
    private void historyWithWho()
    {
        send(this, "[!] Please enter the user you would like to see your message history with.");
        String inputUsername = read();

        targetUsername = inputUsername;
        targetUserId = sql.getUserId(inputUsername);

        // checks if user exists [Need to find a way to have this parameter apply to the max user id at a given time]
        if (targetUserId <= 0) {
            send(this,"[!] Username not found: " + targetUsername);
        } else {
            displayMsgHistory();
        }


    }


    private void userHomeMenu() {
        send(this,"""
                    x--------------------------------------------x
                                       WELCOME
                    USER [%s]
                    [3] My Message History
                    [2] Message a user
                    [1] Sign out
                    [0] Sign out & exit
                    x--------------------------------------------x""".formatted(username)
        );
        int response = readInt(0, 3);

        switch (response) {
            case 3 -> historyWithWho();
            case 2 -> connectUserMenu();
            case 1 -> signOut();
            case 0 -> {
                send(this, "[!] Goodbye!");
                send(this, "%server_disconnect%");
                close();
            }
        }
    }

    private void connectUserMenu() {
        // first print all active users.
        send(this, """
                .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .`
                [!] Active users:""");
        Map<String, Integer> users = sql.getActiveUsers();
        users.remove(username); // remove yourself from list
        if (users.size() == 0) {
            send(this, """
                    [!] You are the only user online currently. Please try again later.
                    .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .`""");
            return; // go back to userHomeMenu()
        }

        for (String username : users.keySet()) {
            send(this, "- " + username);
        }

        // style
        send(this, ".` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .` .`");

        // prompt user for username they want to connect with.
        boolean gettingUser = true;
        while (!closed && gettingUser) {
            send(this, "[!] Enter the user you want to connect with:");
            String username = read();

            // verify user in list
            if (users.containsKey(username)) {
                gettingUser = false;
                // check if user active.
                if (sql.verifyUserChatting(username)) {
                    send(this, "[!] " + username + " is currently in a chat with another user.");
                }
                send(this, """
                        [!] Would you like to connect with %s?
                        [1] Yes
                        [0] No""".formatted(username));
                int response = readInt(0, 1);
                if (response == 1) {
                    waitForConnection(users.get(username));
                }
                // else will go back to connectUserMenu()
            }
            else {
                // prompt user to retry
                send(this, """
                        [!] Incorrect username
                        [2] Reload active user list
                        [1] Retry username
                        [0] Go back""");
                int response = readInt(0, 2);
                switch (response) {
                    // case 1 would just cause a loop
                    case 2 -> {
                        connectUserMenu();
                        gettingUser = false; // prevent infinite looping
                    }
                    case 0 -> gettingUser = false;
                }
            }
        }
    }

    /**
     * Will wait until a connection is made or the user quits.
     * @param userId User id to connect to.
     */
    private void waitForConnection(int userId) {
        sendToServer("[!] Attempting to connect [" + this.username + "] to [" + username + "].");
        // send a request from this user to the other
        ClientConnection recipient = activeUsers.get(userId);
        sql.createRequest(this.userId, userId);

        // checking if request already exists to this user
        if (sql.verifyRequest(userId, this.userId)) {
            sql.cancelRequest(userId, this.userId);
            send(this, "[!] Chat begun with [" + recipient.getUsername() + "].");
            userChatLoop(recipient);
            // accept + cancel the request.
        } else {
            try {
                send(this, "[!] Waiting for [" + username + "] to accept. Hit the enter key to cancel the request.");
                send(recipient, "[!] User [" + username + "] is trying to connect with you!");

                threadRunning = true; // for handling the thread
                // this will wait for the user to hit the enter key to stop trying to connect.
                new Thread(() -> {
                    read();
                    threadRunning = false;
                }).start();

                // now here we will attempt to complete a request every 1.5 seconds.
                while (threadRunning && !sql.verifyRequest(userId, this.userId)) {
                    send(this, ".");
                    Thread.sleep(1500);
                }

                // cancel both requests once the loop ends (canceled or accepted).
                sql.cancelRequest(userId, this.userId);
                sql.cancelRequest(this.userId, userId);

                //  if thread is still running, the request was accepted.
                if (!threadRunning) {
                    sendToServer("[!] User [" + username + "] canceled the request to connect.");
                    send(recipient, "[!] User [" + username + "] cancelled their request to connect.");
                    send(this, "[!] Canceling request.");
                    sql.cancelRequest(this.userId, userId);
                } else {
                    // prompt user to hit enter (will exit the thread).
                    send(this, "[!] User [" + recipient.getUsername() + "] accepted the request. Hit enter to begin chat.");
                    userChatLoop(recipient);
                }
            } catch (InterruptedException e) {
                sendToServer("[!] An error occurred while attempting to connect users.");
                e.printStackTrace();
            }
        }
    }

    private void userChatLoop(ClientConnection recipient) {
        // TODO ALLOW USER TO LEAVE, GET RID OF BUGS MAYBE.
        inChat = true;
        sql.setChatting(this.userId);
        sendToServer("[!] User [" + username + "] has begun a chat with user [" + recipient.getUsername() + "].");
        send(this, "[!] Type %quit to leave the chat.");

        // the idea im having with %quit is that if a client sends %quit, the next client will return %client_exit%, which the
        // server can read and then close the communication.
        while (!closed && inChat) {
            String message = read();
            // timestamp
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String time = now.format(formatter);

            if (message != null) {
                sql.saveMessage(this.userId, userId, message);
                String completeMessage = "%s [%s]: %s".formatted(time, recipient.getUsername(), message);
                send(this, completeMessage);
                send(recipient, completeMessage);
            }
        }
    }

    private String read() {
        while (socket.isConnected() && !closed) {
            try {
                String message = bufferedReader.readLine();
                if (message == null) { // means the socket doesn't exit, possible to happen in between the while loop.
                    close();
                } else {
                    return message;
                }
            } catch (IOException e) {
                sendToServer("[!] An error occurred while reading from the client.");
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
        while (!closed) {
            String stringResponse = read();

            try {
                int response = (stringResponse == null) ? -1 : Integer.parseInt(stringResponse);

                if ( (min <= response) && (response <= max) ) {
                    return response;
                } else {
                    send(this, "[!] Please enter a valid integer between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                send(this, "[!] Please enter a valid integer between " + min + " and " + max + ".");
            }
        }
        return -1;
    }

    /**
     * Will send a message to another socket.
     * @param recipient ClientConnection to send a message to.
     *                  Use 'this' to send a message from the server to the current client.
     * @param message Message to send
     */
    private void send(ClientConnection recipient, String message) {
        if (!closed && !recipient.isClosed()) {
            try {
                recipient.bufferedWriter.write(message);
                recipient.bufferedWriter.newLine();
                recipient.bufferedWriter.flush();
            } catch (IOException e) {
                sendToServer("[!] An error occurred while sending a message to the client.");
                e.printStackTrace();
                close();
            }
        }
    }

    /**
     * Will output a message to the Server.
     * Message will be formatted like so: "[port number]: message".
     */
    private void sendToServer(String message) {
        System.out.println("[" + port + "]: " + message);
    }

    /**
     * Closes ClientConnection.
     */
    private void close() {
        // send exitKey to clients so it can shut down properly
        send(this, "[!] Goodbye!");
        send(this, "%server_disconnect%");

        closed = true;

        if (signedIn) {
            signOut();
        }

        sendToServer("[!] Closing connection.");

        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("An error occurred while closing client.");
            e.printStackTrace();
        }

    }

    public String getUsername() {
        return username;
    }

    public boolean isClosed() {
        return closed;
    }
}
