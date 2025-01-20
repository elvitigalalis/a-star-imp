package src.Algorithm;

import java.util.*;
import src.API.API;
import src.Main;
import src.Algorithm.Maze.Cell;
import src.Algorithm.Maze.MouseLocal;

public class FrontierBased {
    public void explore(MouseLocal mouse, API api, boolean diagonalsAllowed) {
        Set<Cell> frontiers = new HashSet<>();
        Cell start = mouse.getMousePosition();
        frontiers.add(start);
        start.setIsExplored(true);
        Cell currCell = start;

        while (!frontiers.isEmpty()) {     
            // 1) Pick the closest frontier.
            Cell nextFrontier = pickNextFrontier(mouse, frontiers, diagonalsAllowed);
            if (nextFrontier == null) {
                break;
            }

            // 2) Attempt to move to nextFrontier.
            boolean moved = Main.traversePathIteratively(mouse, nextFrontier, diagonalsAllowed);
            if (!moved) {
                api.setText(nextFrontier.getX(), nextFrontier.getY(), "");
                frontiers.remove(nextFrontier);
                continue;
            }

            // 3) We’ve physically arrived: detect walls, mark as explored.
            currCell = mouse.getMousePosition();
            mouse.detectAndSetWalls(api);
            currCell.setIsExplored(true);
            api.setText(currCell.getX(), currCell.getY(), "");
            frontiers.remove(currCell);
            // System.err.println("[NEIGHBOR] Neighbors:" + Arrays.deepToString(mouse.getNeighbors(nextFrontier, diagonalsAllowed).toArray()));

            // 4) Add valid neighbors, ignoring avoided goals.
            for (Cell neighbor : mouse.getNeighbors(currCell, diagonalsAllowed)) {
                if (!neighbor.getIsExplored() && mouse.getMovement(currCell, neighbor, diagonalsAllowed).getCanMove() && !mouse.isGoalCell(neighbor, mouse.getGoalCells())) 
                {
                    api.setText(neighbor.getX(), neighbor.getY(), "*");
                    frontiers.add(neighbor);
                }
            }
        }

        // 5) Finally, visit each avoided goal cell (if reachable).
        Main.traversePathIteratively(mouse, mouse.getGoalCells(), diagonalsAllowed);
    }

    /**
     * Picks the closest frontier using BFS to find distances from current position.
     */
    private Cell pickNextFrontier(MouseLocal mouse, Set<Cell> frontiers, boolean diagonalsAllowed) {
        Cell currCell = mouse.getMousePosition();
        Cell bestCell = null;
        double bestDist = Double.MAX_VALUE;

        for (Cell frontier : frontiers) {
            double dist = getBFSDist(mouse, currCell, diagonalsAllowed).get(frontier);
            if (dist < bestDist) {
                bestDist = dist;
                bestCell = frontier;
            }
        }
        return bestCell;
    }

    private Map<Cell, Double> getBFSDist(MouseLocal mouse, Cell startCell, boolean diagonalsAllowed) {
        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Double> distMap = new HashMap<>();

        distMap.put(startCell, 0.0);
        queue.offer(startCell);

        while (!queue.isEmpty()) {
            Cell currCell = queue.poll();
            double currDist = distMap.get(currCell);

            for (Cell neighbor : mouse.getNeighbors(currCell, diagonalsAllowed)) {
                if (!distMap.containsKey(neighbor) && mouse.getMovement(currCell, neighbor, diagonalsAllowed).getCanMove()) 
                {
                    distMap.put(neighbor, currDist + MouseLocal.euclideanDistance(currCell, neighbor));
                    queue.offer(neighbor);
                }
            }
        }
        return distMap;
    }
}