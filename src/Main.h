#ifndef MAIN_H
#define MAIN_H

#include <iostream>
#include <string>
#include <vector>
#include <thread>
#include <chrono>
#include <sstream>
#include <algorithm>

#include "API/API.h"
#include "Algorithm/AStar.h"
#include "Algorithm/FrontierBased.h"
#include "Algorithm/Maze/Cell.h"
#include "Algorithm/Maze/MouseLocal.h"
#include "Algorithm/Maze/Movement.h"

// Namespace for constants
namespace Constants {
    namespace MazeConstants {
        static const int numCols = 16;
        static const int numRows = 16;
        static const bool showGrid = true;
        static const bool showPath = true;
        static const char startCellColor = 'S';
        static const std::string startCellText = "Start";
        static const char goalCellColor = 'G';
        static const std::string goalCellText = "Goal";
        static const char goalPathColor = 'Y';
        static const char returnPathColor = 'B';
    }

    namespace MouseConstants {
        static const std::string mouseName = "DefaultMouse";
    }
}

// Forward declarations of classes
class Cell;
class MouseLocal;
class API;
class AStar;
class FrontierBased;

// Global variable declarations (defined in Main.cpp)
extern MouseLocal* mousePtr;
extern API* apiPtr;
extern AStar* aStarPtr;
extern FrontierBased* frontierBasedPtr;

// Function prototypes

/**
 * @brief Logs messages to the standard error stream.
 * 
 * @param text The message to log.
 */
void log(const std::string& text);

/**
 * @brief Sets up the maze walls, grid text/colors, etc.
 * 
 * @param startCell The starting Cell of the mouse.
 * @param goalCells A vector of goal Cells.
 */
void setUp(const Cell& startCell, const std::vector<Cell*>& goalCells);

/**
 * @brief Marks all cells in the maze as explored.
 * 
 * @param mouse Pointer to the MouseLocal instance.
 */
void setAllExplored(MouseLocal* mouse);

/**
 * @brief Determines the best path among multiple goals using the A* algorithm.
 * 
 * @param aStar Pointer to the AStar instance.
 * @param goalCells A vector of goal Cells.
 * @param diagonalsAllowed Whether diagonal movements are permitted.
 * @param avoidGoalCells Whether to avoid goal cells during pathfinding.
 * @return std::vector<Cell*> The best path as a vector of Cells.
 */
 //..
std::vector<Cell*> getBestAlgorithmPath(AStar* aStar, 
                                      std::vector<Cell*>& goalCells, 
                                      bool diagonalsAllowed,
                                      bool avoidGoalCells);

/**
 * @brief Turns the mouse from its current cell to face the next cell.
 * 
 * @param currentCell The current Cell position of the mouse.
 * @param nextCell The next Cell position to face.
 */
void turnMouseToNextCell(const Cell& currentCell, const Cell& nextCell);

/**
 * @brief Handles diagonal movements by analyzing the path and sending the correct commands to the API.
 * 
 * @param currCell The current Cell position of the mouse.
 * @param path The path as a string of movement commands.
 * @return std::string The modified path after handling diagonals.
 */
std::string diagonalizeAndRun(Cell& currCell, const std::string& path);

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
                             bool avoidGoalCells);

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
                             bool avoidGoalCells);

#endif // MAIN_H