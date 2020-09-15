package am.ingenium.tictactoe.game;

import am.ingenium.tictactoe.data.Player;
import am.ingenium.tictactoe.data.board.FixedSizeBoard;

public interface Game {

    String getName();

    FixedSizeBoard getBoard();
    Player getCurrentPlayer();

    void switchTurn();
    boolean setCell(int x, int y);
    boolean boardIsFull();
    boolean turnWins();
}
