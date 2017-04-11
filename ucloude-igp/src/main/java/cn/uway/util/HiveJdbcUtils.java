package cn.uway.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class HiveJdbcUtils {
	private static ILogger LOG = LoggerManager.getLogger(HiveJdbcUtils.class);

	private static final String JDBC_DRIVER_NAME = "org.apache.hive.jdbc.HiveDriver";

	public static void main(String[] args) {
		// set the impalad host
		final String IMPALAD_HOST = "192.168.15.192";
		// port 21050 is the default impalad JDBC port
		final String IMPALAD_JDBC_PORT = "21050";
		final String CONNECTION_URL = "jdbc:hive2://" + IMPALAD_HOST + ':'
				+ IMPALAD_JDBC_PORT + "/;auth=noSasl";

		HiveJdbcUtils hive = new HiveJdbcUtils();
		Connection conn = hive.getHiveConnection(CONNECTION_URL, "", "");
		Map<String, String> map = hive.describeTable(conn, "cfg_city");
		System.out.println(map);
		hive.test();
	}

	public Connection getHiveConnection(String CONNECTION_URL, String userName,
			String password) {
		Connection conn = null;
		try {
			Class.forName(JDBC_DRIVER_NAME);
			if (CONNECTION_URL.toLowerCase().contains("nosasl"))
				conn = DriverManager.getConnection(CONNECTION_URL);
			else
				conn = DriverManager.getConnection(CONNECTION_URL, userName,
						password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	public Map<String, String> describeTable(Connection conn, String tableName) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			String sql = "describe " + tableName;
			Statement stmt = conn.createStatement();
			ResultSet res = stmt.executeQuery(sql);
			while (res.next()) {
				map.put(res.getString(1), res.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;

	}

	public void test() {
		try {
			// set the impalad host
			final String IMPALAD_HOST = "192.168.15.192";
			// port 21050 is the default impalad JDBC port
			final String IMPALAD_JDBC_PORT = "21050";

			final String CONNECTION_URL = "jdbc:hive2://" + IMPALAD_HOST + ':'
					+ IMPALAD_JDBC_PORT + "/;auth=noSasl";

			Class.forName(JDBC_DRIVER_NAME);
			Connection conn = DriverManager.getConnection(CONNECTION_URL);
			Statement stmt = conn.createStatement();

			// here is an example query based on one of the Hue Beeswax sample
			// tables
			// final String SQL_STATEMENT = "SELECT * FROM cfg_city limit 10";

			// 创建的表名
			String tableName = "cfg_city";
			/** 第一步:存在就先删除 **/
			String sql = "drop table " + tableName;
			// stmt.executeQuery(sql);

			// /** 第二步:不存在就创建 **/
			// sql = "create table "
			// + tableName
			// +
			// " (key int, value string)  row format delimited fields terminated by '\t'";
			// stmt.executeQuery(sql);

			// 执行“show tables”操作
			sql = "show tables '" + tableName + "'";
			System.out.println("Running:" + sql);
			ResultSet res = stmt.executeQuery(sql);
			System.out.println("执行“show tables”运行结果:");
			if (res.next()) {
				System.out.println("show tables : " + res.getString(1));
			}
			System.out.println("执行“show tables” end:");
			// 执行“describe table”操作
			sql = "describe " + tableName;
			System.out.println("Running:" + sql);
			res = stmt.executeQuery(sql);
			System.out.println("执行“describe table”运行结果:");
			while (res.next()) {
				System.out.println(res.getString(1) + "\t" + res.getString(2));
			}

			// 执行“load data into table”操作
			// String filepath = "/home/test.txt";
			// sql = "load data local inpath '" + filepath + "' into table "
			// + tableName;
			// System.out.println("Running:" + sql);
			// res = stmt.executeQuery(sql);

			// 执行“select * query”操作
			sql = "select * from " + tableName;
			System.out.println("Running:" + sql);
			res = stmt.executeQuery(sql);
			System.out.println("执行“select * query”运行结果:");
			while (res.next()) {
				System.out.println(res.getInt(1) + "\t" + res.getString(2));
			}

			// 执行“regular hive query”操作
			sql = "select count(1) from " + tableName;
			System.out.println("Running:" + sql);
			res = stmt.executeQuery(sql);
			System.out.println("执行“regular hive query”运行结果:");
			while (res.next()) {
				System.out.println(res.getString(1));
			}

			conn.close();
			conn = null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			LOG.error(JDBC_DRIVER_NAME + " not found!", e);
			System.exit(1);
		} catch (SQLException e) {
			e.printStackTrace();
			LOG.error("Connection error!", e);
			System.exit(1);
		}
	}
}