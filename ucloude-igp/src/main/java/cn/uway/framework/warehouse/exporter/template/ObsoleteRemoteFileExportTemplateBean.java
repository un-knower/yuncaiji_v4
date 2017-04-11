package cn.uway.framework.warehouse.exporter.template;

/**
 * Export to remoate file template configuration
 * 
 * @author Spike
 * @date 2012/11/01
 * @version 1.0
 * @since 1.0
 */
public class ObsoleteRemoteFileExportTemplateBean extends FileExportTemplateBean {

	private String ip; // ip地址

	private int port; // 端口

	private String user; // 用户名

	private String pwd; // 密码

	private String remotePath; // 远端位置

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

}
