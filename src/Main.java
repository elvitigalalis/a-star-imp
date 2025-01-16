package src;

import java.util.Arrays;
import java.util.List;

import src.API.API;
import src.Algorithm.AStar;
import src.Algorithm.Maze.Cell;
import src.Algorithm.Maze.MouseLocal;

public class Main {
    private static MouseLocal mouse;
    private static API api;
    private static AStar aStarAlgoFinder;
    public static void main(String[] args) {
        mouse = new MouseLocal();
        api = new API(mouse);
        aStarAlgoFinder = new AStar();

        addEdgesAsWalls();

        log("Running " + Constants.MouseConstants.mouseName + "...");
        api.setColor(0, 0, 'G');
        api.setText(0, 0, "Start");

        while(true) {
            // Creates a cell holding the mouse's current position.
            Cell mouseCurrentCell = mouse.getMousePosition();
            log("Mouse Position: (" + mouseCurrentCell.getX() + ", " + mouseCurrentCell.getY() + ")");

            // If the mouse reaches the goal, then break out of the loop.
            if(mouseCurrentCell.getX() == Constants.MazeConstants.goalPositionX && mouseCurrentCell.getY() == Constants.MazeConstants.goalPositionY) {
                log("Reached goal. :)");
                break;
            }

            log(mouse.localMazeToString());
            // Checks if there is a wall in front, left, or right of the mouse.
            if (api.wallFront()) {
                api.setWall(mouseCurrentCell.getX(), mouseCurrentCell.getY(), mouse.getDirectionAsString(mouse.getMouseDirection()));
            }
            if (api.wallLeft()) {
                api.setWall(mouseCurrentCell.getX(), mouseCurrentCell.getY(), mouse.getDirectionToTheLeft());
            }
            if (api.wallRight()) {
                api.setWall(mouseCurrentCell.getX(), mouseCurrentCell.getY(), mouse.getDirectionToTheRight());
            }
            log(mouse.localMazeToString());

            // Gets the A* path to the goal.
            List<Cell> optimalPath = aStarAlgoFinder.findAStarPath(mouse, mouseCurrentCell, mouse.getCell(Constants.MazeConstants.goalPositionX, Constants.MazeConstants.goalPositionY));

            if (optimalPath == null) {
                log("No path found to goal. Maze is blocked or no route!");
                break;
            }

            log("Path found, length: " + optimalPath.size());
            for (Cell step : optimalPath) {
                log(step.toString());
            }

            // Moves the mouse to the next cell(s) in the path.
            for (int i = 1; i < optimalPath.size(); i++) {
                Cell currentCell = mouse.getCell(optimalPath.get(i - 1).getX(), optimalPath.get(i - 1).getY());
                Cell nextCell = mouse.getCell(optimalPath.get(i).getX(), optimalPath.get(i).getY());
                turnMouseToNextCell(currentCell, nextCell);
                api.moveForward();
                log("Mouse position: {" + mouse.getMousePosition().getX() + ", " + mouse.getMousePosition().getY() + "}");
                api.setColor(mouse.getMousePosition().getX(), mouse.getMousePosition().getY(), 'Y');
            }
        }
    }

    /**
     * Turns the mouse to face the next cell in the path.
     * 
     * @param currentCell The current cell the mouse is in.
     * @param nextCell The next cell the mouse will move to.
     */
    private static void turnMouseToNextCell(Cell currentCell, Cell nextCell) {
        int[] directionNeeded = new int[]{nextCell.getX() - currentCell.getX(), nextCell.getY() - currentCell.getY()};
        int[] halfStepsNeeded = mouse.turnMouseLocal(directionNeeded);

        // Turns the mouse to face the next cell in the most optimal way (turning left vs. right AND 45 degrees or 90 degrees).
        if (halfStepsNeeded[0] % 2 == 0) {
            for (int i = 0; i < halfStepsNeeded[0] / 2; i++) {
                if (halfStepsNeeded[1] == 1) {
                    api.turnRight();
                    log("Turning right...");
                } else if (halfStepsNeeded[1] == -1) {
                    api.turnLeft();
                    log("Turning left...");
                }
            }
        } else {
            for (int i = 0; i < halfStepsNeeded[0]; i++) {
                if (halfStepsNeeded[1] == 1) {
                    api.turnRight45();
                    log("Turning right 45 degrees...");
                } else if (halfStepsNeeded[1] == -1) {
                    api.turnLeft45();
                    log("Turning left 45 degrees...");
                }
            }
        }
    }

    /**
     * Adds all the edges of the maze as walls.
     */
    private static void addEdgesAsWalls() {
        // Add walls to the edges of the maze
        for (int i = 0; i < Constants.MazeConstants.numCols; i++) {
            api.setWall(i, 0, "s"); // Bottom edge
            api.setWall(i, Constants.MazeConstants.numRows - 1, "n"); // Top edge
        }
        for (int j = 0; j < Constants.MazeConstants.numRows; j++) {
            api.setWall(0, j, "w"); // Left edge
            api.setWall(Constants.MazeConstants.numCols - 1, j, "e"); // Right edge
        }
    }

    /**
     * Logs the desired text to console.
     * 
     * @param text The text to log to console.
     */
    private static void log(String text) {
        System.err.println(text);
    }
}
