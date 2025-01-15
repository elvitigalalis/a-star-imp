package src;

import src.API.API;

public class Constants {
    public static class MouseConstants {
        public static final String mouseName = "Ratawoulfie";
        // Calculated using x-offset, y-offset. E.g. {0, 1} signifies a y-displacement of +1, signaling North.
        public static final int[][] possibleMouseDirections = new int[][]{{0, 1}, {1, 1 }, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};

        // Starting mouse position (0,0 -> bottom-left) & direction (0,1 -> North).
        public static final int[] startingMousePosition = new int[]{0, 0};
        public static final int[] startingMouseDirection = new int[]{0, 1};
    }

    public static class MazeConstants {
        public static final int numRows = 16;
        public static final int numCols = 16;

        /*
         * Maze characterized by:
         * 
         * ^       15,15
         * |
         * +y
         * 0,0 +x -->
         * 
         * Goal positions can be randomized, but are set at 8,8 for now (center of the maze).
         */
        public static final int goalPositionX = 7;
        public static final int goalPositionY = 7;
    }
}
