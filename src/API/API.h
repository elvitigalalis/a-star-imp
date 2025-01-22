#ifndef API_H
#define API_H

#include <string>

// Forward declaration of MouseLocal to avoid circular dependencies
class MouseLocal;

/**
 * @brief The API class provides methods to interact with the maze simulator
 *        and control the mouse's movements and actions.
 */
class API {
public:
    /**
     * @brief Constructs an API instance with a reference to a MouseLocal object.
     * 
     * @param mouseLocal Pointer to a MouseLocal instance.
     */
    API(MouseLocal* mouseLocal);

    /*
     ----------------------------------------------------------------
     Maze Dimension Queries
     ----------------------------------------------------------------
    */

    /**
     * @brief Retrieves the width of the maze from the simulator.
     * 
     * @return The width of the maze as an integer.
     */
    int mazeWidth();

    /**
     * @brief Retrieves the height of the maze from the simulator.
     * 
     * @return The height of the maze as an integer.
     */
    int mazeHeight();

    /*
     ----------------------------------------------------------------
     Wall Queries
     ----------------------------------------------------------------
    */

    /**
     * @brief Checks if there is a wall directly in front of the mouse.
     * 
     * @return True if a wall exists in front, false otherwise.
     */
    bool wallFront();

    /**
     * @brief Checks if there is a wall to the right of the mouse.
     * 
     * @return True if a wall exists to the right, false otherwise.
     */
    bool wallRight();

    /**
     * @brief Checks if there is a wall to the left of the mouse.
     * 
     * @return True if a wall exists to the left, false otherwise.
     */
    bool wallLeft();

    /*
     ----------------------------------------------------------------
     Mouse Movement Commands
     ----------------------------------------------------------------
    */

    /**
     * @brief Moves the mouse forward by one step.
     * 
     * @throws std::runtime_error if the simulator does not acknowledge the move.
     */
    void moveForward();

    /**
     * @brief Moves the mouse forward by a specified number of steps.
     * 
     * @param steps The number of steps to move forward.
     * @throws std::runtime_error if the simulator does not acknowledge the move.
     */
    void moveForward(int steps);

    /**
     * @brief Moves the mouse forward by half a step.
     * 
     * @throws std::runtime_error if the simulator does not acknowledge the move.
     * 
     * @note Currently, this method treats a half step as a full step.
     *       Implement actual half-step logic as needed.
     */
    void moveForwardHalf();

    /**
     * @brief Turns the mouse 90 degrees to the right.
     * 
     * @throws std::runtime_error if the simulator does not acknowledge the turn.
     */
    void turnRight();

    /**
     * @brief Turns the mouse 90 degrees to the left.
     * 
     * @throws std::runtime_error if the simulator does not acknowledge the turn.
     */
    void turnLeft();

    /**
     * @brief Turns the mouse 45 degrees to the right.
     * 
     * @throws std::runtime_error if the simulator does not acknowledge the turn.
     */
    void turnRight45();

    /**
     * @brief Turns the mouse 45 degrees to the left.
     * 
     * @throws std::runtime_error if the simulator does not acknowledge the turn.
     */
    void turnLeft45();

    /*
     ----------------------------------------------------------------
     Set / Clear Walls
     ----------------------------------------------------------------
    */

    /**
     * @brief Sets a wall at the specified location and direction.
     *        Supports both cardinal (n, e, s, w) and intercardinal (ne, se, sw, nw) directions.
     * 
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     * @param direction The direction to set the wall ("n", "e", "s", "w", "ne", "se", "sw", "nw").
     * @throws std::invalid_argument if an unexpected direction is provided.
     */
    void setWall(int x, int y, const std::string& direction);

    /**
     * @brief Clears a wall at the specified location and direction.
     *        Supports both cardinal (n, e, s, w) and intercardinal (ne, se, sw, nw) directions.
     * 
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     * @param direction The direction to clear the wall ("n", "e", "s", "w", "ne", "se", "sw", "nw").
     * @throws std::invalid_argument if an unexpected direction is provided.
     */
    void clearWall(int x, int y, const std::string& direction);

    /*
     ----------------------------------------------------------------
     Cell Color / Text
     ----------------------------------------------------------------
    */

    /**
     * @brief Sets the color of a specific cell.
     * 
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     * @param color The color to set (as a character).
     */
    void setColor(int x, int y, char color);

    /**
     * @brief Clears the color of a specific cell.
     * 
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     */
    void clearColor(int x, int y);

    /**
     * @brief Clears the color of all cells.
     */
    void clearAllColor();

    /**
     * @brief Sets the text of a specific cell.
     * 
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     * @param text The text to set in the cell.
     */
    void setText(int x, int y, const std::string& text);

    /**
     * @brief Clears the text of a specific cell.
     * 
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     */
    void clearText(int x, int y);

    /**
     * @brief Clears the text of all cells.
     */
    void clearAllText();

    /*
     ----------------------------------------------------------------
     Reset Booleans
     ----------------------------------------------------------------
    */

    /**
     * @brief Checks if the simulator has been reset.
     * 
     * @return True if the simulator was reset, false otherwise.
     */
    bool wasReset();

    /**
     * @brief Acknowledges the reset status in the simulator.
     * 
     * @throws std::runtime_error if the simulator does not acknowledge the reset.
     */
    void ackReset();

private:
    /*
     ----------------------------------------------------------------
     Internal Helper Methods
     ----------------------------------------------------------------
    */

    /**
     * @brief Sends a command to the simulator and retrieves the response.
     * 
     * @param commandUsed The command to send to the simulator.
     * @return The response from the simulator as a string.
     */
    std::string getResponse(const std::string& commandUsed);

    /**
     * @brief Sends a command to the simulator and retrieves an integer response.
     * 
     * @param commandUsed The command to send to the simulator.
     * @return The integer response from the simulator.
     * @throws std::runtime_error if the response cannot be parsed as an integer.
     */
    int getIntegerResponse(const std::string& commandUsed);

    /**
     * @brief Sends a command to the simulator and retrieves a boolean response.
     * 
     * @param commandUsed The command to send to the simulator.
     * @return True if the response is "true", false otherwise.
     */
    bool getBooleanResponse(const std::string& commandUsed);

    /**
     * @brief Sends a command to the simulator and checks for acknowledgment.
     * 
     * @param commandUsed The command to send to the simulator.
     * @return True if the response is "ack", false otherwise.
     */
    bool getAck(const std::string& commandUsed);

    /*
     ----------------------------------------------------------------
     Member Variables
     ----------------------------------------------------------------
    */

    MouseLocal* mouseLocal; /**< Pointer to the MouseLocal instance to update local state. */
};

#endif // API_H