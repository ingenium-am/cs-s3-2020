package dbservices.dao;

import app.data.Board;
import app.data.Stone;
import app.util.DateUtils;
import dbservices.DBService;
import dbservices.PStatementBuilder;
import dbservices.ResultHandler;
import dbservices.SQLiteService;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Data Access Object class providing access to an underlying database
public class BoardDAO {

    // MAY BE instance of ANY CLASS implementing DBService interface
    private static final DBService DB_SERVICE = SQLiteService.getInstance();


    private static BoardDAO singleton = null;

    // Make class object Singleton - class has only ONE instance
    public static BoardDAO getSingleton() {
        if (singleton == null) {
            singleton = new BoardDAO();
        }
        return singleton;
    }

    // Constructor of Singleton must be private - not available externally
    private BoardDAO() {}


    public Board getGameBoardState(long gameId, byte boardSize) {
        // SQL raw query
        String statement = "SELECT X, Y, player_id, date FROM boards_map WHERE game_id=?";

        // ANONYMOUS implementation of PStatementBuilder interface overriding the only method by lambda
        PStatementBuilder psBuilder = connection -> {
            // Example of closure using variables from outer scope
            PreparedStatement pStatement = connection.prepareStatement(statement);
            pStatement.setLong(1, gameId);
            return pStatement;
        };

        // ANONYMOUS implementation of ResultHandler interface overriding the only method by lambda
        ResultHandler<Board> resultHandler = result -> {
            // Create empty board
            Board board = new Board(boardSize);
            // Loop over ResultSet
            while (result.next()) {
                Date date = DateUtils.integerToDate(result.getLong(4));     // (4) get date
                Stone stone = new Stone(result.getLong(3), date);           // (3) get player_id
                // Set corresponding cell of board (X, Y, Stone)
                board.setCell(result.getByte(1), result.getByte(2), stone); // (1,2) get X,Y
            }
            return board;
        };

        // Pass arguments as callbacks wrapped in objects and return result
        return DB_SERVICE.execQuery(psBuilder, resultHandler);
    }

    public boolean storeMove(long gameId, byte x, byte y, long playerId, Date date, boolean lastMove) {

        List<PStatementBuilder> psBuilders = new ArrayList<>(2);

        // SQL raw query
        String statement = "INSERT INTO boards_map(game_id, X, Y, player_id, date) VALUES (?, ?, ?, ?, ?)";

        PStatementBuilder psBuilderMove = connection -> {
            PreparedStatement pStatement = connection.prepareStatement(statement);
            pStatement.setLong(1, gameId);
            pStatement.setByte(2, x);
            pStatement.setByte(3, y);
            pStatement.setLong(4, playerId);
            pStatement.setLong(5, date.getTime());

            return pStatement;
        };
        psBuilders.add(psBuilderMove);

        // IF GAME must be CLOSED
        if (lastMove) {

            // Create and add additional statement-builder
            PStatementBuilder psBuilderClose = connection -> {
                // Example of closure using variables from outer scope
                PreparedStatement pStatement = connection.prepareStatement("UPDATE games SET closed=1 WHERE id=?");
                pStatement.setLong(1, gameId);
                return pStatement;
            };
            // Add to builders list
            psBuilders.add(psBuilderClose);
        }

        // Pass argument as a list of callbacks and return result
        return DB_SERVICE.execTransaction(psBuilders);
    }
}
