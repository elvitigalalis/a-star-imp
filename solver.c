#include "solver.h"
#include "API.h"

Action solver() {
    return leftWallFollower();
}

// This is an example of a simple left wall following algorithm.
Action leftWallFollower() {
    if(API_wallFront()) {
        if(API_wallLeft()){
            return RIGHT;
        }
        return LEFT;
    }
    return FORWARD;
}


// Put your implementation of floodfill here!
Action floodFill() {
    return IDLE;
}