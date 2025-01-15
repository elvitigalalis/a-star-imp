package src.Algorithm.Maze;

import java.util.Arrays;

public class Cell {
    int x;
    int y;
    double costFromStart;
    double totalCost;
    Wall northWall;
    Wall eastWall;
    Wall southWall;
    Wall westWall;
    Cell prevCellInPath;

    /**
     * Instantiates the cell object with the given x and y coordinates. Each new
     * cell is initialized with a total cost (to the end) of infinity and a cost
     * from the start of infinity.
     * 
     * @param x
     * @param y
     */
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.costFromStart = Double.POSITIVE_INFINITY;
        this.totalCost = Double.POSITIVE_INFINITY;
        this.northWall = new Wall();
        this.eastWall = new Wall();
        this.southWall = new Wall();
        this.westWall = new Wall();
        this.prevCellInPath = null;
    }

    /**
     * Method to add a wall based on a direction. All eight cardinal directions are
     * supported.
     * 
     * @param direction The direction to add the wall(s) to. Possible directions
     *                  could be n, ne, e, se, s, sw, w, nw.
     */
    public void addWall(int[] direction, boolean isShared) {
        // Checks if east/west walls need to be added.
        if (direction[0] == 1) {
            eastWall.setExists(isShared);

        } else if (direction[0] == -1) {
            westWall.setExists(isShared);
        }

        // Checks if north/south walls need to be added.
        if (direction[1] == 1) {
            northWall.setExists(isShared);
        } else if (direction[1] == -1) {
            southWall.setExists(isShared);
        }
    }

    /**
     * Gets the x-coordinate of the cell in the maze.
     * 
     * @return The x-coordinate of the cell in the maze.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the cell in the maze.
     * 
     * @return The y-coordinate of the cell in the maze.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets if a wall exists in the given direction. Four directions are supported (n, e, s, w).
     * 
     * @param direction The direction to check if a wall exists.
     * @return If a wall exists in the given direction.
     * @throws IllegalArgumentException If the direction is not in the four cardinal directions.
     */
    public boolean getWallExists(int[] direction) throws IllegalArgumentException{
        if (Arrays.equals(new int[]{0, 1}, direction)) {
            return getNorthWallExists();
        } else if (Arrays.equals(new int[]{1, 0}, direction)) {
            return getEastWallExists();
        } else if (Arrays.equals(new int[]{0, -1}, direction)) {
            return getSouthWallExists();
        } else if (Arrays.equals(new int[]{-1, 0}, direction)) {
            return getWestWallExists();
        }
        throw new IllegalArgumentException("Direction not in four cardinal directions: " + direction);
    }

    /**
     * Gets if a north wall exists.
     * 
     * @return If a north wall is present.
     */
    public boolean getNorthWallExists() {
        return northWall.getExists();
    }

    /**
     * Gets if an east wall exists.
     * 
     * @return If an east wall is present.
     */
    public boolean getEastWallExists() {
        return eastWall.getExists();
    }

    /**
     * Gets if a south wall exists.
     * 
     * @return If a south wall is present.
     */
    public boolean getSouthWallExists() {
        return southWall.getExists();
    }

    /**
     * Gets if a west wall exists.
     * 
     * @return If a west wall is present.
     */
    public boolean getWestWallExists() {
        return westWall.getExists();
    }

    /**
     * Gets if the north wall is shared.
     * 
     * @return If the north wall is shared.
     */
    public boolean getNorthWallIsShared() {
        return northWall.getIsShared();
    }

    /**
     * Gets if the east wall is shared.
     * 
     * @return If the east wall is shared.
     */
    public boolean getEastWallIsShared() {
        return eastWall.getIsShared();
    }

    /**
     * Gets if the south wall is shared.
     * 
     * @return If the south wall is shared.
     */
    public boolean getSouthWallIsShared() {
        return southWall.getIsShared();
    }

    /**
     * Gets if the west wall is shared.
     * 
     * @return If the west wall is shared.
     */
    public boolean getWestWallIsShared() {
        return westWall.getIsShared();
    }

    /**
     * Gets the previous cell in the path to this cell in the maze.
     * 
     * @return The previous cell in the path to this cell(x,y) in the maze.
     */
    public Cell getPrevCellInPath() {
        return prevCellInPath;
    }

    /**
     * Sets the previous cell in the path to this cell in the maze.
     * 
     * @param prevCellInPath The previous cell in the path to this cell(x,y) in the maze.
     */
    public void setPrevCellInPath(Cell prevCellInPath) {
        this.prevCellInPath = prevCellInPath;
    }

    /**
     * Sets the cost from the start of this cell in the maze.
     * 
     * @param costFromStart The updated cost from the start(0,0) of this cell(x,y) in the maze.
     */
    public void setCostFromStart(double costFromStart) {
        this.costFromStart = costFromStart;
    }

    /**
     * Sets the total cost of this cell in the maze.
     * 
     * @param totalCost The updated total cost of this cell(x,y) in the maze.
     */
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    /**
     * Gets the cost from the start(0,0) to this cell.
     * 
     * @return The cost from the start(0,0) to this cell(x,y).
     */
    public double getCostFromStart() {
        return costFromStart;
    }

    /**
     * Gets the total cost of this cell in the maze (cost from start + heuristic).
     * 
     * @return The total cost of this cell(x,y) in the maze (cost from start + heuristic).
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * Returns a String representation of all variables/Objects in the Cell class.
     */
    public String toString() {
        return "Cell(" + x + "," + y + ") {\n\tcostFromStart=" + costFromStart + ", totalCost=" + totalCost
                + ", \n\tnorthWall=" + northWall + ", eastWall=" + eastWall + ", \n\tsouthWall=" + southWall + ", westWall="
                + westWall + "\n\t  }";
    }

    public static void main(String[] args) {
        Cell cell = new Cell(0, 0);
        System.out.println(cell);
        cell.addWall(new int[]{1, 0}, true); // East
        cell.addWall(new int[]{0, 1}, false); // North
        cell.addWall(new int[]{-1, -1}, true); // South-West
        System.out.println(cell);
    }

    private class Wall {
        boolean exists;
        boolean isShared;

        public Wall() {
            this.exists = false;
            this.isShared = false;
        }

        /**
         * Sets to a wall that exists and if it is shared.
         * 
         * @param isShared If the wall is shared.
         */
        public void setExists(boolean isShared) {
            exists = true;
            this.isShared = isShared;
        }

        public boolean getExists() {
            return exists;
        }

        public boolean getIsShared() {
            return isShared;
        }

        public String toString() {
            return "Wall{exists=" + exists + ", isShared=" + isShared + "}";
        }
    }
}
