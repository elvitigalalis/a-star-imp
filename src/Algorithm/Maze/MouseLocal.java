package src.Algorithm.Maze;

import java.util.ArrayList;
import java.util.Arrays;

import src.Constants;
import src.API.API;

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
    public int[] obtainHalfStepCount(int[] newDirection) {
        int halfStepsRight = (Constants.MouseConstants.possibleMouseDirections.length
                + findDirectionIndexInPossibleDirections(newDirection)
                - findDirectionIndexInPossibleDirections(mouseDirection))
                % Constants.MouseConstants.possibleMouseDirections.length;
        int halfStepsLeft = (Constants.MouseConstants.possibleMouseDirections.length
                + findDirectionIndexInPossibleDirections(mouseDirection)
                - findDirectionIndexInPossibleDirections(newDirection))
                % Constants.MouseConstants.possibleMouseDirections.length;

        // System.err.println("Half steps right: " + halfStepsRight + " Half steps left:
        // " + halfStepsLeft);
        int halfSteps = Math.min(halfStepsRight, halfStepsLeft);

        int direction = (halfStepsRight < halfStepsLeft) ? 1 : -1; // 1 signifies right, -1 signifies left.

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
        int newXPosition = mousePosition[0] + mouseDirection[0];
        int newYPosition = mousePosition[1] + mouseDirection[1];

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
            // System.err.println("Shared wall cell found :)");
        } else {
            mazeCells[x][y].addWall(direction, false);
            // System.err.println("Edge cell found :)"); // FIXME: Remove later.
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
    public Movement getMovement(Cell cell1, Cell cell2, boolean diagonalsAllowed) {
        int[] direction = new int[] { cell2.getX() - cell1.getX(), cell2.getY() - cell1.getY() };
        try {
            if(Math.sqrt(Math.pow(direction[0], 2) + Math.pow(direction[1], 2)) != 1 && Math.sqrt(Math.pow(direction[0], 2) + Math.pow(direction[1], 2)) != Math.sqrt(2)) {
                return new Movement(false, direction);
            }
            // Cardinal direction movement.
            boolean canMove = !cell1.getWallExists(direction);
            Movement cardinalMovement = new Movement(canMove, direction);

            return cardinalMovement;
        } catch (IllegalArgumentException e) {
            Movement diagonalMovement = new Movement(false, direction);
            if (!diagonalsAllowed || !cell2.getIsExplored()) {
                return diagonalMovement;
            }

            // System.err.println("Current cell: " + cell1.getX() + ", " + cell1.getY() + " direction move: " + direction[0] + ", " + direction[1] + " to cell: " + cell2.getX() + ", " + cell2.getY());

            // If part of the eight cardinal directions, but not the four cardinal directions, check the following:
            boolean diagonalVertical = false;
            boolean diagonalHorizontal = false;
            // E.g. direction is (-1, -1) --> check new cell's north and east walls as well as current cell's south and west walls.
            // In this example, if either the new cell's north + current cell's west (upper diagonal) don't exist OR the new cell's east + current cell's south (lower diagonal) don't exist, the mouse can move diagonally.
            int[] horizontalDirectionCheck = new int[] {direction[0], 0}; // E.g. -1 0
            int[] verticalDirectionCheck = new int[] {0, direction[1]}; // E.g. 0 -1

            // System.err.println("Direction: " + direction[0] + ", " + direction[1]);

            diagonalHorizontal = (!cell1.getWallExists(horizontalDirectionCheck) && !cell2.getWallExists(new int[] {0, -verticalDirectionCheck[1]}));
            diagonalVertical = (!cell1.getWallExists(verticalDirectionCheck) && !cell2.getWallExists(new int[] {-horizontalDirectionCheck[0], 0}));

            diagonalMovement.setCanMove(diagonalHorizontal || diagonalVertical);
            if (!diagonalMovement.getCanMove()) {
                return diagonalMovement;
            }

            // Classifies if the horizontal diagonal is the left or right diagonal of the mouse.
            boolean isHorizontalRight = (findDirectionIndexInPossibleDirections(verticalDirectionCheck) < findDirectionIndexInPossibleDirections(horizontalDirectionCheck));
            boolean isVerticalRight = !isHorizontalRight;

            if (isHorizontalRight && diagonalHorizontal) {
                diagonalMovement.setIsDiagonal(true);
                diagonalMovement.setLeftOrRightDiagonal("right");
                diagonalMovement.setCellToMoveToFirst(getCell(cell1.getX() + horizontalDirectionCheck[0], cell1.getY()));
            } else if (!isHorizontalRight && diagonalHorizontal) {
                diagonalMovement.setIsDiagonal(true);
                diagonalMovement.setLeftOrRightDiagonal("left");
                diagonalMovement.setCellToMoveToFirst(getCell(cell1.getX() + horizontalDirectionCheck[0], cell1.getY()));
            } else if (isVerticalRight && diagonalVertical) {
                diagonalMovement.setIsDiagonal(true);
                diagonalMovement.setLeftOrRightDiagonal("right");
                diagonalMovement.setCellToMoveToFirst(getCell(cell1.getX(), cell1.getY() + verticalDirectionCheck[1]));
            } else {
                diagonalMovement.setIsDiagonal(true);
                diagonalMovement.setLeftOrRightDiagonal("left");
                diagonalMovement.setCellToMoveToFirst(getCell(cell1.getX(), cell1.getY() + verticalDirectionCheck[1]));
            }

            return diagonalMovement;
        }
    }

    /**
     * Returns the direction offsets (an array of two integers) based on a given
     * direction.
     * 
     * @param direction The direction to get the offset for represented as a string
     *                  (n, ne, e, se, s, sw, w, nw).
     * @return The direction offsets based on the given direction.
     */
    public int[] getDirectionOffset(String direction) {
        String[] possibleDirections = new String[] { "n", "ne", "e", "se", "s", "sw", "w", "nw" };
        return Constants.MouseConstants.possibleMouseDirections[Arrays.asList(possibleDirections).indexOf(direction)];
    }

    /**
     * Returns the direction as a string (n, ne, e, se, s, sw, w, nw) based on a
     * given direction.
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
        int[] newDirection = Constants.MouseConstants.possibleMouseDirections[(findDirectionIndexInPossibleDirections(
                mouseDirection) + 6) % 8];
        return getDirectionAsString(newDirection);
    }

    /**
     * Returns the direction to the right of the current mouse's direction.
     * 
     * @return The direction to the right of the current mouse's direction.
     */
    public String getDirectionToTheRight() {
        int[] newDirection = Constants.MouseConstants.possibleMouseDirections[(findDirectionIndexInPossibleDirections(
                mouseDirection) + 2) % 8];
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

    /**
     * Returns the mouse's current position in the maze.
     * 
     * @return The mouse's current position in the maze.
     */
    public Cell getMousePosition() {
        return getCell(mousePosition[0], mousePosition[1]);
    }

    /**
     * Returns a 2D representation of the maze as a string (formatted as a map).
     * 
     * @return A 2D representation of the maze as a string (formatted as a map).
     */
    public String localMazeToString() {
        StringBuilder mazeString = new StringBuilder("Maze:\n");
        int numRows = Constants.MazeConstants.numRows;
        int numCols = Constants.MazeConstants.numCols;

        // 0 15 --> 15, 15 (Top row of the maze)
        for (int i = 0; i < numCols; i++) {
            mazeString.append("+---");
        }
        mazeString.append("+\n");

        for (int i = numRows - 1; i >= 0; i--) {
            mazeString.append(printRow(i));
            mazeString.append("\n");
        }

        return mazeString.toString();
    }

    /**
     * Prints the walls of a row of cells in a maze.
     * 
     * @param rowNumber The row number to print the walls of the cells of.
     * @return The walls of the row of cells in the maze formatted as a map.
     */
    private String printRow(int rowNumber) {
        StringBuilder rowString = new StringBuilder("|");
        int numCols = Constants.MazeConstants.numCols;

        for (int i = 0; i < numCols; i++) {
            Cell cellAnalyzed = getCell(i, rowNumber);
            if (cellAnalyzed.getWallExists(new int[] { 1, 0 })) {
                rowString.append("   |");
            } else {
                rowString.append("    ");
            }
        }
        rowString.append("\n");

        for (int i = 0; i < numCols; i++) {
            Cell cellAnalyzed = getCell(i, rowNumber);
            if (cellAnalyzed.getWallExists(new int[] { 0, -1 })) {
                rowString.append("+---");
            } else {
                rowString.append("+   ");
            }
        }
        rowString.append("+\n");

        return rowString.toString();
    }

    /**
     * Gets all valid neighbors in n, e, s, w direcitons.
     * 
     * @param cell The cell to get the neighbors of.
     * @return All valid neighbors in n, e, s, w directions.
     */
    public ArrayList<Cell> getNeighbors(Cell cell, boolean diagonalsAllowed) {
        int x = cell.getX();
        int y = cell.getY();
        ArrayList<Cell> neighbors = new ArrayList<>();

        int possibleDirections[][] = Constants.MouseConstants.possibleMouseDirections;

        for (int i = 0; i < possibleDirections.length; i++) {
            int newX = x + possibleDirections[i][0];
            int newY = y + possibleDirections[i][1];

            if (isValidCell(newX, newY) && (diagonalsAllowed ? true : (possibleDirections[i][0] == 0 || possibleDirections[i][1] == 0))) {
                Cell neighborCell = getCell(newX, newY);
                neighbors.add(neighborCell);
            }
        }
        return neighbors;
    }

    public static boolean isSame(Cell cell1, Cell cell2) {
        return cell1.getX() == cell2.getX() && cell1.getY() == cell2.getY();
    }

    public static double euclideanDistance(Cell cell1, Cell cell2) {
        return Math.sqrt(Math.pow(cell1.getX() - cell2.getX(), 2) + Math.pow(cell1.getY() - cell2.getY(), 2));
    }

    public static double octileDistance(Cell cell1, Cell cell2) {
        int distanceX = Math.abs(cell1.getX() - cell2.getX());
        int distanceY = Math.abs(cell1.getX() - cell2.getX());
        return (distanceX + distanceY) + (Math.sqrt(2) - 2) * Math.min(distanceX, distanceY);
    }

    public void resetCosts() {
        for (int x = 0; x < Constants.MazeConstants.numCols; x++) {
            for (int y = 0; y < Constants.MazeConstants.numRows; y++) {
                Cell cell = getCell(x, y);
                cell.setCostFromStart(Double.POSITIVE_INFINITY);
                cell.setTotalCost(Double.POSITIVE_INFINITY);
            }
        }
    }

    /**
     * Detects and sets walls in front, left, and right of the mouse.
     * 
     * @param mouse The mouse object.
     * @param API   The API object.
     */
    public void detectAndSetWalls(API api) {
        Cell currCell = getMousePosition();
        if (api.wallFront()) {
            api.setWall(currCell.getX(), currCell.getY(), getDirectionAsString(getMouseDirection()));
        }
        if (api.wallLeft()) {
            api.setWall(currCell.getX(), currCell.getY(), getDirectionToTheLeft());
        }
        if (api.wallRight()) {
            api.setWall(currCell.getX(), currCell.getY(), getDirectionToTheRight());
        }
    }

    public ArrayList<Cell> getGoalCells() {
        ArrayList<int[]> goalPoses = Constants.MazeConstants.getGoalCells();
        ArrayList<Cell> goalCells = new ArrayList<Cell>();

        for (int[] goalPos : goalPoses) {
            goalCells.add(getCell(goalPos[0], goalPos[1]));
        }
        return goalCells;
    }


    /**
     * Check if a cell is one of our four avoided goals.
     */
    public boolean isGoalCell(Cell cell, ArrayList<Cell> goalCells) {
        boolean isGoal = false;
        for (Cell goal : goalCells) {
            isGoal = isGoal || isSame(cell, goal);
        }
        return isGoal;
    }
}