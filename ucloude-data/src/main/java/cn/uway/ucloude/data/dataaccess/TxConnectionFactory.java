package cn.uway.ucloude.data.dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class TxConnectionFactory {
	private static final ILogger LOGGER = LoggerManager.getLogger(TxConnectionFactory.class);

    private static final ThreadLocal<Connection> TRANSACT_CONN = new ThreadLocal<Connection>();

    static Connection getCurrentConn() {
        return TRANSACT_CONN.get();
    }

    static Connection getTxConnection(DataSource dataSource) throws SQLException {
        Connection conn = TRANSACT_CONN.get();
        if (conn != null) {
            throw new IllegalStateException("Start second transaction in one thread");
        }
        conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        TRANSACT_CONN.set(conn);
        return conn;
    }

    static void closeTx(Connection conn) {
        try {
            if (conn != null) {
                if (conn.isReadOnly()) {
                    conn.setReadOnly(false);  // restore NOT readOnly before return to pool
                }
                conn.close();
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            TRANSACT_CONN.set(null);
        }
    }
}
