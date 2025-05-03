package tools;

import java.sql.Connection;
import java.sql.*;

public class Database {
    public static Connection connection;

    public static void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.100.12.77:3306/instant_messenger?serverTimezone=UTC",
                    "root",
                    "christ_mas1990pump_kin^(@)"
            );
        } catch (Exception e) {
            System.out.println("[!] DATABASE CONNECTION FAILED:");
            e.printStackTrace();
        }
    }

    public static boolean signUp(String username, String password) {
        try {
            Connection connection = Database.connection;
            PreparedStatement stm = connection.prepareStatement("INSERT INTO users (user_name, user_password) VALUES (?, ?)");
            stm.setString(1, username);
            stm.setString(2, password);

            stm.executeUpdate();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } return false;
    }
    /**
     // Check if user already exists
     PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM users WHERE user_name = ?");
     checkStmt.setString(1, username);
     ResultSet rs = checkStmt.executeQuery();
     if (rs.next()) {
     return false; // Username already taken
     }
     **/
    public static boolean signIn(String username, String password) {
        try {
            Connection connection = Database.connection;
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM users WHERE user_name = ? AND user_password = ?");
            stm.setString(1, username);
            stm.setString(2, password);

            ResultSet result = stm.executeQuery();

            return result.next(); // True if user exists
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return false;
    }

    public static int getUserId(String username) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT user_id FROM users WHERE user_name = ?");
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id");
            }

        } catch (SQLException e) {
            System.out.println("[!] ERROR GETTING USER ID");
            e.printStackTrace();
        }
        return -1;
    }

    public static void saveMessage(int senderId, int receiverId, String content) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO message (sender_id, receiver_id, message_content) VALUES (?, ?, ?)");
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, content);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("[!] ERROR SAVING MESSAGE");
            e.printStackTrace();
        }
    }

    public static ResultSet getMessageHistory(int user1Id, int user2Id) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT m.*, u1.user_name AS sender_name, u2.user_name AS receiver_name " +
                            "FROM message m " +
                            "JOIN users u1 ON m.sender_id = u1.user_id " +
                            "JOIN users u2 ON m.receiver_id = u2.user_id " +
                            "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?) " +
                            "ORDER BY m.time_stamp ASC"
            );
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            stmt.setInt(3, user2Id);
            stmt.setInt(4, user1Id);
            return stmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("[!] ERROR RETRIEVING MESSAGE HISTORY WITH USERNAMES");
            e.printStackTrace();
            return null;
        }
    }

}