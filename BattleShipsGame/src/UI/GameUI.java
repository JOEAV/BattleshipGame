package UI;

import Logic.*;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import java.awt.*;
import java.io.FileNotFoundException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.Scanner;

public class GameUI {
    private GameLogic game;
    private gameStates state;
    private boolean playerChoseToQuit;

    public GameUI() {
        game = new GameLogic();
        state = gameStates.NOT_LOADED;
        playerChoseToQuit = false;
    }

    private enum gameStates {
        NOT_LOADED,
        GAME_LOADED,
        GAME_STARTED
    }

    private void switchOnUserSelection(int userSelection) {
        switch (userSelection) {
            case 1:
                initialGame();
                break;
            case 2:
                play();
                break;
            case 3:
                showGameStates();
                break;
            case 4:
                makeMove();
                break;
            case 5:
                showGameStatistics();
                break;
            case 6:
                PlantMine();
                break;
            case 7:
                restartGame();
                break;
            case 8:
                playerChoseToQuit = true;
                finishGame();
                break;
        }
    }

    public void StartGame() {
        int userSelection;

        while (!game.isGameFinished()) {
            userSelection = showMenu();
            if (userSelection == GameLogic.menuItems.finishGame.getSerial() && state != gameStates.GAME_STARTED) {
                break;
            }
            if (userSelection != GameLogic.menuItems.readXMLData.getSerial() && state == gameStates.NOT_LOADED) {
                System.out.println("Please initialize the game first");
            } else if (userSelection <= GameLogic.menuItems.startGame.getSerial() && state == gameStates.GAME_STARTED) {
                System.out.println("The game already started");
            } else if (state == gameStates.GAME_LOADED && userSelection > GameLogic.menuItems.startGame.getSerial()) {
                System.out.println("Please start the game first");
            } else {
                switchOnUserSelection(userSelection);
            }
        }
    }

    private int showMenu() {
        int counter = 1;
        int menuItemsAmount = GameLogic.menuItems.values().length;
        if (state == gameStates.GAME_STARTED) {
            System.out.println(String.format("It's %s's turn:", game.getCurrentPlayer().getName()));
        }
        System.out.println(String.format("Please Select <1-%d>", menuItemsAmount));
        for (GameLogic.menuItems item : GameLogic.menuItems.values()) {
            System.out.print(counter + ": " + item.ToString() + '\n');
            counter++;
        }
        return getUserSelection();
    }

    private int getUserSelection() {
        int input = 0;
        Boolean isGoodInput = false;
        while (!isGoodInput) {
            try {
                Scanner scanner = new Scanner(System.in);
                isGoodInput = true;
                input = scanner.nextInt();
                if (input > GameLogic.menuItems.values().length || input < 1)
                    throw new SelectionOutOfBoundException("1", String.valueOf(GameLogic.menuItems.values().length));
            } catch (InputMismatchException ex) {
                System.out.print("Please enter numbers only\n");
                isGoodInput = false;
            } catch (SelectionOutOfBoundException ex) {
                System.out.print(ex.getMessage());
                isGoodInput = false;
            }
        }
        return input;
    }

    //////////////////initialGame//////////////////
    private void initialGame() {
        boolean isGameLoaded = false;
        while (!isGameLoaded) {
            game.setXMLPath(getPathFromUser());
            try {
                if (game.initGameFromXML()) {
                    if (game.checkGameInputs()) {
                        System.out.print("\nGame initialized successfully\n\n");
                        isGameLoaded = true;
                        state = gameStates.GAME_LOADED;
                    } else {
                        System.out.println("The inputs in the XML file are invalid, Load another XML file!");
                    }
                } else {
                    System.out.print("\nGame initialization failed!\n\n");
                    isGameLoaded = false;
                }
            } catch (NotXMLFileException ex) {
                System.out.println(ex.getMessage());
            } catch (FileSystemNotFoundException e) {
                System.out.print("File Not Found. Try Again!\n");
                isGameLoaded = false;
            } catch (FileNotFoundException ex) {
                System.out.print("File Not Found. Try Again!\n");
                isGameLoaded = false;
            } catch (InvalidXMLInputsException ex) {
                System.out.println(ex.getMessage());
            } catch (UnmarshalException ex) {
                System.out.println("The XML file doesn't contain the requested data");
            } catch (JAXBException ex) {
                System.out.println("Path parsing didn't work");
            }
        }
    }

    private Path getPathFromUser() {
        String userInput;
        Path path = null;
        boolean isValidPath = false;

        while (!isValidPath) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Please Enter the path of the game's XML file:\n");
            userInput = scanner.nextLine();
            try {
                path = Paths.get(userInput);
                if (!path.toString().endsWith(".xml")) {
                    isValidPath = false;
                    System.out.println("We only accept .xml files here");
                } else {
                    isValidPath = true;
                }
            } catch (FileSystemNotFoundException ex) {
                System.out.print("The file doesn't exists");
                isValidPath = false;
            } catch (IllegalArgumentException ex) {
                System.out.println("The entered URI is invalid");
                isValidPath = false;
            }
        }

        return path;
    }
    //////////////////initialGame//////////////////

    //////////////////Play//////////////////
    private void play() {
        state = gameStates.GAME_STARTED;
        game.initGameComponents();
        showGameStates();
    }
    //////////////////Play//////////////////

    //////////////////gameState//////////////////
    private void showGameStates() {
        System.out.println(String.format("\n%s:\n", game.getCurrentPlayer().getName()));
        printCurrentPlayerBoards();
        System.out.println(String.format("score: %d\n", game.getCurrentPlayer().getStats().getScore()));
        waitForAKey();
    }
    //////////////////gameState//////////////////

    //////////////////MakeMove///////////////////
    private void makeMove() {
        Point attackingPoint = null;
        GameLogic.boardObj hitResult;
        boolean isGoodInput = false;

        game.getCurrentPlayer().getStats().startClock();
        System.out.println(String.format("%s's move:", game.getCurrentPlayer().getName()));
        printCurrentPlayerBoards();
        while (!isGoodInput) {
            attackingPoint = getUserMove();
            if(!game.getCurrentPlayer().checkIfCellWasAttackedBefore(attackingPoint)){
                isGoodInput = true;
            }else{
                System.out.println("You already tried this place, try again:");
            }
        }
        game.getCurrentPlayer().getStats().stopClock();
        hitResult = game.checkMove(attackingPoint);
        checkWhatPlayerHit(hitResult);
        waitForAKey();
    }

    private void checkWhatPlayerHit(GameLogic.boardObj hitResult) {
        if (hitResult.equals(GameLogic.boardObj.ship)) {
            System.out.println("Good Work, you hit a ship!");
            if (game.checkIfGameFinished()) {
                finishGame();
            } else {
                System.out.println("Your turn again");
            }
        } else if (hitResult.equals(GameLogic.boardObj.mine)) {
            System.out.println("Oh No! you hit a mine!");
        } else {
            System.out.println("No ships there :(, Maybe next time..");
        }
    }
    //////////////////MakeMove///////////////////

    /////////////////Statistics//////////////////
    private void showGameStatistics() {
        System.out.println("\n**********************************************\n");
        System.out.println(String.format("Total moves so far: %d", game.getGameStats().getMovesCounter()));
        System.out.println(String.format("It's been %s since the game started", game.getGameStats().getTotalElapsedTimeStr()));
        System.out.println("\n**********************************************\n");
        for (Player player : game.getPlayers()) {
            System.out.println(player.getName() + ":");
            System.out.println("Score: " + player.getStats().getScore());
            System.out.println("Missing shoots: " + player.getStats().getNumMiss());
            System.out.println("Average move time: " + player.getStats().getAvgMovesTimeAsString());
            System.out.println("\n**********************************************\n");
        }
        waitForAKey();
    }
    /////////////////Statistics//////////////////

    /////////////////PlantMine///////////////////
    private void PlantMine() {
        Point minePlace = null;
        boolean isGoodInput = false;
        if (game.checkIfPlayerCanAffordMine()) {
            game.getCurrentPlayer().getStats().startClock();
            System.out.println(String.format("%s's move:", game.getCurrentPlayer().getName()));
            System.out.println("Your ships:\n" + game.getCurrentPlayer().getMyShipBoardToPrint());
            System.out.println("Please select a place to plant a mine in your ship board");
            while (!isGoodInput) {
                minePlace = getUserMove();
                isGoodInput = game.getCurrentPlayer().checkMineLocation(minePlace);
                if (!isGoodInput) {
                    System.out.println("You can't place mine near to a ship");
                }
            }
            game.getCurrentPlayer().getStats().stopClock();
            game.addMineToPlayer(minePlace);
            System.out.println("Your mine was set successfully");
            waitForAKey();
            game.switchPlayers();
        } else {
            System.out.println("You can't put more than 2 mines");
        }
    }

    /////////////////PlantMine///////////////////

    /////////////////RestartGame/////////////////
    private void restartGame() {
        System.out.println("Game restarts!\n");
        state = gameStates.GAME_LOADED;
        game = new GameLogic();
    }
    /////////////////RestartGame/////////////////

    /////////////////FinishGame//////////////////
    private void finishGame() {
        String winningPlayer = playerChoseToQuit ? game.getAttackedPlayer().getName() : game.getCurrentPlayer().getName();
        System.out.println(String.format("\n%s has won the game!\n", winningPlayer));
        for (Player player : game.getPlayers()) {
            System.out.println(player.getName() + "'s board:\n");
            System.out.println(player.getMyShipBoardToPrint());
        }
        waitForAKey();
        showGameStatistics();
        game.setGameFinished(true);
    }

    /////////////////FinishGame//////////////////

    /////////////////AuxiliaryFunc///////////////
    private void waitForAKey() {
        System.out.println("Press any key to continue!");
        try {
            System.in.read();
        } catch (Exception e) {
        }
    }


    private void printCurrentPlayerBoards() {
        System.out.println("Your ships:\n" + game.getCurrentPlayer().getMyShipBoardToPrint());
        System.out.println("Your trace board:\n" + game.getCurrentPlayer().getAttackingBoardToPrint());
    }

    private Point getUserMove() {
        int row;
        char column;
        Point attackedCell;

        row = getRowInput();
        column = getColumnInput();
        attackedCell = new Point((column - 'A' + 1), row);
        return attackedCell;
    }

    private int getRowInput() {
        boolean isGoodInput = false;
        int row = 0;

        while (!isGoodInput) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.print(String.format("Row<1-%d>: ", game.getBoardSize()));
                row = scanner.nextInt();
                if (row < 0 || row > game.getBoardSize()) {
                    throw new SelectionOutOfBoundException("1", String.valueOf(game.getBoardSize()));
                } else {
                    isGoodInput = true;
                }
            } catch (SelectionOutOfBoundException ex) {
                System.out.println(ex.getMessage());
            } catch (InputMismatchException ex) {
                System.out.println("Please enter a number in Row");
            }
        }
        return row;
    }

    private char getColumnInput() {
        boolean isGoodInput = false;
        String userInput;
        Scanner scanner = new Scanner(System.in);
        char column = ' ';
        while (!isGoodInput) {
            System.out.print(String.format("Column<A-%c>: ", (game.getBoardSize() + 'A' - 1)));
            try {
                userInput = scanner.next();
                if (checkColumnInput(userInput)) {
                    column = userInput.toUpperCase().charAt(0);
                    isGoodInput = true;
                }
            } catch (SelectionOutOfBoundException ex) {
                System.out.println(ex.getMessage());
            }
        }

        return column;
    }

    private boolean checkColumnInput(String userInput) throws SelectionOutOfBoundException {
        char matrixUpperBound = (char) (game.getBoardSize() + 'A' - 1);
        if (userInput.toUpperCase().compareTo("A") < 0 ||
                userInput.toUpperCase().compareTo((String.valueOf(matrixUpperBound))) > 0) {
            throw new SelectionOutOfBoundException("A", String.valueOf(matrixUpperBound));
        }
        return true;
    }
    /////////////////AuxiliaryFunc///////////////
}