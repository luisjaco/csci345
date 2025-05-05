import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private boolean closed;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private final Scanner input;

    public static void main(String[] args) throws IOException {
        // when you change to external connections, change localhost to the servers ip address.
        try {
            Socket socket = new Socket("localhost", 65432);
            Client client = new Client(socket);
            client.start();
        } catch (ConnectException e) {
            System.out.println("[!] Could not connect to host.");
            e.printStackTrace();
        }
    }

    /**
     * The Client class connects to a server.
     * @param socket Socket to use to connect to server.
     */
    public Client(Socket socket) {
        closed = true;
        this.socket = socket;
        input = new Scanner(System.in);

        try {
            // Read tools.ClientConnection for description of the following
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
        } catch (IOException e) {
            System.out.println("[!] An error occurred.");
            e.printStackTrace();
            close();
        }
    }

    /**
     * Connects client to server and begins communication.
     */
    public void start(){
        System.out.printf("[!] Client connected to server at: %s:%s.\n", socket.getInetAddress().getHostAddress(), socket.getLocalPort());
        closed = false;
        read();
        send();
    }

    /**
     * Allows a client to send information.
     */
    public void send() {
        try {
            while (socket.isConnected() && !closed) {
                // continually scans for input and sends
                String messageToSend = input.nextLine();
                // Read tools.ClientConnection for description.
                bufferedWriter.write(messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            if (!closed) {
                System.out.println("[!] An error occurred.");
                e.printStackTrace();
                close();
            }
        }
    }

    /**
     * Parallel method, will constantly scan for messages from the server.
     */
    public void read() {
        // continually reads messages and outputs to console
        new Thread(() -> {
            String messageFromServer;

            while (socket.isConnected() && !closed) {
                try {
                    messageFromServer = bufferedReader.readLine(); // will wait for the next line
                    // check for exit keys that can indicate client shut down.
                    if (messageFromServer.equals("%server_disconnect%")) {
                        close();
                        return;
                    }
                    System.out.println(messageFromServer);
                } catch (IOException e) {
                    if (!closed) {
                        System.out.println("[!] An error occurred while attempting to read a message.");
                        e.printStackTrace();
                        close();
                    }
                }
            }
        }).start();
    }

    /**
     * Closes the client-server connection.
     */
    public void close(){
        closed = true;
        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (socket != null) {
                socket.close();
            }
            input.close();
            System.out.println("[!] Successfully closed client.");
        } catch (IOException e) {
            System.out.println("[!] An error occurred while closing the client.");
            e.printStackTrace();
        }
    }
}
