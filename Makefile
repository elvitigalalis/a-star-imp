CXX = g++
CXXFLAGS = -Wall -Wextra -std=c++11 -I./include/

all: API.o Main.o Constants.o Algorithm.o Maze.o

API.o: src/API/API.cpp src/API/API.h
	$(CXX) $(CXXFLAGS) -c src/API/API.cpp -o API.o

Main.o: src/Main.cpp src/Main.h
	$(CXX) $(CXXFLAGS) -c src/Main.cpp -o Main.o

Constants.o : src/Constants.cpp src/Constants.h
	$(CXX) $(CXXFLAGS) -c src/Constants.cpp -o Constants.o

Algorithm.o: src/Algorithm/AStar.cpp src/Algorithm/AStar.h src/Algorithm/FrontierBased.cpp src/Algorithm/FrontierBased.h
	$(CXX) $(CXXFLAGS) -c src/Algorithm/AStar.cpp src/Algorithm/FrontierBased.cpp

Maze.o: src/Algorithm/Maze/Cell.cpp src/Algorithm/Maze/Cell.h src/Algorithm/Maze/MouseLocal.cpp src/Algorithm/Maze/MouseLocal.h src/Algorithm/Maze/Movement.cpp src/Algorithm/Maze/Movement.h
	$(CXX) $(CXXFLAGS) -c src/Algorithm/Maze/Cell.cpp src/Algorithm/Maze/MouseLocal.cpp src/Algorithm/Maze/Movement.cpp

clean:
	rm -f *.o *.out

run:
	./Micromouse.out