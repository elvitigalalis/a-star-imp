package src.Algorithm.Maze;

import java.util.Arrays;

import src.Constants;

public class MouseLocal {
    private Cell[][] mazeCells;
    private int[] mousePosition;
    private int[] mouseDirection;

    public MouseLocal() {
        mazeCells = new Cell[Constants.MazeConstants.numRows][Constants.MazeConstants.numCols];
        mousePosition = Constants.MouseConstants.startingMousePosition;
        mouseDirection = Constants.MouseConstants.startingMouseDirection;

        /*
         * Instantiates each cell in the Maze with an x and y coordinate.
         * 
         * It may be beneficial to visualize the array as the following:
         * - [column 0, column 1, column 2, ..., column 15]
         * - with each column: [row 0 elem, row 1 elem, row 2 elem, ..., row 15 elem]
         * 
         * 0, 0 signifies the bottom-left cell. 15, 15 signifies the top-right cell.
         * Thus, getting element 0, 1 will return the cell above the bottom-left cell
         * according to the following array logic.
         */
        setUpMazeLocal();
    }

    /**
     * Function to set up the maze with cells marked by their x, y coordinates.
     * Maze is characterized by:
     * 
     * ^ 15,15
     * |
     * +y
     * 0,0 +x -->
     */
    private void setUpMazeLocal() {
        for (int i = 0; i < Constants.MazeConstants.numCols; i++) { // X-direction
            for (int j = 0; j < Constants.MazeConstants.numRows; j++) { // Y-direction
                mazeCells[i][j] = new Cell(i, j);
            }
        }
    }

    /**
     * Adjusts the mouse's locally held direction based on the turn performed.
     * 
     * @param halfStepsLeft  # of 45 degree turns to the left.
     * @param halfStepsRight # of 45 degree turns to the right.
     */
    public void turnMouseLocal(int halfStepsLeft, int halfStepsRight) {
        try {
            int[][] possibleMouseDirections = Constants.MouseConstants.possibleMouseDirections;
            mouseDirection = possibleMouseDirections[(findDirectionIndexInPossibleDirections(mouseDirection)
                    + halfStepsRight - halfStepsLeft + possibleMouseDirections.length)
                    % possibleMouseDirections.length];
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adjusts the mouse's locally held direction based on a desired direction.
     * 
     * @param newDirection The new direction to turn the mouse to.
     */
    public int[] turnMouseLocal(int[] newDirection) {
        int[] halfStepCount = obtainHalfStepCount(newDirection);
        turnMouseLocal((halfStepCount[1] == -1) ? halfStepCount[0] : 0, (halfStepCount[1] == 1) ? halfStepCount[0] : 0);
        return halfStepCount;
    }

    /**
     * Obtains optimal # of half steps (left/right) to reach a desired position
     * 
     * @param newDirection The desired direction to turn the mouse to.
     * @return An array containing the # of half steps and the direction to turn
     *         (left or right).
     */
    private int[] obtainHalfStepCount(int[] newDirection) {
        int halfSteps = findDirectionIndexInPossibleDirections(newDirection)
                - findDirectionIndexInPossibleDirections(mouseDirection);
        int direction = (halfSteps > 0) ? 1 : -1; // 1 signifies right, -1 signifies left.

        return new int[] { Math.abs(halfSteps), direction };

    }

    /**
     * Finds the index of a given direction in the array of possible mouse
     * directions.
     * 
     * @param direction The direction fed in.
     * @return The index of the direction in the possibleMouseDirection array.
     * @throws IllegalArgumentException Thrown if the direction is not listed in as
     *                                  a possible direction.
     */
    private int findDirectionIndexInPossibleDirections(int[] direction) throws IllegalArgumentException {
        int[][] possibleMouseDirections = Constants.MouseConstants.possibleMouseDirections;
        for (int i = 0; i < possibleMouseDirections.length; i++) {
            if (Arrays.equals(possibleMouseDirections[i], direction)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Direction not listed as a possible mouse direction: " + direction);
    }

    /**
     * Moves the mouse forward in the direction it is currently in.
     */
    public void moveForwardLocal() {
        int newXPosition = mousePosition[0] + mouseDirection[1];
        int newYPosition = mousePosition[1] + mouseDirection[0];

        if (isValidCell(newXPosition, newYPosition)) {
            mousePosition = new int[] { newXPosition, newYPosition };
        } else {
            System.err.println(
                    "Invalid position (ack), mouse cannot move to (" + newXPosition + "," + newYPosition + ")");
        }
    }

    /**
     * Adds a wall to the local maze based on a direction fed in.
     * 
     * @param x         The x-position of the cell to add a wall to.
     * @param y         The y-position of the cell to add a wall to.
     * @param direction The direction to add the wall to. Could be n, ne, e, se, s,
     *                  sw, w, nw.
     */
    public void addWallLocal(int x, int y, int[] direction) {
        int neighboringCellX = x + direction[0];
        int neighboringCellY = y + direction[1];

        if (isValidCell(neighboringCellX, neighboringCellY)) {
            mazeCells[x][y].addWall(direction, true);
            mazeCells[neighboringCellX][neighboringCellY].addWall(new int[] { -direction[0], -direction[1] }, true);
            System.out.println("Shared wall cell found :)");
        } else {
            mazeCells[x][y].addWall(direction, false);
            System.out.println("Edge cell found :)"); // FIXME: Remove later.
        }
    }

    /**
     * Returns if the cell is in the constricted space of the maze.
     * 
     * @param x The x-position of the cell.
     * @param y The y-position of the cell.
     * @return If the cell is in the constricted space of the maze.
     */
    public boolean isValidCell(int x, int y) {
        return x >= 0 && x < Constants.MazeConstants.numCols && y >= 0 && y < Constants.MazeConstants.numRows;
    }

    /**
     * Returns a boolean stating whether the mouse can move between two cells.
     * 
     * @param cell1 The cell the mouse is moving from.
     * @param cell2 The cell the mouse is moving to.
     * @return If the mouse can move between the two cells.
     */
    public boolean canMoveBetweenCells(Cell cell1, Cell cell2) {
        int[] direction = new int[] { cell2.getX() - cell1.getX(), cell2.getY() - cell1.getY()};
        try {
            // Four cardinal directions are supported.
            return !cell1.getWallExists(direction);
        } catch (IllegalArgumentException e) {
            // If part of the eight cardinal directions, but not the four cardinal directions, check the following:
            System.err.println("Attempting diagonal movement calculations. :)");
            boolean upperLDiagonalPossible = false;
            boolean lowerLDiagonalPossible = false;
            // E.g. direction is (-1, -1) --> check new cell's north and east walls as well as current cell's south and west walls.
            // In this example, if either the new cell's north + current cell's west (upper diagonal) don't exist OR the new cell's east + current cell's south (lower diagonal) don't exist, the mouse can move diagonally.
            int[] direction1ToCheck = new int[] {direction[0], 0}; // E.g. -1 0
            int[] direction2ToCheck = new int[] {0, -direction[1]}; // E.g. 0 -1
            upperLDiagonalPossible = (!cell1.getWallExists(direction1ToCheck) && !cell2.getWallExists(direction2ToCheck));
            lowerLDiagonalPossible = (!cell2.getWallExists(new int[] {-direction1ToCheck[0], 0}) && !cell1.getWallExists(new int[] {0, -direction2ToCheck[1]}));

            return upperLDiagonalPossible || lowerLDiagonalPossible;
        }
    }

    /**
     * Returns the direction offsets (an array of two integers) based on a given direction.
     * 
     * @param direction The direction to get the offset for represented as a string (n, ne, e, se, s, sw, w, nw).
     * @return The direction offsets based on the given direction.
     */
    public int[] getDirectionOffset(String direction) {
        String[] possibleDirections = new String[] { "n", "ne", "e", "se", "s", "sw", "w", "nw" };
        return Constants.MouseConstants.possibleMouseDirections[Arrays.asList(possibleDirections).indexOf(direction)];
    }

    /**
     * Returns the direction as a string (n, ne, e, se, s, sw, w, nw) based on a given direction.
     * 
     * @param direction The direction as offset {+x, +y} to get the direction for.
     * @return The direction as a string (n, ne, e, se, s, sw, w, nw).
     */
    public String getDirectionAsString(int[] direction) {
        String[] possibleDirections = new String[] { "n", "ne", "e", "se", "s", "sw", "w", "nw" };
        return possibleDirections[findDirectionIndexInPossibleDirections(direction)];
    }

    /**
     * Returns the direction to the left of the current mouse's direction.
     * 
     * @return The direction to the left of the current mouse's direction.
     */
    public String getDirectionToTheLeft() {
        int[] newDirection = Constants.MouseConstants.possibleMouseDirections[(findDirectionIndexInPossibleDirections(mouseDirection) + 6) % 8];
        return getDirectionAsString(newDirection);
    }

    /**
     * Returns the direction to the right of the current mouse's direction.
     * 
     * @return The direction to the right of the current mouse's direction.
     */
    public String getDirectionToTheRight() {
        int[] newDirection = Constants.MouseConstants.possibleMouseDirections[(findDirectionIndexInPossibleDirections(mouseDirection) + 2) % 8];
        return getDirectionAsString(newDirection);
    }

    /**
     * Returns the mouse's current direction as an offset.
     * 
     * @return The mouse's current direction as an offset.
     */
    public int[] getMouseDirection() {
        return mouseDirection;
    }

    /**
     * Returns the maze with all cells.
     * 
     * @return The maze with all cells.
     */
    public Cell[][] getMazeCells() {
        return mazeCells;
    }

    /**
     * Returns a particular cell in the maze at position x,y.
     * 
     * @param x The x-position of the cell.
     * @param y The y-position of the cell.
     * @return The cell at position x,y in the maze.
     */
    public Cell getCell(int x, int y) {
        return mazeCells[x][y];
    }

    public Cell getMousePosition() {
        return getCell(mousePosition[0], mousePosition[1]);
    }

    // public String localMazeToString() {
    // StringBuilder mazeString = new StringBuilder();
    // int numRows = Constants.MazeConstants.numRows;
    // int numCols = Constants.MazeConstants.numCols;

    // // 0 15 --> 15, 15 (Top row of the maze)
    // for (int i = 0; i < numCols; i++) {
    // mazeString.append("+");
    // mazeString.append((mazeCells[i][15].getNorthWallExists()) ? "---" : " ");
    // }
    // mazeString.append("/n");

    // // for (int i = 0; i < numCols; i++) {
    // // mazeString.append
    // // }

    // // // 0 14 --> 15, 14
    // // for()

    // return mazeString.toString();
    // }
}