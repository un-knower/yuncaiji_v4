package cn.uway.framework.accessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.framework.connection.pool.database.DbPoolManager;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.DbUtil;

/**
 * DbAccessor DB 接入器暂时不使用连接池，连接池可能会占用数据库资源而一直不释放，暂时使用JDBC直接创建连接
 * DbAccessor输出一个数据库连接对象。供parser使用
 * 
 * @author chenrongqiang 2012-12-4
 */
public class DbAccessor extends AbstractAccessor{
	protected static final ILogger LOGGER = LoggerManager.getLogger(DbAccessor.class);
			
	/**
	 * 数据库连接 目前直接使用JDBC数据库连接
	 */
	private DatabaseConnectionInfo connInfo;

	/**
	 * 数据库原生连接对象
	 */
	private Connection connection;

	public DbAccessor(){
		super();
	}

	@Override
	public void setConnectionInfo(ConnectionInfo connInfo){
		if(!(connInfo instanceof DatabaseConnectionInfo))
			throw new IllegalArgumentException("错误的连接信息.请配置有效的数据库连接配置信息");
		this.connInfo = (DatabaseConnectionInfo)connInfo;
	}

	/**
	 * 数据库接入器接入方法 通过任务配置中连接信息 创建数据库连接 返回JdbcAccessOutObject
	 */
	public AccessOutObject access(GatherPathEntry path) throws Exception{
		this.startTime = new Date();
		connection = getConnection(connInfo);
		this.gatherObj = path.getPath();
		JdbcAccessOutObject outObject = new JdbcAccessOutObject();
		outObject.setConnection(connection);
		getDBType(outObject);
		return outObject;
	}

	private void getDBType(JdbcAccessOutObject outObject){
		//目前先只判断oracle，以后需要时可增加别的数据库的判断
		if(connInfo.getDriver().contains("oracle")){
			outObject.setDbType(JdbcAccessOutObject.DBType.ORACLE);
		} else if (connInfo.getDriver().toLowerCase().contains("hive")) {
			outObject.setDbType(JdbcAccessOutObject.DBType.HIVE);
		} else{
			outObject.setDbType(JdbcAccessOutObject.DBType.OTHER);
		}
	}

	/**
	 * 使用JDBC创建数据库连接 如果连接创建失败 直接异常退出 由外部捕获
	 * 
	 * @param dbConnectionInfo
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection(DatabaseConnectionInfo dbConnectionInfo) throws SQLException{
		// // 注册数据库驱动
		// String driverName = dbConnectionInfo.getDriver();
		// driverName = driverName.trim();
		// try {
		// Class.forName(driverName);
		// } catch (Exception e) {
		// throw new IllegalArgumentException("注册数据库驱动失败", e);
		// }
		// // 创建数据库连接
		// return DriverManager.getConnection(dbConnectionInfo.getUrl(),
		// dbConnectionInfo.getUserName(), dbConnectionInfo.getPassword());
		if (dbConnectionInfo.getDriver().toLowerCase().contains("hive")) {
			try {
				return getHiveConnection(dbConnectionInfo);
			} catch (ClassNotFoundException e) {
				throw new SQLException(e.getMessage());
			}
		}
		
		return DbPoolManager.getConnection(dbConnectionInfo);
	}
	
	public static Connection getHiveConnection(DatabaseConnectionInfo dbConnectionInfo) throws ClassNotFoundException, SQLException {
		LOGGER.debug("DbAccessor::getHiveConnection() driver:{} url:{} user:{} pass:{}", 
				new Object[]{dbConnectionInfo.getDriver(), dbConnectionInfo.getUrl(), dbConnectionInfo.getUserName(), dbConnectionInfo.getPassword()});
		
		Connection conn = null;
		Class.forName(dbConnectionInfo.getDriver());
		if (dbConnectionInfo.getUrl().toLowerCase().contains("nosasl")) {
			conn = DriverManager.getConnection(dbConnectionInfo.getUrl());
		}
		else {
			conn = DriverManager.getConnection(dbConnectionInfo.getUrl(), dbConnectionInfo.getUserName(),
					dbConnectionInfo.getPassword());
		}
		return conn;
	}

	/**
	 * 数据库连接器关闭方法 关闭数据库连接
	 */
	public void close(){
		super.close();
		DbUtil.close(null, null, connection);
	}
}
