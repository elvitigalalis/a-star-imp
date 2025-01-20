package src.Algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import src.Constants;
import src.Algorithm.Maze.Cell;
import src.Algorithm.Maze.MouseLocal;
import src.Algorithm.Maze.Movement;

public class AStar {
    public AStar() {
    }

    public List<Cell> findAStarPath(MouseLocal mouse, Cell goalCell, boolean diagonalsAllowed) {
        mouse.resetCosts();
        Cell currCell = mouse.getMousePosition();

        PriorityQueue<Cell> discoveredCell = new PriorityQueue<Cell>(Comparator.comparing(individualCell -> individualCell.getTotalCost()));
        boolean[][] procCells = new boolean[Constants.MazeConstants.numCols][Constants.MazeConstants.numRows];

        /*
         * It is beneficial to visualize the mathematics in A* as follows:
         * - f(n) = g(n) + h(n)
         * - f(n) is the total cost of the cell.
         * - g(n) is the cost from the starting cell to the current cell.
         * - h(n) is the heuristic cost from the current cell to the goal cell.
         * --------------(Octile heuristic in this case)----------------------
         * 
         * Essentially, the total cost is the sum of the cost from the start + heuristic
         * cost from the current cell to the goal cell.
         */

        Cell startCell = mouse.getCell(currCell.getX(), currCell.getY());
        startCell.setCostFromStart(0.0);
        startCell.setTotalCost(MouseLocal.octileDistance(startCell, goalCell));
        discoveredCell.add(startCell);

        // Analyzes to-be-processed cells until goal is reached.
        while (!discoveredCell.isEmpty()) {
            Cell procCell = discoveredCell.poll();

            if (MouseLocal.isSame(procCell, goalCell)) {
                return reconstructPath(currCell, procCell);
            } else if(procCells[procCell.getX()][procCell.getY()]) {
                continue;
            }
            procCells[procCell.getX()][procCell.getY()] = true; // Marks the cell as processed.

            ArrayList<Cell> neighbors = mouse.getNeighbors(procCell, diagonalsAllowed);
            for (Cell neighbor : neighbors) {
                if (procCells[neighbor.getX()][neighbor.getY()]) {
                    continue;
                }
                
                Movement movement = mouse.getMovement(procCell, neighbor, diagonalsAllowed);
                if (!movement.getCanMove()) {
                    continue;
                }

                double costToNeighbor = MouseLocal.euclideanDistance(procCell, neighbor);
                double costFromStart = procCell.getCostFromStart() + costToNeighbor;
                if (costFromStart < neighbor.getCostFromStart()) { // Updates neighbor costs.
                    neighbor.setCostFromStart(costFromStart);
                    neighbor.setTotalCost(costFromStart + MouseLocal.octileDistance(neighbor, goalCell));
                    neighbor.setPrevCellInPath(procCell);
                    discoveredCell.add(neighbor);
                }
            }
        }
        return null; // No path was found.
    }

    private static List<Cell> reconstructPath(Cell startingCell, Cell goalCell) {
        List<Cell> path = new ArrayList<>();
        Cell pointer = goalCell;
        while (pointer != startingCell) {
            path.add(pointer);
            pointer = pointer.getPrevCellInPath();
        }
        // Reverse to start + 1 -> goal.
        Collections.reverse(path);
        return path;
    }
}
