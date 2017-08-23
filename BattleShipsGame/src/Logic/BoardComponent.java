package Logic;

import java.awt.Point;
import java.util.ArrayList;

public abstract class BoardComponent {
    private ArrayList<Cell> cells = new ArrayList<>();
    private GameLogic.boardObj compKind;
    private boolean wasHit = false;

    public GameLogic.boardObj getCompKind() {
        return compKind;
    }

    public boolean getWasHit(){return wasHit;}

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void setWasHit(boolean wasHit) {
        this.wasHit = wasHit;
    }

    public void setCompKind(GameLogic.boardObj kind){
        compKind = kind;
    }

    public abstract void action(Player current, Player attacked, Point hitLocation);

    public abstract void setPosition(Point position);

    public abstract boolean checkIfInMySpace(Point attackingPoint);
}