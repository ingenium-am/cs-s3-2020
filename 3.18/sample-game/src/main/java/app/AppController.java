package app;

import app.game.Game;
import app.game.GameState;
import app.game.MoveStatus;
import dbservices.dao.BoardDAO;
import dbservices.dao.GameDAO;
import org.json.JSONArray;
import server.shared.DataCache;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* Controller abstraction in front of application
   helping to manage games (creat, start, close) and gameplay */
public class AppController {

    // New games map (key is gameHex - waiting game has no ID as not stored in DB)
    private static final Map<String, Game> waitingGames = new ConcurrentHashMap<>();

    private static AppController singleton = null;

    // Make class object Singleton - class has only ONE instance
    public static AppController getSingleton() {
        if (singleton == null) {
            singleton = new AppController();
        }
        return singleton;
    }

    // Constructor of Singleton must be private - not available externally
    private AppController() {}

    public Map<String, Game> getWaitingGames() {
        return waitingGames;
    }

    // Create a new game and put into waitingGames
    public Game createGame(byte boardSize, long waitingPlayerId) {
        Game game = Game.create(boardSize, waitingPlayerId);
        waitingGames.put(game.getHex(), game);
        return game;
    }

    // Start a waiting game and join second player
    public boolean startGame(Game game, long joiningPlayerId) {

        if (waitingGames.containsKey(game.getHex())) {

            GameDAO gameDAO = GameDAO.getSingleton();

            // First, store the game and get ID
            long createdTime = game.getCreationDate().getTime();
            long gameId = gameDAO.insertGame(game.getBoard().getSize(), game.getPlayer1Id(), joiningPlayerId, createdTime);

            // If succeeded - ID must be a positive integer
            if (gameId > 0) {
                DataCache.updateGamesCache(game);       // put the game in cache
                game.start(gameId, joiningPlayerId);    // turn game's state to STARTED
                waitingGames.remove(game.getHex());     // remove from map (already available from DataCache)
                return true;
            }
        }
        return false;
    }

    // Try to make a move and return corresponding MoveStatus
    public MoveStatus makeMove(String gameHex, byte x, byte y, long playerId) {

        Game game = DataCache.getGame(gameHex);

        // No such game in cache
        if (game == null) {
            // But in waitingGames
            if (waitingGames.containsKey(gameHex)) {
                return MoveStatus.NOT_STARTED;
            }
            // Must be handled in case if game not found
            return null;
        }

        // The game is FINISHED - moves are not allowed
        if (game.getState() == GameState.CLOSED) {
            return MoveStatus.FINISHED;
        }

        // Unauthorized move - game has no player with this ID
        if (!game.hasPlayer(playerId)) {
            return MoveStatus.NOT_AUTHORIZED;
        }

        // Not player's turn
        if (game.getTurn() != playerId) {
            return MoveStatus.NOT_YOUR_TURN;
        }

        // Check if move is valid
        if (game.moveIsValid(x, y)) {

            // Get the date and DO MOVE
            Date moveDate = new Date();
            game.doMove(x, y, moveDate);

            // Check if the move was the last (game is closed after the move)
            boolean lastMove = game.getState() == GameState.CLOSED;

            // Try to store in DB (see lastMove case in calling storeMove method)
            boolean success = BoardDAO.getSingleton().storeMove(game.getId(), x, y, playerId, moveDate, lastMove);

            // FINALLY return status IF the move is successfully stored
            if (success) {
                return postMoveResolver(game);
            }
        }

        // The case if the last <if> clause is false
        return MoveStatus.INVALID_MOVE;
    }

    public boolean gameHasPlayer(String gameHex, long playerId) {
        Game storedGame = DataCache.getGame(gameHex);

        if (storedGame != null && storedGame.hasPlayer(playerId)) {
            return true;
        } else {
            return waitingGames.values().stream()
                    .map(Game::getHex)
                    .anyMatch(hex -> hex.equals(gameHex));
        }
    }

    public boolean hasWaitingGame(String gameHex) {
        Game game = waitingGames.get(gameHex);
        return game != null;
    }

    public JSONArray getBoardJson(String gameHex) {
        JSONArray boardArray = new JSONArray();

        Game game = DataCache.getGame(gameHex);
        // if no such Game in DataCache - board must be anyway empty
        if (game != null) {
            List<Long[]> boardStones = game.getBoard().getState();
            for (Long[] stones : boardStones) {
                boardArray.put(stones);
            }
        }
        return boardArray;
    }


    // * PRIVATE METHODS * //

    private MoveStatus postMoveResolver(Game game) {

        // FIRST, check if win
        if (game.win()) {
            return MoveStatus.WIN;
        }

        // SECOND, check if board is full (last stone may be winner)
        if (game.getBoard().isFull()) {
            return MoveStatus.DRAW;
        }

        // Otherwise - ordinary move
        return MoveStatus.SUCCESS;
    }
}
