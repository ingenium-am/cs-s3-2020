package am.ingenium.tictactoe.game;

import am.ingenium.tictactoe.data.Player;
import am.ingenium.tictactoe.data.board.FixedSizeBoard;
import am.ingenium.tictactoe.data.board.TicTacToeBoard;

import java.util.LinkedList;
import java.util.List;

public final class TicTacToe implements Game {

    private final TicTacToeBoard board = new TicTacToeBoard();
    private final List<Player> players = new LinkedList<>();


    public TicTacToe(Player player1, Player player2) {
        players.add(player1);
        players.add(player2);
    }

    @Override
    public String getName() {
        return "Tic Tac Toe";
    }

    @Override
    public FixedSizeBoard getBoard() {
        return board;
    }

    @Override
    public Player getCurrentPlayer() {
        return players.get(0);
    }

    @Override
    public void switchTurn() {
        // Remove first element and append at the end (switching players)
        players.add(players.remove(0));
    }

    @Override
    public boolean setCell(int x, int y) {
        // Make local variable
        int[][] arr = board.getBoardArray();

        // Check if cell is free
        if (arr[x][y] == 0) {
            // Set new value by player's id confirm successful set
            arr[x][y] = getCurrentPlayer().getId();
            return true;
        }
        // Cell is already set - nothing changed
        return false;
    }

    @Override
    public boolean boardIsFull() {
        // Call board's (or its parent) method
        return board.isFull();
    }

    @Override
    public boolean turnWins() {
        // Turn wins if one of conditions below is true
        return checkRows() || checkColumns() || checkDiagonal1() || checkDiagonal2();
    }


    // PRIVATE METHODS //

    private boolean checkRows() {

        // LOOP over ROWS
        for (int[] row : board.getBoardArray()) {

            boolean guard = true;   // Set default value

            // LOOP over COLUMNS
            for (int col : row) {
                // If cell is not current player's id
                if (col != getCurrentPlayer().getId()) {
                    guard = false;  // set guard to false
                    break;          // exit COLUMN loop
                }
            }

            // Check if guard remains true after COLUMN loop
            if (guard) {
                return true;        // Return true immediately
            }
        }
        // Return false by default (if loops are over - no match)
        return false;
    }

    private boolean checkColumns() {

        // LOOP over X index
        for (int i = 0; i < getBoard().getWidth(); i++) {

            boolean guard = true;   // Set default value

            // LOOP over ROWS
            for (int[] row : board.getBoardArray()) {
                // If 'i' index cell is not current player's id
                if (row[i] != getCurrentPlayer().getId()) {
                    guard = false;  // set guard to false
                    break;          // exit ROW loop
                }
            }

            // Check if guard remains true after ROW loop
            if (guard) {
                return true;        // Return true immediately
            }
        }
        // Return false by default (if loops are over - no match)
        return false;
    }

    // Diagonal TopLeft-BottomRight
    private boolean checkDiagonal1() {

        // Check single index for both dimensions (board is square)
        for (int i = 0; i < getBoard().getWidth(); i++) {
            // Get cells by the same 'i' index - 0,0; 1,1; 2,2
            int cell = board.getBoardArray()[i][i];

            // If cell is not current player's id
            if (cell != getCurrentPlayer().getId()) {
                return false;   // further check is unnecessary
            }
        }
        // Return true by default (if loop is over - match all cell)
        return true;
    }

    // Diagonal TopRight-BottomLeft
    private boolean checkDiagonal2() {

        // Check by double-index loop
        for (int i = 0, j = getBoard().getWidth() - 1; i < getBoard().getHeight() && j >= 0; i++, j--) {
            // Get cells by 'i' and 'j' indices
            int cell = board.getBoardArray()[i][j];

            // If cell is not current player's id
            if (cell != getCurrentPlayer().getId()) {
                return false;   // further check is unnecessary
            }
        }
        // Return true by default (if loop is over - match all cell)
        return true;
    }
}
