#include "Movement.h"

// Constructor: Movement(bool canMove, const array<int, 2>& direction)
Movement::Movement(bool canMove, const array<int, 2> &direction)
    : canMove(canMove),
      isDiagonal(false),
      leftOrRightDiagonal(""),
      cellToMoveToFirst(nullptr),
      direction(direction) {}

// Constructor: Movement(bool canMove, bool isDiagonal, const string& leftOrRightDiagonal, Cell* cellToMoveToFirst, const array<int, 2>& direction)
Movement::Movement(bool canMove, bool isDiagonal, const string &leftOrRightDiagonal, Cell *cellToMoveToFirst, const array<int, 2> &direction)
    : canMove(canMove),
      isDiagonal(isDiagonal),
      leftOrRightDiagonal(leftOrRightDiagonal),
      cellToMoveToFirst(cellToMoveToFirst),
      direction(direction) {}

bool Movement::getCanMove() const
{
    return this->canMove;
}

bool Movement::getIsDiagonal() const
{
    return this->isDiagonal;
}

string Movement::getIsLeftRight() const
{
    return this->leftOrRightDiagonal;
}

Cell *Movement::getFirstMove() const
{
    return this->cellToMoveToFirst;
}

array<int, 2> Movement::getDirection() const
{
    return this->direction;
}

// Setter Methods

void Movement::setCanMove(bool canMove)
{
    this->canMove = canMove;
}

void Movement::setIsDiagonal(bool isDiagonal)
{
    this->isDiagonal = isDiagonal;
}

void Movement::setLeftOrRightDiagonal(const string &leftOrRightDiagonal)
{
    this->leftOrRightDiagonal = leftOrRightDiagonal;
}

void Movement::setCellToMoveToFirst(Cell *cellToMoveToFirst)
{
    this->cellToMoveToFirst = cellToMoveToFirst;
}

void Movement::setDirection(const array<int, 2> &direction)
{
    this->direction = direction;
}