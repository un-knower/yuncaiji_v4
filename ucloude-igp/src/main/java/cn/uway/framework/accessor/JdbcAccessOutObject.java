package cn.uway.framework.accessor;

import java.sql.Connection;

/**
 * 数据库接入器输出对象 包含JDBC连接 JdbcAccessOutObject
 * 
 * @author chenrongqiang 2012-12-4
 */
public class JdbcAccessOutObject extends AccessOutObject{

	/**
	 * 数据库连接 目前直接使用JDBC数据库连接
	 */
	private Connection connection;

	private DBType dbType;

	public DBType getDbType(){
		return dbType;
	}

	public void setDbType(DBType dbType){
		this.dbType = dbType;
	}

	public Connection getConnection(){
		return connection;
	}

	public void setConnection(Connection connection){
		this.connection = connection;
	}

	public static enum DBType{
		ORACLE("oracle"), SYSBASE("sysbase"), SQLSERVICE("sqlservice"), MYSQL("mysql"), HIVE("hive"), OTHER("other");

		private String value;

		DBType(String value){
			this.value = value;
		}

		public String getValue(){
			return value;
		}
	}
}
