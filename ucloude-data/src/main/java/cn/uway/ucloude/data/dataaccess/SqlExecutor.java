package cn.uway.ucloude.data.dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlExecutor<T> {
    T run(Connection conn) throws SQLException;
}
