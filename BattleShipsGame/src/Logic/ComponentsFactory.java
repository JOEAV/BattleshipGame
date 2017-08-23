package Logic;

import java.awt.*;

public class ComponentsFactory {
    public static BoardComponent createShips(String directionStr, Point pos, String currShipType, BattleShipGame.ShipTypes shipTypesInGame) {
        int index = 0;
        BoardComponent res = new Ship();
        while (currShipType.compareTo(shipTypesInGame.shipType.get(index).id) != 0) {
            index++;
        }
        ((Ship) res).setLength(shipTypesInGame.shipType.get(index).length);
        ((Ship) res).setScore(shipTypesInGame.shipType.get(index).score);
        ((Ship) res).setDirection(directionStr);
        res.setPosition(pos);
        res.setWasHit(false);
        ((Ship) res).setPartsHit(0);
        res.setCompKind(GameLogic.boardObj.ship);
        return res;
    }

    public static BoardComponent createMine(Point location) {
        BoardComponent mine = new Mine(location);
        mine.setCompKind(GameLogic.boardObj.mine);
        return mine;
    }
}