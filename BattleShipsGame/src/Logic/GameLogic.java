package Logic;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class GameLogic {
    private final int PLAYERS_AMOUNT = 2;
    private BattleShipGame BSGameInputs;
    private Player[] players;
    private boolean isGameFinished;
    private boolean isGameLoaded;
    private Path XMLPath;
    private Player currentPlayer;
    private Player attackedPlayer;
    private int totalShipPointsOnBoard;
    private GameStatistics gameStats;

    public void initGameComponents() {
        players = new Player[PLAYERS_AMOUNT];
        isGameFinished = false;
        isGameLoaded = false;
        for (int i = 0; i < PLAYERS_AMOUNT; i++) {
            players[i] = new Player("Player " + (i + 1), BSGameInputs.boards.board.get(i), BSGameInputs.shipTypes, BSGameInputs.boardSize);
        }
        currentPlayer = players[0];
        attackedPlayer = players[1];
        calcTotalShipPointsOnBoard();
        gameStats = new GameStatistics();
        gameStats.startWatch();
    }

    public enum boardObj {
        mine,
        none,
        ship
    }

    public Player[] getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameStatistics getGameStats() {
        return gameStats;
    }

    public int getBoardSize() {
        return BSGameInputs.boardSize;
    }

    public Player getAttackedPlayer() {
        return attackedPlayer;
    }

    public void setXMLPath(Path pathFromUser) {
        XMLPath = pathFromUser;
    }

    public void setGameFinished(boolean gameFinished) {
        isGameFinished = gameFinished;
    }

    public boolean checkIfPlayerCanAffordMine() {
        return (currentPlayer.getMinesAmount() < 2);
    }

    private void calcTotalShipPointsOnBoard() {
        for (BattleShipGame.ShipTypes.ShipType ship : BSGameInputs.shipTypes.shipType) {
            totalShipPointsOnBoard += ship.length * ship.amount;
        }
    }

    public boolean isGameFinished() {
        return isGameFinished;
    }

    public boolean initGameFromXML() throws FileNotFoundException, NotXMLFileException, JAXBException {
        FileInputStream inputStream;
        boolean isSuccessfullyLoaded = false;
        inputStream = new FileInputStream(XMLPath.toString());
        BSGameInputs = deserializeFrom(inputStream);
        if (BSGameInputs != null) {
            isSuccessfullyLoaded = true;
        }
        return isSuccessfullyLoaded;
    }

    private BattleShipGame deserializeFrom(InputStream inputStream) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller u = jc.createUnmarshaller();
        return (BattleShipGame) u.unmarshal(inputStream);
    }

    public boardObj checkMove(Point userMove) {
        boardObj res = boardObj.none;
        BoardComponent attackedComp = attackedPlayer.checkIfHitMe(userMove);

        gameStats.updateMoveCounter();
        if (attackedComp != null) {
            res = attackedComp.getCompKind();
            attackedComp.action(currentPlayer, attackedPlayer, userMove);
            if (!attackedComp.getClass().equals(Ship.class)) {
                switchPlayers();
            }
        }
        else{
            currentPlayer.addNewMove(userMove,false,0);
            attackedPlayer.addNewHitInMyBoard(userMove,false);
            switchPlayers();
        }
        return res;
    }

    public boolean checkIfGameFinished() {
        if (currentPlayer.getStats().getNumHits() == totalShipPointsOnBoard) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkGameInputs() throws InvalidXMLInputsException {
        boolean res;
        res = checkInputBoardSize();
        res = res && checkShipTypesDetail();
        res = res && checkBoardComponentsPositions();
        res = res && checkIfShipsMatchShipTypes();
        return res;
    }

    private boolean checkShipTypesDetail() throws InvalidXMLInputsException{
        for (BattleShipGame.ShipTypes.ShipType shipType : BSGameInputs.shipTypes.shipType){
                if(shipType.length > BSGameInputs.boardSize || shipType.length < 1)
                    throw new InvalidXMLInputsException("Ship size in XML file is invalid");
                else if(shipType.amount < 0)
                    throw new InvalidXMLInputsException("Ship amount in XML file is invalid");
                else if(shipType.score < 0)
                    throw new InvalidXMLInputsException("Ship score in XML file is invalid");
        }

        return true;
    }

    private boolean checkIfShipsMatchShipTypes() throws InvalidXMLInputsException {
        String currID;
        int currAmount, totalShipAmount = 0;
        int shipCounterInCurrType = 0, totalShipsCounter = 0;
        for (BattleShipGame.ShipTypes.ShipType shipType : BSGameInputs.shipTypes.shipType) {
            currID = shipType.id;
            currAmount = shipType.amount;
            totalShipAmount += currAmount;
            for (BattleShipGame.Boards.Board board : BSGameInputs.boards.board) {
                for (BattleShipGame.Boards.Board.Ship ship : board.ship) {
                    if (currID.equals(ship.shipTypeId)) {
                        shipCounterInCurrType++;
                        totalShipsCounter++;
                        if (shipCounterInCurrType > currAmount) {
                            throw new InvalidXMLInputsException("The entered ships do not match the ship types!");
                        }
                    }
                }
                shipCounterInCurrType = 0;
            }
        }
        return totalShipAmount == totalShipsCounter / 2;
    }

    private boolean checkBoardComponentsPositions() throws InvalidXMLInputsException {
        ArrayList<Point> prevShipsPoints = new ArrayList<>();
        ArrayList<Point> currShipPoints = new ArrayList<>();
        boolean isGoodInput = true;

        for (BattleShipGame.Boards.Board board : BSGameInputs.boards.board) {
            for (BattleShipGame.Boards.Board.Ship ship : board.ship) {
                isGoodInput = checkShipPosition(ship, prevShipsPoints);
                if (isGoodInput) {
                    for (Point point : currShipPoints) {
                        prevShipsPoints.add(point);
                    }
                }
            }
            currShipPoints.clear();
            prevShipsPoints.clear();
        }
        return isGoodInput;
    }

    private boolean checkShipPosition(BattleShipGame.Boards.Board.Ship ship, ArrayList<Point> prevShipsPoints) throws InvalidXMLInputsException {
        String direction = ship.direction;
        int shipSize = 0;
        for (BattleShipGame.ShipTypes.ShipType shipType : BSGameInputs.shipTypes.shipType) {
            if (shipType.id.equals(ship.shipTypeId)) {
                shipSize = shipType.length;
                break;
            }
        }
        checkIfShipExceedsBoardSize(ship, direction, shipSize);
        ArrayList<Point> currShipPoints = getAllShipPoints(ship, direction, shipSize);
        return checkIfShipOverlapp(currShipPoints, prevShipsPoints);
    }

    private void checkIfShipExceedsBoardSize
            (BattleShipGame.Boards.Board.Ship ship, String direction, int shipSize) throws InvalidXMLInputsException {
        int shipStartPoint = direction.equals("ROW") ? ship.position.x : ship.position.y;
        if (shipStartPoint + shipSize - 1 > getBoardSize()) {
            throw new InvalidXMLInputsException("Ships are exceeding the board's size");
        }
    }

    private boolean checkIfShipOverlapp(ArrayList<Point> currShipPoints, ArrayList<Point> prevShipsPoints) throws InvalidXMLInputsException {
        for (Point currShipPoint : currShipPoints) {
            for (Point prevShipPoint : prevShipsPoints) {
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        if (currShipPoint.x + i == prevShipPoint.x && currShipPoint.y + j == prevShipPoint.y) {
                            throw new InvalidXMLInputsException("Ship are overlapping each other");
                        }
                    }
                }
            }
        }

        return true;
    }

    private ArrayList<Point> getAllShipPoints(BattleShipGame.Boards.Board.Ship ship, String direction, int shipSize) {
        ArrayList<Point> res = new ArrayList<>();
        int row, col;
        if (direction.equals("ROW")) {
            row = 1;
            col = 0;
        } else {
            row = 0;
            col = 1;
        }
        for (int i = 0; i < shipSize; i++) {
            res.add(new Point(ship.position.x + (i * row), ship.position.y + (i * col)));
        }

        return res;
    }

    private boolean checkInputBoardSize() throws InvalidXMLInputsException {
        if (BSGameInputs.boardSize < 5 || BSGameInputs.boardSize > 20)
            throw new InvalidXMLInputsException("Board size is invalid");
        else
            return true;
    }

    public void switchPlayers() {
        Player tempPlayer = currentPlayer;
        currentPlayer = attackedPlayer;
        attackedPlayer = tempPlayer;
    }

    public void addMineToPlayer(Point location) {
        currentPlayer.addMine(location);
    }

    public enum menuItems {
        readXMLData("Read Game Data", 1),
        startGame("Start Game", 2),
        gameState("Get Game State", 3),
        makeMove("Make A Move", 4),
        statistics("Game Statistics", 5),
        putMine("Plant A Mine", 6),
        restartGame("Restart Game", 7),
        finishGame("QUIT", 8);
        private String name;
        private int serial;

        menuItems(String s, int num) {
            this.name = s;
            this.serial = num;
        }

        public int getSerial() {
            return serial;
        }

        public String ToString() {
            return name;
        }
    }
}