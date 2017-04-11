package cn.uway.framework.connection;

/**
 * 数据库方式连接信息 DatabaseConnectionInfo<br>
 * 注意:maxIdle不要提供set方法供外部设置,否则不便于现场人员理解<br>
 * 
 * @author chenrongqiang 2014-4-13
 */
public class DatabaseConnectionInfo extends ConnectionInfo {

	/**
	 * 数据库驱动
	 */
	private String driver;

	/**
	 * 数据库连接字符串
	 */
	private String url;

	/**
	 * 最大连接数
	 */
	private int maxActive;

	/**
	 * 等待获取连接超时时间 单位毫秒 默认5秒
	 */
	private int maxWait = 5 * 1000;

	/**
	 * 最大空闲连接数 默认为1
	 */
	private int maxIdle = 1;

	/**
	 * 数据库检查SQL语句
	 */
	private String validateQuery = "select 1 from dual";

	/**
	 * 构造方法
	 */
	public DatabaseConnectionInfo() {
		super();
		this.connType = CONNECTION_TYPE_DB;
	}

	/**
	 * 获取数据库驱动
	 */
	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
		// 根据数据库的驱动名称获取验证sql
		// oracle.jdbc.dirver.OracleDriver
		if (driver.toLowerCase().indexOf("oracle") > -1)
			this.setValidateQuery("select 1 from dual");
		// com.mysql.jdbc.Driver
		if (driver.toLowerCase().indexOf("mysql") > -1)
			this.setValidateQuery("select 1");
		// com.microsoft.jdbc.sqlserver.SQLServerDriver
		if (driver.toLowerCase().indexOf("sqlserver") > -1)
			this.setValidateQuery("select 1");
		// net.sourceforge.jtds.jdbc.Driver
		if (driver.toLowerCase().indexOf("jtds") > -1)
			this.setValidateQuery("select 1");
		// com.sybase.jdbc.SybDriver
		if (driver.toLowerCase().indexOf("sybase") > -1)
			this.setValidateQuery("select 1");
		// com.ibm.db2.jdbc.app.DB2Driver
		if (driver.toLowerCase().indexOf("db2") > -1)
			this.setValidateQuery("select 1 from sysibm.sysdummy1");
		// com.informix.jdbc.IfxDriver
		if (driver.toLowerCase().indexOf("informix") > -1)
			this.setValidateQuery("select count(*) from systables");
		// org.postgresql.Driver
		if (driver.toLowerCase().indexOf("postgresql") > -1)
			this.setValidateQuery("select version()");
		// org.postgresql.Driver
		if (driver.toLowerCase().indexOf("hive") > -1)
			this.setValidateQuery("select version()");
	}

	/**
	 * 获取数据库连接字符串
	 */
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		// TODO 待完善，目前只提供了ORACLE的校验语句
		// if (url.toLowerCase().startsWith("oracle"))
		// this.setValidateQuery("select 1 from dual");
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getMaxWait() {
		return maxWait;
	}

	/**
	 * 注意 数据库中单位是秒，连接信息实体中是毫秒，此处加以了转换
	 * 
	 * @param maxWait
	 */
	public void setMaxWait(int maxWait) {
		this.maxWait = maxWait * 1000;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public String getValidateQuery() {
		return validateQuery;
	}

	public void setValidateQuery(String validateQuery) {
		this.validateQuery = validateQuery;
	}

	@Override
	public String toString() {
		return "DatabaseConnectionInfo [driver=" + driver + ", url=" + url + ", maxActive=" + maxActive + ", maxWait=" + maxWait + ", maxIdle="
				+ maxIdle + ", validateQuery=" + validateQuery + "]";
	}

}
