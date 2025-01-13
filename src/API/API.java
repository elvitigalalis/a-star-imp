package src.API;

import java.util.Scanner;
import src.Maze.Maze; // Import Maze class

/**
 * API class provides methods to interact with the maze and control the mouse.
 */
public class API {
    private static final Scanner scanner = new Scanner(System.in);

    /*
    ----------------------------------------------------------------
    Internal helper methods for reading from simulator
    ----------------------------------------------------------------
    */
    private static String getResponse(String commandUsed) {
        System.out.println(commandUsed);           // Ask simulator
        return scanner.nextLine();                 // Read response
    }

    private static int getIntegerResponse(String commandUsed) {
        return Integer.parseInt(getResponse(commandUsed));
    }

    private static boolean getBooleanResponse(String commandUsed) {
        return getResponse(commandUsed).equals("true");
    }

    private static boolean getAck(String commandUsed) {
        return getResponse(commandUsed).equals("ack");
    }

    /*
    ----------------------------------------------------------------
    Maze dimension queries
    ----------------------------------------------------------------
    */
    public static int mazeWidth() {
        return getIntegerResponse("mazeWidth");
    }

    public static int mazeHeight() {
        return getIntegerResponse("mazeHeight");
    }

    /*
    ----------------------------------------------------------------
    Wall queries
    ----------------------------------------------------------------
    */
    public static boolean wallFront() {
        return getBooleanResponse("wallFront");
    }

    public static boolean wallRight() {
        return getBooleanResponse("wallRight");
    }

    public static boolean wallLeft() {
        return getBooleanResponse("wallLeft");
    }

    /*
    ----------------------------------------------------------------
    Mouse movement commands
    ----------------------------------------------------------------
    */
    public static void moveForward() {
        boolean ack = getAck("moveForward");

        if (ack) {
            Maze.moveForwardLocal();
        } else {
            throw new RuntimeException("Cannot move forward");
        }
    }

    public static void turnRight() {
        boolean ack = getAck("turnRight");
        if (ack) {
            Maze.turnRightLocal();
        }
    }

    public static void turnLeft() {
        boolean ack = getAck("turnLeft");
        if (ack) {
            Maze.turnLeftLocal();
        }
    }

    public static void turnRight45() {
        boolean ack = getAck("turnRight45");
        if (ack) {
            Maze.turnRight45Local();
        }
    }

    public static void turnLeft45() {
        boolean ack = getAck("turnLeft45");
        if (ack) {
            Maze.turnLeft45Local();
        }
    }

    /*
    ----------------------------------------------------------------
    Set / clear walls
    ----------------------------------------------------------------
    */
    public static void setWall(int x, int y, String direction) {
        switch (direction) {
            case "n", "e", "s", "w" -> {
                System.out.println("setWall " + x + " " + y + " " + direction);
                Maze.setWallLocal(x, y, direction);
            }
            case "ne", "se", "sw", "nw" -> {
                // FIXME: I need to add this later
            }
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    public static void clearWall(int x, int y, String direction) {
        System.out.println("clearWall " + x + " " + y + " " + direction);
    }

    /*
    ----------------------------------------------------------------
    Cell color / text
    ----------------------------------------------------------------
    */
    public static void setColor(int x, int y, char color) {
        System.out.println("setColor " + x + " " + y + " " + color);
    }

    public static void clearColor(int x, int y) {
        System.out.println("clearColor " + x + " " + y);
    }

    public static void clearAllColor() {
        System.out.println("clearAllColor");
    }

    public static void setText(int x, int y, String text) {
        System.out.println("setText " + x + " " + y + " " + text);
    }

    public static void clearText(int x, int y) {
        System.out.println("clearText " + x + " " + y);
    }

    public static void clearAllText() {
        System.out.println("clearAllText");
    }

    /*
    ----------------------------------------------------------------
    Reset booleans
    ----------------------------------------------------------------
    */
    public static boolean wasReset() {
        return getBooleanResponse("wasReset");
    }

    public static void ackReset() {
        getAck("ackReset");
    }
}