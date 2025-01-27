#include "MouseLocal.h"
#include "../../Constants.h"
#include <iostream>
#include <sstream>
#include <cmath>
#include <algorithm>

// Constructor
MouseLocal::MouseLocal() {
    // Initialize mazeCells with numRows x numCols
    mazeCells.resize(Constants::MazeConstants::numRows, std::vector<Cell*>(Constants::MazeConstants::numCols, nullptr));
    
    // Initialize cells with their coordinates
    setUpMazeLocal();

    // Initialize mousePosition and mouseDirection from Constants
    mousePosition = { Constants::MouseConstants::startingMousePosition[0], Constants::MouseConstants::startingMousePosition[1] };
    mouseDirection = { Constants::MouseConstants::startingMouseDirection[0], Constants::MouseConstants::startingMouseDirection[1] };
}

// Sets up the maze with cells
void MouseLocal::setUpMazeLocal() {
    for(int i = 0; i < Constants::MazeConstants::numCols; ++i) { // X-direction
        for(int j = 0; j < Constants::MazeConstants::numRows; ++j) { // Y-direction
            mazeCells[j][i] = new Cell(i, j); // Assuming mazeCells[row][col], row=j, col=i
        }
    }
}

void MouseLocal::deleteMazeLocal()
{
    for(int i = 0; i < Constants::MazeConstants::numCols; ++i) { // X-direction
        for(int j = 0; j < Constants::MazeConstants::numRows; ++j) { // Y-direction
            delete mazeCells[j][i];
        }
    }
}

// Adjusts the mouse's direction based on turn steps
void MouseLocal::turnMouseLocal(int halfStepsLeft, int halfStepsRight) {
    try {
        const int numPossibleDirections = Constants::MouseConstants::possibleMouseDirections.size();
        const auto& possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;

        int currentIndex = findDirectionIndexInPossibleDirections(mouseDirection);
        int newIndex = (currentIndex + halfStepsRight - halfStepsLeft + numPossibleDirections) % numPossibleDirections;
        mouseDirection = possibleMouseDirections[newIndex];
    }
    catch(const std::invalid_argument& e) {
        std::cerr << e.what() << std::endl;
        // Handle exception as needed
    }
}

// Adjusts the mouse's direction based on a desired direction
std::array<int, 2> MouseLocal::turnMouseLocal(const std::array<int, 2>& newDirection) {
    std::array<int, 2> halfStepCount = obtainHalfStepCount(newDirection);
    int halfStepsLeft = (halfStepCount[1] == -1) ? halfStepCount[0] : 0;
    int halfStepsRight = (halfStepCount[1] == 1) ? halfStepCount[0] : 0;
    turnMouseLocal(halfStepsLeft, halfStepsRight);
    return halfStepCount;
}

// Obtains optimal number of half steps to reach a desired direction
std::array<int, 2> MouseLocal::obtainHalfStepCount(const std::array<int, 2>& newDirection) {
    const auto& possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;
    int numDirections = possibleMouseDirections.size();

    int currentIndex = findDirectionIndexInPossibleDirections(mouseDirection);
    int newDirIndex = findDirectionIndexInPossibleDirections(newDirection);

    int halfStepsRight = (newDirIndex - currentIndex + numDirections) % numDirections;
    int halfStepsLeft = (currentIndex - newDirIndex + numDirections) % numDirections;

    int halfSteps = std::min(halfStepsRight, halfStepsLeft);
    int direction = (halfStepsRight < halfStepsLeft) ? 1 : -1; // 1 for right, -1 for left

    return { halfSteps, direction };
}

// Finds the index of a given direction
int MouseLocal::findDirectionIndexInPossibleDirections(const std::array<int, 2>& direction) const {
    const auto& possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;
    for(size_t i = 0; i < possibleMouseDirections.size(); ++i) {
        if(possibleMouseDirections[i][0] == direction[0] && possibleMouseDirections[i][1] == direction[1]) {
            return i;
        }
    }
    throw std::invalid_argument("Direction not listed as a possible mouse direction.");
}

// Moves the mouse forward in the current direction
void MouseLocal::moveForwardLocal() {
    int newXPosition = mousePosition[0] + mouseDirection[0];
    int newYPosition = mousePosition[1] + mouseDirection[1];

    if(isValidCell(newXPosition, newYPosition)) {
        mousePosition = { newXPosition, newYPosition };
    }
    else {
        std::cerr << "Invalid position (ack), mouse cannot move to (" << newXPosition << "," << newYPosition << ")" << std::endl;
    }
}

// Adds a wall locally
void MouseLocal::addWallLocal(int x, int y, const std::array<int, 2>& direction) {
    int neighboringCellX = x + direction[0];
    int neighboringCellY = y + direction[1];

    if(isValidCell(neighboringCellX, neighboringCellY)) {
        mazeCells[y][x]->addWall(direction, true);
        mazeCells[neighboringCellY][neighboringCellX]->addWall({ -direction[0], -direction[1] }, true);
        // std::cerr << "Shared wall cell found :)" << std::endl;
    }
    else {
        mazeCells[y][x]->addWall(direction, false);
        // std::cerr << "Edge cell found :)" << std::endl; // FIXME: Remove later.
    }
}

// Checks if cell is valid
bool MouseLocal::isValidCell(int x, int y) const {
    return x >= 0 && x < Constants::MazeConstants::numCols && y >= 0 && y < Constants::MazeConstants::numRows;
}

// Determines movement between two cells
Movement MouseLocal::getMovement(const Cell& cell1, const Cell& cell2, bool diagonalsAllowed) {
    std::array<int, 2> direction = { cell2.getX() - cell1.getX(), cell2.getY() - cell1.getY() };
    try {
        double distance = std::sqrt(std::pow(direction[0], 2) + std::pow(direction[1], 2));
        if(distance != 1 && distance != std::sqrt(2)) {
            return Movement(false, direction);
        }

        // Cardinal direction movement
        bool canMove = !cell1.getWallExists(direction);
        Movement cardinalMovement(canMove, direction);

        return cardinalMovement;
    }
    catch(const std::invalid_argument& e) {
        Movement diagonalMovement(false, direction);
        if(!diagonalsAllowed || !cell2.getIsExplored()) {
            return diagonalMovement;
        }

        // For diagonals, check if both horizontal and vertical directions are clear
        std::array<int, 2> horizontalDirectionCheck = { direction[0], 0 };
        std::array<int, 2> verticalDirectionCheck = { 0, direction[1] };

        bool diagonalHorizontal = (!cell1.getWallExists(horizontalDirectionCheck) &&
                                    !cell2.getWallExists({ 0, -verticalDirectionCheck[1] }));
        bool diagonalVertical = (!cell1.getWallExists(verticalDirectionCheck) &&
                                  !cell2.getWallExists({ -horizontalDirectionCheck[0], 0 }));

        diagonalMovement.setCanMove(diagonalHorizontal || diagonalVertical);

        if(!diagonalMovement.getCanMove()) {
            return diagonalMovement;
        }

        // Determine if the diagonal is left or right
        bool isHorizontalRight = (findDirectionIndexInPossibleDirections(verticalDirectionCheck) <
                                   findDirectionIndexInPossibleDirections(horizontalDirectionCheck));
        bool isVerticalRight = !isHorizontalRight;

        if(!isHorizontalRight && diagonalHorizontal) {
            diagonalMovement.setIsDiagonal(true);
            diagonalMovement.setLeftOrRightDiagonal("left");
            diagonalMovement.setCellToMoveToFirst(&getCell(cell1.getX() + horizontalDirectionCheck[0],
                                                          cell1.getY()));
        }
        else if(!isVerticalRight && diagonalVertical) {
            diagonalMovement.setIsDiagonal(true);
            diagonalMovement.setLeftOrRightDiagonal("left");
            diagonalMovement.setCellToMoveToFirst(&getCell(cell1.getX(),
                                                          cell1.getY() + verticalDirectionCheck[1]));
        }
        else if(isHorizontalRight && diagonalHorizontal) {
            diagonalMovement.setIsDiagonal(true);
            diagonalMovement.setLeftOrRightDiagonal("right");
            diagonalMovement.setCellToMoveToFirst(&getCell(cell1.getX() + horizontalDirectionCheck[0],
                                                          cell1.getY()));
        }
        else if(isVerticalRight && diagonalVertical) {
            diagonalMovement.setIsDiagonal(true);
            diagonalMovement.setLeftOrRightDiagonal("right");
            diagonalMovement.setCellToMoveToFirst(&getCell(cell1.getX(),
                                                          cell1.getY() + verticalDirectionCheck[1]));
        }
        else {
            diagonalMovement.setCanMove(false);
        }

        return diagonalMovement;
    }
}

// Returns direction offset based on string
std::array<int, 2> MouseLocal::getDirectionOffset(const std::string& direction) const {
    const std::vector<std::array<int,2>>& possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;
    std::vector<std::string> possibleDirections = { "n", "ne", "e", "se", "s", "sw", "w", "nw" };
    auto it = std::find(possibleDirections.begin(), possibleDirections.end(), direction);
    if(it != possibleDirections.end()) {
        size_t index = std::distance(possibleDirections.begin(), it);
        return possibleMouseDirections[index];
    }
    else {
        throw std::invalid_argument("Invalid direction string: " + direction);
    }
}

// Returns direction as string based on direction offset
std::string MouseLocal::getDirectionAsString(const std::array<int, 2>& direction) const {
    const std::vector<std::string> possibleDirections = { "n", "ne", "e", "se", "s", "sw", "w", "nw" };
    const std::vector<std::array<int,2>>& possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;
    for(size_t i=0; i < possibleMouseDirections.size(); ++i) {
        if(possibleMouseDirections[i][0] == direction[0] && possibleMouseDirections[i][1] == direction[1]) {
            return possibleDirections[i];
        }
    }
    throw std::invalid_argument("Invalid direction offset.");
}

// Returns direction to the left
std::string MouseLocal::getDirectionToTheLeft() const {
    const std::vector<std::array<int,2>>& possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;
    size_t currentIndex = findDirectionIndexInPossibleDirections(mouseDirection);
    size_t leftIndex = (currentIndex + 6) % possibleMouseDirections.size(); // Equivalent to turning left
    std::array<int, 2> newDirection = possibleMouseDirections[leftIndex];
    return getDirectionAsString(newDirection);
}

// Returns direction to the right
std::string MouseLocal::getDirectionToTheRight() const {
    const std::vector<std::array<int,2>>& possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;
    size_t currentIndex = findDirectionIndexInPossibleDirections(mouseDirection);
    size_t rightIndex = (currentIndex + 2) % possibleMouseDirections.size(); // Equivalent to turning right
    std::array<int, 2> newDirection = possibleMouseDirections[rightIndex];
    return getDirectionAsString(newDirection);
}

// Returns mouse direction
std::array<int, 2> MouseLocal::getMouseDirection() const {
    return mouseDirection;
}

// Sets mouse direction
void MouseLocal::setMouseDirection(const std::array<int, 2>& newDirection) {
    mouseDirection = newDirection;
}

// Returns the maze cells
const std::vector<std::vector<Cell*>>& MouseLocal::getMazeCells() const {
    return mazeCells;
}

// Returns a particular cell
Cell& MouseLocal::getCell(int x, int y) {
    if(!isValidCell(x, y)) {
        throw std::out_of_range("Invalid cell coordinates.");
    }
    return *mazeCells[y][x];
}

Cell& MouseLocal::getCell(int x, int y) const {
    if(!isValidCell(x, y)) {
        throw std::out_of_range("Invalid cell coordinates.");
    }
    return *mazeCells[y][x];
}

// Returns mouse's current position cell
Cell& MouseLocal::getMousePosition() {
    return getCell(mousePosition[0], mousePosition[1]);
}

// Sets mouse's position
void MouseLocal::setMousePosition(const Cell& newMousePosition) {
    mousePosition = { newMousePosition.getX(), newMousePosition.getY() };
}

// Returns a string representation of the local maze
std::string MouseLocal::localMazeToString() const {
    std::stringstream mazeString;
    mazeString << "Maze:\n";
    int numRows = Constants::MazeConstants::numRows;
    int numCols = Constants::MazeConstants::numCols;

    // Print top boundary
    for(int i = 0; i < numCols; ++i) {
        mazeString << "+---";
    }
    mazeString << "+\n";

    for(int i = numRows - 1; i >=0; --i) {
        mazeString << printRow(i);
        mazeString << "\n";
    }

    return mazeString.str();
}

// Helper method to print a row
std::string MouseLocal::printRow(int rowNumber) const {
    std::stringstream rowString;
    rowString << "|";

    int numCols = Constants::MazeConstants::numCols;

    for(int i = 0; i < numCols; ++i) {
        const Cell& cellAnalyzed = getCell(i, rowNumber);
        if(cellAnalyzed.getWallExists({1,0})) {
            rowString << "   |";
        }
        else {
            rowString << "    ";
        }
    }
    rowString << "\n";

    for(int i = 0; i < numCols; ++i) {
        const Cell& cellAnalyzed = getCell(i, rowNumber);
        if(cellAnalyzed.getWallExists({0,-1})) {
            rowString << "+---";
        }
        else {
            rowString << "+   ";
        }
    }
    rowString << "+\n";

    return rowString.str();
}

// Get neighbors
std::vector<Cell*> MouseLocal::getNeighbors(const Cell& cell, bool diagonalsAllowed) const {
    int x = cell.getX();
    int y = cell.getY();
    std::vector<Cell*> neighbors;

    const std::vector<std::array<int, 2>>& possibleDirections = Constants::MouseConstants::possibleMouseDirections;

    for(const auto& direction : possibleDirections) {
        int newX = x + direction[0];
        int newY = y + direction[1];
        if(isValidCell(newX, newY)) {
            if(diagonalsAllowed || direction[0] == 0 || direction[1] == 0) {
                //.. neighbors.emplace_back(getCell(newX, newY));
                //.. neighbors.emplace_back(new Cell(getCell(newX, newY)));
            }
        }
    }
    return neighbors;
}

// Static method to check if two cells are the same
bool MouseLocal::isSame(const Cell& cell1, const Cell& cell2) {
    return cell1.getX() == cell2.getX() && cell1.getY() == cell2.getY();
}

// Static method to calculate Euclidean distance
double MouseLocal::euclideanDistance(const Cell& cell1, const Cell& cell2) {
    return std::sqrt(std::pow(cell1.getX() - cell2.getX(), 2) + std::pow(cell1.getY() - cell2.getY(), 2));
}

// Static method to calculate octile distance
double MouseLocal::octileDistance(const Cell& cell1, const Cell& cell2) {
    int distanceX = std::abs(cell1.getX() - cell2.getX());
    int distanceY = std::abs(cell1.getY() - cell2.getY());
    return (distanceX + distanceY) + (std::sqrt(2) - 2) * std::min(distanceX, distanceY);
}

// Resets costs of all cells
void MouseLocal::resetCosts() {
    for(int x = 0; x < Constants::MazeConstants::numCols; ++x) {
        for(int y = 0; y < Constants::MazeConstants::numRows; ++y) {
            Cell& cell = getCell(x, y);
            cell.setCostFromStart(std::numeric_limits<double>::infinity());
            cell.setTotalCost(std::numeric_limits<double>::infinity());
        }
    }
}

// Detects and sets walls using the API
void MouseLocal::detectAndSetWalls(API& api) {
    Cell& currCell = getMousePosition();
    if(api.wallFront()) {
        api.setWall(currCell.getX(), currCell.getY(), getDirectionAsString(getMouseDirection()));
    }
    if(api.wallLeft()) {
        api.setWall(currCell.getX(), currCell.getY(), getDirectionToTheLeft());
    }
    if(api.wallRight()) {
        api.setWall(currCell.getX(), currCell.getY(), getDirectionToTheRight());
    }
}

// Retrieves goal cells from Constants
std::vector<Cell*> MouseLocal::getGoalCells() const {
    std::vector<std::array<int,2>> goalPoses = Constants::MazeConstants::getGoalCells();
    std::vector<Cell*> goalCells;
    for(const auto& goalPos : goalPoses) {
        goalCells.emplace_back(&getCell(goalPos[0], goalPos[1]));
    }
    return goalCells;
}

// Checks if a cell is a goal cell
bool MouseLocal::isGoalCell(const Cell& cell, const std::vector<Cell*>& goalCells) const {
    bool isGoal = false;
    for(const auto& goal : goalCells) {
        isGoal = isGoal || isSame(cell, *goal);
    }
    return isGoal;
}

// Gets direction between two cells
std::array<int, 2> MouseLocal::getDirBetweenCells(const Cell& cell1, const Cell& cell2) {
    return { cell2.getX() - cell1.getX(), cell2.getY() - cell1.getY() };
}