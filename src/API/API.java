package src.API;

import java.util.Scanner;

import src.Algorithm.Maze.MouseLocal;

/**
 * API class provides methods to interact with the maze and control the mouse.
 */
public class API {
    private final Scanner input;
    private MouseLocal mouseLocal;

    public API(MouseLocal mouseLocal) {
        input = new Scanner(System.in);
        this.mouseLocal = mouseLocal;
    }
    /*
    ----------------------------------------------------------------
    Internal helper methods for reading from simulator
    ----------------------------------------------------------------
    */
    private String getResponse(String commandUsed) {
        System.out.println(commandUsed);           // Ask simulator
        return input.nextLine();                 // Read response
    }

    private int getIntegerResponse(String commandUsed) {
        return Integer.parseInt(getResponse(commandUsed));
    }

    private boolean getBooleanResponse(String commandUsed) {
        return getResponse(commandUsed).equals("true");
    }

    private boolean getAck(String commandUsed) {
        return getResponse(commandUsed).equals("ack");
    }

    /*
    ----------------------------------------------------------------
    Maze dimension queries
    ----------------------------------------------------------------
    */
    public int mazeWidth() {
        return getIntegerResponse("mazeWidth");
    }

    public int mazeHeight() {
        return getIntegerResponse("mazeHeight");
    }

    /*
    ----------------------------------------------------------------
    Wall queries
    ----------------------------------------------------------------
    */
    public boolean wallFront() {
        return getBooleanResponse("wallFront");
    }

    public boolean wallRight() {
        return getBooleanResponse("wallRight");
    }

    public boolean wallLeft() {
        return getBooleanResponse("wallLeft");
    }

    /*
    ----------------------------------------------------------------
    Mouse movement commands
    ----------------------------------------------------------------
    */
    public void moveForward() {
        boolean ack = getAck("moveForward");

        if (ack) {
            mouseLocal.moveForwardLocal();
        } else {
            System.err.print(mouseLocal.localMazeToString());
            throw new RuntimeException("Cannot move forward");
        }
    }

    public void moveForward(int steps) {
        boolean ack = getAck("moveForward " + steps);

        if (ack) {
            for (int i = 0; i < steps; i++) {
                mouseLocal.moveForwardLocal();
            }
        } else {
            System.err.print(mouseLocal.localMazeToString());
            throw new RuntimeException("Cannot move forward " + steps + " steps");
        }
    }

    public void moveForwardHalf() {
        boolean ack = getAck("moveForwardHalf");

        if (ack) {
            // FIXME: Add half movement
        } else {
            System.err.print(mouseLocal.localMazeToString());
            throw new RuntimeException("Cannot move forward half");
        }
    }

    public void turnRight() {
        boolean ack = getAck("turnRight");
        if (ack) {
            mouseLocal.turnMouseLocal(0, 2);
        }
    }

    public void turnLeft() {
        boolean ack = getAck("turnLeft");
        if (ack) {
            mouseLocal.turnMouseLocal(2, 0);
        }
    }

    public void turnRight45() {
        boolean ack = getAck("turnRight45");
        if (ack) {
            mouseLocal.turnMouseLocal(0, 1);
        }
    }

    public void turnLeft45() {
        boolean ack = getAck("turnLeft45");
        if (ack) {
            mouseLocal.turnMouseLocal(1, 0);
        }
    }

    /*
    ----------------------------------------------------------------
    Set / clear walls
    ----------------------------------------------------------------
    */
    public void setWall(int x, int y, String direction) {
        switch (direction) {
            case "n", "e", "s", "w" -> {
                System.out.println("setWall " + x + " " + y + " " + direction);
                mouseLocal.addWallLocal(x, y, mouseLocal.getDirectionOffset(direction));
            }
            case "ne", "se", "sw", "nw" -> {
                System.out.println("setWall " + x + " " + y + " " + direction.charAt(0));
                System.out.println("setWall " + x + " " + y + " " + direction.charAt(1));
                mouseLocal.addWallLocal(x, y, mouseLocal.getDirectionOffset(direction));
            }
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    public void clearWall(int x, int y, String direction) {
        System.out.println("clearWall " + x + " " + y + " " + direction);
    }

    /*
    ----------------------------------------------------------------
    Cell color / text
    ----------------------------------------------------------------
    */
    public void setColor(int x, int y, char color) {
        System.out.println("setColor " + x + " " + y + " " + color);
    }

    public void clearColor(int x, int y) {
        System.out.println("clearColor " + x + " " + y);
    }

    public void clearAllColor() {
        System.out.println("clearAllColor");
    }

    public void setText(int x, int y, String text) {
        System.out.println("setText " + x + " " + y + " " + text);
    }

    public void clearText(int x, int y) {
        System.out.println("clearText " + x + " " + y);
    }

    public void clearAllText() {
        System.out.println("clearAllText");
    }

    /*
    ----------------------------------------------------------------
    Reset booleans
    ----------------------------------------------------------------
    */
    public boolean wasReset() {
        return getBooleanResponse("wasReset");
    }

    public void ackReset() {
        getAck("ackReset");
    }
}