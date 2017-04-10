package cn.uway.usummary.entity;

public class ConnectionInfo {
	
	/**
	 * 默认FTP测试命令，用于验证FTP连接是否仍然存活。
	 */
	public static final String DEFAULT_FTP_VALIDATE_CMD = "pwd";
	
	private int id;
	
	private String url;
	
	private String driver;
	
	private String userName;
	
	private String passWord;
	
	private String description;
	
	private Integer maxwait;
	
	private Integer maxactive;
	
	private Integer maxidle;
	
	private String validateQuery;
	
	private Integer port;
	
	private boolean passiveFlag;

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
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getMaxwait() {
		return maxwait;
	}

	public void setMaxwait(Integer maxwait) {
		this.maxwait = maxwait;
	}

	public Integer getMaxactive() {
		return maxactive;
	}

	public void setMaxactive(Integer maxactive) {
		this.maxactive = maxactive;
	}

	public Integer getMaxidle() {
		return maxidle;
	}

	public void setMaxidle(Integer maxidle) {
		this.maxidle = maxidle;
	}

	public String getValidateQuery() {
		return validateQuery;
	}

	public void setValidateQuery(String validateQuery) {
		this.validateQuery = validateQuery;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isPassiveFlag() {
		return passiveFlag;
	}

	public void setPassiveFlag(boolean passiveFlag) {
		this.passiveFlag = passiveFlag;
	}
	
	
}
