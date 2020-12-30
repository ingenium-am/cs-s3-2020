package dbservices;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


// Any instance of a class implementing this interface may be set as DB service.
// See DAO (Data Access Objects) classes.
public interface DBService {

    Connection getConnection() throws SQLException;

    <T> T execQuery(PStatementBuilder psBuilder, ResultHandler<T> handler);

    long execInsert(PStatementBuilder psBuilder, ResultHandler<Long> handler);

    int execUpdate(PStatementBuilder psBuilder);

    boolean execTransaction(List<PStatementBuilder> psBuilders);
}
