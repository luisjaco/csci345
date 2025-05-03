import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private boolean closed;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public static void main(String[] args) throws IOException {
        // when you change to external connections, change localhost to the servers ip address.
        Socket socket = new Socket("localhost", 65432);

        Client client = new Client(socket);
        client.start();
    }

    /**
     * The Client class connects to a server.
     * @param socket Socket to use to connect to server.
     */
    public Client(Socket socket) {
        closed = true;
        this.socket = socket;

        try {
            // Read tools.ClientConnection for description of the following
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
        } catch (IOException e) {
            System.out.println("[!] ERROR OCCURRED INITIALIZING.");
            close();
        }
    }

    /**
     * Connects client to server and begins communication.
     */
    public void start(){
        System.out.printf("Client connecting to server at: %s:%s.\n", socket.getInetAddress().getHostAddress(), socket.getLocalPort());
        closed = false;
        read();
        send();
    }

    /**
     * Allows a client to send information.
     */
    public void send() {
        try {
            Scanner input = new Scanner(System.in);
            while (socket.isConnected() && !closed) { // continually scans for input and sends
                String messageToSend = input.nextLine();
                // Read tools.Connection for description.
                bufferedWriter.write(messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            if (!closed) {
                System.out.println("[!] ERROR OCCURRED SENDING MESSAGE.");
                e.printStackTrace();
                close();
            }
        }
    }

    /**
     * Parallel method, will constantly scan for messages from the server.
     */
    public void read() {
        new Thread(new Runnable() { // continually reads messages and outputs to console
            @Override
            public void run() {
                String messageFromServer;

                while (socket.isConnected() && !closed) {
                    try {
                        messageFromServer = bufferedReader.readLine(); // will wait for the next line
                        System.out.println(messageFromServer);
                    } catch (IOException e) {
                        if (!closed) {
                            System.out.println("[!] ERROR OCCURRED READING MESSAGE.");
                            e.printStackTrace();
                            close();
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * Closes the client-server connection.
     */
    public void close(){
        System.out.println("[!] Closing client.");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
