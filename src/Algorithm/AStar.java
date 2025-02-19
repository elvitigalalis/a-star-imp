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

    public List<Cell> findAStarPath(MouseLocal mouse, Cell goalCell, boolean diagonalsAllowed, boolean avoidGoalCells) {
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
                return reconstructPath(currCell, goalCell);
            } else if(procCells[procCell.getX()][procCell.getY()]) {
                continue;
            }
            procCells[procCell.getX()][procCell.getY()] = true; // Marks the cell as processed.

            ArrayList<Cell> neighbors = mouse.getNeighbors(procCell, diagonalsAllowed);
            for (Cell neighbor : neighbors) {
                if (procCells[neighbor.getX()][neighbor.getY()]) {
                    continue;
                }

                if (avoidGoalCells && mouse.isGoalCell(neighbor, mouse.getGoalCells())) {
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

    public static String pathToString(MouseLocal mouse, List<Cell> path) {
        StringBuilder pathString = new StringBuilder();
        Cell origCell = mouse.getMousePosition();
        int[] origDir = mouse.getMouseDirection();

        Cell currCell = origCell;

        for (Cell nextCell : path) {
            // System.err.println("[DEBUG] Current cell " + currCell.getX() + ", " + currCell.getY() + " to next cell " + nextCell.getX() + ", " + nextCell.getY());
            // System.err.println("[DEBUG] Current direction: " + Arrays.toString(mouse.getMouseDirection()));
            int[] newDir = MouseLocal.getDirBetweenCells(currCell, nextCell);
            // System.err.println("[DEBUG] New dir: " + Arrays.toString(newDir));
            int[] turns = mouse.obtainHalfStepCount(newDir);

            if (turns[0] % 2 == 0) {
                if (turns[1] == 1) {
                    for (int i = 0; i < turns[0] / 2; i++) {
                        pathString.append("R#");
                    }
                    mouse.turnMouseLocal(0, turns[0]);
                    pathString.append("F#");
                    mouse.moveForwardLocal();
                } else {
                    for (int i = 0; i < turns[0] / 2; i++) {
                        pathString.append("L#");
                    }
                    mouse.turnMouseLocal(turns[0], 0);
                    pathString.append("F#");
                    mouse.moveForwardLocal();
                }
            } else {
                Cell cellToMoveToFirst = mouse.getMovement(currCell, nextCell, true).getFirstMove();
                // System.err.println("[DEBUG] Cell to move to first: " + cellToMoveToFirst.getX() + ", " + cellToMoveToFirst.getY());
                int[] neededDir = MouseLocal.getDirBetweenCells(currCell, cellToMoveToFirst);
                // System.err.println("[DEBUG] Needed dir: " + Arrays.toString(neededDir));
                int[] firstTurns = mouse.obtainHalfStepCount(neededDir);
                // System.err.println("[DEBUG] First turns: " + Arrays.toString(firstTurns));
                for (int i = 0; i < firstTurns[0] / 2; i++) {
                    if (firstTurns[1] == 1) {
                        pathString.append("R#");
                    } else {
                        pathString.append("L#");
                    }
                }
                if (firstTurns[1] == 1) {
                    mouse.turnMouseLocal(0, firstTurns[0]);
                } else {
                    mouse.turnMouseLocal(firstTurns[0], 0);
                }

                pathString.append("F#");
                mouse.moveForwardLocal();

                // System.err.println("[DEBUG] Current direction: " + Arrays.toString(mouse.getMouseDirection()));

                int[] secNeededDir = MouseLocal.getDirBetweenCells(cellToMoveToFirst, nextCell);
                // System.err.println("[DEBUG] Second needed dir: " + Arrays.toString(secNeededDir));
                int[] secTurns = mouse.obtainHalfStepCount(secNeededDir);
                // System.err.println("[DEBUG] Second turns: " + Arrays.toString(secTurns));
                for (int i = 0; i < secTurns[0] / 2; i++) {
                    if (secTurns[1] == 1) {
                        pathString.append("R#");
                    } else {
                        pathString.append("L#");
                    }
                }
                if (secTurns[1] == 1) {
                    mouse.turnMouseLocal(0, secTurns[0]);
                } else {
                    mouse.turnMouseLocal(secTurns[0], 0);
                }
                // System.err.println("[DEBUG] Current direction: " + Arrays.toString(mouse.getMouseDirection()));
                pathString.append("F#");
                mouse.moveForwardLocal();
            }
            currCell = mouse.getMousePosition();
        }

        mouse.setMousePosition(origCell);
        mouse.setMouseDirection(origDir);
        return pathString.toString();
    }
}
