#include "API.h"
#include "../Algorithm/Maze/MouseLocal.h"

// Constructor
API::API(MouseLocal *mouseLocal) : mouseLocal(mouseLocal) {}

// Internal helper methods for reading from simulator

/**
 * Sends a command to the simulator and retrieves the response.
 *
 * @param commandUsed The command to send to the simulator.
 * @return The response from the simulator as a string.
 */
string API::getResponse(const string &commandUsed)
{
    std::cout << commandUsed << std::endl; // Send command to simulator
    string response;

    std::getline(std::cin, response); // Read full line response
    return response;
}

/**
 * Sends a command to the simulator and retrieves an integer response.
 *
 * @param commandUsed The command to send to the simulator.
 * @return The integer response from the simulator.
 */
int API::getIntegerResponse(const string &commandUsed)
{
    string response = getResponse(commandUsed);
    try
    {
        return std::stoi(response);
    }
    catch (const std::invalid_argument &e)
    {
        throw std::runtime_error("Invalid integer response for command: " + commandUsed);
    }
    catch (const std::out_of_range &e)
    {
        throw std::runtime_error("Integer response out of range for command: " + commandUsed);
    }
}

/**
 * Sends a command to the simulator and retrieves a boolean response.
 *
 * @param commandUsed The command to send to the simulator.
 * @return The boolean response from the simulator.
 */
bool API::getBooleanResponse(const string &commandUsed)
{
    string response = getResponse(commandUsed);
    return response == "true";
}

/**
 * Sends a command to the simulator and checks for acknowledgment.
 *
 * @param commandUsed The command to send to the simulator.
 * @return True if acknowledgment received, false otherwise.
 */
bool API::getAck(const string &commandUsed)
{
    string response = getResponse(commandUsed);
    return response == "ack";
}

// Maze dimension queries

/**
 * Retrieves the width of the maze from the simulator.
 *
 * @return The width of the maze as an integer.
 */
int API::mazeWidth()
{
    return getIntegerResponse("mazeWidth");
}

/**
 * Retrieves the height of the maze from the simulator.
 *
 * @return The height of the maze as an integer.
 */
int API::mazeHeight()
{
    return getIntegerResponse("mazeHeight");
}

// Wall queries

/**
 * Checks if there is a wall in front of the mouse.
 *
 * @return True if a wall exists in front, false otherwise.
 */
bool API::wallFront()
{
    return getBooleanResponse("wallFront");
}

/**
 * Checks if there is a wall to the right of the mouse.
 *
 * @return True if a wall exists to the right, false otherwise.
 */
bool API::wallRight()
{
    return getBooleanResponse("wallRight");
}

/**
 * Checks if there is a wall to the left of the mouse.
 *
 * @return True if a wall exists to the left, false otherwise.
 */
bool API::wallLeft()
{
    return getBooleanResponse("wallLeft");
}

// Mouse movement commands

/**
 * Moves the mouse forward by one step.
 *
 * @throws std::runtime_error if the simulator does not acknowledge the move.
 */
void API::moveForward()
{
    bool ack = getAck("moveForward");

    if (ack)
    {
        mouseLocal->moveForwardLocal();
    }
    else
    {
        cerr << mouseLocal->localMazeToString();
        throw std::runtime_error("Cannot move forward");
    }
}

/**
 * Moves the mouse forward by a specified number of steps.
 *
 * @param steps The number of steps to move forward.
 * @throws std::runtime_error if the simulator does not acknowledge the move.
 */
void API::moveForward(int steps)
{
    ostringstream commandStream;
    commandStream << "moveForward " << steps;
    string command = commandStream.str();

    bool ack = getAck(command);

    if (ack)
    {
        for (int i = 0; i < steps; ++i)
        {
            mouseLocal->moveForwardLocal();
        }
    }
    else
    {
        cerr << mouseLocal->localMazeToString();
        throw std::runtime_error("Cannot move forward " + std::to_string(steps) + " steps");
    }
}

/**
 * Moves the mouse forward by half a step.
 *
 * @throws std::runtime_error if the simulator does not acknowledge the move.
 */
void API::moveForwardHalf()
{
    bool ack = getAck("moveForwardHalf");

    if (ack)
    {
        // FIXME: Implement half movement logic if applicable
        // For now, we assume a half step is equivalent to a full step
        // mouseLocal->moveForwardLocal();
    }
    else
    {
        cerr << mouseLocal->localMazeToString();
        throw std::runtime_error("Cannot move forward half");
    }
}

/**
 * Turns the mouse to the right.
 *
 * @throws std::runtime_error if the simulator does not acknowledge the turn.
 */
void API::turnRight()
{
    bool ack = getAck("turnRight");
    if (ack)
    {
        mouseLocal->turnMouseLocal(0, 2);
    }
    else
    {
        cerr << mouseLocal->localMazeToString();
        throw std::runtime_error("Cannot turn right");
    }
}

/**
 * Turns the mouse to the left.
 *
 * @throws std::runtime_error if the simulator does not acknowledge the turn.
 */
void API::turnLeft()
{
    bool ack = getAck("turnLeft");
    if (ack)
    {
        mouseLocal->turnMouseLocal(2, 0);
    }
    else
    {
        cerr << mouseLocal->localMazeToString();
        throw std::runtime_error("Cannot turn left");
    }
}

/**
 * Turns the mouse 45 degrees to the right.
 *
 * @throws std::runtime_error if the simulator does not acknowledge the turn.
 */
void API::turnRight45()
{
    bool ack = getAck("turnRight45");
    if (ack)
    {
        mouseLocal->turnMouseLocal(0, 1);
    }
    else
    {
        cerr << mouseLocal->localMazeToString();
        throw std::runtime_error("Cannot turn right 45 degrees");
    }
}

/**
 * Turns the mouse 45 degrees to the left.
 *
 * @throws std::runtime_error if the simulator does not acknowledge the turn.
 */
void API::turnLeft45()
{
    bool ack = getAck("turnLeft45");
    if (ack)
    {
        mouseLocal->turnMouseLocal(1, 0);
    }
    else
    {
        cerr << mouseLocal->localMazeToString();
        throw std::runtime_error("Cannot turn left 45 degrees");
    }
}

// Set / clear walls

/**
 * Sets a wall at the specified location and direction.
 * Supports both cardinal (n, e, s, w) and intercardinal (ne, se, sw, nw) directions.
 *
 * @param x The x-coordinate of the cell.
 * @param y The y-coordinate of the cell.
 * @param direction The direction to set the wall ("n", "e", "s", "w", "ne", "se", "sw", "nw").
 * @throws std::invalid_argument if an unexpected direction is provided.
 */
void API::setWall(int x, int y, const string &direction)
{
    if (direction == "n" || direction == "e" || direction == "s" || direction == "w")
    {
        std::cout << "setWall " << x << " " << y << " " << direction << std::endl;
        mouseLocal->addWallLocal(x, y, mouseLocal->getDirectionOffset(direction));
    }
    else if (direction == "ne" || direction == "se" || direction == "sw" || direction == "nw")
    {
        std::cout << "setWall " << x << " " << y << " " << direction[0] << std::endl;
        std::cout << "setWall " << x << " " << y << " " << direction[1] << std::endl;
        mouseLocal->addWallLocal(x, y, mouseLocal->getDirectionOffset(direction));
    }
    else
    {
        throw std::invalid_argument("Unexpected direction: " + direction);
    }
}

/**
 * Clears a wall at the specified location and direction.
 *
 * @param x The x-coordinate of the cell.
 * @param y The y-coordinate of the cell.
 * @param direction The direction to clear the wall ("n", "e", "s", "w", "ne", "se", "sw", "nw").
 * @throws std::invalid_argument if an unexpected direction is provided.
 */
void API::clearWall(int x, int y, const string &direction)
{
    if (direction == "n" || direction == "e" || direction == "s" || direction == "w" ||
        direction == "ne" || direction == "se" || direction == "sw" || direction == "nw")
    {
        std::cout << "clearWall " << x << " " << y << " " << direction << std::endl;
    }
    else
    {
        throw std::invalid_argument("Unexpected direction: " + direction);
    }
}

// Cell color / text

/**
 * Sets the color of a specific cell.
 *
 * @param x The x-coordinate of the cell.
 * @param y The y-coordinate of the cell.
 * @param color The color to set (as a character).
 */
void API::setColor(int x, int y, char color)
{
    std::cout << "setColor " << x << " " << y << " " << color << std::endl;
}

/**
 * Clears the color of a specific cell.
 *
 * @param x The x-coordinate of the cell.
 * @param y The y-coordinate of the cell.
 */
void API::clearColor(int x, int y)
{
    std::cout << "clearColor " << x << " " << y << std::endl;
}

/**
 * Clears the color of all cells.
 */
void API::clearAllColor()
{
    std::cout << "clearAllColor" << std::endl;
}

/**
 * Sets the text of a specific cell.
 *
 * @param x The x-coordinate of the cell.
 * @param y The y-coordinate of the cell.
 * @param text The text to set in the cell.
 */
void API::setText(int x, int y, const string &text)
{
    std::cout << "setText " << x << " " << y << " " << text << std::endl;
}

/**
 * Clears the text of a specific cell.
 *
 * @param x The x-coordinate of the cell.
 * @param y The y-coordinate of the cell.
 */
void API::clearText(int x, int y)
{
    std::cout << "clearText " << x << " " << y << std::endl;
}

/**
 * Clears the text of all cells.
 */
void API::clearAllText()
{
    std::cout << "clearAllText" << std::endl;
}

// Reset booleans

/**
 * Checks if the simulator has been reset.
 *
 * @return True if the simulator was reset, false otherwise.
 */
bool API::wasReset()
{
    return getBooleanResponse("wasReset");
}

/**
 * Acknowledges the reset status in the simulator.
 */
void API::ackReset()
{
    bool ack = getAck("ackReset");
    if (!ack)
    {
        cerr << "Failed to acknowledge reset." << std::endl;
        throw std::runtime_error("Cannot acknowledge reset");
    }
}
