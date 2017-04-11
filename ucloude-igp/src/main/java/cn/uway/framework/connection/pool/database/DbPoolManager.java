package cn.uway.framework.connection.pool.database;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.StringUtil;

/**
 * 数据库连接池管理器（目前只支持oracle）
 * 
 * @author yuy @ 2014-1-10
 */
public class DbPoolManager {

	/**
	 * 数据库连接池缓存器
	 */
	private static Map<Long, BasicDataSource> dataSourceCacher = new HashMap<Long, BasicDataSource>();

	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(DbPoolManager.class);

	/**
	 * 通过链接信息获取数据库链接<br>
	 * 
	 * @param connectionInfo
	 * @return Connection 数据库连接
	 * @throws SQLException
	 */
	public static Connection getConnection(DatabaseConnectionInfo connectionInfo) throws SQLException {
		if (StringUtil.isEmpty(connectionInfo.getUrl()) || StringUtil.isEmpty(connectionInfo.getUserName())
				|| StringUtil.isEmpty(connectionInfo.getPassword())) {
			return null;
		}
		if (connectionInfo.getUrl().indexOf("jdbc:") < 0) {
			return null;
		}
		BasicDataSource datasource = getDatasource(connectionInfo);
		return datasource.getConnection();
	}

	/**
	 * 获取数据源
	 * 
	 * @param url
	 * @param usr
	 * @param pwd
	 * @return
	 */
	public static BasicDataSource getDatasource(DatabaseConnectionInfo connectionInfo) {
		// 获取key码
		long keyCode = getKeyCode(connectionInfo);

		BasicDataSource dataSource = dataSourceCacher.get(keyCode);
		if (dataSource == null) {
			synchronized (dataSourceCacher) {
				dataSource = dataSourceCacher.get(keyCode);
				if (dataSource == null) {
					dataSource = createDatasource(connectionInfo);
					dataSourceCacher.put(keyCode, dataSource);
				}
			}
		}
		return dataSource;
	}

	/**
	 * 创建一个新的数据库连接池
	 * 
	 * @param version
	 *            JNDI的搜索名
	 * @param record
	 *            数据库连接池的基本信息
	 * @param constants
	 *            系统内置的环境管理类
	 * @return 新的数据库连接池，null表示创建失败
	 */
	private static BasicDataSource createDatasource(DatabaseConnectionInfo connectionInfo) {
		BasicDataSource datasource = null;
		try {
			LOGGER.debug("creating dbpool...");
			Properties properties = new Properties();
			properties.put("type", "javax.sql.DataSource");
			properties.put("driverClassName", connectionInfo.getDriver());
			properties.put("url", connectionInfo.getUrl());
			properties.put("username", connectionInfo.getUserName());
			properties.put("password", connectionInfo.getPassword());
			properties.put("maxActive", connectionInfo.getMaxActive());
			properties.put("maxIdle", connectionInfo.getMaxIdle());
			properties.put("maxWait", connectionInfo.getMaxWait());
			if (null != connectionInfo.getValidateQuery() && !"".equalsIgnoreCase(connectionInfo.getValidateQuery())) {
				properties.put("validationQuery", connectionInfo.getValidateQuery());
			}
			properties.put("testOnBorrow", "true");
			properties.put("testOnReturn", "true");
			properties.put("testWhileIdle", "true");
			datasource = (BasicDataSource) BasicDataSourceFactory.createDataSource(properties);
			LOGGER.debug("DbPoolManager：创建数据库连接池，用户：{}，连接信息：{}", connectionInfo.getUserName(), connectionInfo);
		} catch (Exception e) {
			LOGGER.error("DbPoolManager：创建数据源失败：", e);
		}
		return datasource;
	}

	/**
	 * 关闭现有的数据库连接池
	 * 
	 * @param dataSource
	 *            要关闭的数据库连接池
	 */
	public static void close(DatabaseConnectionInfo connectionInfo) {
		// 获取key码
		long keyCode = getKeyCode(connectionInfo);

		DataSource dataSource = dataSourceCacher.get(keyCode);
		try {
			if (dataSource != null) {
				Class<?> classz = dataSource.getClass();
				Class<?>[] types = new Class[0];
				Method method = classz.getDeclaredMethod("close", types);
				if (method != null) {
					method.setAccessible(true);
					Object[] args = new Object[0];
					method.invoke(dataSource, args);
				}
			}
		} catch (Exception e) {
			LOGGER.error("DbPoolManager: 尝试关闭原有的数据库连接池 [" + dataSource.getClass().getName() + "]时失败.", e);
		} finally {
			dataSource = null;
		}
	}

	/**
	 * 获取key码
	 * 
	 * @param url
	 * @param user
	 * @param pwd
	 * @return
	 */
	public static long getKeyCode(DatabaseConnectionInfo connectionInfo) {
		return crc32(connectionInfo.getUrl().trim() + connectionInfo.getUserName().trim() + connectionInfo.getPassword().trim());
	}

	/**
	 * compute the CRC-32 of a string
	 * 
	 * @param str
	 * @return
	 */
	public static long crc32(String str) {
		java.util.zip.CRC32 x = new java.util.zip.CRC32();
		x.update(str.getBytes());
		return x.getValue();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// for (int i = 0; i < 50; i++) {
		// Connection conn = DbPoolManager.getConnection("jdbc:oracle:thin:@132.120.32.26:1521:orcl", "gd_lte", "gd_lte");
		// DbUtil.close(null, null, conn);
		// LOGGER.debug("Conn is null:{}", (conn == null));
		// LOGGER.debug("Conn is close:{}", conn.isClosed());
		// LOGGER.debug("============================");
		// ThreadUtil.sleep(500);
		// }
	}

}
