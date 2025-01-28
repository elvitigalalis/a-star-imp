#include "Main.h"

// Define global variables
MouseLocal* mousePtr = nullptr;
API* apiPtr = nullptr;
AStar* aStarPtr = nullptr;
FrontierBased* frontierBasedPtr = nullptr;

/**
 * @brief Logs messages to the standard error stream.
 * 
 * @param text The message to log.
 */
void log(const std::string& text) {
    std::cerr << text << std::endl;
}

/**
 * @brief Sets up the maze walls, grid text/colors, etc.
 * 
 * @param startCell The starting Cell of the mouse.
 * @param goalCells A vector of goal Cells.
 */
void setUp(const Cell& startCell, const std::vector<Cell*>& goalCells) {
    apiPtr->clearAllColor();
    apiPtr->clearAllText();

    // Add walls to the edges of the maze
    for (int i = 0; i < Constants::MazeConstants::numCols; i++) {
        apiPtr->setWall(i, 0, "s"); // Bottom edge
        apiPtr->setWall(i, Constants::MazeConstants::numRows - 1, "n"); // Top edge
    }
    for (int j = 0; j < Constants::MazeConstants::numRows; j++) {
        apiPtr->setWall(0, j, "w"); // Left edge
        apiPtr->setWall(Constants::MazeConstants::numCols - 1, j, "e"); // Right edge
    }

    // Optional grid labels
    if (Constants::MazeConstants::showGrid) {
        for(int i = 0; i < Constants::MazeConstants::numCols; i++) {
            for(int j = 0; j < Constants::MazeConstants::numRows; j++) {
                apiPtr->setText(i, j, std::to_string(i) + "," + std::to_string(j));
            }
        }
    }

    log("[START] Ready for " + Constants::MouseConstants::mouseName + "!\n");
    apiPtr->setColor(startCell.getX(), startCell.getY(), Constants::MazeConstants::startCellColor);
    apiPtr->setText(startCell.getX(), startCell.getY(), Constants::MazeConstants::startCellText);

    // Set color and text for each goal cell
    for (const auto& goalCell : goalCells) {
        apiPtr->setColor(goalCell->getX(), goalCell->getY(), Constants::MazeConstants::goalCellColor);
        apiPtr->setText(goalCell->getX(), goalCell->getY(), Constants::MazeConstants::goalCellText);
    }
}

/**
 * @brief Marks all cells in the maze as explored.
 * 
 * @param mouse Pointer to the MouseLocal instance.
 */
void setAllExplored(MouseLocal* mouse) {
    for (int i = 0; i < Constants::MazeConstants::numCols; i++) {
        for (int j = 0; j < Constants::MazeConstants::numRows; j++) {
            mouse->getCell(i, j).setIsExplored(true);
        }
    }
}

/**
 * @brief Determines the best path among multiple goals using the A* algorithm.
 * 
 * @param aStar Pointer to the AStar instance.
 * @param goalCells A vector of goal Cells.
 * @param diagonalsAllowed Whether diagonal movements are permitted.
 * @param avoidGoalCells Whether to avoid goal cells during pathfinding.
 * @return std::vector<Cell*> The best path as a vector of Cells.
 */
std::vector<Cell*> getBestAlgorithmPath(AStar* aStar, 
                                      std::vector<Cell*>& goalCells, 
                                      bool diagonalsAllowed,
                                      bool avoidGoalCells) 
{
    std::vector<Cell*> bestPath;
    double bestPathCost = std::numeric_limits<double>::max();

    for (const auto& goal : goalCells) {
        std::vector<Cell*> path = aStar->findAStarPath(*mousePtr, *goal, diagonalsAllowed, avoidGoalCells);
        if (!path.empty()) {
            double cost = goal->getTotalCost();
            if (cost < bestPathCost) {
                bestPath.clear();
                for (const auto& cellPtr : path) {
                    bestPath.push_back(cellPtr);
                }
                bestPathCost = cost;
            }
        }
    }
    return bestPath;
}

/**
 * @brief Turns the mouse from its current cell to face the next cell.
 * 
 * @param currentCell The current Cell position of the mouse.
 * @param nextCell The next Cell position to face.
 */
void turnMouseToNextCell(const Cell& currentCell, const Cell& nextCell) {
    // Calculate direction needed based on current and next cell
    int dx = nextCell.getX() - currentCell.getX();
    int dy = nextCell.getY() - currentCell.getY();

    // Placeholder for direction calculation
    // You need to implement the logic based on your MouseLocal's orientation
    // For example, determine the number of half steps and direction to turn
    int halfStepsNeeded = 0; // Replace with actual logic
    int directionNeeded = 0; // +1 for right, -1 for left, etc. Replace with actual logic

    // Example placeholder logic
    // TODO: Implement actual direction and half-steps calculation based on MouseLocal's state
    if (dx > 0) {
        directionNeeded = 1; // Turn right
    }
    else if (dx < 0) {
        directionNeeded = -1; // Turn left
    }
    else {
        // Handle vertical movements if applicable
        if (dy > 0) {
            // Define logic for north
            directionNeeded = 1; // Example: turn right
        }
        else if (dy < 0) {
            // Define logic for south
            directionNeeded = -1; // Example: turn left
        }
    }

    // Calculate halfStepsNeeded based on your MouseLocal's orientation and direction
    // For now, setting to 0 as a placeholder
    halfStepsNeeded = 0; // Replace with actual calculation

    if (halfStepsNeeded % 2 == 0) {
        for (int i = 0; i < (halfStepsNeeded / 2); i++) {
            if (directionNeeded == 1) {
                apiPtr->turnRight();
            }
            else if (directionNeeded == -1) {
                apiPtr->turnLeft();
            }
        }
    }
    else {
        for (int i = 0; i < halfStepsNeeded; i++) {
            if (directionNeeded == 1) {
                apiPtr->turnRight45();
            }
            else if (directionNeeded == -1) {
                apiPtr->turnLeft45();
            }
        }
    }
}

/**
 * @brief Handles diagonal movements by analyzing the path and sending the correct commands to the API.
 * 
 * @param currCell The current Cell position of the mouse.
 * @param path The path as a string of movement commands.
 * @return std::string The modified path after handling diagonals.
 */
std::string diagonalizeAndRun(Cell& currCell, const std::string& path) {
    std::ostringstream newPath;
    std::vector<std::string> movements;
    
    // Split the path by '#' into tokens
    std::stringstream ss(path);
    std::string token;
    while (std::getline(ss, token, '#')) {
        if(!token.empty()) {
            movements.push_back(token);
        }
    }

    std::string lastMovement;
    int i = 0;

    while (i < static_cast<int>(movements.size()) - 3) {
        currCell = (mousePtr->getMousePosition());
        log("[DEBUG] Mouse CurrPos: (" + std::to_string(currCell.getX()) + ", " + std::to_string(currCell.getY()) + ")");

        // Check for specific movement patterns
        if (movements[i] == "F" && (static_cast<int>(movements.size()) - i) > 4) {
            i++;
            std::string tempBlock = movements[i] + movements[i+1] + movements[i+2] + movements[i+3];
            if (tempBlock == "RFRF" || tempBlock == "LFLF" || tempBlock == "RFLF" || tempBlock == "LFRF") {
                log("[DEBUG] Temp Block: " + tempBlock);
                if (lastMovement != "RFRF" && lastMovement != "LFLF" && lastMovement != "RFLF" && lastMovement != "LFRF") {
                    apiPtr->moveForwardHalf();
                    newPath << "FH#";
                    mousePtr->moveForwardLocal();
                }
                else {
                    if (lastMovement == "RFLF" || lastMovement == "LFLF") {
                        newPath << "L#F#";
                        apiPtr->turnLeft();
                        apiPtr->moveForward();
                    }
                    else if (lastMovement == "LFRF" || lastMovement == "RFRF") {
                        newPath << "R#F#";
                        apiPtr->turnRight();
                        apiPtr->moveForward();
                    }
                }
                lastMovement = "F";
            }
            else {
                i--;
            }
        }

        // Handle movement blocks
        if (i + 3 < static_cast<int>(movements.size())) {
            std::string movementsBlock = movements[i] + movements[i+1] + movements[i+2] + movements[i+3];
            log("[DEBUG] Movement Block: " + movementsBlock);
            log("[DEBUG] Last Movement: " + lastMovement);

            if (movementsBlock == "RFLF") {
                if (lastMovement == movementsBlock || lastMovement == "LFLF") {
                    newPath << "F#";
                    apiPtr->moveForward();
                }
                else if (lastMovement == "LFRF" || lastMovement == "RFRF") {
                    newPath << "R#F#";
                    apiPtr->turnRight();
                    apiPtr->moveForward();
                }
                else if (lastMovement == "F") {
                    newPath << "R45#F#";
                    apiPtr->turnRight45();
                    apiPtr->moveForward();
                }
                else {
                    newPath << "R#FH#L45#FH#";
                    apiPtr->turnRight();
                    apiPtr->moveForwardHalf();
                    apiPtr->turnLeft45();
                    apiPtr->moveForwardHalf();
                    mousePtr->moveForwardLocal();
                }
                i += 4;
                lastMovement = movementsBlock;
                continue;
            }
            else if (movementsBlock == "LFRF") {
                if (lastMovement == movementsBlock || lastMovement == "RFRF") {
                    newPath << "F#";
                    apiPtr->moveForward();
                }
                else if (lastMovement == "RFLF" || lastMovement == "LFLF") {
                    newPath << "L#F#";
                    apiPtr->turnLeft();
                    apiPtr->moveForward();
                }
                else if (lastMovement == "F") {
                    newPath << "L45#F#";
                    apiPtr->turnLeft45();
                    apiPtr->moveForward();
                }
                else {
                    newPath << "L#FH#R45#FH#";
                    apiPtr->turnLeft();
                    apiPtr->moveForwardHalf();
                    apiPtr->turnRight45();
                    apiPtr->moveForwardHalf();
                    mousePtr->moveForwardLocal();
                }
                i += 4;
                lastMovement = movementsBlock;
                continue;
            }
            else if (movementsBlock == "RFRF") {
                if (lastMovement == movementsBlock) {
                    newPath << "R#FH#R#FH#";
                    apiPtr->turnRight();
                    apiPtr->moveForwardHalf();
                    apiPtr->turnRight();
                    apiPtr->moveForwardHalf();
                    mousePtr->moveForwardLocal();
                }
                else if (lastMovement == "RFLF" || lastMovement == "LFLF") {
                    newPath << "FH#R#FH#";
                    apiPtr->moveForwardHalf();
                    apiPtr->turnRight();
                    apiPtr->moveForwardHalf();
                    mousePtr->moveForwardLocal();
                }
                else if (lastMovement == "F") {
                    newPath << "R45#FH#R#FH#";
                    apiPtr->turnRight45();
                    apiPtr->moveForwardHalf();
                    apiPtr->turnRight();
                    apiPtr->moveForwardHalf();
                    mousePtr->moveForwardLocal();
                }
                else {
                    newPath << "R#FH#R45#FH#";
                    apiPtr->turnRight();
                    apiPtr->moveForwardHalf();
                    apiPtr->turnRight45();
                    apiPtr->moveForwardHalf();
                    mousePtr->moveForwardLocal();
                }
                i += 4;
                lastMovement = movementsBlock;
                continue;
            }
            else if (movementsBlock == "LFLF") {
                if (lastMovement == movementsBlock) {
                    newPath << "L#FH#L#FH#";
                    apiPtr->turnLeft();
                    apiPtr->moveForwardHalf();
                    apiPtr->turnLeft();
                    apiPtr->moveForwardHalf();
                    mousePtr->moveForwardLocal();
                }
                else if (lastMovement == "LFRF" || lastMovement == "RFRF") {
                    newPath << "FH#L#FH#";
                    apiPtr->moveForwardHalf();
                    apiPtr->turnLeft();
                    apiPtr->moveForwardHalf();
                    mousePtr->moveForwardLocal();
                }
                else if (lastMovement == "F") {
                    newPath << "L45#FH#L#FH#";
                    apiPtr->turnLeft45();
                    apiPtr->moveForwardHalf();
                    apiPtr->turnLeft();
                    apiPtr->moveForwardHalf();
                    mousePtr->moveForwardLocal();
                }
                else {
                    newPath << "L#FH#L45#FH#";
                    apiPtr->turnLeft();
                    apiPtr->moveForwardHalf();
                    apiPtr->turnLeft45();
                    apiPtr->moveForwardHalf();
                    mousePtr->moveForwardLocal();
                }
                i += 4;
                lastMovement = movementsBlock;
                continue;
            }
            else {
                // Handle smaller blocks or default movements
                if (lastMovement == "RFLF" || lastMovement == "LFLF") {
                    if ((movements[i] + movements[i + 1] == "RF")) {
                        log("POPPPPPY");
                        newPath << "FH#R45#FH#";
                        apiPtr->moveForwardHalf();
                        apiPtr->turnRight45();
                        apiPtr->moveForwardHalf();
                        mousePtr->moveForwardLocal();
                        i += 2;
                        lastMovement = "RF";
                        continue;
                    }
                    else if ((movements[i] + movements[i + 1] == "LF")) {
                        log("POPPY");
                        newPath << "L#FH#L45#FH#";
                        apiPtr->turnLeft();
                        apiPtr->moveForwardHalf();
                        apiPtr->turnLeft45();
                        apiPtr->moveForwardHalf();
                        mousePtr->moveForwardLocal();
                        i += 2;
                        lastMovement = "LF";
                        continue;
                    }
                    else if ((i + 2 < static_cast<int>(movements.size())) && 
                             (movements[i] + movements[i + 1] + movements[i + 2] == "FLF")) {
                        log("POPPY");
                        newPath << "L45#F#L45#FH#R45#FH#";
                        apiPtr->turnLeft45();
                        apiPtr->moveForwardHalf();
                        apiPtr->moveForwardHalf();
                        apiPtr->turnLeft45();
                        apiPtr->moveForwardHalf();
                        mousePtr->moveForwardLocal();
                        apiPtr->turnLeft45();
                        apiPtr->moveForwardHalf();
                        i += 3;
                        lastMovement = "F";
                        continue;
                    }
                    newPath << "L45#FH#";
                    apiPtr->turnLeft45();
                    apiPtr->moveForwardHalf();
                }
                else if (lastMovement == "LFRF" || lastMovement == "RFRF") {
                    if ((movements[i] + movements[i + 1] == "LF")) {
                        log("POPPY");
                        newPath << "FH#L45#FH#";
                        apiPtr->moveForwardHalf();
                        apiPtr->turnLeft45();
                        apiPtr->moveForwardHalf();
                        mousePtr->moveForwardLocal();
                        i += 2;
                        lastMovement = "LF";
                        continue;
                    }
                    else if ((movements[i] + movements[i + 1] == "RF")) {
                        log("POPPPPY");
                        newPath << "R#FH#R45#FH#";
                        apiPtr->turnRight();
                        apiPtr->moveForwardHalf();
                        apiPtr->turnRight45();
                        apiPtr->moveForwardHalf();
                        mousePtr->moveForwardLocal();
                        i += 2;
                        lastMovement = "RF";
                        continue;
                    }
                    else if ((i + 2 < static_cast<int>(movements.size())) && 
                             (movements[i] + movements[i + 1] + movements[i + 2] == "FRF")) {
                        log("POPPPPY");
                        newPath << "R45#F#R45#FH#R45#FH#";
                        apiPtr->turnRight45();
                        apiPtr->moveForwardHalf();
                        apiPtr->moveForwardHalf();
                        apiPtr->turnRight45();
                        apiPtr->moveForwardHalf();
                        mousePtr->moveForwardLocal();
                        apiPtr->turnRight45();
                        apiPtr->moveForwardHalf();
                        i += 3;
                        lastMovement = "F";
                        continue;
                    }
                    newPath << "R45#FH#";
                    apiPtr->turnRight45();
                    apiPtr->moveForwardHalf();
                }

                // Execute the current movement
                newPath << movements[i] << "#";
                if (movements[i] == "F") {
                    apiPtr->moveForward();
                }
                else if (movements[i] == "L") {
                    apiPtr->turnLeft();
                }
                else if (movements[i] == "R") {
                    apiPtr->turnRight();
                }
                else {
                    log("[ERROR] Invalid Movement: " + movements[i]);
                }
                lastMovement = movements[i];
                i++;
            }
        }

        // Handle remaining movements after main loop
        if (lastMovement == "RFLF" || lastMovement == "LFLF") {
            newPath << "L45#FH#";
            apiPtr->turnLeft45();
            apiPtr->moveForwardHalf();
        }
        else if (lastMovement == "LFRF" || lastMovement == "RFRF") {
            newPath << "R45#FH#";
            apiPtr->turnRight45();
            apiPtr->moveForwardHalf();
        }

        // Process any remaining movements
        if (i < static_cast<int>(movements.size())) {
            log("Moves remaining: " + std::to_string(movements.size() - i) + " with moves being ...");
            for (int j = i; j < static_cast<int>(movements.size()); j++) {
                log("[DEBUG] Movement: " + movements[j]);
                currCell = (mousePtr->getMousePosition());
                log("[DEBUG] Mouse CurrPos: (" + std::to_string(currCell.getX()) + ", " + std::to_string(currCell.getY()) + ")");

                if (movements[j] == "F") {
                    apiPtr->moveForward();
                }
                else if (movements[j] == "L") {
                    apiPtr->turnLeft();
                }
                else if (movements[j] == "R") {
                    apiPtr->turnRight();
                }
                else {
                    log("[ERROR] Invalid Movement: " + movements[j]);
                }
            }
            currCell = (mousePtr->getMousePosition());
            log("[DEBUG] Mouse CurrPos: (" + std::to_string(currCell.getX()) + ", " + std::to_string(currCell.getY()) + ")");
        }
        else {
            currCell = (mousePtr->getMousePosition());
            log("[DEBUG] Mouse CurrPos: (" + std::to_string(currCell.getX()) + ", " + std::to_string(currCell.getY()) + ")");
        }

            return newPath.str();
        }

        return std::string();
    }

/**
 * @brief Traverses a single goal cell iteratively.
 * 
 * @param mouse Pointer to the MouseLocal instance.
 * @param goalCell The target Cell to traverse to.
 * @param diagonalsAllowed Whether diagonal movements are permitted.
 * @param allExplored Whether all cells should be marked as explored.
 * @param avoidGoalCells Whether to avoid goal cells during traversal.
 * @return true If traversal was successful.
 * @return false If traversal failed.
 */
bool traversePathIteratively(MouseLocal* mouse, 
                             Cell& goalCell, 
                             bool diagonalsAllowed,
                             bool allExplored, 
                             bool avoidGoalCells) 
{
    std::vector<Cell*> goalCells;
    goalCells.push_back(&goalCell);
    return traversePathIteratively(mouse, goalCells, diagonalsAllowed, allExplored, avoidGoalCells);
}

/**
 * @brief Traverses multiple goal cells iteratively.
 * 
 * @param mouse Pointer to the MouseLocal instance.
 * @param goalCells A vector of target Cells to traverse to.
 * @param diagonalsAllowed Whether diagonal movements are permitted.
 * @param allExplored Whether all cells should be marked as explored.
 * @param avoidGoalCells Whether to avoid goal cells during traversal.
 * @return true If traversal was successful for all goals.
 * @return false If traversal failed for any goal.
 */
bool traversePathIteratively(MouseLocal* mouse, 
                             std::vector<Cell*>& goalCells, 
                             bool diagonalsAllowed,
                             bool allExplored, 
                             bool avoidGoalCells) 
{
    Cell currCell(0, 0); // Initialize with appropriate constructor arguments
    Movement* prevMov = nullptr; // Assuming Movement is a class with pointer semantics

    if (allExplored) {
        setAllExplored(mouse);
    }

    while (true) {
        currCell = (mouse->getMousePosition());
        currCell.setIsExplored(true);

        if (mouse->isGoalCell(currCell, goalCells)) {
            // If the previous movement was diagonal, handle that.
            if (prevMov != nullptr && prevMov->getIsDiagonal()) {
                turnMouseToNextCell(*(prevMov->getFirstMove()), currCell);
                apiPtr->moveForwardHalf();
            }
            break;
        }
        prevMov = nullptr;

        // Detect walls
        mouse->detectAndSetWalls(*apiPtr); // Pass API if needed


        //std::vector<Cell*> goalCells2();
        //std::vector<Cell*> goalCells2;

        // Get path from A* or your best algorithm
        std::vector<Cell*> cellPath = getBestAlgorithmPath(aStarPtr, goalCells, diagonalsAllowed, avoidGoalCells);

        // If you want to color the path
        if (Constants::MazeConstants::showPath && allExplored 
            && !MouseLocal::isSame(*goalCells[0], (mouse->getCell(0,0)))) 
        {
            for (const auto& c : cellPath) {
                apiPtr->setColor(c->getX(), c->getY(), Constants::MazeConstants::goalPathColor);
            }
        } 
        else if (Constants::MazeConstants::showPath && allExplored && goalCells.size() == 1) {
            for (const auto& c : cellPath) {
                apiPtr->setColor(c->getX(), c->getY(), Constants::MazeConstants::returnPathColor);
            }
        }

        // Log the algorithm path
        std::string algPathStr = "";
        for (const auto& c : cellPath) {
            algPathStr += "(" + std::to_string(c->getX()) + ", " + std::to_string(c->getY()) + ") -> ";
        }
        log("[PROCESSED] Algorithm Path: " + algPathStr);

        // Convert path to string
        std::vector<Cell*> cellPathPtrs;
        for (auto& cell : cellPath) {
            cellPathPtrs.push_back(cell);
        }
        std::string path = AStar::pathToString(*mouse, cellPathPtrs);
        // log("[PROCESSED] Path: " + path); // Optional logging

        if(allExplored && diagonalsAllowed) {
            log("[PROCESSED] Path: " + path);
            path = diagonalizeAndRun(currCell, path);
            log("[PROCESSED] Diagonalized Path: " + path);
        } 
        else {
            // Execute the movement commands step-by-step
            std::stringstream ss(path);
            std::string move;
            while (std::getline(ss, move, '#')) {
                if (move == "F") {
                    apiPtr->moveForward();
                } 
                else if (move == "L") {
                    apiPtr->turnLeft();
                } 
                else if (move == "R") {
                    apiPtr->turnRight();
                } 
                else {
                    log("[ERROR] Invalid Movement: " + move);
                }

                currCell = (mouse->getMousePosition());
                log("[POS] Updated Mouse Position: (" + std::to_string(currCell.getX()) + ", " + std::to_string(currCell.getY()) + ")\n");

                if (!currCell.getIsExplored()) {
                    log("[RE-CALC] Cell is unexplored, calculating new path.");
                    break;
                }
                log("[RE-USE] Reusing ...");
            }
        }
        break; // The original Java code has "break; // FIXME" so we do the same
    }
    return true;
}

/**
 * @brief The entry point of the program.
 * 
 * @return int Exit status of the program.
 */
int main() {
    // Create the objects
    mousePtr = new MouseLocal();
    apiPtr = new API(mousePtr);
    aStarPtr = new AStar();
    frontierBasedPtr = new FrontierBased();

    // Initialize start and goal cells
    std::vector<Cell*> startCell;
    startCell.push_back(&(mousePtr->getMousePosition()));
    std::vector<Cell*> goalCells = mousePtr->getGoalCells();

    setUp(*startCell[0], goalCells);

    // Begin exploration
    frontierBasedPtr->explore(*mousePtr, *apiPtr, false);

    // Sleep 2 seconds to mimic Thread.sleep(2000)
    std::this_thread::sleep_for(std::chrono::milliseconds(2000));

    setUp((mousePtr->getMousePosition()), startCell);
    traversePathIteratively(mousePtr, startCell, false, true, false);
    std::this_thread::sleep_for(std::chrono::milliseconds(500));

    setUp(*startCell[0], goalCells);
    traversePathIteratively(mousePtr, goalCells, true, true, false);
    std::this_thread::sleep_for(std::chrono::milliseconds(2000));

    // Cleanup dynamically allocated memory


    delete frontierBasedPtr;
    delete aStarPtr;
    delete apiPtr;
    delete mousePtr;

    return 0;
}