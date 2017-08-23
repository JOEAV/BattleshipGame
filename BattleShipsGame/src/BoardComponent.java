import Logic.Player;

import java.awt.Point;

public abstract class BoardComponent {
    private int length;
    private Point location;
    private int points;
    private boolean wasHit = false;
    private char look;

    public abstract void Action(Player shootingPlayer);
}
