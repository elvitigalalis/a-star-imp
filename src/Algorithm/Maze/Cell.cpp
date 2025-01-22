#include "Cell.h"
#include <iostream>
#include <sstream>
#include <limits>

// Constructor implementation
Cell::Cell(int x, int y)
    : x(x),
      y(y),
      costFromStart(std::numeric_limits<double>::infinity()),
      totalCost(std::numeric_limits<double>::infinity()),
      isExplored(false),
      tremauxCount(0),
      prevCellInPath(nullptr) {}

// Method to add a wall based on direction
void Cell::addWall(const std::array<int, 2>& direction, bool isShared) {
    // Check east/west walls
    if (direction[0] == 1) {
        eastWall.setExists(isShared);
    } else if (direction[0] == -1) {
        westWall.setExists(isShared);
    }

    // Check north/south walls
    if (direction[1] == 1) {
        northWall.setExists(isShared);
    } else if (direction[1] == -1) {
        southWall.setExists(isShared);
    }
}

// Getters and Setters
int Cell::getX() const {
    return x;
}

int Cell::getY() const {
    return y;
}

bool Cell::getIsExplored() const {
    return isExplored;
}

void Cell::setIsExplored(bool isExplored) {
    this->isExplored = isExplored;
}

int Cell::getTremauxCount() const {
    return tremauxCount;
}

void Cell::setTremauxCount(int tremauxCount) {
    this->tremauxCount = tremauxCount;
}

void Cell::incrementTremauxCount() {
    tremauxCount++;
}

// Wall existence checks
bool Cell::getWallExists(const std::array<int, 2>& direction) const {
    if (direction == std::array<int, 2>{0, 1}) {
        return getNorthWallExists();
    } else if (direction == std::array<int, 2>{1, 0}) {
        return getEastWallExists();
    } else if (direction == std::array<int, 2>{0, -1}) {
        return getSouthWallExists();
    } else if (direction == std::array<int, 2>{-1, 0}) {
        return getWestWallExists();
    }
    throw std::invalid_argument("Direction not in four cardinal directions.");
}

bool Cell::getNorthWallExists() const {
    return northWall.getExists();
}

bool Cell::getEastWallExists() const {
    return eastWall.getExists();
}

bool Cell::getSouthWallExists() const {
    return southWall.getExists();
}

bool Cell::getWestWallExists() const {
    return westWall.getExists();
}

// Wall shared status checks
bool Cell::getNorthWallIsShared() const {
    return northWall.getIsShared();
}

bool Cell::getEastWallIsShared() const {
    return eastWall.getIsShared();
}

bool Cell::getSouthWallIsShared() const {
    return southWall.getIsShared();
}

bool Cell::getWestWallIsShared() const {
    return westWall.getIsShared();
}

// Previous cell in path
Cell* Cell::getPrevCellInPath() const {
    return prevCellInPath;
}

void Cell::setPrevCellInPath(Cell* prevCellInPath) {
    this->prevCellInPath = prevCellInPath;
}

// Cost-related methods
void Cell::setCostFromStart(double costFromStart) {
    this->costFromStart = costFromStart;
}

void Cell::setTotalCost(double totalCost) {
    this->totalCost = totalCost;
}

double Cell::getCostFromStart() const {
    return costFromStart;
}

double Cell::getTotalCost() const {
    return totalCost;
}

// String representation
std::string Cell::toString() const {
    return "Cell(" + std::to_string(x) + "," + std::to_string(y) + ") [N=" + northWall.toString()
        + ", E=" + eastWall.toString() + ", S=" + southWall.toString()
        + ", W=" + westWall.toString() + "]";
}

// Implementation of Wall class methods
// Constructor
Cell::Wall::Wall()
    : exists(false), isShared(false) {}

// Getter for exists
bool Cell::Wall::getExists() const {
    return exists;
}

// Setter for exists and isShared
void Cell::Wall::setExists(bool isShared) {
    exists = true;
    this->isShared = isShared;
}

// Getter for isShared
bool Cell::Wall::getIsShared() const {
    return isShared;
}

// String representation of Wall
std::string Cell::Wall::toString() const {
    return exists ? "1" : "0";
}

#ifdef CELL_TEST
// Main function for testing
int main() {
    Cell cell(0, 0);
    std::cout << cell.toString() << std::endl;

    cell.addWall({1, 0}, true);    // East
    cell.addWall({0, 1}, false);   // North
    cell.addWall({-1, -1}, true);  // South-West (Note: Only cardinal directions are handled)

    std::cout << cell.toString() << std::endl;

    return 0;
}
#endif
