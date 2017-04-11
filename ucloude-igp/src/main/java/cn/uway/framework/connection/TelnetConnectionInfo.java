package cn.uway.framework.connection;

/**
 * Telnet方式连接信息
 * 
 * @author chenrongqiang
 * @Date 2012-10-27
 * @version 1.0
 * @since 3.0
 * @see FTPConnectionInfo
 * @see DatabaseConnectionInfo
 */
public class TelnetConnectionInfo extends ConnectionInfo{

	/**
	 * Telnet端口
	 */
	private int port = 23;

	/**
	 * 登陆提示符
	 */
	private String loginSign;

	/**
	 * 终端类型,默认使用ANSI
	 */
	private String termType = "ANSI";

	/**
	 * telnet超时时间
	 */
	private int timeoutMinutes;

	/**
	 * 构造方式
	 */
	public TelnetConnectionInfo(){
		super();
		this.connType = CONNECTION_TYPE_TELNET;
	}

	/**
	 * 获取Telnet端口
	 */
	public int getPort(){
		return port;
	}

	public void setPort(int port){
		this.port = port;
	}

	/**
	 * 获取Telnet登陆提示符
	 */
	public String getLoginSign(){
		return loginSign;
	}

	public void setLoginSign(String loginSign){
		this.loginSign = loginSign;
	}

	/**
	 * 获取终端类型
	 */
	public String getTermType(){
		return termType;
	}

	public void setTermType(String termType){
		this.termType = termType;
	}

	/**
	 * 获取Telnet超时时间，单位分钟
	 */
	public int getTimeoutMinutes(){
		return timeoutMinutes;
	}

	public void setTimeoutMinutes(int timeoutMinutes){
		this.timeoutMinutes = timeoutMinutes;
	}

	public String toString(){
		return "数据采集类型:telnet方式," + " 端口:" + this.port + " 登陆提示符:" + this.loginSign + " 终端类型:" + this.termType
				+ " 超时时间，单位分钟:" + this.timeoutMinutes + super.toString();
	}
}
