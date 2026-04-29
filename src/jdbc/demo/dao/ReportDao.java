package jdbc.demo.dao;

import jdbc.demo.model.QueryResult;

import java.sql.SQLException;
import java.util.List;

public interface ReportDao {
    QueryResult executeReport(int option, List<String> params) throws SQLException;
}
