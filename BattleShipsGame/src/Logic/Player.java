package Logic;

import java.awt.*;
import java.util.ArrayList;

public class Player {
    private ArrayList<Cell> previousMoves;
    private String name;
    private ArrayList<BoardComponent> myComponents;
    private PlayersBoard myShipBoard, myAttackingBoard;
    private Stats stats;
    private int minesAmount;

    public Player(String i_Name, BattleShipGame.Boards.Board myBoard, BattleShipGame.ShipTypes shipTypes, int boardSize) {
        previousMoves = new ArrayList<>();
        name = i_Name;
        myComponents = new ArrayList<>();
        stats = new Stats();
        generateComponents(myBoard, shipTypes);
        myShipBoard = new PlayersBoard(boardSize);
        myShipBoard.insertMyShips(myComponents);
        myAttackingBoard = new PlayersBoard(boardSize);
        minesAmount = 0;
    }

    public PlayersBoard getMyShipBoard() {
        return myShipBoard;
    }

    public Stats getStats() {
        return stats;
    }

    public String getAttackingBoardToPrint() {
        return myAttackingBoard.boardToString();
    }

    public String getMyShipBoardToPrint() {
        return myShipBoard.boardToString();
    }

    public boolean checkIfCellWasAttackedBefore(Point attackedPoint) {
        for (Cell cell : previousMoves) {
            if (cell.getWhere().equals(attackedPoint)) {
                return true;
            }
        }
        return false;
    }

    private void generateComponents(BattleShipGame.Boards.Board myBoard, BattleShipGame.ShipTypes shipTypes) {
        for (BattleShipGame.Boards.Board.Ship comp : myBoard.ship) {
            Point compPos = new Point(comp.position.x, comp.position.y);
            myComponents.add(ComponentsFactory.createShips(comp.direction, compPos, comp.shipTypeId, shipTypes));
        }
    }

    public String getName() {
        return name;
    }

    public void addNewMove(Point userMove, boolean isHit, int hitScore) {
        String sign = Cell.BoardSigns.miss.sign();

        if (isHit) {
            stats.attackSucceeded(hitScore);
            sign = Cell.BoardSigns.hit.sign();
        } else {
            stats.attackFailed();
        }
        Cell hitCell = new Cell(userMove, sign);
        previousMoves.add(hitCell);
        myAttackingBoard.insertToBoard(hitCell);
    }

    public BoardComponent checkIfHitMe(Point attackingPoint) {
        for (BoardComponent comp : myComponents) {
            if (comp.checkIfInMySpace(attackingPoint)) {
                return comp;
            }
        }
        return null;
    }

    public void addNewHitInMyBoard(Point userMove, boolean isHit) {
        String sign = isHit ? Cell.BoardSigns.hit.sign() : Cell.BoardSigns.miss.sign();
        myShipBoard.insertToBoard(new Cell(userMove, sign));
    }

    public boolean checkMineLocation(Point minePlace) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (minePlace.y + j < myShipBoard.getBoardSize() && minePlace.x + i < myShipBoard.getBoardSize()) {
                    if (myShipBoard.getStrAt(minePlace.x + i, minePlace.y + j).equals("#")) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void addMine(Point location) {
        BoardComponent mine = ComponentsFactory.createMine(location);
        myComponents.add(mine);
        myShipBoard.insertToBoard(mine.getCells().get(0));
        minesAmount++;
    }

    public int getMinesAmount() {
        return minesAmount;
    }

    public class Stats {

        private int numHits;
        private int numMiss;
        private long avgMovesTimeMS;
        private String avgMoveTimeStr;
        private StopWatch stopWatch;
        private ArrayList<Long> movesTotalTime;
        private int score;

        private Stats() {
            score = 0;
            numHits = 0;
            numMiss = 0;
            avgMovesTimeMS = 0;
            movesTotalTime = new ArrayList<>();
            stopWatch = new StopWatch();
        }

        public int getScore() {
            return score;
        }

        public int getNumHits() {
            return numHits;
        }

        public long getAvgMovesTimeMS() {
            return avgMovesTimeMS;
        }

        public int getNumMiss() {
            return numMiss;
        }

        private void updateNumHits() {
            this.numHits += 1;
        }

        private void updateNumMiss() {
            this.numMiss += 1;
        }

        public void startClock() {
            stopWatch.initWatch();
        }

        public void updateScore(int shipValue) {
            score += shipValue;
        }

        public String getAvgMovesTimeAsString() {
            return stopWatch.getTimeElapsedAsString(avgMovesTimeMS);
        }

        public void attackSucceeded(int shipValue) {
            updateNumHits();
            updateScore(shipValue);
        }

        public void attackFailed() {
            updateNumMiss();
        }

        public void stopClock() {
            movesTotalTime.add(stopWatch.getElapsedTimeInMS());
            avgMovesTimeMS = (avgMovesTimeMS + movesTotalTime.get(movesTotalTime.size() - 1)) / movesTotalTime.size();
        }
    }
}