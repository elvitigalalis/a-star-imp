#include "AStar.h"
#include "../Constants.h" 
#include <algorithm>
#include <cmath>
#include <stdexcept>
#include <sstream>

// Constructor
AStar::AStar() {
    // No initialization required for this implementation
}

// Finds the A* path from mouse to goalCell
std::vector<Cell*> AStar::findAStarPath(MouseLocal& mouse, Cell& goalCell, bool diagonalsAllowed, bool avoidGoalCells) {
    mouse.resetCosts();
    Cell& currCell = mouse.getMousePosition();

    // Priority queue with custom comparator
    std::priority_queue<Cell*, std::vector<Cell*>, CompareCell> discoveredCell;

    // Processed cells grid
    std::vector<std::vector<bool>> procCells(Constants::MazeConstants::numCols, std::vector<bool>(Constants::MazeConstants::numRows, false));

    // Initialize start cell
    Cell& startCell = mouse.getCell(currCell.getX(), currCell.getY());
    startCell.setCostFromStart(0.0);
    startCell.setTotalCost(MouseLocal::octileDistance(startCell, goalCell));
    discoveredCell.push(&startCell);

    // A* Algorithm Loop
    while (!discoveredCell.empty()) {
        Cell* procCell = discoveredCell.top();
        discoveredCell.pop();

        if (MouseLocal::isSame(*procCell, goalCell)) {
            return reconstructPath(currCell, goalCell);
        }

        if (procCells[procCell->getX()][procCell->getY()]) {
            continue;
        }

        procCells[procCell->getX()][procCell->getY()] = true; // Mark as processed

        std::vector<Cell*> neighbors = mouse.getNeighbors(*procCell, diagonalsAllowed);
        for (Cell* neighbor : neighbors) {
            if (procCells[neighbor->getX()][neighbor->getY()]) {
                continue;
            }

            if (avoidGoalCells && mouse.isGoalCell(*neighbor, mouse.getGoalCells())) {
                continue;
            }

            Movement movement = mouse.getMovement(*procCell, *neighbor, diagonalsAllowed);
            if (!movement.getCanMove()) {
                continue;
            }

            double costToNeighbor = MouseLocal::euclideanDistance(*procCell, *neighbor);
            double costFromStart = procCell->getCostFromStart() + costToNeighbor;

            if (costFromStart < neighbor->getCostFromStart()) { // Update neighbor costs
                neighbor->setCostFromStart(costFromStart);
                neighbor->setTotalCost(costFromStart + MouseLocal::octileDistance(*neighbor, goalCell));
                neighbor->setPrevCellInPath(procCell);
                discoveredCell.push(neighbor);
            }
        }
    }

    return std::vector<Cell*>(); // No path was found
}

// Reconstructs the path from goal to start
std::vector<Cell*> AStar::reconstructPath(Cell& startingCell, Cell& goalCell) {
    std::vector<Cell*> path;
    Cell* pointer = &goalCell;

    while (!MouseLocal::isSame(*pointer, startingCell)) {
        path.push_back(pointer);
        pointer = pointer->getPrevCellInPath();
        if (pointer == nullptr) {
            // Path reconstruction failed
            return std::vector<Cell*>();
        }
    }

    std::reverse(path.begin(), path.end());
    return path;
}

// Converts the path into a string of movement commands
std::string AStar::pathToString(MouseLocal& mouse, const std::vector<Cell*>& path) {
    std::stringstream pathString;
    Cell& origCell = mouse.getMousePosition();
    std::array<int, 2> origDir = mouse.getMouseDirection();

    Cell* currCell = &origCell;

    for (Cell* nextCell : path) {
        // Determine new direction
        std::array<int, 2> newDir = MouseLocal::getDirBetweenCells(*currCell, *nextCell);
        std::array<int, 2> turns = mouse.obtainHalfStepCount(newDir);

        if (turns[0] % 2 == 0) { // Even number of half steps
            if (turns[1] == 1) { // Right turn
                for (int i = 0; i < turns[0] / 2; i++) {
                    pathString << "R#";
                }
                mouse.turnMouseLocal(0, turns[0]);
                pathString << "F#";
                mouse.moveForwardLocal();
            } else { // Left turn
                for (int i = 0; i < turns[0] / 2; i++) {
                    pathString << "L#";
                }
                mouse.turnMouseLocal(turns[0], 0);
                pathString << "F#";
                mouse.moveForwardLocal();
            }
        } else { // Odd number of half steps (diagonal movement)
            Movement movement = mouse.getMovement(*currCell, *nextCell, true);
            Cell* cellToMoveToFirst = movement.getFirstMove();

            if (cellToMoveToFirst == nullptr) {
                // Cannot perform diagonal movement without intermediate cell
                continue;
            }

            // Determine required turns to reach the first intermediate cell
            std::array<int, 2> neededDir = MouseLocal::getDirBetweenCells(*currCell, *cellToMoveToFirst);
            std::array<int, 2> firstTurns = mouse.obtainHalfStepCount(neededDir);

            for (int i = 0; i < firstTurns[0] / 2; i++) {
                if (firstTurns[1] == 1) {
                    pathString << "R#";
                } else {
                    pathString << "L#";
                }
            }
            if (firstTurns[1] == 1) {
                mouse.turnMouseLocal(0, firstTurns[0]);
            } else {
                mouse.turnMouseLocal(firstTurns[0], 0);
            }

            pathString << "F#";
            mouse.moveForwardLocal();

            // Determine required turns to reach the next cell from the intermediate cell
            std::array<int, 2> secNeededDir = MouseLocal::getDirBetweenCells(*cellToMoveToFirst, *nextCell);
            std::array<int, 2> secTurns = mouse.obtainHalfStepCount(secNeededDir);

            for (int i = 0; i < secTurns[0] / 2; i++) {
                if (secTurns[1] == 1) {
                    pathString << "R#";
                } else {
                    pathString << "L#";
                }
            }
            if (secTurns[1] == 1) {
                mouse.turnMouseLocal(0, secTurns[0]);
            } else {
                mouse.turnMouseLocal(secTurns[0], 0);
            }

            pathString << "F#";
            mouse.moveForwardLocal();

            currCell = mouse.getMousePosition().getPrevCellInPath(); // Update current cell
        }

        currCell = &mouse.getMousePosition();
    }

    // Restore original position and direction
    mouse.setMousePosition(origCell);
    mouse.setMouseDirection(origDir);

    return pathString.str();
}