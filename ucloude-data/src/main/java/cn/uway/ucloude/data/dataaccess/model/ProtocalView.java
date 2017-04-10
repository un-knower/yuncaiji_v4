package cn.uway.ucloude.data.dataaccess.model;

import cn.uway.ucloude.serialize.JsonConvert;

public class ProtocalView {
	public int connectionID;
	
	public String connKey;
	
	public boolean isPool;
	
	public ProtocalType protocalType;
	
	public int getConnectionID() {
		return connectionID;
	}

	public void setConnectionID(int connectionID) {
		this.connectionID = connectionID;
	}

	public String getConnKey() {
		return connKey;
	}

	public void setConnKey(String connKey) {
		this.connKey = connKey;
	}

	public boolean isPool() {
		return isPool;
	}

	public void setPool(boolean isPool) {
		this.isPool = isPool;
	}

	public ProtocalType getProtocalType() {
		return protocalType;
	}

	public void setProtocalType(ProtocalType protocalType) {
		this.protocalType = protocalType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}



	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	private String url;
	
	private String driver;
	
	private String UserName;
	
	private String passWord;
	
	
	
	private int maxWait;
	
	private int maxActive;
	
	public int getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(int maxWait) {
		this.maxWait = maxWait;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getMaxIdle() {
		return maxIdle;
	}
	
	
	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	public String getValidateQuery() {
		return validateQuery;
	}

	public void setValidateQuery(String validateQuery) {
		this.validateQuery = validateQuery;
	}

	private int maxIdle;
	
	private String dbName;
	
	
	private String validateQuery;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return JsonConvert.serialize(this);
	}
	
	
	
}
