package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import src.API.API;
import src.Algorithm.AStar;
import src.Algorithm.FrontierBased;
import src.Algorithm.Maze.Cell;
import src.Algorithm.Maze.MouseLocal;
import src.Algorithm.Maze.Movement;

public class Main {
    private static MouseLocal mouse;
    private static API api;
    private static AStar aStar;
    private static FrontierBased frontierBased;

    public static void main(String[] args) throws InterruptedException {
        mouse = new MouseLocal();
        api = new API(mouse);
        aStar = new AStar();
        frontierBased = new FrontierBased();

        ArrayList<Cell> startCell = new ArrayList<>(Arrays.asList(mouse.getMousePosition()));
        ArrayList<Cell> goalCells = mouse.getGoalCells();

        setUp(startCell.get(0), goalCells);
        // frontierBased.explore(mouse, api, false);
        Thread.sleep(500);

        // setUp(mouse.getMousePosition(), startCell);
        // traversePathIteratively(mouse, startCell, false);
        // Thread.sleep(2000);

        setAllExplored(mouse);
        // setUp(startCell.get(0), goalCells);
        traversePathIteratively(mouse, goalCells, false);

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

    private static void setUp(Cell startCell, ArrayList<Cell> goalCells) {
        api.clearAllColor();
        api.clearAllText();

        // Add walls to the edges of the maze
        for (int i = 0; i < Constants.MazeConstants.numCols; i++) {
            api.setWall(i, 0, "s"); // Bottom edge
            api.setWall(i, Constants.MazeConstants.numRows - 1, "n"); // Top edge
        }
        for (int j = 0; j < Constants.MazeConstants.numRows; j++) {
            api.setWall(0, j, "w"); // Left edge
            api.setWall(Constants.MazeConstants.numCols - 1, j, "e"); // Right edge
        }

        log("[START] Ready for " + Constants.MouseConstants.mouseName.toUpperCase() + "!\n");
        api.setColor(0, 0, Constants.MazeConstants.startCellColor);
        api.setText(0, 0, Constants.MazeConstants.startCellText);
        for (Cell goalCell : goalCells) {
            api.setColor(goalCell.getX(), goalCell.getY(), Constants.MazeConstants.goalCellColor);
            api.setText(goalCell.getX(), goalCell.getY(), Constants.MazeConstants.goalCellText);
        }
    }

    public static boolean traversePathIteratively(MouseLocal mouse, Cell goalCell, boolean diagonalsAllowed) {
        ArrayList<Cell> goalCells = new ArrayList<>();
        goalCells.add(goalCell);
        return traversePathIteratively(mouse, goalCells, diagonalsAllowed);
    }

    public static boolean traversePathIteratively(MouseLocal mouse, ArrayList<Cell> goalCells, boolean diagonalsAllowed) {
        Cell currCell;
        Movement prevMov = null;

        while (true) {
            currCell = mouse.getMousePosition();
            currCell.setIsExplored(true);

            if (mouse.isGoalCell(currCell, goalCells)) {
                log("[END] Reached GOAL.");
                if (prevMov != null && prevMov.getIsDiagonal()) {
                    turnMouseToNextCell(prevMov.getFirstMove(), currCell);
                    api.moveForwardHalf();
                }
                break;
            }
            prevMov = null;

            log("[PROCESSING] Detecting Walls");
            mouse.detectAndSetWalls(api);

            log("[PROCESSING] Making New Path");
            List<Cell> path = getBestAlgorithmPath(aStar, goalCells, diagonalsAllowed);
            log("[PROCESSED] " + AStar.pathToString(mouse, path));

            if (path == null) {
                log("[FATAL ERROR] No path found to goal.");
                break;
            }
            log("[PROCESSED] Algorithm Path: " + path.stream().map(cell -> "(" + cell.getX() + "," + cell.getY() + ")").collect(Collectors.joining(" -> ")) + "\n");

            for (Cell nextCell : path) {
                log("[PROCESSING] Calculating Movement");
                Movement currMov = mouse.getMovement(currCell, nextCell, diagonalsAllowed);

                boolean isCurrDiag = currMov.getIsDiagonal() && nextCell.getIsExplored();
                boolean isPrevDiag = (prevMov != null && prevMov.getIsDiagonal());

                if (isCurrDiag && isPrevDiag) {
                    log("[MOVE] D2D: " + Arrays.toString(currMov.getDirection()));
                    diagonalToDiagonalStep(currCell, nextCell, prevMov, currMov);
                } else if (isCurrDiag && !isPrevDiag) {
                    log("[MOVE] C2D: " + Arrays.toString(currMov.getDirection()));
                    cardinalToDiagonalStep(currCell, nextCell, prevMov, currMov);
                } else if (!isCurrDiag && isPrevDiag) {
                    log("[MOVE] D2C: " + Arrays.toString(currMov.getDirection()));
                    diagonalToCardinalStep(currCell, nextCell, prevMov, currMov);
                } else {
                    log("[MOVE] C2C: " + Arrays.toString(currMov.getDirection()));
                    cardinalToCardinalStep(currCell, nextCell, prevMov, currMov);
                }
                currCell = mouse.getMousePosition();
                prevMov = currMov;
                log("[POS] Updated Position: (" + mouse.getMousePosition().getX() + ", " + mouse.getMousePosition().getY() + ")\n");

                if (!nextCell.getIsExplored()) {
                    log("[RE-CALC] Cell is unexplored, calculating new path!");
                    break;
                }
                log("[RE-USE] Reusing ...");
            }
        }
        return true;
    }

    private static List<Cell> getBestAlgorithmPath(AStar aStar, ArrayList<Cell> goalCells, boolean diagonalsAllowed) {
        List<Cell> bestPath = null;
        double bestPathCost = Double.MAX_VALUE;
        for (Cell goalCell : goalCells) {
            List<Cell> path = aStar.findAStarPath(mouse, goalCell, diagonalsAllowed);
            if (path != null && goalCell.getTotalCost() < bestPathCost) {
                bestPath = path;
                bestPathCost = goalCell.getTotalCost();
            }
        }
        return bestPath;
    }

    public static void turnMouseToNextCell(Cell currentCell, Cell nextCell) {
        int[] directionNeeded = new int[] { nextCell.getX() - currentCell.getX(), nextCell.getY() - currentCell.getY() };
        int[] halfStepsNeeded = mouse.obtainHalfStepCount(directionNeeded);
       
        if (halfStepsNeeded[0] % 2 == 0) {
            for (int i = 0; i < halfStepsNeeded[0] / 2; i++) {
                if (halfStepsNeeded[1] == 1) {
                    api.turnRight();
                } else if (halfStepsNeeded[1] == -1) {
                    api.turnLeft();
                }
            }
        } else {
            for (int i = 0; i < halfStepsNeeded[0]; i++) {
                if (halfStepsNeeded[1] == 1) {
                    api.turnRight45();
                } else if (halfStepsNeeded[1] == -1) {
                    api.turnLeft45();
                }
            }
        }
    }

    public static void diagonalToDiagonalStep(Cell currCell, Cell nextCell, Movement prevMov, Movement currMov) {
        turnMouseToNextCell(currCell, nextCell);
        api.moveForward();

        //FIxme
        // FINISHED
    }

    public static void diagonalToCardinalStep(Cell currCell, Cell nextCell, Movement prevMov, Movement currMov) {
        turnMouseToNextCell(prevMov.getFirstMove(), currCell);
        api.moveForwardHalf();
        turnMouseToNextCell(currCell, nextCell);
        api.moveForward();

        // FINISHED
    }

    public static void cardinalToDiagonalStep(Cell currCell, Cell nextCell, Movement prevMov, Movement currMov) {
        turnMouseToNextCell(currCell, currMov.getFirstMove());
        api.moveForwardHalf();
        // mouse.moveForwardLocal();
        turnMouseToNextCell(currCell, nextCell);
        api.moveForwardHalf();
        mouse.moveForwardLocal();

        // FINISHED, FIXME
    }

    public static void cardinalToCardinalStep(Cell currCell, Cell nextCell, Movement prevMov, Movement currMov) {
        turnMouseToNextCell(currCell, nextCell);
        api.moveForward();

        // FINISHED
    }

    public static void setAllExplored(MouseLocal mouse) {
        for (int i = 0; i < Constants.MazeConstants.numCols; i++) {
            for (int j = 0; j < Constants.MazeConstants.numRows; j++) {
                mouse.getCell(i, j).setIsExplored(true);
            }
        }
    }

    // public static void previousStepWasNotDiagonal(Cell mouseCurrentCell, Cell
    // nextCell) {
    // int[] firstDirection = mouse.canMove(mouseCurrentCell,
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