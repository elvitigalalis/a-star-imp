package src.Algorithm.Maze;

public class Movement {
    private boolean canMove;
    private boolean isDiagonal;
    private String leftOrRightDiagonal;
    private Cell cellToMoveToFirst;
    private int[] direction;

    public Movement(boolean canMove, int[] direction) {
        this.canMove = canMove;
        this.isDiagonal = false;
        this.leftOrRightDiagonal = "";
        this.cellToMoveToFirst = null;
        this.direction = direction;
    }

    public Movement(boolean canMove, boolean isDiagonal, String leftOrRightDiagonal, Cell cellToMoveToFirst, int[] direction) {
        this.canMove = canMove;
        this.isDiagonal = isDiagonal;
        this.leftOrRightDiagonal = leftOrRightDiagonal;
        this.cellToMoveToFirst = cellToMoveToFirst;
        this.direction = direction;
    }

    public boolean getCanMove() {
        return this.canMove;
    }

    public boolean getIsDiagonal() {
        return this.isDiagonal;
    }

    public String getIsLeftRight() {
        return this.leftOrRightDiagonal;
    }

    public Cell getFirstMove() {
        return this.cellToMoveToFirst;
    }

    public int[] getDirection() {
        return this.direction;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public void setIsDiagonal(boolean isDiagonal) {
        this.isDiagonal = isDiagonal;
    }

    public void setLeftOrRightDiagonal(String leftOrRightDiagonal) {
        this.leftOrRightDiagonal = leftOrRightDiagonal;
    }

    public void setCellToMoveToFirst(Cell cellToMoveToFirst) {
        this.cellToMoveToFirst = cellToMoveToFirst;
    }

    public void setDirection(int[] direction) {
        this.direction = direction;
    }
}
