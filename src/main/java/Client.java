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
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private FileOutputStream fileOutputStream;
    private FileInputStream fileInputStream;
    private boolean canSendFile;
    private boolean waitingToSendFile;
    private boolean awaitingFile;
    private String downloadFileDirectory;
    public static void main(String[] args) throws IOException {
        // when you change to external connections, change localhost to the servers ip address.
        String ipAddress = ""; // todo SET THE SERVER IP HERE
        int port = 65432; // todo SET THE SERVER PORT HERE
        String downloadFileDirectory = ""; // todo SET YOUR DOWNLOAD DIRECTORY (WHERE FILES WILL DOWNLOAD).
        try {
            Socket socket = new Socket(ipAddress, port);
            Client client = new Client(socket, downloadFileDirectory);
            client.start();
        } catch (ConnectException e) {
            System.out.println("[!] Could not connect to host.");
            e.printStackTrace();
        }
    }
//%file:/Users/luisjaco/Downloads/testing.txt
    /**
     * The Client class connects to a server.
     * @param socket Socket to use to connect to server.
     */
    public Client(Socket socket, String downloadFileDirectory) {
        closed = true;
        this.socket = socket;
        input = new Scanner(System.in);
        waitingToSendFile = false;
        canSendFile = false;
        this.downloadFileDirectory = downloadFileDirectory;
        try {
            // Read tools.ClientConnection for description of the following
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);

            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
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

                if (messageToSend.contains("%file:")) {
                    String path = messageToSend.substring(6);
                    File file = new File(path);
                    // we are going to change the string so that it contains only the file name
                    String[] windowsSplit = path.split("\"");
                    String[] fullSplit = windowsSplit[windowsSplit.length - 1].split("/");
                    String fileName = fullSplit[fullSplit.length - 1];
                    messageToSend = "%file:" + fileName;

                    if (file.exists() && file.isFile()) {
                        // send file key
                        bufferedWriter.write(messageToSend);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        waitingToSendFile = true;

                        // send file
                        sendFile(file);
                        return;
                    }
                    else {
                        System.out.println("[!] This file is either not a file or doesn't exist, please try again.");
                    }
                } else {
                    // Read tools.ClientConnection for description.
                    bufferedWriter.write(messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
        } catch (IOException e) {
            if (!closed) {
                System.out.println("[!] An error occurred.");
                e.printStackTrace();
                close();
            }
        }
    }

    private void sendFile(File file) {
        while (waitingToSendFile) {
            // stall for confirmation
        }

        if (canSendFile) {
            try {
                fileInputStream = new FileInputStream(file);

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) > 0) {
                    dataOutputStream.write(buffer, 0, bytesRead);
                }

                fileInputStream.close();
                dataOutputStream.flush();
            } catch (FileNotFoundException e) {
                System.out.println("[!] An error occurred while reading a file.");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("[!] An error occurred while sending a file.");
                e.printStackTrace();
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
                    if (messageFromServer == null) {
                        System.out.println("[!] Server closed. Hit enter to end the program.");
                        close();
                    } else if (messageFromServer.equals("%server_disconnect%")) { // check for exit keys that can indicate client shut down.
                        close();
                    } else if (messageFromServer.equals("%user_file_ready%")) { // confirmation this client can send file
                        canSendFile = true;
                    } else if (messageFromServer.contains("%file:")) { // means user must read file
                        String fileName = messageFromServer.substring(6);
                        fileOutputStream = new FileOutputStream(downloadFileDirectory + "new_" + fileName);

                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        while ((bytesRead = dataInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }

                        fileOutputStream.close();
                    }

                    else {
                        System.out.println(messageFromServer);
                    }
                    waitingToSendFile = false; // do not let send file if any messages are sent
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
