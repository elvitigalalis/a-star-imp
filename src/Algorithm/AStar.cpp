#include "AStar.h"

AStar::AStar() {}

vector<Cell *> AStar::findAStarPath(MouseLocal &mouse, Cell &goalCell, bool diagonalsAllowed, bool avoidGoalCells)
{
    mouse.resetCosts();
    Cell &currCell = mouse.getMousePosition();

    priority_queue<Cell *, vector<Cell *>, CompareCell> discoveredCell;
    vector<vector<bool>> procCells(Constants::MazeConstants::numCols, vector<bool>(Constants::MazeConstants::numRows, false));

    /*
     * It is beneficial to visualize the mathematics in A* as follows:
     * - f(n) = g(n) + h(n)
     * - f(n) is the total cost of the cell.
     * - g(n) is the cost from the starting cell to the current cell.
     * - h(n) is the heuristic cost from the current cell to the goal cell.
     * --------------(Octile heuristic in this case)----------------------
     *
     * Essentially, the total cost is the sum of the cost from the start + heuristic
     * cost from the current cell to the goal cell.
     */

    Cell &startCell = mouse.getCell(currCell.getX(), currCell.getY());
    startCell.setCostFromStart(0.0);
    startCell.setTotalCost(MouseLocal::octileDistance(startCell, goalCell));
    discoveredCell.push(&startCell);

    // Analyzes to-be-processed cells until goal is reached.
    while (!discoveredCell.empty())
    {
        Cell *procCell = discoveredCell.top();
        discoveredCell.pop();

        if (MouseLocal::isSame(*procCell, goalCell))
        {
            return reconstructPath(currCell, goalCell);
        }
        else if (procCells[procCell->getX()][procCell->getY()])
        {
            continue;
        }
        procCells[procCell->getX()][procCell->getY()] = true; // Marks the cell as processed.

        vector<Cell *> neighbors = mouse.getNeighbors(*procCell, diagonalsAllowed);
        for (Cell *neighbor : neighbors)
        {
            if (procCells[neighbor->getX()][neighbor->getY()])
            {
                continue;
            }

            if (avoidGoalCells && mouse.isGoalCell(*neighbor, mouse.getGoalCells()))
            {
                continue;
            }

            Movement movement = mouse.getMovement(*procCell, *neighbor, diagonalsAllowed);
            if (!movement.getCanMove())
            {
                continue;
            }

            double costToNeighbor = MouseLocal::euclideanDistance(*procCell, *neighbor);
            double costFromStart = procCell->getCostFromStart() + costToNeighbor;
            if (costFromStart < neighbor->getCostFromStart())
            { // Update neighbor costs.
                neighbor->setCostFromStart(costFromStart);
                neighbor->setTotalCost(costFromStart + MouseLocal::octileDistance(*neighbor, goalCell));
                neighbor->setPrevCellInPath(procCell);
                discoveredCell.push(neighbor);
            }
        }
    }
    return vector<Cell *>(); // No path was found
}

vector<Cell *> AStar::reconstructPath(Cell &startingCell, Cell &goalCell)
{
    vector<Cell *> path;
    Cell *pointer = &goalCell;
    while (!MouseLocal::isSame(*pointer, startingCell))
    {
        path.push_back(pointer);
        pointer = pointer->getPrevCellInPath();
    }
    // Reverse to start + 1 -> goal.
    std::reverse(path.begin(), path.end());
    return path;
}

// Converts the path into a string of movement commands
string AStar::pathToString(MouseLocal &mouse, const vector<Cell *> &path)
{
    stringstream pathString;
    Cell &origCell = mouse.getMousePosition();
    array<int, 2> origDir = mouse.getMouseDirection();

    Cell *currCell = &origCell;

    for (Cell *nextCell : path)
    {
        // LOG_DEBUG("Current cell " + std::to_string(currCell->getX()) + ", " + std::to_string(currCell->getY()) + " to next cell " + std::to_string(nextCell->getX()) + ", " + std::to_string(nextCell->getY()));
        // LOG_DEBUG("Current direction: " + std::to_string(mouse.getMouseDirection()[0]) + ", " + std::to_string(mouse.getMouseDirection()[1]));
        array<int, 2> newDir = MouseLocal::getDirBetweenCells(*currCell, *nextCell);
        // LOG_DEBUG("New direction: " + std::to_string(newDir[0]) + ", " + std::to_string(newDir[1]));
        array<int, 2> turns = mouse.obtainHalfStepCount(newDir);

        // If even or odd # of turns needed.
        if (turns[0] % 2 == 0)
        {
            if (turns[1] == 1)
            {
                for (int i = 0; i < turns[0] / 2; i++)
                {
                    pathString << "R#";
                }
                mouse.turnMouseLocal(0, turns[0]);
                pathString << "F#";
                mouse.moveForwardLocal();
            }
            else
            {
                for (int i = 0; i < turns[0] / 2; i++)
                {
                    pathString << "L#";
                }
                mouse.turnMouseLocal(turns[0], 0);
                pathString << "F#";
                mouse.moveForwardLocal();
            }
        }
        else
        {
            Cell *cellToMoveToFirst = mouse.getMovement(*currCell, *nextCell, true).getFirstMove();
            // LOG_DEBUG("Intermediate cell: " + cellToMoveToFirst->toString());
            array<int, 2> neededDir = MouseLocal::getDirBetweenCells(*currCell, *cellToMoveToFirst);
            // LOG_DEBUG("Needed direction: " + std::to_string(neededDir[0]) + ", " + std::to_string(neededDir[1]));
            array<int, 2> firstTurns = mouse.obtainHalfStepCount(neededDir);
            // LOG_DEBUG("First turns: " + std::to_string(firstTurns[0]) + ", " + std::to_string(firstTurns[1]));
            for (int i = 0; i < firstTurns[0] / 2; i++)
            {
                if (firstTurns[1] == 1)
                {
                    pathString << "R#";
                }
                else
                {
                    pathString << "L#";
                }
            }
            if (firstTurns[1] == 1)
            {
                mouse.turnMouseLocal(0, firstTurns[0]);
            }
            else
            {
                mouse.turnMouseLocal(firstTurns[0], 0);
            }

            pathString << "F#";
            mouse.moveForwardLocal();

            // LOG_DEBUG("Current direction: " + std::to_string(mouse.getMouseDirection()[0]) + ", " + std::to_string(mouse.getMouseDirection()[1]));

            array<int, 2> secNeededDir = MouseLocal::getDirBetweenCells(*cellToMoveToFirst, *nextCell);
            // LOG_DEBUG("Second needed direction: " + std::to_string(secNeededDir[0]) + ", " + std::to_string(secNeededDir[1]));
            array<int, 2> secTurns = mouse.obtainHalfStepCount(secNeededDir);
            // LOG_DEBUG("Second turns: " + std::to_string(secTurns[0]) + ", " + std::to_string(secTurns[1]));
            for (int i = 0; i < secTurns[0] / 2; i++)
            {
                if (secTurns[1] == 1)
                {
                    pathString << "R#";
                }
                else
                {
                    pathString << "L#";
                }
            }
            if (secTurns[1] == 1)
            {
                mouse.turnMouseLocal(0, secTurns[0]);
            }
            else
            {
                mouse.turnMouseLocal(secTurns[0], 0);
            }
            // LOG_DEBUG("Current direction: " + std::to_string(mouse.getMouseDirection()[0]) + ", " + std::to_string(mouse.getMouseDirection()[1]));
            pathString << "F#";
            mouse.moveForwardLocal();
        }
        currCell = &mouse.getMousePosition();
    }

    mouse.setMousePosition(origCell);
    mouse.setMouseDirection(origDir);
    return pathString.str();
}