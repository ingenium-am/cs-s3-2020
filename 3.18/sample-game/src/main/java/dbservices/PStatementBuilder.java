package dbservices;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/* Wrapper of a method taking Connection and returning PreparedStatement
   (helps to wrap PreparedStatement creating logic without requesting DB connection) */
public interface PStatementBuilder {

    // See DAO (Data Access Objects) classes for implementation
    PreparedStatement prepare(Connection connection) throws SQLException;
}
