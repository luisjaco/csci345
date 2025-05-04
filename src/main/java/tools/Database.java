package tools;

import java.sql.Connection;
import java.sql.*;

public class Database {
    private Connection connection;
    private boolean connected;
    /**
     * The database class will handle all sql queries.
     * You must use connect() before running any queries.
     */
    public Database() {
        connected = false;
    }

    /**
     * Connects to the sql database.
     */
    public void connect() {
        try {
            String tailScaleIp = "100.117.12.77"; // this was the ip address for the tailscale sql
            String otherIp = "127.100.12.77"; // this was the ip I (luis) found before testing myself
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + tailScaleIp + ":3306/instant_messenger?serverTimezone=UTC&connectTimeout=5000",
                    "messenger_user",
                    "christ_mas1990pump_kin^(@"
            );
            connected = true;
            System.out.println("[!] Connected to SQL database.");
        } catch (Exception e) {
            System.out.println("[!] An error occurred connecting to the sql database.");
            e.printStackTrace();
        }
    }

    /**
     * Will insert a username and password into the users table.
     * @param username New username
     * @param password New password
     * @return If the insert update was successful
     */
    public boolean signUp(String username, String password) {
        try {
            PreparedStatement stm = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
            stm.setString(1, username);
            stm.setString(2, password);

            stm.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("[!] An error occurred.");
            e.printStackTrace();
        }
        return false;
    }

    // todo

    /*
     // Check if user already exists
     PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
     checkStmt.setString(1, username);
     ResultSet rs = checkStmt.executeQuery();
     if (rs.next()) {
     return false; // Username already taken
     }
     */

    /**
     * Will sign in a user.
     * Sets their active status to true once they are signed in.
     * Only signs in a user with an active status of false. (Prevent double sign-in).
     * @param username User username
     * @param password User password
     * @return If sign in successful
     */
    public boolean signIn(String username, String password) {
        try {
            PreparedStatement stm = connection.prepareStatement("""
                SELECT * FROM users
                WHERE username = ? AND password = ?
                """);
            stm.setString(1, username);
            stm.setString(2, password);

            ResultSet result = stm.executeQuery();

            boolean validUser = result.next(); // True if user exists

            // Final check if user active, otherwise, sign in and set user as active.
            if (validUser && !verifyUserActive(username)) {
                int userId = result.getInt("user_id");
                PreparedStatement update = connection.prepareStatement("""
                        UPDATE users
                        SET active = 1
                        WHERE user_id = ?;
                        """);
                update.setInt(1, userId);

                update.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Will update the users table and sign out a user.
     * Sets active = false, chatting = false.
     * @param userId User username
     */
    public void signOut(int userId) {
        try {
            //TODO FINISH
            PreparedStatement update = connection.prepareStatement("""
                    UPDATE """);
        } catch (SQLException e) {
            System.out.println("[!] An error occurred while signing a user out.");
            e.printStackTrace();
        }
    }
    /**
     * Will retrieve user id based on username.
     * @param username Username to search
     * @return User id.
     * If user id not found, will return -1.
     */
    public int getUserId(String username) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT user_id FROM users WHERE username = ?");
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

    /**
     * Will insert a new messages into the messages table.
     * @param senderId Sender user id
     * @param receiverId Receiver user id
     * @param content Message content
     */
    public void saveMessage(int senderId, int receiverId, String content) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO messages (sender_id, receiver_id, message_content) VALUES (?, ?, ?)");
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, content);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("[!] ERROR SAVING MESSAGE");
            e.printStackTrace();
        }
    }

    // TODO could use this to make something like a retrieve previous chat feature.
    /**
     * Retrieves message history between two users.
     * @param user1Id First user
     * @param user2Id Second user
     * @return Query result
     */
    public ResultSet getMessageHistory(int user1Id, int user2Id) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT m.*, u1.username AS sender_name, u2.username AS receiver_name " +
                            "FROM messages m " +
                            "JOIN users u1 ON m.sender_id = u1.user_id " +
                            "JOIN users u2 ON m.receiver_id = u2.user_id " +
                            "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?) " +
                            "ORDER BY m.timestamp ASC"
            );
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            stmt.setInt(3, user2Id);
            stmt.setInt(4, user1Id);
            System.out.println("[DEBUG] Getting message history between user " + user1Id + " and user " + user2Id);
            return stmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("[!] ERROR RETRIEVING MESSAGE HISTORY WITH USERNAMES");
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Will verify whether a user is active or not.
     * Will return false if user is either not active or doesn't exist.
     * @return If user is active or false if user doesn't exist
     */
    public boolean verifyUserActive(String username) {
        try {
            PreparedStatement query = connection.prepareStatement("""
                    SELECT * FROM users
                    WHERE username = ?;""");
            query.setString(1, username);

            ResultSet resultSet = query.executeQuery();

            // if result is empty this will be skipped
            if (resultSet.next()) {
                return resultSet.getBoolean("active");
            }
        } catch (SQLException e) {
            System.out.println("[!] An error occurred.");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Will reset all status on users (set active and chatting to false)
     */
    public void resetUserStatus() {
        try {
            /*
             If you are wondering what SQL_SAFE_UPDATES means, it basically prevents updating
             a table based on something other than the primary key. So here I turn it off and turn it on right after.
             */
            PreparedStatement sql_safe_off = connection.prepareStatement("SET SQL_SAFE_UPDATES = 0;");
            sql_safe_off.executeUpdate();

            PreparedStatement update = connection.prepareStatement("""
                    UPDATE users
                    SET active = 0 AND chatting = 0
                    """);
            update.executeUpdate();

            PreparedStatement sql_safe_on = connection.prepareStatement("SET SQL_SAFE_UPDATES = 1;");
            sql_safe_on.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[!] An error occurred while resetting user status'");
            e.printStackTrace();
        }
    }

    /**
     * Will verify if a user exists based on a given username
     * @param username User username
     * @return If user with corresponding username exists
     */
    public boolean verifyUserExists(String username) {
        try {
            PreparedStatement query = connection.prepareStatement("""
                    SELECT * FROM users
                    WHERE username = ?;
                    """);
            query.setString(1, username);

            ResultSet resultSet = query.executeQuery();
            return resultSet.next(); // will be true if user exists.
        } catch (SQLException e) {
            System.out.println("[!] An error occurred.");
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Closes the database connection.
     */
    public void close() {
        if (connected) {
            try {
                connection.close();
                System.out.println("[!] Successfully closed database connection.");
            } catch (SQLException e) {
                System.out.println("[!] An error occurred.");
            }
        }
    }
}