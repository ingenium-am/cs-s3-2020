package dbservices.dao;

import app.game.Game;
import app.data.Board;
import app.game.GameState;
import app.util.DateUtils;
import dbservices.DBService;
import dbservices.PStatementBuilder;
import dbservices.ResultHandler;
import dbservices.SQLiteService;

import java.sql.PreparedStatement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Data Access Object class providing access to an underlying database
public class GameDAO {

    // MAY BE instance of ANY CLASS implementing DBService interface
    private static final DBService DB_SERVICE = SQLiteService.getInstance();


    private static GameDAO singleton = null;

    // Make class object Singleton - class has only ONE instance
    public static GameDAO getSingleton() {
        if (singleton == null) {
            singleton = new GameDAO();
        }
        return singleton;
    }

    // Constructor of Singleton must be private - not available externally
    private GameDAO() {}


    public long insertGame(byte boardSize, long p1id, long p2id, long dateCreated) {
        // SQL raw query
        String statement = "INSERT INTO games (board_size, date_created, player1, player2) VALUES (?, ?, ?, ?)";

        // ANONYMOUS implementation of PStatementBuilder interface overriding the only method by lambda
        PStatementBuilder psBuilder = connection -> {
            // Example of closure using variables from outer scope
            PreparedStatement pStatement = connection.prepareStatement(statement);
            pStatement.setByte(1, boardSize);
            pStatement.setLong(2, dateCreated);
            pStatement.setLong(3, p1id);
            pStatement.setLong(4, p2id);
            return pStatement;
        };

        // ANONYMOUS implementation of ResultHandler interface overriding the only method by lambda
        ResultHandler<Long> resultHandler = result -> {
            if (result.next()) {            // Check if ResultSet of query has result (expecting ID)
                return result.getLong(1);   // Get data from ResultSet and return long type
            }
            return (long) -1;               // Or return negative value indicating fail of insert
        };

        // Pass arguments as callbacks wrapped in objects and return result
        return DB_SERVICE.execInsert(psBuilder, resultHandler);
    }

    public Map<String, Game> getAllGames() {
        // SQL raw query
        String statement = "SELECT * FROM games";

        // ANONYMOUS implementation of PStatementBuilder interface overriding the only method by lambda
        PStatementBuilder psBuilder = connection -> connection.prepareStatement(statement);

        // ANONYMOUS implementation of PStatementBuilder interface overriding the only method by lambda
        ResultHandler<Map<String, Game>> resultHandler = result -> {
            // Create empty HashMap to collect results
            Map<String, Game> allGames = new HashMap<>();

            // Loop over ResultSet and put created games into HashMap
            while (result.next()) {

                long gameId = result.getLong(1);
                Board board = BoardDAO.getSingleton().getGameBoardState(gameId, result.getByte(2));
                Date date = DateUtils.integerToDate(result.getLong(3));
                long player1id = result.getLong(4);
                long player2id = result.getLong(5);
                byte state = result.getByte(6);

                GameState gameState = state == 0 ? GameState.STARTED : GameState.CLOSED;

                Game game = Game.load(gameId, board, date, player1id, player2id, gameState);
                allGames.put(game.getHex(), game);
            }

            return allGames;
        };

        // Pass arguments as callbacks wrapped in objects and return result
        return DB_SERVICE.execQuery(psBuilder, resultHandler);
    }
}
