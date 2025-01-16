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

        log("Running " + Constants.MouseConstants.mouseName + "...\n");
        api.setColor(0, 0, 'G');
        api.setText(0, 0, "Start");

        findGoalIncrementalAStar(mouse, mouse.getCell(Constants.MazeConstants.goalPositionX, Constants.MazeConstants.goalPositionY), "goal");
        
        // api.clearAllColor();
        // api.clearAllText();
        api.setColor(mouse.getMousePosition().getX(), mouse.getMousePosition().getY(), 'R');

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        findGoalIncrementalAStar(mouse, mouse.getCell(0, 0), "return");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        findGoalIncrementalAStar(mouse, mouse.getCell(Constants.MazeConstants.goalPositionX, Constants.MazeConstants.goalPositionY), "fast");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        findGoalIncrementalAStar(mouse, mouse.getCell(0, 0), "return");
    }

    private static void findGoalIncrementalAStar(MouseLocal mouse, Cell goalCell, String mode) {
        while (true) {
            // Creates a cell holding the mouse's current position.
            Cell mouseCurrentCell = mouse.getMousePosition();
            // log("Mouse Position: (" + mouseCurrentCell.getX() + ", " + mouseCurrentCell.getY() + ")");

            // If the mouse reaches the goal, then break out of the loop.
            if (mouseCurrentCell.getX() == goalCell.getX()
                    && mouseCurrentCell.getY() == goalCell.getY()) {
                log("Reached goal. :)");
                api.setColor(mouseCurrentCell.getX(), mouseCurrentCell.getY(), 'G');
                api.setText(mouseCurrentCell.getX(), mouseCurrentCell.getY(), "Goal");

                break;
            }

            // log(mouse.localMazeToString());
            // Checks if there is a wall in front, left, or right of the mouse.
            if (api.wallFront()) {
                api.setWall(mouseCurrentCell.getX(), mouseCurrentCell.getY(),
                        mouse.getDirectionAsString(mouse.getMouseDirection()));
            }
            if (api.wallLeft()) {
                api.setWall(mouseCurrentCell.getX(), mouseCurrentCell.getY(), mouse.getDirectionToTheLeft());
            }
            if (api.wallRight()) {
                api.setWall(mouseCurrentCell.getX(), mouseCurrentCell.getY(), mouse.getDirectionToTheRight());
            }
            // log(mouse.localMazeToString());

            // Gets the A* path to the goal.
            List<Cell> optimalPath = aStarAlgoFinder.findAStarPath(mouse, mouseCurrentCell,
                    goalCell);

            if (optimalPath == null) {
                log("No path found to goal. Maze is blocked or no route!");
                System.err.println("Cell: " + mouseCurrentCell.toString());
                System.err.print(mouse.localMazeToString());
                break;
            }

            log("Path found, length: " + optimalPath.size());
            for (Cell step : optimalPath) {
            log(step.toString());
            }

            Cell nextCell = mouse.getCell(optimalPath.get(0).getX(), optimalPath.get(0).getY());
            log("Next cell coordinates: (" + nextCell.getX() + ", " + nextCell.getY() + ")");
            turnMouseToNextCell(mouseCurrentCell, nextCell);
            api.moveForward();
            log("Mouse position: (" + mouse.getMousePosition().getX() + ", " + mouse.getMousePosition().getY() + ")");
            log("Mouse direction: " + mouse.getDirectionAsString(mouse.getMouseDirection()));
            api.setColor(mouse.getMousePosition().getX(), mouse.getMousePosition().getY(), (mode.equals("goal") ? 'Y' : (mode.equals("return")) ? 'C' : 'O'));
            api.setText(mouse.getMousePosition().getX(), mouse.getMousePosition().getY(), (mode.equals("goal") ? "v" : (mode.equals("return")) ? "r" : "%"));

            // Moves the mouse to the next cell(s) in the path.
            // for (int i = 1; i < optimalPath.size(); i++) {
            // Cell currentCell = mouse.getCell(optimalPath.get(i - 1).getX(),
            // optimalPath.get(i - 1).getY());
            // Cell nextCell = mouse.getCell(optimalPath.get(i).getX(),
            // optimalPath.get(i).getY());
            // log("Next cell coordinates: (" + nextCell.getX() + ", " + nextCell.getY() +
            // ")");
            // turnMouseToNextCell(currentCell, nextCell);
            // api.moveForward();
            // log("Mouse position: (" + mouse.getMousePosition().getX() + ", " +
            // mouse.getMousePosition().getY() + ")");
            // log("Mouse direction: " +
            // mouse.getDirectionAsString(mouse.getMouseDirection()));
            // api.setColor(mouse.getMousePosition().getX(),
            // mouse.getMousePosition().getY(), 'Y');
            // }
        }
    }

    /**
     * Turns the mouse to face the next cell in the path.
     * 
     * @param currentCell The current cell the mouse is in.
     * @param nextCell    The next cell the mouse will move to.
     */
    private static void turnMouseToNextCell(Cell currentCell, Cell nextCell) {
        int[] directionNeeded = new int[] { nextCell.getX() - currentCell.getX(),
                nextCell.getY() - currentCell.getY() };
        log("Direction needed: " + mouse.getDirectionAsString(directionNeeded) + "\n");
        int[] halfStepsNeeded = mouse.obtainHalfStepCount(directionNeeded);

        // Turns the mouse to face the next cell in the most optimal way (turning left
        // vs. right AND 45 degrees or 90 degrees).
        if (halfStepsNeeded[0] % 2 == 0) {
            for (int i = 0; i < halfStepsNeeded[0] / 2; i++) {
                if (halfStepsNeeded[1] == 1) {
                    api.turnRight();
                    log("Turning right 90 deg...");
                } else if (halfStepsNeeded[1] == -1) {
                    api.turnLeft();
                    log("Turning left 90 deg...");
                }
            }
        } else {
            for (int i = 0; i < halfStepsNeeded[0]; i++) {
                if (halfStepsNeeded[1] == 1) {
                    api.turnRight45();
                    log("Turning right 45 deg...");
                } else if (halfStepsNeeded[1] == -1) {
                    api.turnLeft45();
                    log("Turning left 45 deg...");
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
