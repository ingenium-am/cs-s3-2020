package dbservices;

import java.sql.ResultSet;
import java.sql.SQLException;

/* Wrapper of a parametrized method taking ResultSet
   and returning object of T type by corresponding logic */
public interface ResultHandler<T> {

	// See DAO (Data Access Objects) classes for implementation
	T handle(ResultSet resultSet) throws SQLException;
}
