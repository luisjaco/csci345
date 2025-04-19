package com.github.luisjaco;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;
    private boolean closed;

    public static void main(String[] args) throws IOException {
        /*
        // USE THIS CODE WHEN YOU ARE USING EXTERNAL CONNECTIONS
        InetAddress ip = InetAddress.getByName("put ip address here");
        ServerSocket serverSocket = new ServerSocket(65432, 50, ip);
        */

        // THIS CODE IS WITH FOR LOCALHOST, IT WILL ONLY ACCEPT LOCAL CONNECTIONS (ON YOUR MACHINE ONLY)
        ServerSocket serverSocket = new ServerSocket(65432); // COMMENT OUT IF NOT USING

        Server server = new Server(serverSocket);

        server.start();
    }

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        closed = true;
    }

    public void start() {
        System.out.printf("[!] Server listening on: %s:%s.\n", serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
        scanExitKey(); // watch for exit key in the background
        closed = false;
        connectClients();
    }

    public void connectClients() {
        try {
            while (!serverSocket.isClosed() && !closed) { // WILL LOOP INDEFINITELY, UNTIL SERVER IS CLOSED

                Socket socket = serverSocket.accept(); // waits for a client to connect
                System.out.println("[!] Client connected.");

                // will create a new Connection instance to handle the server-client communication
                Connection connection = new Connection(socket);

                Thread thread = new Thread(connection);
                thread.start(); // begins a thread, client-server communication runs in parallel to the program
            }
        } catch (IOException e) {
            /*
             IOException will be called when the user closes. This is because serverSocket.accept() throws an IOException
             due the socket being closed (It cannot tell we intended it to close). So we check to see if it is unexpected.
             */
            if (!closed) {
                System.out.println("[!] ERROR OCCURRED.");
                e.printStackTrace();
                close();
            }
        }
    }

    public void scanExitKey() {
        // will wait for the user to enter the exit key while the program runs.
        // USER WILL TYPE %close TO CLOSE THE SERVER
        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner input = new Scanner(System.in);

                while (true) { // if any other phrase is entered do nothing.
                    if (input.nextLine().equals("%close")) {
                        close();
                        break;
                    }
                }
            }
        }).start();
    }

    public void close() {
        System.out.println("[!] Closing server.");
        closed = true;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}