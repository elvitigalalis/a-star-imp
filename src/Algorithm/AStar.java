package src.Algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import src.Constants;
import src.Algorithm.Maze.Cell;
import src.Algorithm.Maze.MouseLocal;

public class AStar {
    public AStar() {
    }

    public List<Cell> findAStarPath(MouseLocal mouse, Cell currentCell, Cell goalCell) {
        resetCosts(mouse);
        // Creates a priority queue to store discovered, to-be-processed cells sorted
        // with the lowest total cost first in the queue.
        PriorityQueue<Cell> discoveredCell = new PriorityQueue<Cell>(
                Comparator.comparing(individualCell -> individualCell.getTotalCost()));

        // Creates a 2D array to store processed cells to avoid reprocessing cells.
        // E.g. if starting cell is processed, we don't want to reprocess it.
        boolean[][] processedCells = new boolean[Constants.MazeConstants.numCols][Constants.MazeConstants.numRows];

        /*
         * It is beneficial to visualize the mathematics in A* as follows:
         * - f(n) = g(n) + h(n)
         * - f(n) is the total cost of the cell.
         * - g(n) is the cost from the starting cell to the current cell.
         * - h(n) is the heuristic cost from the current cell to the goal cell. I used
         * the octile heuristic.
         * 
         * In essence, the total cost is the sum of the cost from the start to the
         * current cell and the heuristic cost from the current cell to the goal cell.
         */
        // Calculates the octile heuristic for the starting cell to the goal cell. Sets
        // to total cost because the cost from start is zero: 0 + heuristic = heuristic.
        Cell startCell = mouse.getCell(currentCell.getX(), currentCell.getY());
        startCell.setCostFromStart(0.0);
        startCell.setTotalCost(heuristic(currentCell.getX(), currentCell.getY(), goalCell.getX(), goalCell.getY()));
        // Adds the starting cell to the priority queue so all paths from this cell to
        // the goal can be evaluated ("processing...").
        discoveredCell.add(startCell);

        while (discoveredCell.size() > 0) {
            // Retrieves the cell with the lowest total cost from the priority queue.
            Cell toBeProcessedCell = discoveredCell.poll();

            // If the cell has already been processed, skip it.
            if (processedCells[toBeProcessedCell.getX()][toBeProcessedCell.getY()]) {
                continue;
            }
            // Otherwise, mark the cell as processed.
            else {
                processedCells[toBeProcessedCell.getX()][toBeProcessedCell.getY()] = true;
            }

            // If the cell is the goal cell, reconstruct the path taken (in Cells) to get to
            // the goal.
            if (toBeProcessedCell.getX() == goalCell.getX() && toBeProcessedCell.getY() == goalCell.getY()) {
                return constructPath(currentCell, toBeProcessedCell);
            }

            // For each and every possible direction the mouse can move, evaluate the cost
            // using the octile heuristic.
            int[][] possibleMouseDirections = Constants.MouseConstants.possibleMouseDirections;
            for (int[] mouseDirection : possibleMouseDirections) {
                // Calculate the neighboring cell's position (adding offsets from the
                // direction).
                int neighboringX = toBeProcessedCell.getX() + mouseDirection[0];
                int neighboringY = toBeProcessedCell.getY() + mouseDirection[1];

                // If the neighboring cell isn't a valid cell or has been processed already,
                // skip it.
                if (!mouse.isValidCell(neighboringX, neighboringY) || processedCells[neighboringX][neighboringY]) {
                    continue;
                }
                // If there is a wall blocking travel between the cells, skip it.
                Cell neighboringCell = mouse.getCell(neighboringX, neighboringY);
                // System.err.println("Neighboring cell: " + neighboringCell);
                if (!mouse.canMoveBetweenCells(toBeProcessedCell, neighboringCell)) {
                    continue;
                }

                // Calculates and sets the cost from the start to the neighboring cell based on
                // the current cell's cost from the start as well as the cost to the neighboring
                // cell (calculated through the heuristic).
                // Cost to neighbor is distance between the two cells. √2 for diagonal, 1 for straight.
                double costToNeighbor = Math.sqrt(Math.abs(mouseDirection[0]) + Math.abs(mouseDirection[1]));
                // Neighboring cell cost = previous cell cost + cost from that cell to neighbor.
                double neighboringCellCostFromStart = toBeProcessedCell.getCostFromStart() + costToNeighbor;
                // If the neighboring cell's cost from the start is less than the current cost, update the variables of the neighboring cell.
                if (neighboringCellCostFromStart < neighboringCell.getCostFromStart()) {
                    neighboringCell.setCostFromStart(neighboringCellCostFromStart);
                    // Calculates the total cost of the neighboring cell (cost from start + heuristic).
                    neighboringCell.setTotalCost(neighboringCellCostFromStart + heuristic(neighboringX, neighboringY, goalCell.getX(), goalCell.getY()));;
                    neighboringCell.setPrevCellInPath(toBeProcessedCell);
                    // Adds the neighboring cell to the priority queue to be processed.
                    discoveredCell.add(neighboringCell);
                }
            }
        }
        return null; // No path found (the goal is never reached so construct path is not called).
    }

    public static void resetCosts(MouseLocal mouse) {
        for (int x = 0; x < Constants.MazeConstants.numCols; x++) {
            for (int y = 0; y < Constants.MazeConstants.numRows; y++) {
                Cell cell = mouse.getCell(x, y);
                cell.setCostFromStart(Double.POSITIVE_INFINITY);
                cell.setTotalCost(Double.POSITIVE_INFINITY);
            }
        }
    }

    /**
     * Calculates the octile heuristic from one cell to the goal cell.
     * 
     * @param x1 The x-position of the current cell.
     * @param y1 The y-position of the current cell.
     * @param x2 The x-position of the goal cell.
     * @param y2 The y-position of the goal cell.
     * @return
     */
    private static double heuristic(int x1, int y1, int x2, int y2) {
        int distanceX = Math.abs(x1 - x2);
        int distanceY = Math.abs(y1 - y2);
        return (distanceX + distanceY) + (Math.sqrt(2) - 2) * Math.min(distanceX, distanceY);
    }

    /**
     * Constructs the path taken from the starting cell to the goal cell.
     * 
     * @param goalCell The goal cell.
     * @return The path taken from the starting cell to the goal cell as a list from start cell to goal cell.
     */
    private static List<Cell> constructPath(Cell startingCell, Cell goalCell) {
        // Creates an array list to append the cells taken to reach goal (from goal to start).
        List<Cell> pathToGoal = new ArrayList<>();
        Cell pointerCell = goalCell;
        while (pointerCell != startingCell) {
            pathToGoal.add(pointerCell);
            pointerCell = pointerCell.getPrevCellInPath();
        }
        // Reverses the path to get the path from start to goal (since traversed from goal to start).
        Collections.reverse(pathToGoal);
        return pathToGoal;
    }
}
