import tools.ClientConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import tools.Database;
import java.util.ArrayList;

public class Server {
    private final ServerSocket serverSocket;
    private boolean closed;
    private final Database sql;
    private final ArrayList<ClientConnection> connectedClients;
    public static void main(String[] args) throws IOException {
        /*
        // USE THIS CODE WHEN YOU ARE USING EXTERNAL CONNECTIONS
        InetAddress ip = InetAddress.getByName("put ip address here");
        ServerSocket serverSocket = new ServerSocket(65432, 50, ip);
        */

        // THIS CODE IS FOR LOCALHOST, IT WILL ONLY ACCEPT LOCAL CONNECTIONS (ON YOUR MACHINE ONLY)
        ServerSocket serverSocket = new ServerSocket(65432); // COMMENT OUT IF NOT USING

        Server server = new Server(serverSocket);
        server.start();
    }

    /**
     * The Server class will host a server, which
     * clients may connect to.
     * @param serverSocket ServerSocket object to host server with.
     */
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        closed = true;
        sql = new Database();
        connectedClients = new ArrayList<>();
    }

    /**
     * Starts the server.
     */
    public void start() {
        System.out.printf("[!] Server listening on: %s:%s.\n", serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
        System.out.println("[!] Type [%close] to close server.");
        sql.connect(); // begin sql connection
        sql.resetDatabase(); // ensure all users have active=false and chatting=false
        scanExitKey(); // watch for exit key in the background
        closed = false;
        connectClients();
    }

    /**
     * Will create new ClientConnection classes for each new
     * connected client. The ClientConnection will handle
     * client-server communication.
     */
    public void connectClients() {
        try {
            while (!serverSocket.isClosed() && !closed) { // WILL LOOP INDEFINITELY, UNTIL SERVER IS CLOSED

                Socket socket = serverSocket.accept(); // waits for a client to connect
                int clientPort = socket.getPort();
                System.out.println("[!] Client connected on port #" + clientPort);
                // will create a new tools.Connection instance to handle the server-client communication
                ClientConnection connection = new ClientConnection(socket, sql, clientPort);
                connectedClients.add(connection);

                Thread thread = new Thread(connection);
                thread.start(); // begins a thread, client-server communication runs in parallel to the program
            }
        } catch (IOException e) {
            /*
             IOException will be called even when the user closes. This is because serverSocket.accept() throws an IOException
             due the socket being closed (It cannot tell the client intended it to close). So we check to see if it is unexpected.
             */
            if (!closed) {
                System.out.println("[!] ERROR OCCURRED.");
                e.printStackTrace();
                close();
            }
        }
    }

    /**
     * Waits for the user to input an exit key [%close].
     * Once user enters the exit key, will close the server.
     */
    public void scanExitKey() {
        // will wait for the user to enter the exit key while the program runs.
        // USER WILL TYPE %close TO CLOSE THE SERVER
        new Thread(() -> {
            Scanner input = new Scanner(System.in);

            while (true) { // if any other phrase is entered do nothing.
                if (input.nextLine().equals("%close")) {
                    close();
                    break;
                }
            }
        }).start();
    }

    /**
     * Will close the server.
     */
    public void close() {

        // close all connectedClients
        for (ClientConnection clientConnection : connectedClients) {
            if (!clientConnection.isClosed()) { clientConnection.close(); }
        }

        System.out.println("[!] Closing server.");
        closed = true;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sql.resetDatabase(); // reset user
        sql.close();
    }
}