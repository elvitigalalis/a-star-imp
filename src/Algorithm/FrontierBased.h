#ifndef FRONTIERBASED_H
#define FRONTIERBASED_H

#include <unordered_set>
#include <unordered_map>
#include <vector>
#include <limits>
#include "Maze/Cell.h"
#include "Maze/MouseLocal.h"
#include "../API/API.h"
#include "Maze/Movement.h"
#include "../Main.h"

/**
 * @brief The FrontierBased class implements the Frontier-Based Exploration algorithm for Micromouse.
 *
 * It explores the maze by selecting frontiers (boundaries between explored and unexplored areas),
 * moving towards them, and updating the maze knowledge based on sensor data.
 */
class FrontierBased
{
public:
    /**
     * @brief Explores the maze using the Frontier-Based algorithm.
     *
     * @param mouse Reference to the MouseLocal instance representing the mouse's state.
     * @param api Reference to the API instance for interacting with the maze display.
     * @param diagonalsAllowed Whether diagonal movements are permitted.
     */
    void explore(MouseLocal &mouse, API &api, bool diagonalsAllowed);

private:
    /**
     * @brief Picks the closest frontier using BFS to find distances from current position.
     *
     * @param mouse Reference to the MouseLocal instance.
     * @param frontiers Reference to the set of frontiers.
     * @param diagonalsAllowed Whether diagonal movements are permitted.
     * @return Cell* Pointer to the closest frontier cell. Returns nullptr if no frontier is found.
     */
    Cell *pickNextFrontier(MouseLocal &mouse, std::unordered_set<Cell *> &frontiers, bool diagonalsAllowed);

    /**
     * @brief Performs BFS to calculate distances from the start cell to all reachable cells.
     *
     * @param mouse Reference to the MouseLocal instance.
     * @param startCell Pointer to the starting Cell.
     * @param diagonalsAllowed Whether diagonal movements are permitted.
     * @return std::unordered_map<Cell*, double> Map of cells to their distance from the start cell.
     */
    std::unordered_map<Cell *, double> getBFSDist(MouseLocal &mouse, Cell *startCell, bool diagonalsAllowed);
};

#endif // FRONTIERBASED_H