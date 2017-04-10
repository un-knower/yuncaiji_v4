package cn.uway.ucloude.data.dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlExecutorVoid {
    void run(Connection conn) throws SQLException;
}
