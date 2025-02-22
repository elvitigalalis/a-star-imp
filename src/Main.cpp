#include "Main.h"

MouseLocal* mousePtr;
API* apiPtr;
AStar* aStarPtr;
FrontierBased* frontierBasedPtr;

int main() {
	mousePtr = new MouseLocal();
	apiPtr = new API(mousePtr);
	aStarPtr = new AStar();
	frontierBasedPtr = new FrontierBased();

	vector<Cell*> startCells = vector<Cell*>{&mousePtr->getMousePosition()};
	vector<Cell*> goalCells = mousePtr->getGoalCells();

	// Explore maze using frontier-based search.
	setUp(startCells, goalCells);
	frontierBasedPtr->explore(*mousePtr, *apiPtr, false);
	sleepFor(1000);

	// Travel to start cell using A*.
	setUp(startCells);
	traversePathIteratively(mousePtr, startCells, false, true, false);
	sleepFor(1000);

	// Travel to goal cells using A*.
	setUp(goalCells);
	traversePathIteratively(mousePtr, goalCells, true, true, false);
	sleepFor(1000);

	delete frontierBasedPtr;
	delete aStarPtr;
	delete apiPtr;
	delete mousePtr;
	return 0;
}

void setUp(const vector<Cell*>& goalCells) {
	setUp(vector<Cell*>{&mousePtr->getMousePosition()}, goalCells);
}

void setUp(const vector<Cell*>& startCells, const vector<Cell*>& goalCells) {
	apiPtr->clearAllColor();
	apiPtr->clearAllText();

	// Adds boundary mazes.
	for (int i = 0; i < Constants::MazeConstants::numCols; i++) {
		apiPtr->setWall(i, 0, "s");										 // Bottom edge
		apiPtr->setWall(i, Constants::MazeConstants::numRows - 1, "n");	 // Top edge
	}
	for (int j = 0; j < Constants::MazeConstants::numRows; j++) {
		apiPtr->setWall(0, j, "w");										 // Left edge
		apiPtr->setWall(Constants::MazeConstants::numCols - 1, j, "e");	 // Right edge
	}

	// Adds grid labels.
	if (Constants::MazeConstants::showGrid) {
		for (int i = 0; i < Constants::MazeConstants::numCols; i++) {
			for (int j = 0; j < Constants::MazeConstants::numRows; j++) {
				apiPtr->setText(i, j, std::to_string(i) + "," + std::to_string(j));
			}
		}
	}

	LOG_INFO("Running " + Constants::MouseConstants::mouseName + "...\n");

	// Adds color/text to start and goal cells.
	for (const auto& startCell : startCells) {
		apiPtr->setColor(startCell->getX(), startCell->getY(), Constants::MazeConstants::startCellColor);
		apiPtr->setText(startCell->getX(), startCell->getY(), Constants::MazeConstants::startCellText);
	}
	for (const auto& goalCell : goalCells) {
		apiPtr->setColor(goalCell->getX(), goalCell->getY(), Constants::MazeConstants::goalCellColor);
		apiPtr->setText(goalCell->getX(), goalCell->getY(), Constants::MazeConstants::goalCellText);
	}
}

void sleepFor(int milliseconds) {
	// Sleeps for a specified number of milliseconds.
	std::this_thread::sleep_for(std::chrono::milliseconds(milliseconds));
}

void setAllExplored(MouseLocal* mouse) {
	// Sets all cells to explored.
	for (int i = 0; i < Constants::MazeConstants::numCols; i++) {
		for (int j = 0; j < Constants::MazeConstants::numRows; j++) {
			mouse->getCell(i, j).setIsExplored(true);
		}
	}
}

vector<Cell*> getBestAlgorithmPath(AStar* aStar, vector<Cell*>& goalCells, bool diagonalsAllowed, bool avoidGoalCells) {
	vector<Cell*> bestPath;
	double bestPathCost = std::numeric_limits<double>::max();

	// Returns the least cost path to the goal cells.
	for (const auto& goal : goalCells) {
		vector<Cell*> path = aStar->findAStarPath(*mousePtr, *goal, diagonalsAllowed, avoidGoalCells);
		if (!path.empty()) {
			double cost = goal->getTotalCost();
			if (cost < bestPathCost) {
				bestPath.clear();
				for (const auto& cellPtr : path) {
					bestPath.push_back(cellPtr);
				}
				bestPathCost = cost;
			}
		}
	}
	return bestPath;
}

/**
 * @brief Turns the mouse from its current cell to face the next cell.
 *
 * @param currentCell The current Cell position of the mouse.
 * @param nextCell The next Cell position to face.
 */
void turnMouseToNextCell(const Cell& currentCell, const Cell& nextCell) {
	array<int, 2> halfSteps = mousePtr->obtainHalfStepCount(mousePtr->getDirBetweenCells(currentCell, nextCell));

	for (int i = 0; i < halfSteps[0]; i++) {
		if (halfSteps[1] == -1) {
			apiPtr->turnLeft45();
		} else {
			apiPtr->turnRight45();
		}
	}
}

// Define the enum for movement blocks.
enum class MovementBlock { RFLF, LFRF, RFRF, LFLF, DEFAULT };

// Helper function to convert a block string into the enum.
MovementBlock parseMovementBlock(const string& block) {
	if (block == "RFLF")
		return MovementBlock::RFLF;
	if (block == "LFRF")
		return MovementBlock::LFRF;
	if (block == "RFRF")
		return MovementBlock::RFRF;
	if (block == "LFLF")
		return MovementBlock::LFLF;
	return MovementBlock::DEFAULT;
}

string diagonalizeAndRun(Cell& currCell, const string& path) {
	ostringstream newPath;
	vector<string> movements;
	string lastMovement;
	int i;

	// Split the path by '#' into tokens
	stringstream ss(path);
	string token;
	while (std::getline(ss, token, '#')) {
		if (!token.empty()) {
			movements.push_back(token);
		}
	}

	for (i = 0; i < static_cast<int>(movements.size()) - 3; i++) {
		currCell = mousePtr->getMousePosition();
		LOG_DEBUG("Mouse CurrPos: (" + std::to_string(currCell.getX()) + ", " + std::to_string(currCell.getY()) + ")");

		// Check for specific movement patterns
		if (movements[i] == "F" && (static_cast<int>(movements.size()) - i) > 4) {
			i++;
			string tempBlock = movements[i] + movements[i + 1] + movements[i + 2] + movements[i + 3];
			if (tempBlock == "RFRF" || tempBlock == "LFLF" || tempBlock == "RFLF" || tempBlock == "LFRF") {
				LOG_DEBUG(" Temp Block: " + tempBlock);
				if (lastMovement != "RFRF" && lastMovement != "LFLF" && lastMovement != "RFLF" &&
					lastMovement != "LFRF") {
					apiPtr->moveForwardHalf();
					newPath << "FH#";
					mousePtr->moveForwardLocal();
				} else {
					if (lastMovement == "RFLF" || lastMovement == "LFLF") {
						newPath << "L#F#";
						apiPtr->turnLeft();
						apiPtr->moveForward();
					} else if (lastMovement == "LFRF" || lastMovement == "RFRF") {
						newPath << "R#F#";
						apiPtr->turnRight();
						apiPtr->moveForward();
					}
				}
				lastMovement = "F";
			} else {
				i--;
			}
		}

		string movementsBlock = movements[i] + movements[i + 1] + movements[i + 2] + movements[i + 3];
		LOG_DEBUG(" Movement Block: " + movementsBlock);
		LOG_DEBUG(" Last Movement: " + lastMovement);

		// Convert the block string to an enum value.
		MovementBlock blockType = parseMovementBlock(movementsBlock);

		// Use a switch statement on the enum value.
		switch (blockType) {
			case MovementBlock::RFLF:
				if (lastMovement == movementsBlock || lastMovement == "LFLF") {
					newPath << "F#";
					apiPtr->moveForward();
				} else if (lastMovement == "LFRF" || lastMovement == "RFRF") {
					newPath << "R#F#";
					apiPtr->turnRight();
					apiPtr->moveForward();
				} else if (lastMovement == "F") {
					newPath << "R45#F#";
					apiPtr->turnRight45();
					apiPtr->moveForward();
				} else {
					newPath << "R#FH#L45#FH#";
					apiPtr->turnRight();
					apiPtr->moveForwardHalf();
					apiPtr->turnLeft45();
					apiPtr->moveForwardHalf();
					mousePtr->moveForwardLocal();
				}
				i += 3;
				lastMovement = movementsBlock;
				break;

			case MovementBlock::LFRF:
				if (lastMovement == movementsBlock || lastMovement == "RFRF") {
					newPath << "F#";
					apiPtr->moveForward();
				} else if (lastMovement == "RFLF" || lastMovement == "LFLF") {
					newPath << "L#F#";
					apiPtr->turnLeft();
					apiPtr->moveForward();
				} else if (lastMovement == "F") {
					newPath << "L45#F#";
					apiPtr->turnLeft45();
					apiPtr->moveForward();
				} else {
					newPath << "L#FH#R45#FH#";
					apiPtr->turnLeft();
					apiPtr->moveForwardHalf();
					apiPtr->turnRight45();
					apiPtr->moveForwardHalf();
					mousePtr->moveForwardLocal();
				}
				i += 3;
				lastMovement = movementsBlock;
				break;

			case MovementBlock::RFRF:
				if (lastMovement == movementsBlock) {
					newPath << "R#FH#R#FH#";
					apiPtr->turnRight();
					apiPtr->moveForwardHalf();
					apiPtr->turnRight();
					apiPtr->moveForwardHalf();
					mousePtr->moveForwardLocal();
				} else if (lastMovement == "RFLF" || lastMovement == "LFLF") {
					newPath << "FH#R#FH#";
					apiPtr->moveForwardHalf();
					apiPtr->turnRight();
					apiPtr->moveForwardHalf();
					mousePtr->moveForwardLocal();
				} else if (lastMovement == "F") {
					newPath << "R45#FH#R#FH#";
					apiPtr->turnRight45();
					apiPtr->moveForwardHalf();
					apiPtr->turnRight();
					apiPtr->moveForwardHalf();
					mousePtr->moveForwardLocal();
				} else {
					newPath << "R#FH#R45#FH#";
					apiPtr->turnRight();
					apiPtr->moveForwardHalf();
					apiPtr->turnRight45();
					apiPtr->moveForwardHalf();
					mousePtr->moveForwardLocal();
				}
				i += 3;
				lastMovement = movementsBlock;
				break;

			case MovementBlock::LFLF:
				if (lastMovement == movementsBlock) {
					newPath << "L#FH#L#FH#";
					apiPtr->turnLeft();
					apiPtr->moveForwardHalf();
					apiPtr->turnLeft();
					apiPtr->moveForwardHalf();
					mousePtr->moveForwardLocal();
				} else if (lastMovement == "LFRF" || lastMovement == "RFRF") {
					newPath << "FH#L#FH#";
					apiPtr->moveForwardHalf();
					apiPtr->turnLeft();
					apiPtr->moveForwardHalf();
					mousePtr->moveForwardLocal();
				} else if (lastMovement == "F") {
					newPath << "L45#FH#L#FH#";
					apiPtr->turnLeft45();
					apiPtr->moveForwardHalf();
					apiPtr->turnLeft();
					apiPtr->moveForwardHalf();
					mousePtr->moveForwardLocal();
				} else {
					newPath << "L#FH#L45#FH#";
					apiPtr->turnLeft();
					apiPtr->moveForwardHalf();
					apiPtr->turnLeft45();
					apiPtr->moveForwardHalf();
					mousePtr->moveForwardLocal();
				}
				i += 3;
				lastMovement = movementsBlock;
				break;

			case MovementBlock::DEFAULT:
			default:
				// Handle smaller blocks or default movements
				if (lastMovement == "RFLF" || lastMovement == "LFLF") {
					if ((movements[i] + movements[i + 1]) == "RF") {
						LOG_DEBUG("POPPPPPY");
						newPath << "FH#R45#FH#";
						apiPtr->moveForwardHalf();
						apiPtr->turnRight45();
						apiPtr->moveForwardHalf();
						mousePtr->moveForwardLocal();
						i++;
						lastMovement = "RF";
						break;
					} else if ((movements[i] + movements[i + 1]) == "LF") {
						LOG_DEBUG("POPPY");
						newPath << "L#FH#L45#FH#";
						apiPtr->turnLeft();
						apiPtr->moveForwardHalf();
						apiPtr->turnLeft45();
						apiPtr->moveForwardHalf();
						mousePtr->moveForwardLocal();
						i++;
						lastMovement = "LF";
						break;
					} else if ((i + 2 < static_cast<int>(movements.size())) &&
							   (movements[i] + movements[i + 1] + movements[i + 2] == "FLF")) {
						LOG_DEBUG("POPPY");
						newPath << "L45#F#L45#FH#R45#FH#";
						apiPtr->turnLeft45();
						apiPtr->moveForwardHalf();
						apiPtr->moveForwardHalf();
						apiPtr->turnLeft45();
						apiPtr->moveForwardHalf();
						mousePtr->moveForwardLocal();
						apiPtr->turnLeft45();
						apiPtr->moveForwardHalf();
						i += 2;
						lastMovement = "F";
						break;
					}
					newPath << "L45#FH#";
					apiPtr->turnLeft45();
					apiPtr->moveForwardHalf();
				} else if (lastMovement == "LFRF" || lastMovement == "RFRF") {
					if ((movements[i] + movements[i + 1]) == "LF") {
						LOG_DEBUG("POPPY");
						newPath << "FH#L45#FH#";
						apiPtr->moveForwardHalf();
						apiPtr->turnLeft45();
						apiPtr->moveForwardHalf();
						mousePtr->moveForwardLocal();
						i++;
						lastMovement = "LF";
						break;
					} else if ((movements[i] + movements[i + 1]) == "RF") {
						LOG_DEBUG("POPPPPY");
						newPath << "R#FH#R45#FH#";
						apiPtr->turnRight();
						apiPtr->moveForwardHalf();
						apiPtr->turnRight45();
						apiPtr->moveForwardHalf();
						mousePtr->moveForwardLocal();
						i++;
						lastMovement = "RF";
						break;
					} else if ((i + 2 < static_cast<int>(movements.size())) &&
							   (movements[i] + movements[i + 1] + movements[i + 2] == "FRF")) {
						LOG_DEBUG("POPPPPY");
						newPath << "R45#F#R45#FH#R45#FH#";
						apiPtr->turnRight45();
						apiPtr->moveForwardHalf();
						apiPtr->moveForwardHalf();
						apiPtr->turnRight45();
						apiPtr->moveForwardHalf();
						mousePtr->moveForwardLocal();
						apiPtr->turnRight45();
						apiPtr->moveForwardHalf();
						i += 2;
						lastMovement = "F";
						break;
					}
					newPath << "R45#FH#";
					apiPtr->turnRight45();
					apiPtr->moveForwardHalf();
				}

				// Execute the current movement
				newPath << movements[i] << "#";
				if (movements[i] == "F") {
					apiPtr->moveForward();
				} else if (movements[i] == "L") {
					apiPtr->turnLeft();
				} else if (movements[i] == "R") {
					apiPtr->turnRight();
				} else {
					LOG_DEBUG("[ERROR] Invalid Movement: " + movements[i]);
				}
				lastMovement = movements[i];
				break;
		}  // end switch
	}  // end for loop

	// Handle remaining movements after main loop //FIXME this is it
	if (lastMovement == "RFLF" || lastMovement == "LFLF") {
		newPath << "L45#FH#";
		apiPtr->turnLeft45();
		apiPtr->moveForwardHalf();
	} else if (lastMovement == "LFRF" || lastMovement == "RFRF") {
		newPath << "R45#FH#";
		apiPtr->turnRight45();
		apiPtr->moveForwardHalf();
	}

	// Process any remaining movements
	if (i < static_cast<int>(movements.size())) {
		LOG_DEBUG("Moves remaining: " + std::to_string(movements.size() - i) + " with moves being ...");
		for (int j = i; j < static_cast<int>(movements.size()); j++) {
			LOG_DEBUG(" Movement: " + movements[j]);
			currCell = mousePtr->getMousePosition();
			LOG_DEBUG(" Mouse CurrPos: (" + std::to_string(currCell.getX()) + ", " + std::to_string(currCell.getY()) +
					  ")");

			if (movements[j] == "F") {
				apiPtr->moveForward();
			} else if (movements[j] == "L") {
				apiPtr->turnLeft();
			} else if (movements[j] == "R") {
				apiPtr->turnRight();
			} else {
				LOG_DEBUG("[ERROR] Invalid Movement: " + movements[j]);
			}
		}
		currCell = mousePtr->getMousePosition();
		LOG_DEBUG(" Mouse CurrPos: (" + std::to_string(currCell.getX()) + ", " + std::to_string(currCell.getY()) + ")");
	} else {
		currCell = mousePtr->getMousePosition();
		LOG_DEBUG(" Mouse CurrPos: (" + std::to_string(currCell.getX()) + ", " + std::to_string(currCell.getY()) + ")");
	}
	return newPath.str();
}

/**
 * @brief Traverses a single goal cell iteratively.
 *
 * @param mouse Pointer to the MouseLocal instance.
 * @param goalCell The target Cell to traverse to.
 * @param diagonalsAllowed Whether diagonal movements are permitted.
 * @param allExplored Whether all cells should be marked as explored.
 * @param avoidGoalCells Whether to avoid goal cells during traversal.
 * @return true If traversal was successful.
 * @return false If traversal failed.
 */
bool traversePathIteratively(MouseLocal* mouse, Cell& goalCell, bool diagonalsAllowed, bool allExplored,
							 bool avoidGoalCells) {
	vector<Cell*> goalCells;
	goalCells.push_back(&goalCell);
	return traversePathIteratively(mouse, goalCells, diagonalsAllowed, allExplored, avoidGoalCells);
}

/**
 * @brief Traverses multiple goal cells iteratively.
 *
 * @param mouse Pointer to the MouseLocal instance.
 * @param goalCells A vector of target Cells to traverse to.
 * @param diagonalsAllowed Whether diagonal movements are permitted.
 * @param allExplored Whether all cells should be marked as explored.
 * @param avoidGoalCells Whether to avoid goal cells during traversal.
 * @return true If traversal was successful for all goals.
 * @return false If traversal failed for any goal.
 */
bool traversePathIteratively(MouseLocal* mouse, vector<Cell*>& goalCells, bool diagonalsAllowed, bool allExplored,
							 bool avoidGoalCells) {
	Cell currCell(0, 0);		  // Initialize with appropriate constructor arguments
	Movement* prevMov = nullptr;  // Assuming Movement is a class with pointer semantics

	if (allExplored) {
		setAllExplored(mouse);
	}

	while (true) {
		currCell = (mouse->getMousePosition());
		currCell.setIsExplored(true);

		if (mouse->isGoalCell(currCell, goalCells)) {
			// If the previous movement was diagonal, handle that.
			if (prevMov != nullptr && prevMov->getIsDiagonal()) {
				turnMouseToNextCell(*(prevMov->getFirstMove()), currCell);
				apiPtr->moveForwardHalf();
			}
			break;
		}
		prevMov = nullptr;

		// Detect walls
		mouse->detectAndSetWalls(*apiPtr);	// Pass API if needed

		// vector<Cell*> goalCells2();
		// vector<Cell*> goalCells2;

		// Get path from A* or your best algorithm
		vector<Cell*> cellPath = getBestAlgorithmPath(aStarPtr, goalCells, diagonalsAllowed, avoidGoalCells);

		// If you want to color the path
		if (Constants::MazeConstants::showPath && allExplored &&
			!MouseLocal::isSame(*goalCells[0], (mouse->getCell(0, 0)))) {
			for (const auto& c : cellPath) {
				apiPtr->setColor(c->getX(), c->getY(), Constants::MazeConstants::goalPathColor);
			}
		} else if (Constants::MazeConstants::showPath && allExplored && goalCells.size() == 1) {
			for (const auto& c : cellPath) {
				apiPtr->setColor(c->getX(), c->getY(), Constants::MazeConstants::returnPathColor);
			}
		}

		// Log the algorithm path
		string algPathStr = "";
		for (const auto& c : cellPath) {
			algPathStr += "(" + std::to_string(c->getX()) + ", " + std::to_string(c->getY()) + ") -> ";
		}
		LOG_DEBUG("[PROCESSED] Algorithm Path: " + algPathStr);

		// Convert path to string
		vector<Cell*> cellPathPtrs;
		for (auto& cell : cellPath) {
			cellPathPtrs.push_back(cell);
		}
		string path = AStar::pathToString(*mouse, cellPathPtrs);
		// LOG_DEBUG("[PROCESSED] Path: " + path); // Optional logging

		if (allExplored && diagonalsAllowed) {
			LOG_DEBUG("[PROCESSED] Path: " + path);
			path = diagonalizeAndRun(currCell, path);
			LOG_DEBUG("[PROCESSED] Diagonalized Path: " + path);
		} else {
			// Execute the movement commands step-by-step
			stringstream ss(path);
			string move;
			while (std::getline(ss, move, '#')) {
				if (move == "F") {
					apiPtr->moveForward();
				} else if (move == "L") {
					apiPtr->turnLeft();
				} else if (move == "R") {
					apiPtr->turnRight();
				} else {
					LOG_DEBUG("[ERROR] Invalid Movement: " + move);
				}

				currCell = (mouse->getMousePosition());
				LOG_DEBUG("[POS] Updated Mouse Position: (" + std::to_string(currCell.getX()) + ", " +
						  std::to_string(currCell.getY()) + ")\n");

				if (!currCell.getIsExplored()) {
					LOG_DEBUG("[RE-CALC] Cell is unexplored, calculating new path.");
					break;
				}
				LOG_DEBUG("[RE-USE] Reusing ...");
			}
		}
		break;	// The original Java code has "break; // FIXME" so we do the same
	}
	return true;
}