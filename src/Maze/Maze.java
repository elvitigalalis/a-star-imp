package src.Maze;

import src.API.API;

public class Maze {
    // Maze specifications
    public static final int maxRows = API.mazeHeight();
    public static final int maxCols = API.mazeWidth();
    private static final int[][] mazeGrid = new int[maxRows][maxCols];
    private static final int[][][] mazeWalls = new int[maxRows][maxCols][4];
    // Mouse name/position/direction in one place
    private static final String mouseName = "Ratawoulfie";
    private static int[] mousePosition = { 0, 0 }; // x, y
    // 0=n,1=ne,2=e,3=se,4=s,5=sw,6=w,7=nw
    public static int[][] mouseDirections = {{-1, 0},  {-1, 1}, { 0, 1},  { 1, 1}, { 1, 0},  { 1,-1}, { 0,-1},  {-1,-1}};
    private static int mouseDirection = 0;

    /*
    ----------------------------------------------------
    MAZE GRID LOGIC
    ----------------------------------------------------
     */
    public static int[][] getMaze() {
        return mazeGrid;
    }

    public static boolean validCell(int x, int y) {
        return x >= 0 && x < maxRows && y >= 0 && y < maxCols;
    }

    public static void setWallLocal(int x, int y, String direction) {
        switch (direction) {
            case "n" -> mazeWalls[x][y][0] = 1;
            case "e" -> mazeWalls[x][y][1] = 1;
            case "s" -> mazeWalls[x][y][2] = 1;
            case "w" -> mazeWalls[x][y][3] = 1;
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }


    /*
    ----------------------------------------------------
    BASIC GETTERS
    ----------------------------------------------------
     */
    public static String getMouseName() {
        return mouseName;
    }

    public static int[] getMousePosition() {
        return mousePosition;
    }

    // Convert the numeric direction to a string
    public static String getMouseDirection() {
        String[] directions = { "n", "ne", "e", "se", "s", "sw", "w", "nw" };
        return directions[mouseDirection];
    }

    /*
    ----------------------------------------------------
    MOVEMENT + TURN LOGIC
    ----------------------------------------------------
     */
    public static void moveForwardLocal() {
        // The local update of (x, y) based on direction
        switch (getMouseDirection()) {
            case "n" -> mousePosition[1]++;
            case "ne" -> {
                mousePosition[0]++;
                mousePosition[1]++;
            }
            case "e" -> mousePosition[0]++;
            case "se" -> {
                mousePosition[0]++;
                mousePosition[1]--;
            }
            case "s" -> mousePosition[1]--;
            case "sw" -> {
                mousePosition[0]--;
                mousePosition[1]--;
            }
            case "w" -> mousePosition[0]--;
            case "nw" -> {
                mousePosition[0]--;
                mousePosition[1]++;
            }
            default -> throw new IllegalStateException("Unexpected direction: " + getMouseDirection());
        }
    }

    public static void turnRightLocal() {
        turnHalfStepRight(2);
    }

    public static void turnLeftLocal() {
        turnHalfStepLeft(2);
    }

    public static void turnRight45Local() {
        turnHalfStepRight(1);
    }

    public static void turnLeft45Local() {
        turnHalfStepLeft(1);
    }

    private static void turnHalfStepRight(int stepCount) {
        mouseDirection = (mouseDirection + stepCount) % 8;
    }

    private static void turnHalfStepLeft(int stepCount) {
        mouseDirection = (mouseDirection - stepCount + 8) % 8;
    }
}