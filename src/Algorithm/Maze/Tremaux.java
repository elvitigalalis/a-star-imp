package src.Algorithm.Maze;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import src.Constants;

public class Tremaux {
    public Tremaux() {}

    public List<Cell> findTremauxPath(MouseLocal mouse, Cell goalCell) {
        Stack<Cell> cellStack = new Stack<Cell>();

        // Resets maze traversal counts for each cell.
        for (int i = 0; i < Constants.MazeConstants.numCols; i++) {
            for (int j = 0; j < Constants.MazeConstants.numRows; j++) {
                Cell currentCell = mouse.getCell(i, j);
                currentCell.setTremauxCount(0);
            }
        }

        // Pushes the starting cell onto the stack and increments traversal count.
        Cell currentCell = mouse.getMousePosition();
        cellStack.push(currentCell);
        currentCell.incrementTremauxCount();
        currentCell.setIsExplored();

        while (cellStack.size() > 0) {
            currentCell = cellStack.peek();

            // If the current cell is the goal cell, return the path.
            if (currentCell.getX() == goalCell.getX() && currentCell.getY() == goalCell.getY()) {
                return constructPath(cellStack);
            }

            // If the current cell has not been explored, explore it.
            int[][] possibleDirections = new int[][]{{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
            boolean haveMoved = false;

            for (int[] direction : possibleDirections) {
                int neighborX = currentCell.getX() + direction[0];
                int neighborY = currentCell.getY() + direction[1];

                // If the neighbor cell is invalid or a wall exists between the current cell and the neighbor cell, skip it.
                if (!mouse.isValidCell(neighborX, neighborY)) {
                    continue;
                }

                // If the neighbor cell is invalid or a wall exists between the current cell and the neighbor cell, skip it.
                Cell neighborCell = mouse.getCell(neighborX, neighborY);
                if(!mouse.canMoveBetweenCells(currentCell, neighborCell)) {
                    continue;
                }

                // If the neighbor cell has not been explored, push it onto the stack and increment traversal count.
                if (neighborCell.getTremauxCount() == 0) {
                    cellStack.push(neighborCell);
                    neighborCell.incrementTremauxCount();
                    System.err.println("Neighbor cell: " + neighborCell.getX() + ", " + neighborCell.getY()+ " " + neighborCell.getTremauxCount());
                    haveMoved = true;
                    break;
                } else if (!neighborCell.getIsExplored()) {
                    cellStack.push(neighborCell);
                    neighborCell.incrementTremauxCount();
                    neighborCell.setIsExplored();
                    haveMoved = true;
                    break;
                } else {
                    // If the neighbor cell has been explored, increment traversal count.
                    neighborCell.incrementTremauxCount();
                }
            }

            // If the current cell has no unexplored neighbors, pop it from the stack.
            if (!haveMoved) {
                Cell backTrackedCell = cellStack.pop();
                backTrackedCell.incrementTremauxCount();
            }
        }
        return null;
    }

    private List<Cell> constructPath(Stack<Cell> cellStack) {
        List<Cell> tremauxPath = new ArrayList<Cell>();
        while (!cellStack.isEmpty()) {
            tremauxPath.add(cellStack.pop());
        }
        // Reverse the path
        List<Cell> reversedPath = new ArrayList<Cell>();
        for (int i = tremauxPath.size() - 1; i >= 0; i--) {
            reversedPath.add(tremauxPath.get(i));
        }
        // Remove the first element
        if (!reversedPath.isEmpty()) {
            reversedPath.remove(0);
        }
        return reversedPath;
    }
}