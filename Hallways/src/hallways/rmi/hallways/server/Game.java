package hallways.rmi.hallways.server;

import hallways.rmi.hallways.common.*;
import hallways.rmi.hallways.common.board.Board;
import hallways.rmi.hallways.common.board.Cell;
import hallways.rmi.hallways.common.board.Grid;
import hallways.rmi.hallways.common.rabbitmqprovider.RabbitmqProvider;
import hallways.rmi.hallways.common.status.*;

public class Game  {

    Controller controller;

    Grid grid;

    PlayerItem currentPlayer = PlayerItem.player1;
    GameStatus gameStatus;
    MoveStatus moveStatus;
    Board currentBoard;
    Cell neighbourCellLeftOrTop;
    Cell neighbourCellRightOrDown;
    RabbitmqProvider rabbitmqProvider;


    public Game(Controller controller) {
        this.controller = controller;
        grid = new Grid();
        gameStatus = GameStatus.CONTINUE;
        try {
            rabbitmqProvider = new RabbitmqProvider();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PlayerItem getCurrentPlayer() {
        return currentPlayer;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public MoveStatus getMoveStatus() {
        return moveStatus;
    }

    public Board getCurrentBoard() {
        return currentBoard;
    }

    public Cell getNeighbourCellLeftOrTop() {
        return neighbourCellLeftOrTop;
    }

    public Cell getNeighbourCellRightOrDown() {
        return neighbourCellRightOrDown;
    }

    public void processMove(PlayerItem player, Board clickedBoard) throws Exception {

        if (!validateMove(player, clickedBoard)) {
            moveStatus = MoveStatus.FAILED;
            return;
        }

        currentBoard = grid.getBoard(clickedBoard.getPosition(),
                clickedBoard.getIndexX(), clickedBoard.getIndexY());

        if (player == PlayerItem.player1)
            currentBoard.setStatus(BoardStatus.OCCUPIED_BY_FIRST);
        else currentBoard.setStatus(BoardStatus.OCCUPIED_BY_SECOND);

        boolean firstCellOccupied = false, secondCellOccupied = false;

        if (clickedBoard.getNeighbourLeftOrTop() != null) {
            neighbourCellLeftOrTop = grid.getCell(clickedBoard.getNeighbourLeftOrTop().getX(),
                    clickedBoard.getNeighbourLeftOrTop().getY());
            firstCellOccupied = processCell(neighbourCellLeftOrTop, currentBoard);
        }
        if (clickedBoard.getNeighbourRightOrDown() != null) {
            neighbourCellRightOrDown = grid.getCell(clickedBoard.getNeighbourRightOrDown().getX(),
                    clickedBoard.getNeighbourRightOrDown().getY());
            secondCellOccupied = processCell(neighbourCellRightOrDown, currentBoard);
        }

        checkGameStatus(player);

        if (!firstCellOccupied && !secondCellOccupied && gameStatus == GameStatus.CONTINUE)
            changePlayer();

        moveStatus = MoveStatus.SUCCESS;
    }

    private void checkGameStatus(PlayerItem player) throws Exception {
        boolean allCellsOccupied = true;
        int nFirstCells = 0, nSecondCells = 0;
        for (int i = 0; i < Grid.SIZE; i++)
            for (int j = 0; j < Grid.SIZE; j++) {
                if (grid.getCell(i, j).getStatus() == CellStatus.FREE) {
                    allCellsOccupied = false;
                    break;
                }
                else if (grid.getCell(i,j).getStatus()==CellStatus.OCCUPIED_BY_FIRST)
                    nFirstCells++;
                else if (grid.getCell(i,j).getStatus()==CellStatus.OCCUPIED_BY_SECOND)
                    nSecondCells++;
            }
        if (allCellsOccupied) {
            if (nFirstCells > nSecondCells) {
                gameStatus = GameStatus.FIRST_WON;
                rabbitmqProvider.SendMessage("score", String.format("final-%d-%d-%d", 1, nFirstCells, nSecondCells));
            }
            else if (nFirstCells < nSecondCells) {
                gameStatus = GameStatus.SECOND_WON;
                rabbitmqProvider.SendMessage("score", String.format("final-%d-%d-%d", 2, nFirstCells, nSecondCells));
            }
        }

    }

    private boolean validateMove(PlayerItem player, Board clickedBoard) {
        if (currentPlayer != player) return false;
        return clickedBoard.getStatus() == BoardStatus.FREE;
    }

    private void changePlayer(){
        if (currentPlayer == PlayerItem.player1)
            currentPlayer = PlayerItem.player2;
        else currentPlayer = PlayerItem.player1;
    }

    private boolean processCell(Cell cell, Board lastBoard) throws Exception {

        if (cell == null || cell.getStatus() != CellStatus.FREE)
            return false;

        BoardStatus leftBoardStatus = cell.getLeftBoard().getStatus();
        BoardStatus rightBoardStatus = cell.getRightBoard().getStatus();
        BoardStatus topBoardStatus = cell.getTopBoard().getStatus();
        BoardStatus downBoardStatus = cell.getDownBoard().getStatus();

        if ((leftBoardStatus == BoardStatus.OCCUPIED_BY_FIRST ||
                leftBoardStatus == BoardStatus.OCCUPIED_BY_SECOND) &&
                (rightBoardStatus == BoardStatus.OCCUPIED_BY_FIRST ||
                        rightBoardStatus == BoardStatus.OCCUPIED_BY_SECOND) &&
                (topBoardStatus == BoardStatus.OCCUPIED_BY_FIRST ||
                        topBoardStatus == BoardStatus.OCCUPIED_BY_SECOND) &&
                (downBoardStatus == BoardStatus.OCCUPIED_BY_FIRST ||
                        downBoardStatus == BoardStatus.OCCUPIED_BY_SECOND)) {

            if (lastBoard.getStatus() == BoardStatus.OCCUPIED_BY_FIRST) {
                cell.setStatus(CellStatus.OCCUPIED_BY_FIRST);
                rabbitmqProvider.SendMessage("score", "1");
            }
            else
            {
                cell.setStatus(CellStatus.OCCUPIED_BY_SECOND);
                rabbitmqProvider.SendMessage("score", "2");
            }

            return true;
        }

        return false;
    }

}
