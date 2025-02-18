#ifndef MOVEMENT_H
#define MOVEMENT_H

#include <string>
#include <array>
#include <stdexcept>
#include "Cell.h"

using std::array;

// Forward declaration of Cell to avoid circular dependencies
class Cell;

/**
 * @brief The Movement class represents a movement from one cell to another in the maze.
 *
 * It encapsulates whether the movement is possible, the direction of movement,
 * and additional details for diagonal movements.
 */
class Movement
{
public:
    /**
     * @brief Constructs a Movement instance indicating if movement is possible and its direction.
     *
     * @param canMove Indicates whether the movement is possible.
     * @param direction The direction of movement as an array [dx, dy].
     */
    Movement(bool canMove, const array<int, 2> &direction);

    /**
     * @brief Constructs a Movement instance with detailed movement information.
     *
     * @param canMove Indicates whether the movement is possible.
     * @param isDiagonal Indicates whether the movement is diagonal.
     * @param leftOrRightDiagonal Specifies if the diagonal movement is to the left or right.
     * @param cellToMoveToFirst Pointer to the first cell to move to in case of diagonal movement.
     * @param direction The direction of movement as an array [dx, dy].
     */
    Movement(bool canMove, bool isDiagonal, const string &leftOrRightDiagonal, Cell *cellToMoveToFirst, const array<int, 2> &direction);

    // Getter Methods

    /**
     * @brief Retrieves whether the movement is possible.
     *
     * @return True if movement is possible, false otherwise.
     */
    bool getCanMove() const;

    /**
     * @brief Retrieves whether the movement is diagonal.
     *
     * @return True if movement is diagonal, false otherwise.
     */
    bool getIsDiagonal() const;

    /**
     * @brief Retrieves whether the diagonal movement is to the left or right.
     *
     * @return A string indicating "left" or "right" for diagonal movement.
     */
    string getIsLeftRight() const;

    /**
     * @brief Retrieves the first cell to move to in case of diagonal movement.
     *
     * @return Pointer to the first cell to move to. Can be nullptr if not applicable.
     */
    Cell *getFirstMove() const;

    /**
     * @brief Retrieves the direction of movement.
     *
     * @return The direction as an array [dx, dy].
     */
    array<int, 2> getDirection() const;

    // Setter Methods

    /**
     * @brief Sets whether the movement is possible.
     *
     * @param canMove True if movement is possible, false otherwise.
     */
    void setCanMove(bool canMove);

    /**
     * @brief Sets whether the movement is diagonal.
     *
     * @param isDiagonal True if movement is diagonal, false otherwise.
     */
    void setIsDiagonal(bool isDiagonal);

    /**
     * @brief Sets whether the diagonal movement is to the left or right.
     *
     * @param leftOrRightDiagonal A string indicating "left" or "right" for diagonal movement.
     */
    void setLeftOrRightDiagonal(const string &leftOrRightDiagonal);

    /**
     * @brief Sets the first cell to move to in case of diagonal movement.
     *
     * @param cellToMoveToFirst Pointer to the first cell to move to.
     */
    void setCellToMoveToFirst(Cell *cellToMoveToFirst);

    /**
     * @brief Sets the direction of movement.
     *
     * @param direction The direction as an array [dx, dy].
     */
    void setDirection(const array<int, 2> &direction);

private:
    bool canMove;               /**< Indicates whether the movement is possible. */
    bool isDiagonal;            /**< Indicates whether the movement is diagonal. */
    string leftOrRightDiagonal; /**< Specifies if the diagonal movement is to the left or right. */
    Cell *cellToMoveToFirst;    /**< Pointer to the first cell to move to in case of diagonal movement. */
    array<int, 2> direction;    /**< Direction of movement as [dx, dy]. */
};

#endif // MOVEMENT_H