#ifndef CELL_H
#define CELL_H

#include <string>
#include <array>
#include <stdexcept>

using std::array;
using std::string;

class Cell
{
public:
    // Constructor
    Cell(int x, int y);

    // Method to add a wall based on direction
    void addWall(const array<int, 2> &direction, bool isShared);

    // Getters and Setters
    int getX() const;
    int getY() const;
    bool getIsExplored() const;
    void setIsExplored(bool isExplored);
    int getTremauxCount() const;
    void setTremauxCount(int tremauxCount);
    void incrementTremauxCount();

    // Wall existence checks
    bool getWallExists(const array<int, 2> &direction) const;
    bool getNorthWallExists() const;
    bool getEastWallExists() const;
    bool getSouthWallExists() const;
    bool getWestWallExists() const;

    // Wall shared status checks
    bool getNorthWallIsShared() const;
    bool getEastWallIsShared() const;
    bool getSouthWallIsShared() const;
    bool getWestWallIsShared() const;

    // Previous cell in path
    Cell *getPrevCellInPath() const;
    void setPrevCellInPath(Cell *prevCellInPath);

    // Cost-related methods
    void setCostFromStart(double costFromStart);
    void setTotalCost(double totalCost);
    double getCostFromStart() const;
    double getTotalCost() const;

    // String representation
    string toString() const;

    // Nested Wall class
    class Wall
    {
    public:
        Wall();
        bool getExists() const;
        void setExists(bool isShared);
        bool getIsShared() const;
        string toString() const;

    private:
        bool exists;
        bool isShared;
    };

private:
    int x;
    int y;
    double costFromStart;
    double totalCost;
    bool isExplored;
    int tremauxCount;

    Wall northWall;
    Wall eastWall;
    Wall southWall;
    Wall westWall;
    Cell *prevCellInPath;
};

#endif // CELL_H