package src;

import java.util.Arrays;
import java.util.List;

import src.API.API;
import src.Algorithm.AStar;
import src.Algorithm.FrontierBasedAvoidGoals;
import src.Algorithm.Tremaux;
import src.Algorithm.Maze.Cell;
import src.Algorithm.Maze.MouseLocal;
import src.Constants.MazeConstants;

public class Main {
    private static MouseLocal mouse;
    private static API api;
    private static AStar aStarAlgoFinder;
    private static FrontierBasedAvoidGoals frontierBasedExplorer;

    public static void main(String[] args) {
        mouse = new MouseLocal();
        api = new API(mouse);
        aStarAlgoFinder = new AStar();
        frontierBasedExplorer = new FrontierBasedAvoidGoals();

        addEdgesAsWalls();

        log("Running " + Constants.MouseConstants.mouseName + "...\n");
        api.setColor(0, 0, Constants.MazeConstants.startCellColor);
        api.setText(0, 0, Constants.MazeConstants.startCellText);
        api.setColor(Constants.MazeConstants.goalPositionX, Constants.MazeConstants.goalPositionY, Constants.MazeConstants.goalCellColor);
        api.setText(Constants.MazeConstants.goalPositionX, Constants.MazeConstants.goalPositionY, Constants.MazeConstants.goalCellText);

        Cell startCell = mouse.getMousePosition();
        Cell goalCell = mouse.getCell(Constants.MazeConstants.goalPositionX, Constants.MazeConstants.goalPositionY);

        frontierBasedExplorer.exploreMazeAvoidGoals(mouse, api);
        traversePathIteratively(mouse, startCell, "A*", "return", true);
        traversePathIteratively(mouse, goalCell, "A*", "fast", true);

        // api.moveForward();
        // api.turnRight();
        // api.moveForward();
        // api.moveForwardHalf();
        // api.turnLeft45();
        // api.moveForwardHalf();
        // findGoalIncrementalAStar(mouse,
        // mouse.getCell(Constants.MazeConstants.goalPositionX,
        // Constants.MazeConstants.goalPositionY), "goal");

        // // api.clearAllColor();
        // // api.clearAllText();
        // api.setColor(mouse.getMousePosition().getX(),
        // mouse.getMousePosition().getY(), 'R');

        // try {
        // Thread.sleep(2000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }

        // findGoalIncrementalAStar(mouse, mouse.getCell(0, 0), "return");
        // try {
        // Thread.sleep(2000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        // findGoalIncrementalAStar(mouse,
        // mouse.getCell(Constants.MazeConstants.goalPositionX,
        // Constants.MazeConstants.goalPositionY), "fast");

        // try {
        // Thread.sleep(2000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        // findGoalIncrementalAStar(mouse, mouse.getCell(0, 0), "return");
    }

    /**
     * Traverses a path iteratively using either the A* or Tremaux algorithm.
     * 
     * @param mouse     The mouse object.
     * @param goalCell  The goal cell.
     * @param algorithm The algorithm to use.
     * @param mode      The mode to use. (goal, return, fast)
     */
    public static boolean traversePathIteratively(MouseLocal mouse, Cell goalCell, String algorithm, String mode, boolean sleep) {
        while (true) {
            // Creates a cell holding the mouse's current position.
            Cell mouseCurrentCell = mouse.getMousePosition();
            mouseCurrentCell.setIsExplored(true);

            // If the mouse reaches the goal, then break out of the loop.
            if (mouseCurrentCell.getX() == goalCell.getX() && mouseCurrentCell.getY() == goalCell.getY()) {
                // log("Reached GOAL.");
                break;
            }

            // Checks if there is a wall in front, left, or right of the mouse.
            detectAndSetWalls(mouse, api);

            // Gets the path to the goal.
            List<Cell> algorithmPath = algorithm.equals("A*") ? aStarAlgoFinder.findAStarPath(mouse, goalCell) : null;
                    // : tremauxExplorer.findTremauxPath(mouse, goalCell);

            // Checks if path is null.
            if (algorithmPath == null) {
                log("No path found to goal. Maze is blocked or no route!");
                System.err.print(mouse.localMazeToString());
                break;
            }

            // Confirms path is found.
            // log("Path found, length: " + algorithmPath.size());

            for (Cell nextCell : algorithmPath) {
                // log("Next cell coordinates: (" + nextCell.getX() + ", " + nextCell.getY() + ")");

                // Logs the mouse's position and direction.
                // log("Mouse position: (" + mouse.getMousePosition().getX() + ", " + mouse.getMousePosition().getY()
                //         + ")");
                // log("Mouse direction: " + mouse.getDirectionAsString(mouse.getMouseDirection()));

                // Turns the mouse to face the next cell in the path and moves the mouse
                // forward.
                turnMouseToNextCell(mouseCurrentCell, nextCell);
                api.moveForward();
                api.setText(mouse.getMousePosition().getX(), mouse.getMousePosition().getY(),
                        (mode.equals("goal") ? Constants.MazeConstants.goalPathString : (mode.equals("return")) ? Constants.MazeConstants.returnPathString : Constants.MazeConstants.fastPathString));
                if (!nextCell.getIsExplored()) {
                    log("Creating new path...");
                    api.setColor(mouse.getMousePosition().getX(), mouse.getMousePosition().getY(),
                    (mode.equals("goal") ? Constants.MazeConstants.goalPathColor : (mode.equals("return")) ? Constants.MazeConstants.returnPathColor : Constants.MazeConstants.fastPathColor));
                    break;
                } else {
                    mouseCurrentCell = mouse.getMousePosition();
                    api.setColor(mouseCurrentCell.getX(), mouseCurrentCell.getY(), (mode.equals("fast")) ? Constants.MazeConstants.fastPathColor : Constants.MazeConstants.overlapPathColor);
                    api.setText(mouseCurrentCell.getX(), mouseCurrentCell.getY(), (mode.equals("fast")) ? Constants.MazeConstants.fastPathString : Constants.MazeConstants.overLapPathString);
                    log("Reusing path created...");
                }
            }
        }
        if (sleep) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Detects and sets walls in front, left, and right of the mouse.
     * 
     * @param mouse The mouse object.
     * @param API   The API object.
     */
    public static void detectAndSetWalls(MouseLocal mouse, API api) {
        Cell mouseCurrentCell = mouse.getMousePosition();
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
    }

    /**
     * Turns the mouse to face the next cell in the path.
     * 
     * @param currentCell The current cell the mouse is in.
     * @param nextCell    The next cell the mouse will move to.
     */
    public static void turnMouseToNextCell(Cell currentCell, Cell nextCell) {
        // log("Current cell coordinates: (" + currentCell.getX() + ", " + currentCell.getY() + ")");
        // log("Next cell coordinates: (" + nextCell.getX() + ", " + nextCell.getY() + ")");
        int[] directionNeeded = new int[] { nextCell.getX() - currentCell.getX(),
                nextCell.getY() - currentCell.getY() };
        // log("Direction needed: " + Arrays.toString(directionNeeded));
        // log("Direction needed: " + mouse.getDirectionAsString(directionNeeded) + "\n");
        int[] halfStepsNeeded = mouse.obtainHalfStepCount(directionNeeded);

        // Turns the mouse to face the next cell in the most optimal way (turning left
        // vs. right AND 45 degrees or 90 degrees).
        if (halfStepsNeeded[0] % 2 == 0) {
            for (int i = 0; i < halfStepsNeeded[0] / 2; i++) {
                if (halfStepsNeeded[1] == 1) {
                    api.turnRight();
                    // log("Turning right 90 deg...");
                } else if (halfStepsNeeded[1] == -1) {
                    api.turnLeft();
                    // log("Turning left 90 deg...");
                }
            }
        } else {
            for (int i = 0; i < halfStepsNeeded[0]; i++) {
                if (halfStepsNeeded[1] == 1) {
                    api.turnRight45();
                    // log("Turning right 45 deg...");
                } else if (halfStepsNeeded[1] == -1) {
                    api.turnLeft45();
                    // log("Turning left 45 deg...");
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
