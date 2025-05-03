package tests;

import tools.Database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MessageTest {
    public static void main(String[] args) {
        Database sql = new Database();
        sql.connect();

        String userA = "testUser1";
        String userB = "testUser2";

        // Ensure both users exist
        if (!sql.signIn(userA, "pass1")) {
            sql.signUp(userA, "pass1");
        }

        if (!sql.signIn(userB, "pass2")) {
            sql.signUp(userB, "pass2");
        }

        // Get their user IDs
        int userAId = sql.getUserId(userA);
        int userBId = sql.getUserId(userB);

        System.out.println("User IDs -> " + userA + ": " + userAId + ", " + userB + ": " + userBId);

        // Save a message
        String message = "Hey " + userB + ", this is a test message from " + userA + "!";
        sql.saveMessage(userAId, userBId, message);
        System.out.println("[TEST] Message saved.");

        // Fetch and print the chat history
        System.out.println("\n[TEST] Chat history between " + userA + " and " + userB + ":");
        ResultSet rs = sql.getMessageHistory(userAId, userBId);

        try {
            while (rs != null && rs.next()) {
                String sender = rs.getString("sender_name");
                String receiver = rs.getString("receiver_name");
                String content = rs.getString("message_content");
                String timestamp = rs.getString("time_stamp");

                System.out.println("[" + timestamp + "] " + sender + " ➡ " + receiver + ": " + content);
            }
        } catch (SQLException e) {
            System.out.println("[!] Error reading chat history.");
            e.printStackTrace();
        }
    }
}

