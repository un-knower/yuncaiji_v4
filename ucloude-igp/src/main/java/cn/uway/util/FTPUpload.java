package cn.uway.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPClient;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class FTPUpload {

	private static final ILogger LOG = LoggerManager.getLogger(FTPUpload.class);

	private String ip;

	private int port;

	private String userName;

	private String password;

	private boolean usePasvMode;

	private String encode;

	public FTPUpload(String ip, int port, String userName, String password, boolean usePasvMode, String encode) {
		super();
		this.ip = ip;
		this.port = port;
		this.userName = userName;
		this.password = password;
		this.usePasvMode = usePasvMode;
		this.encode = encode;
	}

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

	public boolean isUsePasvMode() {
		return usePasvMode;
	}

	public void setUsePasvMode(boolean usePasvMode) {
		this.usePasvMode = usePasvMode;
	}

	public String getEncode() {
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	/**
	 * FTP上传。
	 * 
	 * @param fileStream
	 *            待上传的文件流。
	 * @param ftpDir
	 *            上传的FTP目录。
	 * @param ftpFileName
	 *            上传到FTP的文件名。
	 * @throws Exception
	 *             上传失败。
	 */
	public void upload(InputStream fileStream, String ftpDir, String ftpFileName) throws Exception {
		LOG.debug("准备上传文件到FTP，文件名：{}，FTP目录：{}，FTP地址：{}。", new Object[]{ftpFileName, ftpDir, this.getIp()});
		FTPClient ftp = new FTPClient();
		try {
			String dir = encodeFTPPath(ftpDir);
			// 2015-08-17 sunt 
			// 1、发现本方法在其他地方无调用
			// 2、如果把路径和文件名拼接在一起，目标文件名错误
//			String name = encodeFTPPath(FilenameUtils.normalize(ftpDir + "/" + ftpFileName));
			String name = encodeFTPPath(FilenameUtils.normalize(ftpFileName));
			ftp.connect(this.getIp(), this.getPort());
			ftp.login(this.getUserName(), this.getPassword());
			LOG.debug("FTP登录成功：{}", ftp.getReplyString().trim());
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			if (this.isUsePasvMode()) {
				ftp.enterLocalPassiveMode();
				LOG.debug("FTP进入被动模式。");
			}
			if (!ftp.changeWorkingDirectory(dir)) {
				LOG.debug("FTP目录{}不存在（{}），准备创建。", new Object[]{ftpDir, ftp.getReplyString().trim()});
				if (!ftp.makeDirectory(dir))
					throw new Exception("创建FTP目录失败：" + ftpDir + "，响应：" + ftp.getReplyString().trim());
				LOG.debug("FTP目录创建成功：{}", ftp.getReplyString().trim());
			}
			if (ftp.storeFile(name, fileStream)) {
				LOG.debug("FTP上传成功：" + ftp.getReplyString().trim());
			} else {
				throw new Exception("FTP上传失败:" + ftp.getReplyString().trim());
			}

		} finally {
			FTPUtil.logoutAndCloseFTPClient(ftp);
		}
	}

	private String encodeFTPPath(String ftpPath) {
		try {
			String str = StringUtil.isNotEmpty(encode) ? new String(ftpPath.getBytes(encode), "iso_8859_1") : ftpPath;// iso_8859_1
			return str;
		} catch (UnsupportedEncodingException e) {
			LOG.error("设置的编码不正确:" + encode, e);
		}
		return ftpPath;
	}

	public static void main(String[] args) throws Exception {
		FTPUpload up = new FTPUpload("192.168.15.223", 21, "rd", "uway_rd_good", true, "gbk");
		InputStream in = new FileInputStream("/Users/chensijiang/Downloads/slf4j-api-1.6.1.jar");
		up.upload(in, "/Inter", "test.jar");
		in.close();
	}
}
