package tests;

import tools.Database;

public class DatabaseTest {
    public static void main(String[] args) {
        Database sql = new Database();
        sql.connect();

        // 1. Try to sign up a user
        String testUsername = "testUser1";
        String testPassword = "testPass123";

        boolean signupSuccess = sql.signUp(testUsername, testPassword);
        System.out.println("[TEST] Signup success: " + signupSuccess);

        // 2. Try to sign in with correct credentials
        boolean loginSuccess = sql.signIn(testUsername, testPassword);
        System.out.println("[TEST] Login success with correct password: " + loginSuccess);

        // 3. Try to sign in with wrong credentials
        boolean loginFail = sql.signIn(testUsername, "wrongPassword");
        System.out.println("[TEST] Login success with wrong password: " + loginFail);
    }
}
