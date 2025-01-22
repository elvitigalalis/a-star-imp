#include "Constants.h"
#include <stdexcept>

namespace Constants {

    namespace MouseConstants {
        // Name of the mouse
        const std::string mouseName = "Ratawoulfie";

        // Possible directions the mouse can face, represented as [dx, dy]
        const std::vector<std::array<int, 2>> possibleMouseDirections = { 
            {0, 1},   // North
            {1, 1},   // North-East
            {1, 0},   // East
            {1, -1},  // South-East
            {0, -1},  // South
            {-1, -1}, // South-West
            {-1, 0},  // West
            {-1, 1}   // North-West
        };

        // Starting position of the mouse [x, y]
        const std::array<int, 2> startingMousePosition = {0, 0};

        // Starting direction of the mouse [dx, dy]
        const std::array<int, 2> startingMouseDirection = {0, 1};
    }

    namespace MazeConstants {
        // Dimensions of the maze
        const int numRows = 16;
        const int numCols = 16;

        // Central goal position
        const int goalX = 8;
        const int goalY = 8;

        /**
         * @brief Retrieves the goal cells in the maze.
         * 
         * @return A vector of arrays, each containing the x and y coordinates of a goal cell.
         */
        std::vector<std::array<int, 2>> getGoalCells() {
            std::vector<std::array<int, 2>> goalCells = {
                {7, 7},
                {7, 8},
                {8, 7},
                {8, 8}
            };
            return goalCells;
        }

        // Colors and texts for specific cells
        const char startCellColor = 'B';
        const std::string startCellText = "Start";

        const char goalCellColor = 'G';
        const std::string goalCellText = "Goal";

        const char goalPathColor = 'C';
        const char returnPathColor = 'Y';

        // Visualization flags
        bool showGrid = true;
        bool showPath = true;
    }

} // namespace Constants