package src;

import java.util.ArrayList;

import src.Algorithm.Maze.Cell;

public class Constants {
    public static class MouseConstants {
        public static final String mouseName = "Ratawoulfie";
        // Calculated using x-offset, y-offset. E.g. {0, 1} signifies a y-displacement
        // of +1, signaling North.
        public static final int[][] possibleMouseDirections = new int[][] { { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 },
                { 0, -1 }, { -1, -1 }, { -1, 0 }, { -1, 1 } };

        // Starting mouse position (0,0 -> bottom-left) & direction (0,1 -> North).
        public static final int[] startingMousePosition = new int[] { 0, 0 };
        public static final int[] startingMouseDirection = new int[] { 0, 1 };
    }

    public static class MazeConstants {
        public static final int numRows = 16;
        public static final int numCols = 16;

        /*
         * Maze characterized by:
         * 
         * ^ 15,15
         * |
         * +y
         * 0,0 +x -->
         * 
         * Goal positions can be randomized, but are set at 8,8 for now (center of the
         * maze).
         */
        public static final int goalX = 8;
        public static final int goalY = 8;  

        public static ArrayList<int[]> getGoalCells() {
            ArrayList<int[]> goalCells = new ArrayList<>();
            goalCells.add(new int[] {7, 7});
            goalCells.add(new int[] {7, 8});
            goalCells.add(new int[]{8, 7});
            goalCells.add(new int[]{8, 8});
            return goalCells;
        }

        /*
         * The available colors are as follows:
         * 
         * Char Color
         * k Black
         * b Blue
         * a Gray
         * c Cyan
         * g Green
         * o Orange
         * r Red
         * w White
         * y Yellow
         * B Dark Blue
         * C Dark Cyan
         * A Dark Gray
         * G Dark Green
         * R Dark Red
         * Y Dark Yellow
         */

        public static final char startCellColor = 'B';
        public static final String startCellText = "Start";

        public static final char goalCellColor = 'G';
        public static final String goalCellText = "Goal";

        public static final char goalPathColor = 'A';
        public static final String goalPathString = "";

        public static final char returnPathColor = 'A';
        public static final String returnPathString = "";

        public static final char overlapPathColor = 'A';
        public static final String overLapPathString = "";

        public static final char fastPathColor = 'a';
        public static final String fastPathString = "Fast";

        public static boolean showGrid = true;
        public static boolean showPath = false;
    }
}
