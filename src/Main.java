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

        // setUp(startCell.get(0), goalCells);
        // frontierBased.explore(mouse, api, false);
        // Thread.sleep(2000);

        setUp(startCell.get(0), goalCells);
        traversePathIteratively(mouse, goalCells, false, true, false);
        Thread.sleep(2000);

        setUp(mouse.getMousePosition(), startCell);
        traversePathIteratively(mouse, startCell, false, true, false);
        Thread.sleep(500);

        setUp(startCell.get(0), goalCells);
        traversePathIteratively(mouse, goalCells, true, true, false);
        Thread.sleep(2000);

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

        if (Constants.MazeConstants.showGrid) {
            for (int i = 0; i < Constants.MazeConstants.numCols; i++) {
                for (int j = 0; j < Constants.MazeConstants.numRows; j++) {
                    api.setText(i, j, i + "," + j);
                }
            }
        }

        log("[START] Ready for " + Constants.MouseConstants.mouseName.toUpperCase() + "!\n");
        api.setColor(0, 0, Constants.MazeConstants.startCellColor);
        api.setText(0, 0, Constants.MazeConstants.startCellText);
        for (Cell goalCell : goalCells) {
            api.setColor(goalCell.getX(), goalCell.getY(), Constants.MazeConstants.goalCellColor);
            api.setText(goalCell.getX(), goalCell.getY(), Constants.MazeConstants.goalCellText);
        }
    }

    public static boolean traversePathIteratively(MouseLocal mouse, Cell goalCell, boolean diagonalsAllowed,
            boolean allExplored, boolean avoidGoalCells) {
        ArrayList<Cell> goalCells = new ArrayList<>();
        goalCells.add(goalCell);
        return traversePathIteratively(mouse, goalCells, diagonalsAllowed, allExplored, avoidGoalCells);
    }

    public static boolean traversePathIteratively(MouseLocal mouse, ArrayList<Cell> goalCells, boolean diagonalsAllowed,
            boolean allExplored, boolean avoidGoalCells) {
        Cell currCell;
        Movement prevMov = null;
        if (allExplored) {
            setAllExplored(mouse);
        }

        while (true) {
            currCell = mouse.getMousePosition();
            currCell.setIsExplored(true);

            if (mouse.isGoalCell(currCell, goalCells)) {
                // log("[END] Reached GOAL.");
                if (prevMov != null && prevMov.getIsDiagonal()) {
                    turnMouseToNextCell(prevMov.getFirstMove(), currCell);
                    api.moveForwardHalf();
                }
                break;
            }
            prevMov = null;

            // log("[PROCESSING] Detecting Walls...");
            mouse.detectAndSetWalls(api);

            // log("[PROCESSING] Making New Path...");
            List<Cell> cellPath = getBestAlgorithmPath(aStar, goalCells, diagonalsAllowed, avoidGoalCells);

            if (Constants.MazeConstants.showPath && allExplored
                    && !MouseLocal.isSame(goalCells.get(0), mouse.getCell(0, 0))) {
                for (Cell c : cellPath) {
                    api.setColor(c.getX(), c.getY(), Constants.MazeConstants.goalPathColor);
                }
            } else if (Constants.MazeConstants.showPath && allExplored && goalCells.size() == 1) {
                for (Cell c : cellPath) {
                    api.setColor(c.getX(), c.getY(), Constants.MazeConstants.returnPathColor);
                }
            }
            log("[PROCESSED] Algorithm Path: " + cellPath.stream()
                    .map(cell -> "(" + cell.getX() + ", " + cell.getY() + ")").collect(Collectors.joining(" -> ")));
            String path = AStar.pathToString(mouse, cellPath);
            // log("[PROCESSED] Path: " + path);

            if (allExplored && diagonalsAllowed) {
                log("[PROCESSED] Path: " + path);

                path = diagonalizeAndRun(currCell, path);
                log("[PROCESSED] Diagonalized Path: " + path);
            } else {
                for (String movement : path.split("#")) {
                    // log("[PROCESSING] Calculating Movement...");

                    // log("[DEBUG] Movement: " + movement);
                    switch (movement) {
                        case "F":
                            api.moveForward();
                            break;
                        case "L":
                            api.turnLeft();
                            break;
                        case "R":
                            api.turnRight();
                            break;
                        default:
                            log("[ERROR] Invalid Movement: " + movement);
                            break;
                    }

                    currCell = mouse.getMousePosition();
                    log("[POS] Updated Mouse Position: (" + mouse.getMousePosition().getX() + ", "
                            + mouse.getMousePosition().getY() + ")\n");
                    // log("[POS] Is Explored: " + currCell.getIsExplored());
                    if (!currCell.getIsExplored()) {
                        log("[RE-CALC] Cell is unexplored, calculating new path.");
                        break;
                    }
                    log("[RE-USE] Reusing ...");
                }
            }
            break; // FIXME
        }
        return true;
    }

    private static List<Cell> getBestAlgorithmPath(AStar aStar, ArrayList<Cell> goalCells, boolean diagonalsAllowed,
            boolean avoidGoalCells) {
        List<Cell> bestPath = null;
        double bestPathCost = Double.MAX_VALUE;
        for (Cell goalCell : goalCells) {
            List<Cell> path = aStar.findAStarPath(mouse, goalCell, diagonalsAllowed, avoidGoalCells);
            if (path != null && goalCell.getTotalCost() < bestPathCost) {
                bestPath = path;
                bestPathCost = goalCell.getTotalCost();
            }
        }
        return bestPath;
    }

    private static String diagonalizeAndRun(Cell currCell, String path) {
        StringBuilder newPath = new StringBuilder();
        String[] movements = path.split("#");
        String lastMovement = "";
        int i;

        for (i = 0; i < movements.length - 3; i++) {
            currCell = mouse.getMousePosition();
            log("[DEBUG] Mouse CurrPos: (" + currCell.getX() + ", " + currCell.getY() + ")");
            if (movements[i].equals("F") && movements.length - i > 4) {
                i++;
                String tempBlock = movements[i] + movements[i + 1] + movements[i + 2] + movements[i + 3];
                if (tempBlock.equals("RFRF") || tempBlock.equals("LFLF") || tempBlock.equals("RFLF")
                        || tempBlock.equals("LFRF")) {
                    log("[DEBUG] Temp Block: " + tempBlock);
                    if (!lastMovement.equals("RFRF") && !lastMovement.equals("LFLF") && !lastMovement.equals("RFLF")
                            && !lastMovement.equals("LFRF")) {
                        api.moveForwardHalf();
                        newPath.append("FH#");
                        mouse.moveForwardLocal();
                    } else {
                        if (lastMovement.equals("RFLF") || lastMovement.equals("LFLF")) {
                            newPath.append("L45#F#");
                            api.turnLeft45();
                            api.moveForward();
                        } else if (lastMovement.equals("LFRF") || lastMovement.equals("RFRF")) {
                            newPath.append("R45#F#");
                            api.turnRight45();
                            api.moveForward();
                        }
                    }
                    lastMovement = "F";
                } else {
                    i--;
                }
            }

            String movementsBlock = movements[i] + movements[i + 1] + movements[i + 2] + movements[i + 3];
            log("[DEBUG] Movement Block: " + movementsBlock);
            log("[DEBUG] Last Movement: " + lastMovement);

            switch (movementsBlock) {
                case "RFLF":
                    if (lastMovement.equals(movementsBlock) || lastMovement.equals("LFLF")) {
                        newPath.append("F#");
                        api.moveForward();
                    } else if (lastMovement.equals("LFRF") || lastMovement.equals("RFRF")) {
                        newPath.append("R#F#");
                        api.turnRight();
                        api.moveForward();
                    } else if (lastMovement.equals("F")) {
                        newPath.append("R45#F#");
                        api.turnRight45();
                        api.moveForward();
                    } else {
                        newPath.append("R#FH#L45#FH#");
                        api.turnRight();
                        api.moveForwardHalf();
                        api.turnLeft45();
                        api.moveForwardHalf();
                        mouse.moveForwardLocal();
                    }
                    i += 3;
                    lastMovement = movementsBlock;
                    break;

                // FINISHED
                case "LFRF":
                    if (lastMovement.equals(movementsBlock) || lastMovement.equals("RFRF")) {
                        newPath.append("F#");
                        api.moveForward();
                    } else if (lastMovement.equals("RFLF") || lastMovement.equals("LFLF")) {
                        newPath.append("L#F#");
                        api.turnLeft();
                        api.moveForward();
                    } else if (lastMovement.equals("F")) {
                        newPath.append("L45#F#");
                        api.turnLeft45();
                        api.moveForward();
                    } else {
                        newPath.append("L#FH#R45#FH#");
                        api.turnLeft();
                        api.moveForwardHalf();
                        api.turnRight45();
                        api.moveForwardHalf();
                        mouse.moveForwardLocal();
                    }
                    i += 3;
                    lastMovement = movementsBlock;
                    break;

                // FINISHED
                case "RFRF":
                    if (lastMovement.equals(movementsBlock)) {
                        newPath.append("R#FH#R#FH#");
                        api.turnRight();
                        api.moveForwardHalf();
                        api.turnRight();
                        api.moveForwardHalf();
                        mouse.moveForwardLocal();
                    } else if (lastMovement.equals("RFLF")) {
                        newPath.append("FH#R#FH#");
                        api.moveForwardHalf();
                        api.turnRight();
                        api.moveForwardHalf();
                        mouse.moveForwardLocal();
                    } else if (lastMovement.equals("LFLF")) {
                        newPath.append("FH#R#FH#");
                        api.moveForwardHalf();
                        api.turnRight();
                        api.moveForwardHalf();
                        mouse.moveForwardLocal();
                    } else if (lastMovement.equals("F")) {
                        newPath.append("R45#FH#R#FH#");
                        api.turnRight45();
                        api.moveForwardHalf();
                        api.turnRight();
                        api.moveForwardHalf();
                        mouse.moveForwardLocal();
                    } else {
                        newPath.append("R#FH#R45#FH#");
                        api.turnRight();
                        api.moveForwardHalf();
                        api.turnRight45();
                        api.moveForwardHalf();
                        mouse.moveForwardLocal();
                    }
                    i += 3;
                    lastMovement = movementsBlock;
                    break;

                case "LFLF":
                    if (lastMovement.equals(movementsBlock)) {
                        newPath.append("L#FH#L#FH#");
                        api.turnLeft();
                        api.moveForwardHalf();
                        api.turnLeft();
                        api.moveForwardHalf();
                        mouse.moveForwardLocal();
                    } else if (lastMovement.equals("LFRF")) {
                        newPath.append("FH#L#FH#");
                        api.moveForwardHalf();
                        api.turnLeft();
                        api.moveForwardHalf();
                        mouse.moveForwardLocal();
                    } else if (lastMovement.equals("RFRF")) {
                        newPath.append("FH#L#FH#");
                        api.moveForwardHalf();
                        api.turnLeft();
                        api.moveForwardHalf();
                        mouse.moveForwardLocal();
                    } else if (lastMovement.equals("F")) {
                        newPath.append("L45#FH#L#FH#");
                        api.turnLeft45();
                        api.moveForwardHalf();
                        api.turnLeft();
                        api.moveForwardHalf();
                        mouse.moveForwardLocal();
                    } else {
                        newPath.append("L#FH#L45#FH#");
                        api.turnLeft();
                        api.moveForwardHalf();
                        api.turnLeft45();
                        api.moveForwardHalf();
                        mouse.moveForwardLocal();
                    }
                    i += 3;
                    lastMovement = movementsBlock;
                    break;

                default:
                    if (lastMovement.equals("RFLF") || lastMovement.equals("LFLF")) {
                        if ((movements[i] + movements[i + 1]).equals("RF")) {
                            log("POPPPPPY");
                            newPath.append("FH#R45#FH#");
                            api.moveForwardHalf();
                            api.turnRight45();
                            api.moveForwardHalf();
                            mouse.moveForwardLocal();
                            i++;
                            lastMovement = "RF";
                            break;
                        } else if ((movements[i] + movements[i + 1]).equals("LF")) {
                            log("POPPY");
                            newPath.append("L#FH#L45#FH#");
                            api.turnLeft();
                            api.moveForwardHalf();
                            api.turnLeft45();
                            api.moveForwardHalf();
                            mouse.moveForwardLocal();
                            i++;
                            lastMovement = "LF";
                            break;
                        } else if ((movements[i] + movements[i + 1] + movements[i + 2]).equals("FLF")) {
                            log("POPPY");
                            newPath.append("L45#F#L45#FH#R45#FH#");
                            api.turnLeft45();
                            api.moveForwardHalf();
                            api.moveForwardHalf();
                            api.turnLeft45();
                            api.moveForwardHalf();
                            mouse.moveForwardLocal();
                            api.turnLeft45();
                            api.moveForwardHalf();
                            i += 2;
                            lastMovement = "F";
                            break;
                        }
                        newPath.append("L45#FH#");
                        api.turnLeft45();
                        api.moveForwardHalf();
                    } else if (lastMovement.equals("LFRF") || lastMovement.equals("RFRF")) {
                        if ((movements[i] + movements[i + 1]).equals("LF")) {
                            log("POPPY");
                            newPath.append("FH#L45#FH#");
                            api.moveForwardHalf();
                            api.turnLeft45();
                            api.moveForwardHalf();
                            mouse.moveForwardLocal();
                            i++;
                            lastMovement = "LF";
                            break;
                        } else if ((movements[i] + movements[i + 1]).equals("RF")) {
                            log("POPPPPY");
                            newPath.append("R#FH#R45#FH#");
                            api.turnRight();
                            api.moveForwardHalf();
                            api.turnRight45();
                            api.moveForwardHalf();
                            mouse.moveForwardLocal();
                            i++;
                            lastMovement = "RF";
                            break;
                        } else if ((movements[i] + movements[i + 1] + movements[i + 2]).equals("FRF")) {
                            log("POPPPPY");
                            newPath.append("R45#F#R45#FH#R45#FH#");
                            api.turnRight45();
                            api.moveForwardHalf();
                            api.moveForwardHalf();
                            api.turnRight45();
                            api.moveForwardHalf();
                            mouse.moveForwardLocal();
                            api.turnRight45();
                            api.moveForwardHalf();
                            i += 2;
                            lastMovement = "F";
                            break;
                        }
                        newPath.append("R45#FH#");
                        api.turnRight45();
                        api.moveForwardHalf();
                    }
                    newPath.append(movements[i] + "#");
                    // log("[DEBUG] Movement: " + movements[i]);
                    switch (movements[i]) {
                        case "F":
                            api.moveForward();
                            break;
                        case "L":
                            api.turnLeft();
                            break;
                        case "R":
                            api.turnRight();
                            break;
                        default:
                            log("[ERROR] Invalid Movement: " + movements[i]);
                            break;
                    }
                    lastMovement = movements[i];
                    break;
            }
        }
        if (lastMovement.equals("RFLF") || lastMovement.equals("LFLF")) {
            newPath.append("L45#FH#");
            api.turnLeft45();
            api.moveForwardHalf();
        } else if (lastMovement.equals("LFRF") || lastMovement.equals("RFRF")) {
            newPath.append("R45#FH#");
            api.turnRight45();
            api.moveForwardHalf();
        }
        if (i < movements.length) {
            log("Moves remaining: " + (movements.length - i) + " with moves being "
                    + Arrays.toString(Arrays.copyOfRange(movements, i, movements.length)));
            for (int j = i; j < movements.length; j++) {
                log("[DEBUG] Movement: " + movements[j]);
                currCell = mouse.getMousePosition();
                log("[DEBUG] Mouse CurrPos: (" + currCell.getX() + ", " + currCell.getY() + ")");
                switch (movements[j]) {
                    case "F":
                        api.moveForward();
                        break;
                    case "L":
                        api.turnLeft();
                        break;
                    case "R":
                        api.turnRight();
                        break;
                    default:
                        log("[ERROR] Invalid Movement: " + movements[j]);
                        break;
                }
            }
            currCell = mouse.getMousePosition();
            log("[DEBUG] Mouse CurrPos: (" + currCell.getX() + ", " + currCell.getY() + ")");
        } else {
            currCell = mouse.getMousePosition();
            log("[DEBUG] Mouse CurrPos: (" + currCell.getX() + ", " + currCell.getY() + ")");
        }
        return newPath.toString();
    }

    public static void turnMouseToNextCell(Cell currentCell, Cell nextCell) {
        int[] directionNeeded = new int[] { nextCell.getX() - currentCell.getX(),
                nextCell.getY() - currentCell.getY() };
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

    // public static void diagonalToDiagonalStep(Cell currCell, Cell nextCell,
    // Movement prevMov, Movement currMov) {
    // turnMouseToNextCell(currCell, nextCell);
    // api.moveForward();

    // //FIxme
    // // FINISHED
    // }

    // public static void diagonalToCardinalStep(Cell currCell, Cell nextCell,
    // Movement prevMov, Movement currMov) {
    // turnMouseToNextCell(prevMov.getFirstMove(), currCell);
    // api.moveForwardHalf();
    // turnMouseToNextCell(currCell, nextCell);
    // api.moveForward();

    // // FINISHED
    // }

    // public static void cardinalToDiagonalStep(Cell currCell, Cell nextCell,
    // Movement prevMov, Movement currMov) {
    // turnMouseToNextCell(currCell, currMov.getFirstMove());
    // api.moveForwardHalf();
    // // mouse.moveForwardLocal();
    // turnMouseToNextCell(currCell, nextCell);
    // api.moveForwardHalf();
    // mouse.moveForwardLocal();

    // // FINISHED, FIXME
    // }

    // public static void cardinalToCardinalStep(Cell currCell, Cell nextCell,
    // Movement prevMov, Movement currMov) {
    // turnMouseToNextCell(currCell, nextCell);
    // api.moveForward();

    // // FINISHED
    // }

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