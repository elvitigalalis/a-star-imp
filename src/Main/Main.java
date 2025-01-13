package src.Main;

import java.util.List;

import src.API.API;
import src.Maze.AStar;
import src.Maze.Maze;

public class Main {    
    public static void main(String[] args) {
        log("Running " + Maze.getMouseName() + "...");
        API.setColor(0, 0, 'G');
        API.setText(0, 0, "Start");

        int width = API.mazeWidth(); // e.g., 16
        int height = API.mazeHeight(); // e.g., 16
        log("Maze dimensions: " + width + " x " + height);

        int startX = 0;
        int startY = 0;

        int goalX = 7;
        int goalY = 7;
        List<int[]> singlePath = AStar.findAStarPath(startX, startY, goalX, goalY);

        if (singlePath == null) {
            log("No path found to single goal (" + goalX + "," + goalY + ")!");
        } else {
            log("Single-goal path found, size = " + singlePath.size());
            for (int[] step : singlePath) {
                log("(" + step[0] + ", " + step[1] + ")");
            }
        }

        // ---------------------------------------------------------------------
        // APPROACH B: Multi-Goal (the 4 center cells in a 16x16)
        // ---------------------------------------------------------------------
        int[][] centerCells = { { 7, 7 }, { 7, 8 }, { 8, 7 }, { 8, 8 } };
        List<int[]> bestPath = null;

        for (int[] center : centerCells) {
            List<int[]> path = AStar.findAStarPath(startX, startY, center[0], center[1]);
            if (path != null) {
                // Pick whichever path is shorter (fewest steps)
                if (bestPath == null || path.size() < bestPath.size()) {
                    bestPath = path;
                }
            }
        }

        if (bestPath == null) {
            System.out.println("No path found to any of the center goals!");
        } else {
            System.out.println("Multi-goal best path, size = " + bestPath.size());
            for (int[] step : bestPath) {
                System.out.println("(" + step[0] + ", " + step[1] + ")");
            }
        }
    }

    private static void log(String text) {
        System.err.println(text);
    }
}
