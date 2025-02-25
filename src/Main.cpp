
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

void turnMouseToNextCell(const Cell& currentCell, const Cell& nextCell) {
	array<int, 2> halfSteps = mousePtr->obtainHalfStepCount(mousePtr->getDirBetweenCells(currentCell, nextCell));

	// Turns mouse to face next cell.
	for (int i = 0; i < halfSteps[0]; i++) {
		if (halfSteps[1] == -1) {
			apiPtr->turnLeft45();
		} else {
			apiPtr->turnRight45();
		}
	}
}

enum class MovementBlock { RFLF, LFRF, RFRF, LFLF, FLF, FRF, F, RF, LF, L, R, DEFAULT };

MovementBlock parseMovementBlock(const string& block) {
	// Maps movement block strings to MovementBlock values.
	static const unordered_map<string, MovementBlock> movementMap = {
		{"RFLF", MovementBlock::RFLF}, {"LFRF", MovementBlock::LFRF}, {"RFRF", MovementBlock::RFRF}, {"LFLF", MovementBlock::LFLF},
		{"FLF", MovementBlock::FLF},   {"FRF", MovementBlock::FRF},	  {"F", MovementBlock::F},		 {"RF", MovementBlock::RF},
		{"LF", MovementBlock::LF},	   {"L", MovementBlock::L},		  {"R", MovementBlock::R}};
	auto it = movementMap.find(block);
	if (it != movementMap.end())
		return it->second;
	return MovementBlock::DEFAULT;
}

vector<string> splitPath(const string& path) {
	// Splits the path strings into individual movements ("vector-ize"/"split").
	vector<string> movements;
	stringstream ss(path);
	string token;
	while (getline(ss, token, '#')) {
		if (!token.empty())
			movements.push_back(token);
	}
	return movements;
}

void executeSequence(const string& seq, ostringstream& diagPath) {
	diagPath << seq;
	stringstream ss(seq);
	string token;
	// Executes a command sequence.
	while (getline(ss, token, '#')) {
		if (token.empty())
			continue;
		if (token == "R") {
			apiPtr->turnRight();
		} else if (token == "L") {
			apiPtr->turnLeft();
		} else if (token == "F") {
			apiPtr->moveForward();
		} else if (token == "R45") {
			apiPtr->turnRight45();
		} else if (token == "L45") {
			apiPtr->turnLeft45();
		} else if (token == "FH") {
			apiPtr->moveForwardHalf();
		}
	}
}

void performSequence(const string& seq, ostringstream& diagPath, bool localMove = false) {
	// Executes and updates mouse position for combo moves.
	if (localMove) {
		("Performing Sequence: " + seq);
	}
	executeSequence(seq, diagPath);
	if (localMove) {
		mousePtr->moveForwardLocal();
	}
}

void executeIndividualMovement(const vector<string>& movementsSequence, int& i, ostringstream& diagPath, MovementBlock& prevBlockType,
							   Cell& currCell) {
	MovementBlock threeMovementBlock = MovementBlock::DEFAULT;
	MovementBlock twoMovementBlock = MovementBlock::DEFAULT;
	MovementBlock nextMovementBlock = MovementBlock::DEFAULT;

	if (i + 2 < static_cast<int>(movementsSequence.size())) {
		threeMovementBlock = parseMovementBlock(movementsSequence[i] + movementsSequence[i + 1] + movementsSequence[i + 2]);
	} else if (i + 1 < static_cast<int>(movementsSequence.size())) {
		twoMovementBlock = parseMovementBlock(movementsSequence[i] + movementsSequence[i + 1]);
	} else {
		nextMovementBlock = parseMovementBlock(movementsSequence[i]);
	}

	if (movementsSequence.size() != 1) {
		if (prevBlockType == MovementBlock::RFLF || prevBlockType == MovementBlock::LFLF) {
			if (twoMovementBlock == MovementBlock::RF) {
				performSequence("FH#R45#FH#", diagPath, true);
				prevBlockType = twoMovementBlock;
				i++;
			} else if (twoMovementBlock == MovementBlock::LF) {
				performSequence("L#FH#L45#FH#", diagPath, true);
				prevBlockType = twoMovementBlock;
				i++;
			} else if (threeMovementBlock == MovementBlock::FLF) {
				performSequence("L45#F#L45#FH#R45#FH#", diagPath, true);
				prevBlockType = MovementBlock::F;
				i += 2;
			} else {
				performSequence("L45#FH#", diagPath);
			}
		} else if (prevBlockType == MovementBlock::LFRF || prevBlockType == MovementBlock::RFRF) {
			if (twoMovementBlock == MovementBlock::LF) {
				performSequence("FH#L45#FH#", diagPath, true);
				prevBlockType = twoMovementBlock;
				i++;
			} else if (twoMovementBlock == MovementBlock::RF) {
				performSequence("R#FH#R45#FH#", diagPath, true);
				prevBlockType = twoMovementBlock;
				i++;
			} else if (threeMovementBlock == MovementBlock::FRF) {
				performSequence("R45#F#R45#FH#L45#FH#", diagPath, true);
				prevBlockType = MovementBlock::F;
				i += 2;
			} else {
				performSequence("R45#FH#", diagPath);
			}
		}
	} else {
		if (nextMovementBlock == MovementBlock::R) {
			performSequence("R#", diagPath);
		} else if (nextMovementBlock == MovementBlock::L) {
			performSequence("L#", diagPath);
		} else {
			performSequence("F#", diagPath);
		}
		prevBlockType = nextMovementBlock;
	}
	currCell = mousePtr->getMousePosition();
	// LOG_INFO("Step " + to_string(i) + ": " + currCell.toString());
}

void executeIndividualMovement(string movement, Cell& currCell) {
	vector<string> movementsSequence = {movement};
	ostringstream path = ostringstream();
	MovementBlock prevBlockType = MovementBlock::DEFAULT;
	int i = 0;
	executeIndividualMovement(movementsSequence, i, path, prevBlockType, currCell);
}

string diagonalizeAndRun(Cell& currCell, const string& path) {
	ostringstream diagPath;
	vector<string> movementsSequence = splitPath(path);
	MovementBlock blockType;
	MovementBlock prevBlockType = MovementBlock::DEFAULT;
	int i = 0;

	while (i < static_cast<int>(movementsSequence.size())) {
		currCell = mousePtr->getMousePosition();
		LOG_INFO("Step " + to_string(i) + ": " + currCell.toString());

		// Check for a forward combo: "F" followed by a 4-move combo block.
		if (movementsSequence[i] == "F" && (i + 4 < static_cast<int>(movementsSequence.size()))) {
			MovementBlock nextMovementBlock =
				parseMovementBlock(movementsSequence[i + 1] + movementsSequence[i + 2] + movementsSequence[i + 3] + movementsSequence[i + 4]);
			if (nextMovementBlock == MovementBlock::RFRF || nextMovementBlock == MovementBlock::LFLF || nextMovementBlock == MovementBlock::RFLF ||
				nextMovementBlock == MovementBlock::LFRF) {
				i++;  // Consume the "F"
				if (prevBlockType != MovementBlock::RFRF && prevBlockType != MovementBlock::LFLF && prevBlockType != MovementBlock::RFLF &&
					prevBlockType != MovementBlock::LFRF) {
					performSequence("FH#", diagPath, true);
				} else {
					if (prevBlockType == MovementBlock::RFLF || prevBlockType == MovementBlock::LFLF) {
						performSequence("L45#F#", diagPath);
					} else if (prevBlockType == MovementBlock::LFRF || prevBlockType == MovementBlock::RFRF) {
						performSequence("R45#F#", diagPath);
					}
				}
				prevBlockType = MovementBlock::F;
			}
		}

		// Attempt to form a 4-move combo block if possible.
		if (i + 3 < static_cast<int>(movementsSequence.size())) {
			blockType = parseMovementBlock(movementsSequence[i] + movementsSequence[i + 1] + movementsSequence[i + 2] + movementsSequence[i + 3]);
			LOG_WARN("Block Type: " + movementsSequence[i] + movementsSequence[i + 1] + movementsSequence[i + 2] + movementsSequence[i + 3]);
			switch (blockType) {
				case MovementBlock::RFLF:
					if (prevBlockType == blockType || prevBlockType == MovementBlock::LFLF)
						performSequence("F#", diagPath);
					else if (prevBlockType == MovementBlock::LFRF || prevBlockType == MovementBlock::RFRF)
						performSequence("R#F#", diagPath);
					else if (prevBlockType == MovementBlock::F)
						performSequence("R45#F#", diagPath);
					else
						performSequence("R#FH#L45#FH#", diagPath, true);
					i += 3;
					break;

				case MovementBlock::LFRF:
					if (prevBlockType == blockType || prevBlockType == MovementBlock::RFRF)
						performSequence("F#", diagPath);
					else if (prevBlockType == MovementBlock::RFLF || prevBlockType == MovementBlock::LFLF)
						performSequence("L#F#", diagPath);
					else if (prevBlockType == MovementBlock::F)
						performSequence("L45#F#", diagPath);
					else
						performSequence("L#FH#R45#FH#", diagPath, true);
					i += 3;
					break;

				case MovementBlock::RFRF:
					if (prevBlockType == blockType)
						performSequence("R#FH#R#FH#", diagPath, true);
					else if (prevBlockType == MovementBlock::RFLF || prevBlockType == MovementBlock::LFLF)
						performSequence("FH#R#FH#", diagPath, true);
					else if (prevBlockType == MovementBlock::F)
						performSequence("R45#FH#R#FH#", diagPath, true);
					else
						performSequence("R#FH#R45#FH#", diagPath, true);
					i += 3;
					break;

				case MovementBlock::LFLF:
					if (prevBlockType == blockType)
						performSequence("L#FH#L#FH#", diagPath, true);
					else if (prevBlockType == MovementBlock::LFRF || prevBlockType == MovementBlock::RFRF)
						performSequence("FH#L#FH#", diagPath, true);
					else if (prevBlockType == MovementBlock::F)
						performSequence("L45#FH#L#FH#", diagPath, true);
					else
						performSequence("L#FH#L45#FH#", diagPath, true);
					i += 3;
					break;

				default:
					LOG_WARN("Default Movement: " + movementsSequence[i]);
					executeIndividualMovement(movementsSequence, i, diagPath, prevBlockType, currCell);
					break;
			}

			prevBlockType = blockType;
			i++;
		}
	}
	return diagPath.str();
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
bool traversePathIteratively(MouseLocal* mouse, Cell& goalCell, bool diagonalsAllowed, bool allExplored, bool avoidGoalCells) {
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
bool traversePathIteratively(MouseLocal* mouse, vector<Cell*>& goalCells, bool diagonalsAllowed, bool allExplored, bool avoidGoalCells) {
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

		// Get path from A* or your best algorithm
		vector<Cell*> cellPath = getBestAlgorithmPath(aStarPtr, goalCells, diagonalsAllowed, avoidGoalCells);

		// If you want to color the path
		if (Constants::MazeConstants::showPath && allExplored && !MouseLocal::isSame(*goalCells[0], (mouse->getCell(0, 0)))) {
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

		// Convert path to string
		vector<Cell*> cellPathPtrs;
		for (auto& cell : cellPath) {
			cellPathPtrs.push_back(cell);
		}
		string path = AStar::pathToString(*mouse, cellPathPtrs);

		if (cellPath.size() > 9 && allExplored) {
			LOG_INFO("Path: " + algPathStr);
			LOG_INFO("LFR Path: " + path);
		}

		if (allExplored && diagonalsAllowed) {
			path = diagonalizeAndRun(currCell, path);
			LOG_WARN("Diag Path: " + path);
		} else {
			// Execute the movement commands step-by-step
			stringstream ss(path);
			string move;
			while (std::getline(ss, move, '#')) {
				executeIndividualMovement(move, currCell);

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