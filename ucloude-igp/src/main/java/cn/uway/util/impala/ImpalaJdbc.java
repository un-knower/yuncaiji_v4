package cn.uway.util.impala;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class ImpalaJdbc {
	private static ILogger LOG = LoggerManager.getLogger(ImpalaJdbc.class);

	private static final String JDBC_DRIVER_NAME_IMPALA = "org.apache.hive.jdbc.HiveDriver";
	private String connectUrl = "";
	private Connection conn = null;

	/**
	 * <b> 一定要在finally中调用{@link release}方法
	 * 
	 * @param connectUrl
	 *            连接字符串，格式：jdbc:hive2://ip:port/db_name;auth=noSasl
	 */
	public ImpalaJdbc(String connectUrl) {
		try {
			Class.forName(JDBC_DRIVER_NAME_IMPALA);
		} catch (ClassNotFoundException e) {
			LOG.error("缺少hive数据库驱动，程序将退出");
			System.exit(-1);
		}
		this.connectUrl = connectUrl;
		open();
	}

	/**
	 * 初始化数据库连接
	 */
	private void open() {
		try {
			conn = DriverManager.getConnection(connectUrl);
		} catch (SQLException e) {
			LOG.error("创建数据库连接失败");
		}
	}

	/**
	 * 执行sql
	 * 
	 * <p>
	 * <b> 一定要在finally中调用{@link ImpalaJdbc.closeResultSet}方法
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public ResultSet executeSql(String sql) throws SQLException {
		LOG.info(String.format("execute sql:%s", sql));
		Statement stmt = conn.createStatement();
		return stmt.executeQuery(sql);
	}

	/**
	 * 返回表结构信息(包含分区)，建议从schema文件中取
	 * 
	 * @param tableName
	 * @return
	 */
	public Map<String, String> describeTable(String tableName) {
		Map<String, String> map = new HashMap<String, String>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "describe " + tableName;
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				map.put(rs.getString(1), rs.getString(2));
			}
		} catch (SQLException e) {
			LOG.error(String.format("查询表结构失败:%s", tableName));
		} finally {
			closeResultSet(rs);
			closeStmt(stmt);
		}
		return map;
	}

	/**
	 * 显式关闭rs、stmt
	 * 
	 * @param rs
	 */
	public static void closeResultSet(ResultSet rs) {
		try {
			if (rs == null) {
				LOG.info("结果集为空，不用关闭！！");
			} else {
				Statement stmt = rs.getStatement();
				try {
					rs.close();
				} catch (SQLException e) {
					throw e;
				} finally {
					closeStmt(stmt);
				}
			}
		} catch (SQLException ex) {
			LOG.error("关闭失败，也许会影响程序正常运行");
		}
		LOG.info("rs已关闭");
	}
	
	public void refreshTable(String tableName)
	{
		Statement stmt = null;
		try {
			String sql = "refresh " + tableName;
			stmt = conn.createStatement();
			stmt.execute(sql);
		} catch (SQLException e) {
			LOG.error(String.format("刷新表失败:%s", tableName)+"原因："+e);
		} finally {
			closeStmt(stmt);
		}
	}

	/**
	 * 显式关闭stmt
	 */
	private static void closeStmt(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				LOG.error("关闭stmt失败，也许会影响程序正常运行");
			}
		}
		LOG.info("stmt已关闭");
	}

	/**
	 * <p>
	 * 建议在finally中调用本方法
	 * </p>
	 * 释放资源占用，包括conn
	 */
	public void release() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				LOG.error("关闭conn失败，也许会影响程序正常运行");
			}
		}
		LOG.info("conn已关闭");
	}

	public static void main(String[] args) {
		String connectUrl = "jdbc:hive2://192.168.15.192:21050/st;auth=noSasl";
		ImpalaJdbc jdbc = new ImpalaJdbc(connectUrl);

		// desc
		Map<String, String> m = jdbc.describeTable("stpar");
		for (Entry<String, String> entry : m.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}

		// select
		ResultSet rs = null;
		try {
			rs = jdbc.executeSql("select * from stpar limit 5");
			while (rs.next()) {
				System.out.println(rs.getString(1));
				System.out.println(rs.getString(2));
			}
		} catch (SQLException e) {
			LOG.error("查询失败");
		} finally {
			ImpalaJdbc.closeResultSet(rs);
			jdbc.release();
		}

	}
}
