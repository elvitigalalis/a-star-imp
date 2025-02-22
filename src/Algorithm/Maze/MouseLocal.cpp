#include "MouseLocal.h"

#define MAZE  Constants::MazeConstants
#define MOUSE Constants::MouseConstants

MouseLocal::MouseLocal()
{
    mazeCells = vector<vector<Cell *>>(MAZE::numRows, vector<Cell *>(MAZE::numCols));
    mousePosition = {MOUSE::startingMousePosition[0], MOUSE::startingMousePosition[1]};
    mouseDirection = {MOUSE::startingMouseDirection[0], MOUSE::startingMouseDirection[1]};

    /*
     * Instantiates each cell in the Maze with an x and y coordinate.
     *
     * It may be beneficial to visualize the array as the following:
     * - [column 0, column 1, column 2, ..., column 15]
     * - with each column: [row 0 elem, row 1 elem, row 2 elem, ..., row 15 elem]
     *
     * 0, 0 signifies the bottom-left cell. 15, 15 signifies the top-right cell.
     * Thus, getting element 0, 1 will return the cell above the bottom-left cell
     * according to the following array logic.
     */
    setUpMazeLocal();
}

MouseLocal::~MouseLocal() { deleteMazeLocal(); }

/**
 * Function to set up the maze with cells marked by their x, y coordinates.
 * Maze is characterized by:
 *
 * ^ 15,15
 * |
 * +y
 * 0,0 +x -->
 */
void MouseLocal::setUpMazeLocal()
{
    for (int i = 0; i < Constants::MazeConstants::numCols; ++i) { // X-direction
	for (int j = 0; j < Constants::MazeConstants::numRows; ++j) { // Y-direction
	    mazeCells[j][i] = new Cell(i, j); // Assuming mazeCells[row][col], row=j, col=i
	}
    }
}

void MouseLocal::deleteMazeLocal()
{
    for (int i = 0; i < Constants::MazeConstants::numCols; ++i) { // X-direction
	for (int j = 0; j < Constants::MazeConstants::numRows; ++j) { // Y-direction
	    delete mazeCells[j][i];
	}
    }
}

void MouseLocal::turnMouseLocal(int halfStepsLeft, int halfStepsRight)
{
    try {
	const int numPossibleDirections = Constants::MouseConstants::possibleMouseDirections.size();
	const auto &possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;

	int currentIndex = findDirectionIndexInPossibleDirections(mouseDirection);
	int newIndex = (currentIndex + halfStepsRight - halfStepsLeft + numPossibleDirections) % numPossibleDirections;
	mouseDirection = possibleMouseDirections[newIndex];
    } catch (const std::invalid_argument &e) {
	cerr << e.what() << std::endl;
    }
}

// Adjusts the mouse's direction based on a desired direction
array<int, 2> MouseLocal::turnMouseLocal(const array<int, 2> &newDirection)
{
    array<int, 2> halfStepCount = obtainHalfStepCount(newDirection);
    int halfStepsLeft = (halfStepCount[1] == -1) ? halfStepCount[0] : 0;
    int halfStepsRight = (halfStepCount[1] == 1) ? halfStepCount[0] : 0;
    turnMouseLocal(halfStepsLeft, halfStepsRight);
    return halfStepCount;
}

// Obtains optimal number of half steps to reach a desired direction
array<int, 2> MouseLocal::obtainHalfStepCount(const array<int, 2> &newDirection)
{
    const auto &possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;
    int numDirections = possibleMouseDirections.size();

    int currentIndex = findDirectionIndexInPossibleDirections(mouseDirection);
    int newDirIndex = findDirectionIndexInPossibleDirections(newDirection);

    int halfStepsRight = (newDirIndex - currentIndex + numDirections) % numDirections;
    int halfStepsLeft = (currentIndex - newDirIndex + numDirections) % numDirections;

    int halfSteps = std::min(halfStepsRight, halfStepsLeft);
    int direction = (halfStepsRight < halfStepsLeft) ? 1 : -1; // 1 for right, -1 for left

    return {halfSteps, direction};
}

// Finds the index of a given direction
int MouseLocal::findDirectionIndexInPossibleDirections(const array<int, 2> &direction) const
{
    const auto &possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;
    for (size_t i = 0; i < possibleMouseDirections.size(); i++) {
	if (possibleMouseDirections[i][0] == direction[0] && possibleMouseDirections[i][1] == direction[1]) {
	    return i;
	}
    }
    throw std::invalid_argument("Direction not listed as a possible mouse direction.");
}

// Moves the mouse forward in the current direction
void MouseLocal::moveForwardLocal()
{
    int newXPosition = mousePosition[0] + mouseDirection[0];
    int newYPosition = mousePosition[1] + mouseDirection[1];

    if (isValidCell(newXPosition, newYPosition)) {
	mousePosition = {newXPosition, newYPosition};
    } else {
	cerr << "Invalid position (ack), mouse cannot move to (" << newXPosition << "," << newYPosition << ")"
	     << std::endl;
    }
}

// Adds a wall locally
void MouseLocal::addWallLocal(int x, int y, const array<int, 2> &direction)
{
    int neighboringCellX = x + direction[0];
    int neighboringCellY = y + direction[1];

    if (isValidCell(neighboringCellX, neighboringCellY)) {
	mazeCells[y][x]->addWall(direction, true);
	mazeCells[neighboringCellY][neighboringCellX]->addWall({-direction[0], -direction[1]}, true);
	// cerr << "Shared wall cell found :)" << std::endl;
    } else {
	mazeCells[y][x]->addWall(direction, false);
	// cerr << "Edge cell found :)" << std::endl; // FIXME: Remove later.
    }
}

// Checks if cell is valid
bool MouseLocal::isValidCell(int x, int y) const
{
    return x >= 0 && x < Constants::MazeConstants::numCols && y >= 0 && y < Constants::MazeConstants::numRows;
}

// Determines movement between two cells
Movement MouseLocal::getMovement(const Cell &cell1, const Cell &cell2, bool diagonalsAllowed)
{
    array<int, 2> direction = {cell2.getX() - cell1.getX(), cell2.getY() - cell1.getY()};
    try {
	double distance = std::sqrt(std::pow(direction[0], 2) + std::pow(direction[1], 2));
	if (distance != 1 && distance != std::sqrt(2)) {
	    return Movement(false, direction);
	}

	// Cardinal direction movement
	bool canMove = !cell1.getWallExists(direction);
	Movement cardinalMovement(canMove, direction);

	return cardinalMovement;
    } catch (const std::invalid_argument &e) {
	Movement diagonalMovement(false, direction);
	if (!diagonalsAllowed || !cell2.getIsExplored()) {
	    return diagonalMovement;
	}
	// LOG_DEBUG("Current cell:" + std::to_string(cell1.getX()) + ", " + std::to_string(cell1.getY()));

	// If part of the eight cardinal directions, but not the four cardinal directions, check the following:
	bool diagonalVertical = false;
	bool diagonalHorizontal = false;
	// E.g. direction is (-1, -1) --> check new cell's north and east walls as well as current cell's south and west
	// walls. In this example, if either the new cell's north + current cell's west (upper diagonal) don't exist OR
	// the new cell's east + current cell's south (lower diagonal) don't exist, the mouse can move diagonally.
	array<int, 2> horizontalDirectionCheck = {direction[0], 0};
	array<int, 2> verticalDirectionCheck = {0, direction[1]};

	// LOG_DEBUG("Direction" + std::to_string(direction[0]) + ", " + std::to_string(direction[1]));

	// Determine if the diagonal is left or right
	diagonalHorizontal =
	    (!cell1.getWallExists(horizontalDirectionCheck) && !cell2.getWallExists({0, -verticalDirectionCheck[1]}));
	// LOG_DEBUG("Diagonal horizontal: " + std::to_string(diagonalHorizontal));
	diagonalVertical =
	    (!cell1.getWallExists(verticalDirectionCheck) && !cell2.getWallExists({-horizontalDirectionCheck[0], 0}));
	// LOG_DEBUG("Diagonal vertical: " + std::to_string(diagonalVertical));

	diagonalMovement.setCanMove(diagonalHorizontal || diagonalVertical);
	if (!diagonalMovement.getCanMove()) {
	    return diagonalMovement;
	}

	// Classifies if the horizontal diagonal is the left or right diagonal of the mouse.
	bool isHorizontalRight =
	    (findDirectionIndexInPossibleDirections(verticalDirectionCheck) <
	     findDirectionIndexInPossibleDirections(horizontalDirectionCheck));
	LOG_DEBUG(
	    "Index of vertical direction: " +
	    std::to_string(findDirectionIndexInPossibleDirections(verticalDirectionCheck)) +
	    ", Index of horizontal direction: " +
	    std::to_string(findDirectionIndexInPossibleDirections(horizontalDirectionCheck)));
	LOG_DEBUG("Is horizontal right: " + std::to_string(isHorizontalRight));
	bool isVerticalRight = !isHorizontalRight;

	if (!isHorizontalRight && diagonalHorizontal) {
	    diagonalMovement.setIsDiagonal(true);
	    diagonalMovement.setLeftOrRightDiagonal("left");
	    diagonalMovement.setCellToMoveToFirst(&getCell(cell1.getX() + horizontalDirectionCheck[0], cell1.getY()));
	    LOG_DEBUG("Diagonal movement to the left 1");
	} else if (!isVerticalRight && diagonalVertical) {
	    diagonalMovement.setIsDiagonal(true);
	    diagonalMovement.setLeftOrRightDiagonal("left");
	    diagonalMovement.setCellToMoveToFirst(&getCell(cell1.getX(), cell1.getY() + verticalDirectionCheck[1]));
	    LOG_DEBUG("Diagonal movement to the left 2");
	} else if (isHorizontalRight && diagonalHorizontal) {
	    diagonalMovement.setIsDiagonal(true);
	    diagonalMovement.setLeftOrRightDiagonal("right");
	    diagonalMovement.setCellToMoveToFirst(&getCell(cell1.getX() + horizontalDirectionCheck[0], cell1.getY()));
	    LOG_DEBUG("Diagonal movement to the right 1");
	} else if (isVerticalRight && diagonalVertical) {
	    diagonalMovement.setIsDiagonal(true);
	    diagonalMovement.setLeftOrRightDiagonal("right");
	    diagonalMovement.setCellToMoveToFirst(&getCell(cell1.getX(), cell1.getY() + verticalDirectionCheck[1]));
	    LOG_DEBUG("Diagonal movement to the right 2");
	} else {
	    diagonalMovement.setCanMove(false);
	}
	return diagonalMovement;
    }
}

// Returns direction offset based on string
array<int, 2> MouseLocal::getDirectionOffset(const string &direction) const
{
    const vector<array<int, 2>> &possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;
    vector<string> possibleDirections = {"n", "ne", "e", "se", "s", "sw", "w", "nw"};
    auto it = std::find(possibleDirections.begin(), possibleDirections.end(), direction);
    if (it != possibleDirections.end()) {
	size_t index = std::distance(possibleDirections.begin(), it);
	return possibleMouseDirections[index];
    } else {
	throw std::invalid_argument("Invalid direction string: " + direction);
    }
}

// Returns direction as string based on direction offset
string MouseLocal::getDirectionAsString(const array<int, 2> &direction) const
{
    const vector<string> possibleDirections = {"n", "ne", "e", "se", "s", "sw", "w", "nw"};
    const vector<array<int, 2>> &possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;
    for (size_t i = 0; i < possibleMouseDirections.size(); ++i) {
	if (possibleMouseDirections[i][0] == direction[0] && possibleMouseDirections[i][1] == direction[1]) {
	    return possibleDirections[i];
	}
    }
    throw std::invalid_argument("Invalid direction offset.");
}

// Returns direction to the left
string MouseLocal::getDirectionToTheLeft() const
{
    const vector<array<int, 2>> &possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;
    size_t currentIndex = findDirectionIndexInPossibleDirections(mouseDirection);
    size_t leftIndex = (currentIndex + 6) % possibleMouseDirections.size(); // Equivalent to turning left
    array<int, 2> newDirection = possibleMouseDirections[leftIndex];
    return getDirectionAsString(newDirection);
}

// Returns direction to the right
string MouseLocal::getDirectionToTheRight() const
{
    const vector<array<int, 2>> &possibleMouseDirections = Constants::MouseConstants::possibleMouseDirections;
    size_t currentIndex = findDirectionIndexInPossibleDirections(mouseDirection);
    size_t rightIndex = (currentIndex + 2) % possibleMouseDirections.size(); // Equivalent to turning right
    array<int, 2> newDirection = possibleMouseDirections[rightIndex];
    return getDirectionAsString(newDirection);
}

// Returns mouse direction
array<int, 2> MouseLocal::getMouseDirection() const { return mouseDirection; }

// Sets mouse direction
void MouseLocal::setMouseDirection(const array<int, 2> &newDirection) { mouseDirection = newDirection; }

// Returns the maze cells
const vector<vector<Cell *>> &MouseLocal::getMazeCells() const { return mazeCells; }

// Returns a particular cell
Cell &MouseLocal::getCell(int x, int y)
{
    if (!isValidCell(x, y)) {
	throw std::out_of_range("Invalid cell coordinates.");
    }
    return *mazeCells[y][x];
}

Cell &MouseLocal::getCell(int x, int y) const
{
    if (!isValidCell(x, y)) {
	throw std::out_of_range("Invalid cell coordinates.");
    }
    return *mazeCells[y][x];
}

// Returns mouse's current position cell
Cell &MouseLocal::getMousePosition() { return getCell(mousePosition[0], mousePosition[1]); }

// Sets mouse's position
void MouseLocal::setMousePosition(const Cell &newMousePosition)
{
    mousePosition = {newMousePosition.getX(), newMousePosition.getY()};
}

// Returns a string representation of the local maze
string MouseLocal::localMazeToString() const
{
    stringstream mazeString;
    mazeString << "Maze:\n";
    int numRows = Constants::MazeConstants::numRows;
    int numCols = Constants::MazeConstants::numCols;

    // Print top boundary
    for (int i = 0; i < numCols; ++i) {
	mazeString << "+---";
    }
    mazeString << "+\n";

    for (int i = numRows - 1; i >= 0; --i) {
	mazeString << printRow(i);
	mazeString << "\n";
    }

    return mazeString.str();
}

// Helper method to print a row
string MouseLocal::printRow(int rowNumber) const
{
    stringstream rowString;
    rowString << "|";

    int numCols = Constants::MazeConstants::numCols;

    for (int i = 0; i < numCols; ++i) {
	const Cell &cellAnalyzed = getCell(i, rowNumber);
	if (cellAnalyzed.getWallExists({1, 0})) {
	    rowString << "   |";
	} else {
	    rowString << "    ";
	}
    }
    rowString << "\n";

    for (int i = 0; i < numCols; ++i) {
	const Cell &cellAnalyzed = getCell(i, rowNumber);
	if (cellAnalyzed.getWallExists({0, -1})) {
	    rowString << "+---";
	} else {
	    rowString << "+   ";
	}
    }
    rowString << "+\n";

    return rowString.str();
}

// Get neighbors
vector<Cell *> MouseLocal::getNeighbors(const Cell &cell, bool diagonalsAllowed) const
{
    int x = cell.getX();
    int y = cell.getY();
    vector<Cell *> neighbors;

    const vector<array<int, 2>> &possibleDirections = Constants::MouseConstants::possibleMouseDirections;

    for (const auto &direction : possibleDirections) {
	int newX = x + direction[0];
	int newY = y + direction[1];
	if (isValidCell(newX, newY)) {
	    if (diagonalsAllowed || direction[0] == 0 || direction[1] == 0) {
		neighbors.emplace_back(&getCell(newX, newY));
	    }
	}
    }
    return neighbors;
}

// Static method to check if two cells are the same
bool MouseLocal::isSame(const Cell &cell1, const Cell &cell2)
{
    return cell1.getX() == cell2.getX() && cell1.getY() == cell2.getY();
}

// Static method to calculate Euclidean distance
double MouseLocal::euclideanDistance(const Cell &cell1, const Cell &cell2)
{
    return std::sqrt(std::pow(cell1.getX() - cell2.getX(), 2) + std::pow(cell1.getY() - cell2.getY(), 2));
}

// Static method to calculate octile distance
double MouseLocal::octileDistance(const Cell &cell1, const Cell &cell2)
{
    int distanceX = std::abs(cell1.getX() - cell2.getX());
    int distanceY = std::abs(cell1.getY() - cell2.getY());
    return (distanceX + distanceY) + (std::sqrt(2) - 2) * std::min(distanceX, distanceY);
}

// Resets costs of all cells
void MouseLocal::resetCosts()
{
    for (int x = 0; x < Constants::MazeConstants::numCols; ++x) {
	for (int y = 0; y < Constants::MazeConstants::numRows; ++y) {
	    Cell &cell = getCell(x, y);
	    cell.setCostFromStart(std::numeric_limits<double>::infinity());
	    cell.setTotalCost(std::numeric_limits<double>::infinity());
	}
    }
}

// Detects and sets walls using the API
void MouseLocal::detectAndSetWalls(API &api)
{
    Cell &currCell = getMousePosition();
    if (api.wallFront()) {
	api.setWall(currCell.getX(), currCell.getY(), getDirectionAsString(getMouseDirection()));
    }
    if (api.wallLeft()) {
	api.setWall(currCell.getX(), currCell.getY(), getDirectionToTheLeft());
    }
    if (api.wallRight()) {
	api.setWall(currCell.getX(), currCell.getY(), getDirectionToTheRight());
    }
}

// Retrieves goal cells from Constants
vector<Cell *> MouseLocal::getGoalCells() const
{
    vector<array<int, 2>> goalPoses = Constants::MazeConstants::getGoalCells();
    vector<Cell *> goalCells;
    for (const auto &goalPos : goalPoses) {
	goalCells.emplace_back(&getCell(goalPos[0], goalPos[1]));
    }
    return goalCells;
}

// Checks if a cell is a goal cell
bool MouseLocal::isGoalCell(const Cell &cell, const vector<Cell *> &goalCells) const
{
    bool isGoal = false;
    for (const auto &goal : goalCells) {
	isGoal = isGoal || isSame(cell, *goal);
    }
    return isGoal;
}

// Gets direction between two cells
array<int, 2> MouseLocal::getDirBetweenCells(const Cell &cell1, const Cell &cell2)
{
    return {cell2.getX() - cell1.getX(), cell2.getY() - cell1.getY()};
}