package dbservices.dao;

import dbservices.DBService;
import dbservices.PStatementBuilder;
import dbservices.ResultHandler;
import dbservices.SQLiteService;
import app.data.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

// Data Access Object class providing access to an underlying database
public class UserDAO {

    // MAY BE instance of ANY CLASS implementing DBService interface
    private static final DBService DB_SERVICE = SQLiteService.getInstance();


    private static UserDAO singleton = null;

    // Make class object Singleton - class has only ONE instance
    public static UserDAO getSingleton() {
        if (singleton == null) {
            singleton = new UserDAO();
        }
        return singleton;
    }

    // Constructor of Singleton must be private - not available externally
    private UserDAO() {}


    public User getUserByLogin(String login) {
        // SQL raw query
        String statement = "SELECT * FROM users WHERE login=?";

        // ANONYMOUS implementation of PStatementBuilder interface overriding the only method by lambda
        PStatementBuilder psBuilder = connection -> {
            // Example of closure using variables from outer scope
            PreparedStatement pStatement = connection.prepareStatement(statement);
            pStatement.setString(1, login);
            return pStatement;
        };

        // ANONYMOUS implementation of ResultHandler interface overriding the only method by lambda
        ResultHandler<User> resultHandler = result -> {
            // Check if ResultSet of query has result
            if (result.next()) {
                // Get data from ResultSet and return object of parametrization type (User)
                return new User(result.getLong(1), result.getString(2), result.getString(3));
            }
            // Must be checked for null from calling method
            return null;
        };

        // Pass arguments as callbacks wrapped in objects and return result
        return DB_SERVICE.execQuery(psBuilder, resultHandler);
    }

    public long insertUser(String login, String passwordHash) {
        // SQL raw query
        String statement = "INSERT INTO users (login, password) VALUES (?, ?)";

        // ANONYMOUS implementation of PStatementBuilder interface overriding the only method by lambda
        PStatementBuilder psBuilder = connection -> {
            // Example of closure using variables from outer scope
            PreparedStatement pStatement = connection.prepareStatement(statement);
            pStatement.setString(1, login);
            pStatement.setString(2, passwordHash);
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

    public boolean removeUser(long userId) {
        // SQL raw query
        String statement = "DELETE FROM users WHERE id=?";

        // ANONYMOUS implementation of PStatementBuilder interface overriding the only method by lambda
        PStatementBuilder psBuilder = (Connection connection) -> {
            // Example of closure using variables from outer scope
            PreparedStatement pStatement = connection.prepareStatement(statement);
            pStatement.setLong(1, userId);
            return pStatement;
        };

        // Pass argument as a callback and return result of checking update count
        return DB_SERVICE.execUpdate(psBuilder) == 1;
    }

    public Map<Long, User> getAllUsers() {
        // SQL raw query
        String statement = "SELECT * FROM users";

        // ANONYMOUS implementation of PStatementBuilder interface overriding the only method by lambda
        PStatementBuilder psBuilder = connection -> connection.prepareStatement(statement);

        // ANONYMOUS implementation of PStatementBuilder interface overriding the only method by lambda
        ResultHandler<HashMap<Long, User>> resultHandler = result -> {
            // Create empty HashMap to collect results
            HashMap<Long, User> allUsers = new HashMap<>();
            // Loop over ResultSet and put created users into HashMap
            while (result.next()) {
                User currentUser = new User(result.getLong(1), result.getString(2), result.getString(3));
                allUsers.put(currentUser.getId(), currentUser);
            }
            return allUsers;
        };

        // Pass arguments as callbacks wrapped in objects and return result
        return DB_SERVICE.execQuery(psBuilder, resultHandler);
    }
}
