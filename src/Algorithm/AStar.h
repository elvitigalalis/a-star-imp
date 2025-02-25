#ifndef ASTAR_H
#define ASTAR_H

#include <algorithm>
#include <cmath>
#include <queue>
#include <sstream>
#include <stdexcept>
#include <string>
#include <vector>
#include "../Constants.h"
#include "../Logger.h"
#include "Maze/Cell.h"
#include "Maze/MouseLocal.h"
#include "Maze/Movement.h"

using std::array;
using std::priority_queue;
using std::string;
using std::stringstream;
using std::vector;

/**
 * @brief The AStar class implements the A* pathfinding algorithm for navigating the maze.
 *
 * It provides methods to find the optimal path from the mouse's current position to a goal cell,
 * considering various movement constraints and preferences.
 */
class AStar {
   public:
	/**
     * @brief Constructs an AStar instance.
     */
	AStar();

	/**
     * @brief Finds the A* path from the mouse's current position to the goal cell.
     *
     * @param mouse Reference to the MouseLocal instance representing the mouse's state.
     * @param goalCell Reference to the Cell instance representing the goal.
     * @param diagonalsAllowed Whether diagonal movements are permitted.
     * @param avoidGoalCells Whether to avoid goal cells during pathfinding.
     * @return A vector of pointers to Cells representing the path. Returns an empty vector if no path is found.
     */
	vector<Cell*> findAStarPath(MouseLocal& mouse, Cell& goalCell, bool diagonalsAllowed, bool avoidGoalCells);

	/**
     * @brief Reconstructs the path from the starting cell to the goal cell.
     *
     * @param startingCell Reference to the starting Cell.
     * @param goalCell Reference to the goal Cell.
     * @return A vector of pointers to Cells representing the reconstructed path.
     */
	static vector<Cell*> reconstructPath(Cell& startingCell, Cell& goalCell);

	/**
     * @brief Converts the path into a string of movement commands.
     *
     * @param mouse Reference to the MouseLocal instance to update its state during path conversion.
     * @param path A vector of pointers to Cells representing the path.
     * @return A string representing the sequence of movement commands.
     */
	static string pathToString(MouseLocal& mouse, const vector<Cell*>& path);

   private:
	/**
     * @brief Comparator struct for the priority queue to order cells based on total cost.
     */
	struct CompareCell {
		bool operator()(const Cell* a, const Cell* b) const {
			return a->getTotalCost() > b->getTotalCost();  // Min-heap based on totalCost
		}
	};
};

#endif	// ASTAR_H
