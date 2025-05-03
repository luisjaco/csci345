package tools;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Input {
    private Scanner input;
    private boolean opened;
    public Input() {
        opened = false;
    }

    public void open() {
        System.out.println("[!] Input scanner opened.");
        input = new Scanner(System.in);
        opened = true;
    }
    /**
     * Will retrieve an integer. If user inputs wrong type, will continuously loop until valid
     * integer found.
     * @return User-defined integer
     */
    public int getInt() {
        while (true) {
            try {
                System.out.print("~%: ");
                int value = input.nextInt();
                input.nextLine();
                return value;
            } catch (Exception e){
                System.out.println("[X] Please input an integer.");
                input.nextLine();
            }
        }
    }

    /**
     * Will retrieve an integer within a specific range (inclusive). Will check that
     * the given input >= min, and input <= max. If user inputs wrong type or value outside
     * of range, will continuously loop until valid integer found.
     * @param min Minimum possible value
     * @param max Maximum possible value
     * @return User-defined integer within range
     */
    public int getInt(int min, int max) {
        while (true) {
            try {
                System.out.print("~%: ");
                int value = input.nextInt();
                input.nextLine();
                if ( (value < min) || (value > max)) {
                    System.out.printf("[X] Please input an integer between %d and %d.\n", min, max);
                }
                else {
                    return value;
                }
            } catch (InputMismatchException e){
                System.out.println("[X] Please input an integer.");
                input.nextLine();
            }
        }
    }

    /**
     * Retrieves a string from the user.
     * @return User-defined string.
     */
    public String getString() {
        System.out.print("~%: ");
        return input.nextLine();
    }

    public boolean isOpened(){
        return opened;
    }

    /**
     * Closes the Scanner.
     */
    public void close() {
        opened = false;
        System.out.println("[!] Input scanner closed.");
        input.close();
    }


}


