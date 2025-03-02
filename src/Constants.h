#ifndef CONSTANTS_H
#define CONSTANTS_H

#include <array>
#include <string>
#include <vector>

using std::array;
using std::string;
using std::vector;
namespace Constants {

namespace MouseConstants {
extern const string mouseName;
extern const vector<array<int, 2>> possibleMouseDirections;
extern const array<int, 2> startingMousePosition;
extern const array<int, 2> startingMouseDirection;
}  // namespace MouseConstants

namespace MazeConstants {
extern const int numRows;
extern const int numCols;
extern const int goalX;
extern const int goalY;

vector<array<int, 2>> getGoalCells();

extern const char startCellColor;
extern const string startCellText;

extern const char goalCellColor;
extern const string goalCellText;

extern const char goalPathColor;
extern const char returnPathColor;

extern bool showGrid;
extern bool showPath;
}  // namespace MazeConstants
}  // namespace Constants

#endif