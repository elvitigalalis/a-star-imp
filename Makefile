CXX = g++
CXXFLAGS = -Wall -Wextra -std=c++11 -I./include/

all: API.o Main.o Constants.o Logger.o AlgorithmAStar.o AlgorithmFrontierBased.o MazeCell.o MazeMouseLocal.o MazeMovement.o
	$(CXX) $(CXXFLAGS) API.o Main.o Constants.o Logger.o AlgorithmAStar.o AlgorithmFrontierBased.o MazeCell.o MazeMouseLocal.o MazeMovement.o -o Micromouse.out

API.o: src/API/API.cpp src/API/API.h
	$(CXX) $(CXXFLAGS) -c src/API/API.cpp -o API.o

Main.o: src/Main.cpp src/Main.h
	$(CXX) $(CXXFLAGS) -c src/Main.cpp -o Main.o

Constants.o : src/Constants.cpp src/Constants.h
	$(CXX) $(CXXFLAGS) -c src/Constants.cpp -o Constants.o

Logger.o : src/Logger.cpp src/Logger.h
	$(CXX) $(CXXFLAGS) -c src/Logger.cpp -o Logger.o

AlgorithmAStar.o: src/Algorithm/AStar.cpp src/Algorithm/AStar.h
	$(CXX) $(CXXFLAGS) -c src/Algorithm/AStar.cpp -o AlgorithmAStar.o

AlgorithmFrontierBased.o: src/Algorithm/FrontierBased.cpp src/Algorithm/FrontierBased.h
	$(CXX) $(CXXFLAGS) -c src/Algorithm/FrontierBased.cpp -o AlgorithmFrontierBased.o

MazeCell.o: src/Algorithm/Maze/Cell.cpp src/Algorithm/Maze/Cell.h
	$(CXX) $(CXXFLAGS) -c src/Algorithm/Maze/Cell.cpp -o MazeCell.o

MazeMouseLocal.o: src/Algorithm/Maze/MouseLocal.cpp src/Algorithm/Maze/MouseLocal.h
	$(CXX) $(CXXFLAGS) -c src/Algorithm/Maze/MouseLocal.cpp -o MazeMouseLocal.o

MazeMovement.o: src/Algorithm/Maze/Movement.cpp src/Algorithm/Maze/Movement.h
	$(CXX) $(CXXFLAGS) -c src/Algorithm/Maze/Movement.cpp -o MazeMovement.o

clean:
	rm -f *.o *.out

run:
	./Micromouse.out