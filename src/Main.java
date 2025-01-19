package src;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import src.API.API;
import src.Algorithm.AStar;
import src.Algorithm.FrontierBasedAvoidGoals;
import src.Algorithm.Maze.Cell;
import src.Algorithm.Maze.MouseLocal;
import src.Algorithm.Maze.Movement;

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
        api.setColor(Constants.MazeConstants.goalPositionX, Constants.MazeConstants.goalPositionY,
                Constants.MazeConstants.goalCellColor);
        api.setText(Constants.MazeConstants.goalPositionX, Constants.MazeConstants.goalPositionY,
                Constants.MazeConstants.goalCellText);

        Cell startCell = mouse.getMousePosition();
        Cell goalCell = mouse.getCell(Constants.MazeConstants.goalPositionX, Constants.MazeConstants.goalPositionY);

        // frontierBasedExplorer.exploreMazeAvoidGoals(mouse, api);
        traversePathIteratively(mouse, goalCell, "A*", "goal", true);
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
     * Traverses a path iteratively using either the A* or Tremaux algorithm.
     * 
     * @param mouse     The mouse object.
     * @param goalCell  The goal cell.
     * @param algorithm The algorithm to use.
     * @param mode      The mode to use. (goal, return, fast)
     */
    public static boolean traversePathIteratively(MouseLocal mouse, Cell goalCell, String algorithm, String mode,
            boolean sleep) {
        Movement previousStepDirectionNeeded = null;
        Cell mouseCurrentCell;
        while (true) {
            // Creates a cell holding the mouse's current position.
            mouseCurrentCell = mouse.getMousePosition();
            mouseCurrentCell.setIsExplored(true);

            // If the mouse reaches the goal, then break out of the loop.
            if (mouseCurrentCell.getX() == goalCell.getX() && mouseCurrentCell.getY() == goalCell.getY()) {
                log("[GOAL] Reached GOAL.");
                break;
            }

            // Checks if there is a wall in front, left, or right of the mouse.
            detectAndSetWalls(mouse, api);

            // Gets the path to the goal.
            List<Cell> algorithmPath = algorithm.equals("A*") ? aStarAlgoFinder.findAStarPath(mouse, goalCell) : null;

            // Checks if path is null.
            if (algorithmPath == null) {
                log("[DEBUG] No path found to goal. Maze is blocked or no route!");
                System.err.print(mouse.localMazeToString());
                break;
            }

            log("[PROCESSING] Making New Path");
            // log("[PROCESSED] Algorithm Path: " + algorithmPath.stream().map(cell -> "(" +
            // cell.getX() + "," + cell.getY() + ")").collect(Collectors.joining(" -> ")) +
            // "\n");

            // Sets previous direction needed to null.
            previousStepDirectionNeeded = null;
            // Iterate over each cell in the path
            for (Cell nextCellInPath : algorithmPath) {
                if (mouseCurrentCell == nextCellInPath) {
                    continue;
                }

                // Figure out the direction from our current cell to the next
                Movement movement = mouse.canMoveBetweenCells(mouseCurrentCell, nextCellInPath);
                // FIXME new path not updating
                // Check if the direction is diagonal
                boolean isDiagonalStep = isDiagonalMovement(movement);
                boolean isPreviousStepDiagonal = (previousStepDirectionNeeded != null
                        && isDiagonalMovement(previousStepDirectionNeeded));

                log("[DEBUG] Current Mouse Position: " + mouseCurrentCell.getX() + ", " + mouseCurrentCell.getY());
                log("[DEBUG] Next Cell: " + nextCellInPath.getX() + ", " + nextCellInPath.getY());

                // Performs the necessary movements to move the mouse to the next cell in the
                // path.
                if (isDiagonalStep && isPreviousStepDiagonal) {
                    log("[DEBUG] (1) Diagonal Step From Diagonal: " + Arrays.toString(movement.getDirection()));
                    diagonalToDiagonalStep(mouseCurrentCell, nextCellInPath, previousStepDirectionNeeded, movement,
                            algorithmPath);

                } else if (isDiagonalStep && !isPreviousStepDiagonal) {
                    log("[DEBUG] (2) Diagonal Step From Cardinal: " + Arrays.toString(movement.getDirection()));
                    cardinalToDiagonalStep(mouseCurrentCell, nextCellInPath, previousStepDirectionNeeded, movement);
                } else if (!isDiagonalStep && isPreviousStepDiagonal) {
                    log("[DEBUG] (3) Cardinal Step From Diagonal: " + Arrays.toString(movement.getDirection()));
                    diagonalToCardinalStep(mouseCurrentCell, nextCellInPath, previousStepDirectionNeeded, movement);
                } else {
                    log("[DEBUG] (4) Cardinal Step From Cardinal: " + Arrays.toString(movement.getDirection()));
                    cardinalToCardinalStep(mouseCurrentCell, nextCellInPath, previousStepDirectionNeeded, movement);
                }
                log("Updated Position: " + mouse.getMousePosition().getX() + ", " + mouse.getMousePosition().getY()
                        + "\n");

                // Mark next cell with text or color, etc.
                api.setText(mouse.getMousePosition().getX(), mouse.getMousePosition().getY(),
                        (mode.equals("goal") ? Constants.MazeConstants.goalPathString
                                : (mode.equals("return")) ? Constants.MazeConstants.returnPathString
                                        : Constants.MazeConstants.fastPathString));

                // If the next cell is unexplored, break to re-run pathfinding.
                if (!nextCellInPath.getIsExplored()) {
                    log("[BREAK] Next cell is unexplored, breaking to re-run pathfinding...");
                    break;
                }

                // Update the current cell to the next cell.
                mouseCurrentCell = mouse.getMousePosition();
                previousStepDirectionNeeded = movement;

                log("Reusing path...");
            }
        }
        if (previousStepDirectionNeeded != null && previousStepDirectionNeeded.isDiagonal()) {
            mouseCurrentCell = mouse.getMousePosition();
            log("[DEBUG] Last Step: " + previousStepDirectionNeeded.getCellToMoveToFirst().getX() + ", "
                    + previousStepDirectionNeeded.getCellToMoveToFirst().getY() + " -> " + mouseCurrentCell.getX()
                    + ", " + mouseCurrentCell.getY());
            turnMouseToNextCell(previousStepDirectionNeeded.getCellToMoveToFirst(), mouseCurrentCell);
            api.moveForwardHalf();
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
        // log("Current cell coordinates: (" + currentCell.getX() + ", " +
        // currentCell.getY() + ")");
        // log("Next cell coordinates: (" + nextCell.getX() + ", " + nextCell.getY() +
        // ")");
        int[] directionNeeded = new int[] { nextCell.getX() - currentCell.getX(),
                nextCell.getY() - currentCell.getY() };
        // log("Direction needed: " + Arrays.toString(directionNeeded));
        // log("Direction needed: " + mouse.getDirectionAsString(directionNeeded) +
        // "\n");
        int[] halfStepsNeeded = mouse.obtainHalfStepCount(directionNeeded);
        // log("Half steps needed: " + Arrays.toString(halfStepsNeeded));
        // log("Direction needed: " + Arrays.toString(directionNeeded));

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

    private static boolean isDiagonalMovement(Movement movement) {
        return movement.getDirection()[0] != 0 && movement.getDirection()[1] != 0;
    }

    public static Movement diagonalToDiagonalStep(Cell mouseCurrentCell, Cell nextCell, Movement prevMovement,
            Movement currentMovement, List<Cell> algorithmPath) {
                Movement diagonalMovement = mouse.canMoveBetweenCells(mouseCurrentCell, nextCell);

        // If can't move, return
        if (!diagonalMovement.canMove()) {
            log("[ERROR] diagonalToDiagonalStep: can't move diagonally!");
            return diagonalMovement;
        }

        // We can read which side we came from vs. which side we're going to:
        String fromSide = prevMovement.isLeftOrRightDiagonal(); // "left" or "right"
        String toSide = currentMovement.isLeftOrRightDiagonal(); // "left" or "right"
        log("[DEBUG] D2D from " + fromSide + " → " + toSide);

        // 2) Move half
        if (fromSide.equals(toSide)) {
            turnMouseToNextCell(mouseCurrentCell, nextCell);
            int forwardCounter = 0;
            Cell prevCell = mouseCurrentCell;
            for (int i = 1; i < algorithmPath.size(); i++) {
                // log("[DEBUG] Checking cell: " + algorithmPath.get(i).getX() + ", " +
                // algorithmPath.get(i).getY());
                Movement canMove = mouse.canMoveBetweenCells(prevCell, algorithmPath.get(i));
                if (canMove.canMove() && canMove.isDiagonal() && canMove.isLeftOrRightDiagonal().equals(toSide)) {
                    forwardCounter++;
                } else {
                    break;
                }
                prevCell = algorithmPath.get(i);
            }

            // Long diagonals
            for (int i = 0; i < 1; i++) {
                mouseCurrentCell = mouse.getMousePosition();
                nextCell = algorithmPath.get(i + 1);
                prevMovement = mouse.canMoveBetweenCells(mouseCurrentCell, nextCell);
                turnMouseToNextCell(mouseCurrentCell, nextCell);
                api.moveForward();
            }
            mouseCurrentCell = mouse.getMousePosition();
            // log("Current cell: " + mouseCurrentCell.getX() + ", " +
            // mouseCurrentCell.getY() + " Intermediary Cell: " +
            // prevMovement.getCellToMoveToFirst().getX() + ", " +
            // prevMovement.getCellToMoveToFirst().getY());
            // log("Curent dir: " + Arrays.toString(mouse.getMouseDirection()));
            log("[DEBUG] Updated Position: " + mouseCurrentCell.getX() + ", " + mouseCurrentCell.getY());

        } else {
            // api.moveForward();
        }
        // api.moveForwardHalf();
        // // 3) Turn from corner to the final diagonal cell
        // turnMouseToNextCell(mouseCurrentCell, nextCell);
        // // 4) Move half
        // api.moveForwardHalf();
        // // 5) Update local position
        // mouse.moveForwardLocal();

        return diagonalMovement;
    }

    public static void diagonalToCardinalStep(Cell mouseCurrentCell, Cell nextCell, Movement prevMovement,
            Movement currentMovement) {
        turnMouseToNextCell(prevMovement.getCellToMoveToFirst(), mouseCurrentCell);
        api.moveForwardHalf();
        turnMouseToNextCell(mouseCurrentCell, nextCell);
        api.moveForward();
    }

    public static void cardinalToDiagonalStep(Cell mouseCurrentCell, Cell nextCell, Movement prevMovement,
            Movement currentMovement) {
        Movement diagonalMovement = mouse.canMoveBetweenCells(mouseCurrentCell, nextCell);
        turnMouseToNextCell(mouseCurrentCell, diagonalMovement.getCellToMoveToFirst());
        api.moveForwardHalf();
        turnMouseToNextCell(mouseCurrentCell, nextCell);
        api.moveForwardHalf();
        mouse.moveForwardLocal();
        // FINISHED, this will land the mouse on the edge.
    }

    public static void cardinalToCardinalStep(Cell mouseCurrentCell, Cell nextCell, Movement prevMovement,
            Movement currentMovement) {
        Movement cardinalMovement = mouse.canMoveBetweenCells(mouseCurrentCell, nextCell);
        turnMouseToNextCell(mouseCurrentCell, nextCell);
        api.moveForward();
        // FINISHED, DO NOT TOUCH.
    }

    // public static void previousStepWasNotDiagonal(Cell mouseCurrentCell, Cell
    // nextCell) {
    // int[] firstDirection = mouse.canMoveBetweenCells(mouseCurrentCell,
    // nextCell)[1];
    // turnMouseToNextCell(mouseCurrentCell, mouse.getCell(mouseCurrentCell.getX() +
    // firstDirection[0],
    // mouseCurrentCell.getY() + firstDirection[1]));

    // // Half step forward
    // api.moveForwardHalf();

    // turnMouseToNextCell(mouseCurrentCell, nextCell);

    // // Now do a full forward
    // api.moveForwardHalf();
    // mouse.moveForwardLocal();
    // }

    // public static void previousStepWasDiagonal(Cell mouseCurrentCell, Cell
    // nextCell, int[] prevDirectionNeeded, int[] directionNeeded) {
    // if (prevDirectionNeeded[0] == directionNeeded[0]) {
    // // If the direction is the same, just move forward
    // api.moveForward();
    // } else {
    // // If the direction is different, turn to the new direction
    // turnMouseToNextCell(mouseCurrentCell, mouse.getCell(mouseCurrentCell.getX(),
    // mouseCurrentCell.getY() + prevDirectionNeeded[1]));

    // api.moveForwardHalf();
    // }
    // previousStepWasNotDiagonal(mouseCurrentCell, nextCell);
    // }

    /**
     * Logs the desired text to console.
     * 
     * @param text The text to log to console.
     */
    private static void log(String text) {
        System.err.println(text);
    }
}