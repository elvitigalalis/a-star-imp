package src.Algorithm;

import java.util.ArrayList;
import java.util.List;

import src.Constants;
import src.Algorithm.Maze.Cell;
import src.Algorithm.Maze.MouseLocal;

public class FrontierBasedExploration {
    public FrontierBasedExploration() {}

    public List<Cell> findFrontierBasedPath(MouseLocal mouse, Cell goalCell) {
        while (true) {
            List<Cell> frontiers = identifyFrontiers(mouse);

            if (frontiers.isEmpty()) {
                System.err.println("No frontiers found. Exiting...");
                return null;
            }

            Cell nearestFrontier = findNearestFrontier(mouse, frontiers);

            if(nearestFrontier == null) {
                System.err.println("No nearest frontier found. Exiting...");
                return null;
            }
        }
    }

    /**
     * Find the frontier cells of all explored cells.
     * 
     * @param mouse The mouse object.
     * @return A list of frontier cells.
     */
    private List<Cell> identifyFrontiers(MouseLocal mouse) {
        List<Cell> frontiers = new ArrayList<Cell>();
        
        for (int i = 0; i < Constants.MazeConstants.numCols; i++) {
            for (int j = 0; j < Constants.MazeConstants.numRows; j++) {
                Cell currentCell = mouse.getCell(i, j);
                // If the cell is not explored, skip it.
                if (!currentCell.getIsExplored()) {
                    continue;
                }
                
                // For each cell, check all cells adjacent to it.
                for (int[] direction : Constants.MouseConstants.possibleMouseDirections) {
                    int x = i + direction[0];
                    int y = j + direction[1];
                    Cell adjacentCell = mouse.getCell(x, y);

                    if (mouse.isValidCell(x, y) && !adjacentCell.getIsExplored() && !frontiers.contains(adjacentCell)) {
                        // If the adjacent [valid] cell is not explored, it is a frontier.
                        frontiers.add(adjacentCell);
                    }
                }
            }
        }

        // If frontiers are found, return them.
        System.err.println("Frontiers identified: " + frontiers.toString());
        return frontiers;
    }

    private Cell findNearestFrontier(MouseLocal mouse, List<Cell> frontiers) {
        Cell currentCell = mouse.getMousePosition();
        double minDistance = Double.POSITIVE_INFINITY;
        Cell nearestFrontier = null;

        // Calculates the Euclidean distance between the current cell and all frontiers.
        for (Cell frontier : frontiers) {
            double euclideanDistance = Math.sqrt(Math.pow(frontier.getX() - currentCell.getX(), 2) + Math.pow(frontier.getY() - currentCell.getY(), 2));
            if (euclideanDistance < minDistance) {
                minDistance = euclideanDistance;
                nearestFrontier = frontier;
            }
        }

        // If no frontier is found, exit.
        if (nearestFrontier == null) {
            System.err.println("No nearest frontier found. Exiting...");
            return null;
        }

        // If a frontier is found, return it.
        System.err.println("Nearest frontier: " + nearestFrontier.toString() + " with distance: " + minDistance);
        return nearestFrontier;
    } 

}
