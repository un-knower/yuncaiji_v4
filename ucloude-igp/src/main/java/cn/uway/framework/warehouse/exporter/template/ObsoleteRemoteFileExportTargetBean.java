package cn.uway.framework.warehouse.exporter.template;

/**
 * 远程FTP文件输出目的地定义 RemoteFileExportTargetBean
 * 
 * @author chenrongqiang 2012-12-5
 */
public class ObsoleteRemoteFileExportTargetBean extends FileExporterBean {

	// 远程FTP地址
	private String url;

	// 远程FTP端口
	private String port;

	// FTP用户名
	private String userName;

	// FTP密码
	private String password;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
