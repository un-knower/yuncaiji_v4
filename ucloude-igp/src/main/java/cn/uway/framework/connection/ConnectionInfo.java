package cn.uway.framework.connection;

/**
 * 连接信息基类<br>
 * 
 * @author chenrongqiang
 * @2012-10-27
 */
public abstract class ConnectionInfo{

	// 采集源类型 FTP
	public static final int CONNECTION_TYPE_FTP = 1;

	// 采集源类型 TELNET
	public static final int CONNECTION_TYPE_TELNET = 2;

	// 采集源类型DB
	public static final int CONNECTION_TYPE_DB = 3;

	// 采集源类型SFTP
	public static final int CONNECTION_TYPE_SFTP = 4;
	
	// 采集源类型HDFS
	public static final int CONNECTION_TYPE_HDFS = 5;
	
	/**
	 * 连接编号
	 */
	protected int id;

	/**
	 * 连接IP地址信息
	 */
	protected String ip;

	/**
	 * 设备厂家信息
	 */
	protected String vendor;

	/**
	 * 连接类型<br>
	 */
	protected int connType;

	/**
	 * 用户名
	 */
	protected String userName;

	/**
	 * 密码
	 */
	protected String password;

	/**
	 * 连接描述
	 */
	protected String description;

	/**
	 * @return the ip
	 */
	public String getIp(){
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip){
		this.ip = ip;
	}

	/**
	 * 判断是否是合法的连接类型配置<br>
	 * 
	 * @param connType
	 * @return 是否是合法的连接类型配置，合法则返回true，否则返回false.
	 */
	public static boolean validate(int connType){
		return connType == CONNECTION_TYPE_FTP || connType == CONNECTION_TYPE_DB || connType == CONNECTION_TYPE_TELNET || connType == CONNECTION_TYPE_SFTP || connType == CONNECTION_TYPE_HDFS;
	}

	/**
	 * 获取连接编号
	 */
	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	/**
	 * 获取连接用户名
	 */
	public String getUserName(){
		return userName;
	}

	public void setUserName(String userName){
		this.userName = userName;
	}

	/**
	 * @return the connType
	 */
	public int getConnType(){
		return connType;
	}

	/**
	 * @param connType the connType to set
	 */
	public void setConnType(int connType){
		this.connType = connType;
	}

	/**
	 * @return the password
	 */
	public String getPassword(){
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password){
		this.password = password;
	}

	/**
	 * 获取连接描述信息
	 */
	public String getDescription(){
		return description;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getVendor(){
		return vendor;
	}

	public void setVendor(String vendor){
		this.vendor = vendor;
	}

	@Override
	public String toString(){
		return "ConnectionInfo [id=" + id + ", ip=" + ip + ", vendor=" + vendor + ", connType=" + connType
				+ ", userName=" + userName + ", password=" + password + ", description=" + description + "]";
	}
}
