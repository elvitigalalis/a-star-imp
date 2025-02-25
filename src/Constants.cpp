#include "Constants.h"

namespace Constants {

namespace MouseConstants {
const string mouseName = "Ratawoulfie";
const vector<array<int, 2>> possibleMouseDirections = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
const array<int, 2> startingMousePosition = {0, 0};
const array<int, 2> startingMouseDirection = {0, 1};
}  // namespace MouseConstants

namespace MazeConstants {
const int numRows = 16;
const int numCols = 16;
const int goalX = 8;
const int goalY = 8;

vector<array<int, 2>> getGoalCells() {
	return {{7, 7}, {7, 8}, {8, 7}, {8, 8}};
}

const char startCellColor = 'B';
const string startCellText = "Start";
const char goalCellColor = 'G';
const string goalCellText = "Goal";
const char goalPathColor = 'C';
const char returnPathColor = 'Y';

bool showGrid = true;
bool showPath = true;
}  // namespace MazeConstants
}  // namespace Constants