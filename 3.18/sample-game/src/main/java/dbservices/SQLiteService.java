package dbservices;

import org.apache.commons.dbutils.DbUtils;

import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

// Database service for SQLite DBMS implementing DBService interface methods
// See DBService class annotation
public class SQLiteService implements DBService {

    // SQLite database file location
    private static File DB_FILE = null;


    private static SQLiteService singleton = null;

    // Make class object Singleton - class has only ONE instance.
    public static SQLiteService getInstance() {
        if (singleton == null) {
            singleton = new SQLiteService();
        }
        return singleton;
    }

    // Try to initialize DB file created build-time (see pom.xml)
    // Constructor of Singleton must be private - not available externally
    private SQLiteService() {

        // The body MAY BE COMMENTED-OUT if DB_FILE path is set EXPLICITLY

        // Get VM property which is set by 'server/InitContext.java'
        String webInfPath = System.getProperty("web-inf-path");
        System.out.println("GET - web-inf-path: " + webInfPath);

        // Get value of 'web-inf-path' as an absolute path to 'db.properties' file
        // Paths.get() joins path parts regardless of OS separator ('\' or '/')
        File propertiesFile = Paths.get(webInfPath, "db.properties").toFile();

        // Try to read 'db.properties' file
        try (InputStream input = new FileInputStream(propertiesFile)) {

            Properties prop = new Properties();
            prop.load(input);

            // Get value of 'db.local.path' as an absolute path to DB file
            String pathString = prop.getProperty("db.local.path");
            System.out.println("GET - db.local.path: " + pathString);

            if (pathString != null) {
                File path = new File(pathString);

                if (path.exists()) {
                    Path dbPath = Paths.get(pathString, "sample_game.db");

                    // Set static variable as a File object pointing to SQLite DB
                    DB_FILE = dbPath.toFile();
                }
            }

            // Invoke class initializer to register class for JDBC DriverManager
            Class.forName("org.sqlite.JDBC");

        } catch (ClassNotFoundException | IOException e) {
            System.out.println(">>> LOCAL DB FOLDER IS NOT FOUND!");
            handleException(e);
        }
    }


    // Connect to database by provided JDBC driver and a path (DB_FILE)
    // Requesting side MUST CLOSE connection as a resource after use
    @Override
    public Connection getConnection() throws SQLException {

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
        System.out.println(">>> Connect to DB");

        return connection;
    }


    /* Generic method (parametrized with type T) expecting SELECT queries
       wrapped in PStatementBuilder object and returning result (type T)
       of generic ResultHandler's handler method */
    @Override
    public <T> T execQuery(PStatementBuilder psBuilder, ResultHandler<T> handler) {
        Connection connection = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;
        T data = null;

        try {
            connection = getConnection();               // Get Connection

            pStatement = psBuilder.prepare(connection); // Get PreparedStatement from PStatementBuilder
            resultSet = pStatement.executeQuery();      // Get ResultSet executing PreparedStatement
            data = handler.handle(resultSet);           // Get target object (T) using ResultHandler object

        } catch (SQLException e) {
            handleException(e);

        } finally {
            // Use DbUtils to avoid additional try/catch clauses
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pStatement);
            DbUtils.closeQuietly(connection);
        }
        return data;
    }


    /* Expecting INSERT queries wrapped in PStatementBuilder object
       and returning result of ResultHandler's handler method (i.e. ID of inserted record) */
    @Override
    public long execInsert(PStatementBuilder psBuilder, ResultHandler<Long> handler) {
        Connection connection = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;
        long id = -1;

        try {
            connection = getConnection();               // Get Connection

            pStatement = psBuilder.prepare(connection); // Get PreparedStatement from PStatementBuilder
            pStatement.execute();                       // Execute PreparedStatement
            resultSet = pStatement.getGeneratedKeys();  // Get ResultSet (ID keys) from PreparedStatement

            // Get id of created record using ResultHandler object
            id = handler.handle(resultSet);

        } catch (SQLException e) {
            handleException(e);

        } finally {
            // Use DbUtils to avoid additional try/catch clauses
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pStatement);
            DbUtils.closeQuietly(connection);
        }
        return id;
    }

    /* Expecting UPDATE or DELETE queries wrapped in PStatementBuilder object
       and returning updated records count */
    @Override
    public int execUpdate(PStatementBuilder psBuilder) {
        Connection connection = null;
        PreparedStatement pStatement = null;
        int count = -1;

        try {
            connection = getConnection();               // Get Connection

            pStatement = psBuilder.prepare(connection); // Get PreparedStatement from PStatementBuilder
            pStatement.execute();                       // Execute PreparedStatement
            count = pStatement.getUpdateCount();        // Get update count from PreparedStatement

        } catch (SQLException e) {
            handleException(e);

        } finally {
            // Use DbUtils to avoid additional try/catch clauses
            DbUtils.closeQuietly(pStatement);
            DbUtils.closeQuietly(connection);
        }
        return count;
    }

    /* Expecting list of different queries wrapped in PStatementBuilder objects
       which must executed in sequence as a single transaction */
    @Override
    public boolean execTransaction(List<PStatementBuilder> psBuilders) {
        Connection connection = null;
        PreparedStatement pStatement = null;
        boolean committed = false;

        try {
            connection = getConnection();                   // Get Connection

            connection.setAutoCommit(false);                // TURN OFF AUTOCOMMIT

            // Loop over builders of PreparedStatement
            for (PStatementBuilder psBuilder : psBuilders) {

                pStatement = psBuilder.prepare(connection); // Get PreparedStatement from PStatementBuilder
                pStatement.execute();                       // Execute and close each statement
                pStatement.close();                         // Close for current iteration
            }

            connection.commit();                            // COMMIT TRANSACTION

            // IF NO exception - successfully committed
            committed = true;

        } catch (SQLException e) {

            // EXCEPTION means NOT COMMITTED - try to ROLLBACK TRANSACTION
            try {
                // Use DbUtils to avoid additional try/catch clauses
                DbUtils.rollback(connection);

            } catch (SQLException e1) {
                handleException(e);
            }
            handleException(e);

        } finally {
            // Use DbUtils to avoid additional try/catch clauses
            DbUtils.closeQuietly(pStatement);
            DbUtils.closeQuietly(connection);
        }
        return committed;
    }


    // * PRIVATE METHODS * //

    // Wrapper of exceptions printing message and stacktrace (if set)
    public void handleException(Exception e) {
        System.out.println(e.getMessage());

        // Comment out to hide stacktrace
        e.printStackTrace();
    }
}
