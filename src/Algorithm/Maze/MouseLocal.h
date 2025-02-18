#ifndef MOUSELOCAL_H
#define MOUSELOCAL_H

#include <vector>
#include <array>
#include <string>
#include <stdexcept>
#include <iostream>
#include <sstream>
#include <cmath>
#include <algorithm>
#include "Cell.h"
#include "../../API/API.h"
#include "Movement.h"
#include "../../Constants.h"
#include "../../Logger.h"

using std::array;
using std::string;
using std::stringstream;
using std::vector;

/**
 * @brief The MouseLocal class represents the mouse's local state within the maze.
 *
 * It keeps track of the mouse's position, direction, and the local maze state.
 */
class MouseLocal
{
public:
    /**
     * @brief Constructs a MouseLocal instance and initializes the maze.
     */
    MouseLocal();

    ~MouseLocal();

    void deleteMazeLocal();

    /**
     * @brief Adjusts the mouse's locally held direction based on the turn performed.
     *
     * @param halfStepsLeft Number of 45-degree turns to the left.
     * @param halfStepsRight Number of 45-degree turns to the right.
     */
    void turnMouseLocal(int halfStepsLeft, int halfStepsRight);

    /**
     * @brief Adjusts the mouse's locally held direction based on a desired direction.
     *
     * @param newDirection The new direction to turn the mouse to.
     * @return An array containing the number of half steps and the direction to turn (left or right).
     */
    array<int, 2> turnMouseLocal(const array<int, 2> &newDirection);

    /**
     * @brief Obtains the optimal number of half steps (left/right) to reach a desired direction.
     *
     * @param newDirection The desired direction to turn the mouse to.
     * @return An array containing the number of half steps and the direction to turn (left or right).
     */
    array<int, 2> obtainHalfStepCount(const array<int, 2> &newDirection);

    /**
     * @brief Finds the index of a given direction in the array of possible mouse directions.
     *
     * @param direction The direction to find.
     * @return The index of the direction in the possibleMouseDirection array.
     * @throws std::invalid_argument if the direction is not listed as a possible direction.
     */
    int findDirectionIndexInPossibleDirections(const array<int, 2> &direction) const;

    /**
     * @brief Moves the mouse forward in the direction it is currently facing.
     */
    void moveForwardLocal();

    /**
     * @brief Adds a wall to the local maze based on a direction.
     *
     * @param x The x-position of the cell to add a wall to.
     * @param y The y-position of the cell to add a wall to.
     * @param direction The direction to add the wall to as an array [dx, dy].
     */
    void addWallLocal(int x, int y, const array<int, 2> &direction);

    /**
     * @brief Checks if the specified cell is within the bounds of the maze.
     *
     * @param x The x-position of the cell.
     * @param y The y-position of the cell.
     * @return True if the cell is valid, false otherwise.
     */
    bool isValidCell(int x, int y) const;

    /**
     * @brief Determines if the mouse can move between two cells, considering walls and diagonals.
     *
     * @param cell1 The cell the mouse is moving from.
     * @param cell2 The cell the mouse is moving to.
     * @param diagonalsAllowed Whether diagonal movements are allowed.
     * @return A Movement object representing the possible movement.
     */
    Movement getMovement(const Cell &cell1, const Cell &cell2, bool diagonalsAllowed);

    /**
     * @brief Returns the direction offsets based on a given direction string.
     *
     * @param direction The direction string (n, ne, e, se, s, sw, w, nw).
     * @return The direction offsets as an array [dx, dy].
     */
    array<int, 2> getDirectionOffset(const string &direction) const;

    /**
     * @brief Returns the direction as a string based on a given direction offset.
     *
     * @param direction The direction offset as an array [dx, dy].
     * @return The direction string (n, ne, e, se, s, sw, w, nw).
     */
    string getDirectionAsString(const array<int, 2> &direction) const;

    /**
     * @brief Returns the direction to the left of the current mouse's direction.
     *
     * @return The direction string to the left (n, ne, e, se, s, sw, w, nw).
     */
    string getDirectionToTheLeft() const;

    /**
     * @brief Returns the direction to the right of the current mouse's direction.
     *
     * @return The direction string to the right (n, ne, e, se, s, sw, w, nw).
     */
    string getDirectionToTheRight() const;

    /**
     * @brief Returns the mouse's current direction as an offset.
     *
     * @return The mouse's current direction as an array [dx, dy].
     */
    array<int, 2> getMouseDirection() const;

    /**
     * @brief Sets the mouse's direction to a new direction.
     *
     * @param newDirection The new direction as an array [dx, dy].
     */
    void setMouseDirection(const array<int, 2> &newDirection);

    /**
     * @brief Returns the maze with all cells.
     *
     * @return A 2D vector representing the maze cells.
     */
    const vector<vector<Cell *>> &getMazeCells() const;

    /**
     * @brief Returns a particular cell in the maze at position (x, y).
     *
     * @param x The x-position of the cell.
     * @param y The y-position of the cell.
     * @return A reference to the cell at position (x, y).
     * @throws std::out_of_range if the cell coordinates are invalid.
     */
    Cell &getCell(int x, int y);
    Cell &getCell(int x, int y) const;

    /**
     * @brief Returns the mouse's current position in the maze.
     *
     * @return A reference to the cell where the mouse is currently located.
     */
    Cell &getMousePosition();

    /**
     * @brief Sets the mouse's current position in the maze.
     *
     * @param newMousePosition The new position as a Cell.
     */
    void setMousePosition(const Cell &newMousePosition);

    /**
     * @brief Returns a string representation of the local maze.
     *
     * @return A formatted string representing the maze.
     */
    string localMazeToString() const;

    /**
     * @brief Gets all valid neighbors in n, e, s, w directions.
     *
     * @param cell The cell to get the neighbors of.
     * @param diagonalsAllowed Whether diagonal neighbors are allowed.
     * @return A vector of neighboring cells.
     */
    vector<Cell *> getNeighbors(const Cell &cell, bool diagonalsAllowed) const;

    /**
     * @brief Checks if two cells are the same based on their coordinates.
     *
     * @param cell1 The first cell.
     * @param cell2 The second cell.
     * @return True if both cells have the same coordinates, false otherwise.
     */
    static bool isSame(const Cell &cell1, const Cell &cell2);

    /**
     * @brief Calculates the Euclidean distance between two cells.
     *
     * @param cell1 The first cell.
     * @param cell2 The second cell.
     * @return The Euclidean distance.
     */
    static double euclideanDistance(const Cell &cell1, const Cell &cell2);

    /**
     * @brief Calculates the octile distance between two cells.
     *
     * @param cell1 The first cell.
     * @param cell2 The second cell.
     * @return The octile distance.
     */
    static double octileDistance(const Cell &cell1, const Cell &cell2);

    /**
     * @brief Resets the costs (costFromStart and totalCost) of all cells in the maze.
     */
    void resetCosts();

    /**
     * @brief Detects and sets walls in front, left, and right of the mouse using the API.
     *
     * @param api A reference to the API object.
     */
    void detectAndSetWalls(API &api);

    /**
     * @brief Retrieves the goal cells from the Constants.
     *
     * @return A vector of goal cells.
     */
    vector<Cell *> getGoalCells() const;

    /**
     * @brief Checks if a cell is one of the goal cells.
     *
     * @param cell The cell to check.
     * @param goalCells The vector of goal cells.
     * @return True if the cell is a goal cell, false otherwise.
     */
    bool isGoalCell(const Cell &cell, const vector<Cell *> &goalCells) const;

    /**
     * @brief Gets the direction vector between two cells.
     *
     * @param cell1 The starting cell.
     * @param cell2 The ending cell.
     * @return An array representing the direction vector.
     */
    static array<int, 2> getDirBetweenCells(const Cell &cell1, const Cell &cell2);

private:
    /**
     * @brief Prints the walls of a row of cells in a maze.
     *
     * @param rowNumber The row number to print.
     * @return The walls of the row as a formatted string.
     */
    string printRow(int rowNumber) const;

    /**
     * @brief Sets up the maze by initializing each cell with its coordinates.
     */
    void setUpMazeLocal();

    // Member Variables
    vector<vector<Cell *>> mazeCells; /**< 2D grid representing the maze cells. */
    array<int, 2> mousePosition;      /**< Current position of the mouse [x, y]. */
    array<int, 2> mouseDirection;     /**< Current direction of the mouse [dx, dy]. */
};

#endif // MOUSELOCAL_H
