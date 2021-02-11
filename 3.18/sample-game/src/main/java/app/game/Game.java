package app.game;

import app.data.Board;
import app.data.Move;
import app.data.Stone;

import java.util.Date;

public class Game {

    private long id;                        // 0 (default long) on create
    private final Board board;
    private final Date creationDate;
    private final long player1Id;           // is always defined - final
    private long player2Id;                 // may be 0 on create
    private GameState state;

    private final String gameHex;           // id replacement on create

    // Private (not available) - use dedicated static methods
    private Game(long id, Board board, Date creationDate,
                 long player1Id, long player2Id, GameState state) {

        this.id = id;
        this.board = board;
        this.creationDate = creationDate;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.state = state;

        // Create alternative identifier (created but not started/stored games has no ID)
        this.gameHex = generateHex();
    }

    // Static method to create instance from stored data
    public static Game load(long id, Board board, Date creationDate,
                            long player1Id, long player2Id, GameState state) {

        return new Game(id, board, creationDate, player1Id, player2Id, state);
    }

    // Static method to create a new game which may or may not be started/stored
    public static Game create(byte boardSize, long player1Id) {
        Board board = new Board(boardSize);
        Date creationDate = new Date();

        // Default value of uninitialized long is 0
        return new Game(0, board, creationDate, player1Id, 0, GameState.CREATED);
    }

    // Instance method to start a game
    public void start(long id, long player2Id) {
        this.id = id;
        this.player2Id = player2Id;
        this.state = GameState.STARTED;
    }

    public long getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public long getPlayer1Id() {
        return player1Id;
    }

    public long getPlayer2Id() {
        return player2Id;
    }

    public GameState getState() {
        return state;
    }

    public String getHex() {
        return gameHex;
    }

    public boolean hasPlayer(long playerId) {
        return playerId == player1Id || playerId == player2Id;
    }

    public long getOpponentId(long playerId) {
        if (playerId == player1Id) {
            return player2Id;
        } else if (playerId == player2Id) {
            return player1Id;
        }
        return -1;
    }

    // To do a move in the game by coordinates and the date
    public void doMove(byte x, byte y, Date moveDate) {

        // Allowed only if the game is already started
        if (state == GameState.STARTED) {

            long playerId = getTurn();                      // get player who's doing the move
            Stone stone = new Stone(playerId, moveDate);    // create a Stone by playersId and the date
            board.setCell(x, y, stone);                     // set Stone on the cell

            // Check if WIN or table is full
            if (win() || board.isFull()) {
                state = GameState.CLOSED;
            }
        }
    }

    // Check if the move is valid using board's method
    public boolean moveIsValid(byte x, byte y) {
        return board.coordsAreValid(x, y);
    }

    // Get turn by total moves count
    public Long getTurn() {
        // EVEN - player1, ODD - player2
        return board.getMoves() % 2 == 0 ? player1Id : player2Id;
    }

    // Check if the board has a winner
    public boolean win() {

        // TODO - implement WIN logic to return true if there is a winner
        //      Pure logic may be extracted as another method
        //      (See UI title in game.jsp after implementation)
        if (board.getMoves() > 8) {

            Move lastMove = board.getLastMove();
            byte lastX = lastMove.getX();
            byte lastY = lastMove.getY();
            byte checkX = lastX;
            byte checkY = lastY;

            int right = 0;
            int left = 0;
            int down = 0;
            int up = 0;
            int topLeftBottomRight = 0;
            int bottomRightTopLeft = 0;
            int bottomLeftTopRight = 0;
            int topRightBottomLeft = 0;


            while (checkX != 0 && board.getCell(--checkX, checkY) != null && isLastMovePlayer(checkX, checkY)) {
                left++;
                if (left + right >= 4)
                    return true;
            }

            checkX = lastX;

            while (checkX != board.getSize() - 1 && board.getCell(++checkX, checkY) != null && isLastMovePlayer(checkX, checkY)) {
                right++;
                if (right + left >= 4)
                    return true;
            }

            checkX = lastX;

            while (checkY != 0 && board.getCell(checkX, --checkY) != null && isLastMovePlayer(checkX, checkY)) {
                down++;
                if (down + up >= 4)
                    return true;
            }

            checkY = lastY;

            while (checkY != board.getSize() - 1 && board.getCell(checkX, ++checkY) != null && isLastMovePlayer(checkX, checkY)) {
                up++;
                if (up + down >= 4)
                    return true;
            }

            checkY = lastY;

            while (checkX != 0 && checkY != 0 && board.getCell(--checkX, --checkY) != null && isLastMovePlayer(checkX, checkY)) {
                topLeftBottomRight++;
                if (topLeftBottomRight + bottomRightTopLeft >= 4)
                    return true;
            }

            checkX = lastX;
            checkY = lastY;

            while (checkX != board.getSize() - 1 && checkY != board.getSize() - 1 && board.getCell(++checkX, ++checkY) != null && isLastMovePlayer(checkX, checkY)) {
                bottomRightTopLeft++;
                if (bottomRightTopLeft + topLeftBottomRight >= 4)
                    return true;
            }

            checkX = lastX;
            checkY = lastY;

            while (checkX != 0 && checkY != board.getSize() - 1 && board.getCell(--checkX, ++checkY) != null && isLastMovePlayer(checkX, checkY)) {
                bottomLeftTopRight++;
                if (bottomLeftTopRight + topRightBottomLeft >= 4)
                    return true;
            }

            checkX = lastX;
            checkY = lastY;

            while (checkX != board.getSize() - 1 && checkY != 0 && board.getCell(++checkX, --checkY) != null && isLastMovePlayer(checkX, checkY)) {
                topRightBottomLeft++;
                if (topRightBottomLeft + bottomLeftTopRight >= 4)
                    return true;
            }

        }

        return false;
    }

    boolean isLastMovePlayer(byte x, byte y) {
        if (board.getCell(x, y) != null) {
            return board.getCell(x, y).getPlayerId() == board.getLastMove().getPlayerID();
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;

        Game that = (Game) other;
        return this.getCreationDate().equals(that.getCreationDate())
                && this.getPlayer1Id() == that.getPlayer1Id()
                && this.getPlayer2Id() == that.getPlayer2Id();
    }


    // * PRIVATE METHODS * //

    // Alternative identifier - use date, board size and first player's ID
    private String generateHex() {
        String dateHex = Long.toHexString(creationDate.getTime());
        String sizeHex = Long.toHexString(board.getSize());
        String player1Hex = Long.toHexString(player1Id);

        return dateHex + sizeHex + player1Hex;
    }
}
