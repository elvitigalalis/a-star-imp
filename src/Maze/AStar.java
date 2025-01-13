package src.Maze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
public class AStar {
    public static List<int[]> findAStarPath(int startingPosX, int startingPosY, int goalPosX, int goalPosY) {
        if (!Maze.validCell(startingPosX, startingPosY) || !Maze.validCell(goalPosX, goalPosY)) {
            return null;
        }
        Cell[][] cellsGrid = new Cell[Maze.maxRows][Maze.maxCols];
        for (int row = 0; row < Maze.maxRows; row++) {
            for (int col = 0; col < Maze.maxCols; col++) {
                cellsGrid[row][col] = new Cell(row, col, Maze.getMaze()[row][col] == 1);
            }
        }

        PriorityQueue<Cell> openCells = new PriorityQueue<>(Comparator.comparing(cell -> cell.totalCost));
        Cell startingCell = cellsGrid[startingPosX][startingPosY];
        startingCell.costFromStart = 0;
        startingCell.totalCost = heuristic(startingPosX, startingPosY, goalPosX, goalPosY);

        openCells.add(startingCell);
        boolean[][] visitedCells = new boolean[Maze.maxRows][Maze.maxCols];

        while(!openCells.isEmpty()) {
            Cell currentCell = openCells.poll(); // Returns head

            if(visitedCells[currentCell.x][currentCell.y]) {
                continue;
            } else {
                visitedCells[currentCell.x][currentCell.y] = true;
            }
 
            // If we reached the goal, then we're going to reconstruct the path taken to it
            if (currentCell.x == goalPosX && currentCell.y == goalPosY) {
                return constructPath(currentCell);
            }

            for (int[] mouseDirection : Maze.mouseDirections) {
                int newX = currentCell.x + mouseDirection[0];
                int newY = currentCell.y + mouseDirection[1];

                if (!Maze.validCell(newX, newY) || cellsGrid[newX][newY].isblockedCell) {
                    continue;
                }
                Cell neighbor = cellsGrid[newX][newY];

                double costToNeighbor = Math.sqrt(Math.abs(mouseDirection[0]) + Math.abs(mouseDirection[1]));
                double totalCostFromStart = currentCell.costFromStart + costToNeighbor;
                if (totalCostFromStart < neighbor.costFromStart) {
                    neighbor.costFromStart = totalCostFromStart;
                    neighbor.totalCost = totalCostFromStart + heuristic(newX, newY, goalPosX, goalPosY);
                    neighbor.prevCell = currentCell;
                    openCells.add(neighbor);
                }
            }
        }
        return null;
    }

    // This is the Octile distance heuristic (because we can move diagonally)
    private static double heuristic(int x1, int y1, int x2, int y2) {
        int distanceX = Math.abs(x1 - x2);
        int distanceY = Math.abs(y1 - y2);
        return (distanceX + distanceY) + (Math.sqrt(2) - 2) * Math.min(distanceX, distanceY);
    }

    private static List<int[]> constructPath(Cell goalCell) {
        List<int[]> path = new ArrayList<>();
        Cell currentCell = goalCell;
        while (currentCell != null) {
            path.add(new int[] { currentCell.x, currentCell.y });
            currentCell = currentCell.prevCell;
        }
        Collections.reverse(path);
        return path;
    }

    private static class Cell {
        int x, y;
        boolean isblockedCell;
        double costFromStart;
        double totalCost;
        Cell prevCell;

        public Cell(int x, int y, boolean isblockedCell) {
            this.x = x;
            this.y = y;
            this.isblockedCell = isblockedCell;
            this.costFromStart = Double.POSITIVE_INFINITY;
            this.totalCost = Double.POSITIVE_INFINITY;
            this.prevCell = null;
        }
    }
}
