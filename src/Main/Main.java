package src.Main;

import src.API.API;
import src.Maze.Maze;

public class Main {    
    private static final String mouseName = Maze.getMouseName();
    public static void main(String[] args) {
        log("Running " + mouseName + "...");
        API.setColor(0, 0, 'G');
        API.setText(0, 0, "abc");


        
        while (true) {
            if (!API.wallLeft()) {
                API.turnLeft();
            }
            while (API.wallFront()) {
                API.setWall(Maze.getMousePosition()[0], Maze.getMousePosition()[1], Maze.getMouseDirection());
                API.turnRight45();
            }
            API.moveForward();
        }
    }

    private static void log(String text) { System.err.println(text); }
}
