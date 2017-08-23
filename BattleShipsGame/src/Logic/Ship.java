package Logic;

import java.awt.*;
import java.util.ArrayList;

public class Ship extends BoardComponent {
    private int score;
    private String direction;
    private int length;
    private int partsHit;

    public int getScore() {
        return score;
    }

    public void setPartsHit(int partsHit) {
        this.partsHit = partsHit;
    }

    public void setPosition(Point position) {
        if (direction.compareTo("ROW") == 0) {
            for (int i = 0; i < length; i++) {
                getCells().add(new Cell(new Point(position.x + i, position.y), "#"));
            }
        } else {
            for (int i = 0; i < length; i++) {
                getCells().add(new Cell(new Point(position.x, position.y + i), "#"));
            }
        }
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean checkIfInMySpace(Point attackingPoint) {
        for (Cell compCell : getCells()) {
            if (compCell.getWhere().equals(attackingPoint) && compCell.isGoodHit() != true) {
                compCell.setGoodHit(true);
                partsHit++;
                if (partsHit == length) {
                    setWasHit(true);
                }
                return true;
            }
        }
        return false;
    }

    public void action(Player current, Player attacked, Point hitLocation) {
        current.addNewMove(hitLocation, true, 1);
        attacked.addNewHitInMyBoard(hitLocation, true);
    }

}
