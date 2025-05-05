package tests;

import java.io.*;
import java.net.*;

public class FileReceiver {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(65432);
        System.out.println("Server started. Waiting for file...");

        Socket socket = serverSocket.accept();
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        FileOutputStream fos = new FileOutputStream("received_file.txt");

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = dis.read(buffer)) > 0) {
            fos.write(buffer, 0, bytesRead);
        }

        System.out.println("File received.");
        fos.close();
        dis.close();
        socket.close();
        serverSocket.close();
    }
}