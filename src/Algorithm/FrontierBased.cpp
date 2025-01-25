#include "FrontierBased.h"
#include "../Main.h" // Ensure Main.h defines the Main class or namespace
#include <limits>

/**
 * @brief Explores the maze using the Frontier-Based algorithm.
 * 
 * @param mouse Reference to the MouseLocal instance representing the mouse's state.
 * @param api Reference to the API instance for interacting with the maze display.
 * @param diagonalsAllowed Whether diagonal movements are permitted.
 */
void FrontierBased::explore(MouseLocal& mouse, API& api, bool diagonalsAllowed) {
    std::unordered_set<Cell*> frontiers;
    Cell* start = &mouse.getMousePosition();
    frontiers.insert(start);
    start->setIsExplored(true);
    Cell* currCell = start;

    while (!frontiers.empty()) {     
        // 1) Pick the closest frontier.
        Cell* nextFrontier = pickNextFrontier(mouse, frontiers, diagonalsAllowed);
        if (nextFrontier == nullptr) {
            break;
        }

        bool moved = traversePathIteratively(&mouse, *nextFrontier, diagonalsAllowed, false, true);
        if (!moved) {
            api.setText(nextFrontier->getX(), nextFrontier->getY(), "");
            frontiers.erase(nextFrontier);
            continue;
        }

        // 3) We’ve physically arrived: detect walls, mark as explored.
        currCell = &mouse.getMousePosition();
        mouse.detectAndSetWalls(api);
        currCell->setIsExplored(true);
        api.setText(currCell->getX(), currCell->getY(), "");
        frontiers.erase(currCell);
        // Debugging statement (optional)
        // std::cerr << "[NEIGHBOR] Neighbors:" << ...; 

        // 4) Add valid neighbors, ignoring avoided goals.
        std::vector<Cell*> neighbors = mouse.getNeighbors(*currCell, diagonalsAllowed);
        for (Cell* neighbor : neighbors) {
            if (!neighbor->getIsExplored() &&
                mouse.getMovement(*currCell, *neighbor, diagonalsAllowed).getCanMove() &&
                !mouse.isGoalCell(*neighbor, mouse.getGoalCells())) 
            {
                api.setText(neighbor->getX(), neighbor->getY(), "*");
                frontiers.insert(neighbor);
            }
        }
    }

    // 5) Finally, visit each avoided goal cell (if reachable).
    std::vector<Cell*> goalCells = mouse.getGoalCells();
    traversePathIteratively(&mouse, goalCells, diagonalsAllowed, false, false);
}

/**
 * @brief Picks the closest frontier using BFS to find distances from current position.
 * 
 * @param mouse Reference to the MouseLocal instance.
 * @param frontiers Reference to the set of frontiers.
 * @param diagonalsAllowed Whether diagonal movements are permitted.
 * @return Cell* Pointer to the closest frontier cell. Returns nullptr if no frontier is found.
 */
Cell* FrontierBased::pickNextFrontier(MouseLocal& mouse, std::unordered_set<Cell*>& frontiers, bool diagonalsAllowed) {
    Cell* currCell = &mouse.getMousePosition();
    Cell* bestCell = nullptr;
    double bestDist = std::numeric_limits<double>::infinity();

    // Get BFS distances from current cell to all reachable cells
    std::unordered_map<Cell*, double> distMap = getBFSDist(mouse, currCell, diagonalsAllowed);

    for (Cell* frontier : frontiers) {
        auto it = distMap.find(frontier);
        if (it != distMap.end()) {
            double dist = it->second;
            if (dist < bestDist) {
                bestDist = dist;
                bestCell = frontier;
            }
        }
    }
    return bestCell;
}

/**
 * @brief Performs BFS to calculate distances from the start cell to all reachable cells.
 * 
 * @param mouse Reference to the MouseLocal instance.
 * @param startCell Pointer to the starting Cell.
 * @param diagonalsAllowed Whether diagonal movements are permitted.
 * @return std::unordered_map<Cell*, double> Map of cells to their distance from the start cell.
 */
std::unordered_map<Cell*, double> FrontierBased::getBFSDist(MouseLocal& mouse, Cell* startCell, bool diagonalsAllowed) {
    std::queue<Cell*> queue;
    std::unordered_map<Cell*, double> distMap;

    distMap[startCell] = 0.0;
    queue.push(startCell);

    while (!queue.empty()) {
        Cell* currCell = queue.front();
        queue.pop();
        double currDist = distMap[currCell];

        std::vector<Cell*> neighbors = mouse.getNeighbors(*currCell, diagonalsAllowed);
        for (Cell* neighbor : neighbors) {
            if (distMap.find(neighbor) == distMap.end() &&
                mouse.getMovement(*currCell, *neighbor, diagonalsAllowed).getCanMove()) 
            {
                distMap[neighbor] = currDist + MouseLocal::euclideanDistance(*currCell, *neighbor);
                queue.push(neighbor);
            }
        }
    }
    return distMap;
}
