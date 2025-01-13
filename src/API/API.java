package src.API;

import java.util.Scanner;

/**
 * API class provides methods to interact with the maze and control the mouse.
 */
public class API {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String mouseName = "Ratawoulfie";
    private static int[] mousePosition = { 0, 0 }; // x, y
    private static int mouseDirection = 0; // n(0), ne(1), e(2), se(3), s(4), sw(5), w(6), nw(7)

    /* Mouse name getter */
    public static String getMouseName() {
        return mouseName;
    }

    /* Command reponse getters */
    private static String getResponse(String commandUsed) {
        System.out.println(commandUsed);
        String programResponse = scanner.nextLine();
        return programResponse;
    }

    private static int getIntegerResponse(String commandUsed) {
        return Integer.parseInt(getResponse(commandUsed));
    }

    private static boolean getBooleanResponse(String commandUsed) {
        return getResponse(commandUsed).equals("true");
    }

    /* Maze dimensions */
    public static int mazeWidth() {
        return API.getIntegerResponse("mazeWidth");
    }

    public static int mazeHeight() {
        return API.getIntegerResponse("mazeHeight");
    }

    /* Walls around mouse booleans */
    public static boolean wallFront() {
        return API.getBooleanResponse("wallFront");
    }

    public static boolean wallRight() {
        return API.getBooleanResponse("wallRight");
    }

    public static boolean wallLeft() {
        return API.getBooleanResponse("wallLeft");
    }

    /* Mouse movement commands; "ack" if successful, "crash" otherwise */
    private static boolean getAck(String commandUsed) {
        return getResponse(commandUsed).equals("ack");
    }

    public static void moveForward() {
        boolean ack = API.getAck("moveForward");

        // Adjusts mouse position
        switch (getMouseDirection()) {
            case "n":
                mousePosition[1]++;
                break;
            case "ne":
                mousePosition[0]++;
                mousePosition[1]++;
                break;
            case "e":
                mousePosition[0]++;
                break;
            case "se":
                mousePosition[0]++;
                mousePosition[1]--;
                break;
            case "s":
                mousePosition[1]--;
                break;
            case "sw":
                mousePosition[0]--;
                mousePosition[1]--;
                break;
            case "w":
                mousePosition[0]--;
                break;
            case "nw":
                mousePosition[0]--;
                mousePosition[1]++;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + getMouseDirection());
        }

        if (!ack) {
            throw new RuntimeException("Cannot move forward");
        }
    }

    public static void turnRight() {
        API.getAck("turnRight");
        turnHalfStepRight(2);
    }

    public static void turnLeft() {
        API.getAck("turnLeft");
        turnHalfStepLeft(2);
    }

    public static void turnRight45() {
        API.getAck("turnRight45");
        turnHalfStepRight(1);
    }

    public static void turnLeft45() {
        API.getAck("turnLeft45");
        turnHalfStepLeft(1);
    }

    private static void turnHalfStepRight(int stepCount) {
        mouseDirection = (mouseDirection + stepCount) % 8;
    }

    private static void turnHalfStepLeft(int stepCount) {
        mouseDirection = (mouseDirection - stepCount + 8) % 8;
    }

    public static String getMouseDirection() {
        String[] directions = { "n", "ne", "e", "se", "s", "sw", "w", "nw" };
        return directions[mouseDirection];
    }

    public static int[] getMousePosition() {
        return mousePosition;
    }

    /* Sets wall states */
    public static void setWall(int x, int y, String direction) {
        switch (direction) {
            case "n", "e", "s", "w" -> System.out.println("setWall " + x + " " + y + " " + direction);
            case "ne", "se", "sw", "nw" -> {
                // System.out.println("setWall " + x + " " + y + " " + direction.charAt(0));
                // System.out.println("setWall " + x + " " + y + " " + direction.charAt(1));
            }
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    public static void clearWall(int x, int y, String direction) {
        System.out.println("clearWall " + x + " " + y + " " + direction);
    }

    /* Sets cell colors */
    public static void setColor(int x, int y, char color) {
        System.out.println("setColor " + x + " " + y + " " + color);
    }

    public static void clearColor(int x, int y) {
        System.out.println("clearColor " + x + " " + y);
    }

    public static void clearAllColor() {
        System.out.println("clearAllColor");
    }

    /* Sets cell texts */
    public static void setText(int x, int y, String text) {
        System.out.println("setText " + x + " " + y + " " + text);
    }

    public static void clearText(int x, int y) {
        System.out.println("clearText " + x + " " + y);
    }

    public static void clearAllText() {
        System.out.println("clearAllText");
    }

    /* Indicators for mouse reset */
    public static boolean wasReset() {
        return API.getBooleanResponse("wasReset");
    }

    public static void ackReset() {
        API.getAck("ackReset");
    }
}