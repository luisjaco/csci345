package tests;

import tools.Database;

public class DatabaseTest {
    public static void main(String[] args) {
        Database.connect();

        // 1. Try to sign up a user
        String testUsername = "testUser1";
        String testPassword = "testPass123";

        boolean signupSuccess = Database.signUp(testUsername, testPassword);
        System.out.println("[TEST] Signup success: " + signupSuccess);

        // 2. Try to sign in with correct credentials
        boolean loginSuccess = Database.signIn(testUsername, testPassword);
        System.out.println("[TEST] Login success with correct password: " + loginSuccess);

        // 3. Try to sign in with wrong credentials
        boolean loginFail = Database.signIn(testUsername, "wrongPassword");
        System.out.println("[TEST] Login success with wrong password: " + loginFail);
    }
}
