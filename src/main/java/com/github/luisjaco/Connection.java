package com.github.luisjaco;

import java.io.*;
import java.net.Socket;

public class Connection implements Runnable {
    private boolean closed;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    public Connection(Socket socket) {
        closed = true;
        try {
            this.socket = socket;

            // The following are classes which reads and sends data. Data is converted like so:
            // bytes (stream) -> characters (streamReader/streamWriter) -> buffer (think of a frame of data)

            // Send
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            this.bufferedWriter = new BufferedWriter(outputStreamWriter);
            // Receive
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
        //TODO
        // this method should handle the client-server interaction of logging in as a user
        // it should verify the password, and also that the user is not already signed in.
        send(this, "[SERVER]: Please enter your username.");

        String username = read();
        System.out.println(username + " CONNECTED."); // TODO this is just testing, remove later

        send(this, "[SERVER]: Please enter the password.");
    }

    public void connectUser() {
        // TODO
        // This method should get the user this particular client wants to message.
        // Check if they're online, and if both users are attempting to message each other, connect them
    }

    public void connectionLoop() {
        // TODO
        // This method will handle the messages to and from a user who are currently connected
    }
    public String read() {
        // this method will read from the client, it will essentially stop and wait until a message is returned, or client leaves.
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

    public void send(Connection recipient, String message) {
        try {
            recipient.bufferedWriter.write(message); // Just adds the message to the buffer
            recipient.bufferedWriter.newLine(); // Adds a new line to the message
            /* A BufferedWriter will not send until the buffer is completely full. By using flush(), you can say we're
             "filling" the rest of the buffer, so it can send; typically our message will not be large enough to fill out
             an entire buffer.
             */
            recipient.bufferedWriter.flush(); // Sends the message.
        } catch (IOException e) {
            System.out.println("[!] ERROR OCCURRED SENDING TO CLIENT");
            e.printStackTrace();
            close();
        }
    }

    public void close() {
        //TODO replace the part which says [client] to the clients username
        //TODO set client as offline
        System.out.println("[!] Closing [client] connection.");
        closed = true;
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
