package src.Algorithm;

import java.util.*;
import src.API.API;
import src.Constants;
import src.Main;
import src.Algorithm.Maze.Cell;
import src.Algorithm.Maze.MouseLocal;

public class FrontierBasedAvoidGoals {

    // The “avoid goal” cells we explore last
    private final Set<String> avoidGoalCells = new HashSet<>(
        Arrays.asList("8,8", "7,8", "7,7", "8,7")
    );

    public void exploreMazeAvoidGoals(MouseLocal mouse, API api) {
        Set<Cell> frontiers = new HashSet<>();
        Cell start = mouse.getMousePosition();
        frontiers.add(start);
        start.setIsExplored(true);

        while (!frontiers.isEmpty()) {
            // 1) Pick the closest frontier
            Cell nextFrontier = pickNextFrontier(mouse, frontiers);
            if (nextFrontier == null) {
                break; // No reachable frontiers left
            }

            // 2) Attempt to move to nextFrontier
            //    If it fails, unmark & remove from frontier
            boolean moved = Main.traversePathIteratively(mouse, nextFrontier, "A*", "return", false);
            if (!moved) {
                clearCellText(api, nextFrontier);
                frontiers.remove(nextFrontier);
                continue;
            }

            // 3) We’ve physically arrived: detect walls, mark as explored
            Main.detectAndSetWalls(mouse, api);
            mouse.getMousePosition().setIsExplored(true);
            clearCellText(api, mouse.getMousePosition());
            frontiers.remove(mouse.getMousePosition());

            // 4) Add valid neighbors, ignoring avoided goals
            for (Cell neighbor : mouse.getNeighbors(mouse.getMousePosition())) {
                // System.err.println("Neighbor: " + neighbor.getX() + " " + neighbor.getY());
                if (!neighbor.getIsExplored() 
                    && mouse.canMoveBetweenCells(mouse.getMousePosition(), neighbor).canMove()
                    && !isAvoidGoalCell(neighbor)) 
                {
                    markCellText(api, neighbor);
                    frontiers.add(neighbor);
                }
            }
        }

        // 5) Finally, visit each avoided goal cell (if reachable)
        visitAvoidGoals(mouse, api);
    }

    /**
     * Helper to place "*" text on a cell in the Maze.
     */
    private void markCellText(API api, Cell cell) {
        api.setText(cell.getX(), cell.getY(), "*");
    }

    /**
     * Helper to clear the text on a cell.
     */
    private void clearCellText(API api, Cell cell) {
        api.setText(cell.getX(), cell.getY(), "");
    }

    /**
     * Tries to visit each "avoid goal" cell in the set, using A*.
     */
    private void visitAvoidGoals(MouseLocal mouse, API api) {
        for (String key : avoidGoalCells) {
            Cell goalCell = parseCellKey(mouse, key);
            if (goalCell != null) {
                Main.traversePathIteratively(mouse, goalCell, "A*", "goal", false);
            }
        }
    }

    /**
     * Picks the closest frontier using BFS to find distances from current position.
     */
    private Cell pickNextFrontier(MouseLocal mouse, Set<Cell> frontiers) {
        Cell current = mouse.getMousePosition();
        Map<Cell, Integer> distMap = bfsDistances(mouse, current);

        Cell best = null;
        int bestDist = Integer.MAX_VALUE;

        for (Cell f : frontiers) {
            Integer d = distMap.get(f);
            if (d != null && d < bestDist) {
                bestDist = d;
                best = f;
            }
        }
        return best;
    }

    /**
     * BFS: distance from 'start' to each reachable cell.
     */
    private Map<Cell, Integer> bfsDistances(MouseLocal mouse, Cell start) {
        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Integer> distMap = new HashMap<>();

        distMap.put(start, 0);
        queue.offer(start);

        while (!queue.isEmpty()) {
            Cell cur = queue.poll();
            int curDist = distMap.get(cur);

            for (Cell neighbor : mouse.getNeighbors(cur)) {
                if (!distMap.containsKey(neighbor) 
                    && mouse.canMoveBetweenCells(cur, neighbor).canMove()) 
                {
                    distMap.put(neighbor, curDist + 1);
                    queue.offer(neighbor);
                }
            }
        }
        return distMap;
    }

    /**
     * Check if a cell is one of our four avoided goals.
     */
    private boolean isAvoidGoalCell(Cell c) {
        String key = c.getX() + "," + c.getY();
        return avoidGoalCells.contains(key);
    }

    /**
     * Converts "x,y" string to Cell, if valid.
     */
    private Cell parseCellKey(MouseLocal mouse, String key) {
        String[] parts = key.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        return mouse.getCell(x, y);
    }
}