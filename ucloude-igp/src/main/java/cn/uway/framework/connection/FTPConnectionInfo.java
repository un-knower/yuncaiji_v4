package cn.uway.framework.connection;

import cn.uway.util.StringUtil;

/**
 * FTP方式数据源连接信息
 * 
 * @author MikeYang
 * @Date 2012-10-27
 * @version 1.0
 * @since 3.0
 * @see DatabaseConnectionInfo
 * @see TelnetConnectionInfo
 */
public class FTPConnectionInfo extends ConnectionInfo {

	/**
	 * FTP端口
	 */
	private int port = 21;

	/**
	 * 字符集
	 */
	private String charset;

	/**
	 * 主被动模式
	 */
	private boolean passiveFlag;

	/**
	 * 是否支持断点
	 */
	private boolean breakpointEnableFlag = false;

	/**
	 * 最大连接数。
	 */
	private int maxConnections;

	/**
	 * 从FTP连接池中获取连接的最大等待时长（秒）。
	 */
	private int maxWaitSecond;

	/**
	 * FTP连接测试命令，用于测试FTP连接是否存活。
	 */
	private String validateCmd;

	/**
	 * FTP操作失败时的重试次数。
	 */
	private int retryTimes;

	/**
	 * FTP操作失败后重试的休眠时间（秒）。
	 */
	private int retryDelaySecond;

	/**
	 * 默认FTP测试命令，用于验证FTP连接是否仍然存活。
	 */
	public static final String DEFAULT_FTP_VALIDATE_CMD = "pwd";

	/**
	 * 默认FTP最大连接数量。
	 */
	public static final int DEFAULT_FTP_MAX_CONNECTION = 5;

	/**
	 * 默认FTP最大等待时长（秒），连接池无空闲连接时的最大等待时间，超过此时间后抛出异常。
	 */
	public static final int DEFAULT_FTP_MAX_WAIT_SECOND = 300;

	/**
	 * 默认FTP操作的重试次数。
	 */
	public static final int DEFAULT_FTP_RETRY_TIMES = 3;

	/**
	 * 默认FTP操作的重试间隔时间（毫秒）。
	 */
	public static final int DEFAULT_FTP_RETRY_DELAY_MILLS = 3 * 1000;

	/**
	 * 构造方法
	 */
	public FTPConnectionInfo(int connType) {
		super();
		this.connType = connType;
	}
	
	/**
	 * 构造方法
	 */
	public FTPConnectionInfo() {
		super();
		this.connType = ConnectionInfo.CONNECTION_TYPE_FTP;
	}

	/**
	 * 获取Ftp端口
	 */
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * 获取Ftp字符集
	 */
	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * 是否为被动模式
	 */
	public boolean isPassiveFlag() {
		return passiveFlag;
	}

	public void setPassiveFlag(boolean passiveFlag) {
		this.passiveFlag = passiveFlag;
	}

	/**
	 * Ftp服务器是否支持断点下载
	 */
	public boolean isBreakpointEnableFlag() {
		return breakpointEnableFlag;
	}

	public void setBreakpointEnableFlag(boolean breakpointEnableFlag) {
		this.breakpointEnableFlag = breakpointEnableFlag;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections > 0 ? maxConnections : DEFAULT_FTP_MAX_CONNECTION;
	}

	public int getMaxWaitSecond() {
		return maxWaitSecond;
	}

	public void setMaxWaitSecond(int maxWaitSecond) {
		this.maxWaitSecond = maxWaitSecond;
	}

	public String getValidateCmd() {
		return validateCmd;
	}

	public void setValidateCmd(String validateCmd) {
		this.validateCmd = StringUtil.isEmpty(validateCmd) ? DEFAULT_FTP_VALIDATE_CMD : validateCmd;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public int getRetryDelaySecond() {
		return retryDelaySecond;
	}

	public void setRetryDelaySecond(int retryDelaySecond) {
		this.retryDelaySecond = retryDelaySecond > 0 ? retryDelaySecond * 1000 : DEFAULT_FTP_RETRY_DELAY_MILLS;
	}

	public String toString() {
		return "数据采集类型:ftp方式, 编号: " + this.id + " 端口:" + this.port + " 字符集:" + this.charset + " 主动或被动模式，0为主动，1为被动:" + this.passiveFlag
				+ " 断点服务是否开启，0为关闭，1为开启:" + this.breakpointEnableFlag + super.toString();
	}
}
