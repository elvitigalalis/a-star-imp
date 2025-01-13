package src.Main;

import src.API.API;

public class Main {    
    private static final String mouseName = API.getMouseName();
    public static void main(String[] args) {
        log("Running " + mouseName + "...");
        API.setColor(0, 0, 'G');
        API.setText(0, 0, "abc");


        
        while (true) {
            if (!API.wallLeft()) {
                API.turnLeft();
            }
            while (API.wallFront()) {
                API.setWall(API.getMousePosition()[0], API.getMousePosition()[1], API.getMouseDirection());
                API.turnRight45();
            }
            API.moveForward();
        }
    }

    private static void log(String text) { System.err.println(text); }
}
