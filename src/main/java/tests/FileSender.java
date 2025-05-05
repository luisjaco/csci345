package tests;

import java.io.*;
import java.net.*;

public class FileSender {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 65432);
        File file = new File("file_to_send.txt");
        FileInputStream fis = new FileInputStream(file);
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) > 0) {
            dos.write(buffer, 0, bytesRead);
        }

        System.out.println("File sent.");
        fis.close();
        dos.close();
        socket.close();

    }
}