#ifndef CONSTANTS_H
#define CONSTANTS_H

#include <string>
#include <vector>
#include <array>

namespace Constants {

    namespace MouseConstants {
        extern const std::string mouseName;
        extern const std::vector<std::array<int, 2>> possibleMouseDirections;
        extern const std::array<int, 2> startingMousePosition;
        extern const std::array<int, 2> startingMouseDirection;
    }

    namespace MazeConstants {
        extern const int numRows;
        extern const int numCols;
        extern const int goalX;
        extern const int goalY;

        /**
         * @brief Retrieves the goal cells in the maze.
         * 
         * @return A vector of arrays, each containing the x and y coordinates of a goal cell.
         */
        std::vector<std::array<int, 2>> getGoalCells();

        extern const char startCellColor;
        extern const std::string startCellText;

        extern const char goalCellColor;
        extern const std::string goalCellText;

        extern const char goalPathColor;
        extern const char returnPathColor;

        extern bool showGrid;
        extern bool showPath;
    }

}

#endif
