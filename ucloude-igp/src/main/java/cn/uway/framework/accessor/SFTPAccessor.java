package cn.uway.framework.accessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.framework.connection.pool.sftp.SFTPClient;
import cn.uway.framework.connection.pool.sftp.SFTPClientPool;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.FileUtil;
import cn.uway.ucloude.utils.IoUtil;
import cn.uway.ucloude.utils.UcloudePathUtil;


public class SFTPAccessor extends AbstractAccessor {

	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(SFTPAccessor.class);

	/**
	 * FTP连接数据源对象
	 */
	protected FTPConnectionInfo connInfo;

	/**
	 * 保存最后一次取得的流。
	 */
	private InputStream currIn;
	
	private SFTPClientPool sftpClientPool;
	
	private SFTPClient sftpClient;
	
	private String tmpFile;

	@Override
	public void setConnectionInfo(ConnectionInfo connInfo){
		if(!(connInfo instanceof FTPConnectionInfo))
			throw new IllegalArgumentException("错误的连接信息.请配置有效的SFTP连接配置信息");
		this.connInfo = (FTPConnectionInfo)connInfo;
	}

	/**
	 * SFTP的连接池规则:
	 * 1、如果新建一个文件，
	 */
	@Override
	public boolean beforeAccess(){
		this.sftpClientPool = SFTPClientPool.getSFTPClientPool(this.connInfo.getId(), this.connInfo.getMaxConnections());
		
		return sftpClientPool != null;
	}
	
	/**
	 * FTP接入器核心工作方法<br>
	 * 1、创建FTP连接<br>
	 * 2、创建文件读取流<br>
	 * 3、组装接入结果对象.
	 */
	@Override
	public AccessOutObject access(GatherPathEntry path) throws Exception{
		this.startTime = new Date();
		// 获取FTP连接。
		LOGGER.debug("[SFTPAccessor]等待获取SFTP连接......KEY={}", new Object[]{connInfo.getId()});
		
		String hostIP = connInfo.getIp();
		int hostPort = connInfo.getPort();
		String user = connInfo.getUserName();
		String pass = connInfo.getPassword();
		this.sftpClient = this.sftpClientPool.getSftpClient(hostIP , hostPort, user, pass, connInfo.getCharset());
		if (this.sftpClient == null) {
			throw new Exception("连接到SFTP失败");
		}

		LOGGER.debug("[SFTPAccessor]获取SFTP连接成功，KEY={}",	new Object[]{connInfo.getId()});
		String ftpPath = path.getPath();
		/*
		 * 日志乱码的根源所在，之前这里默认转换为UTF-8格式，但是JVM默认的是GBK格式，<br/>
		 * 所以在FtpClient拿回来的路径是以GBK格式为基础的ISO-8859-1编码集，因此在     <br/> 
		 * 程序中造成乱码.<br>
		 * 
		 * @author Niow 2014-6-11
		 */
		String decodedFTPPath = ftpPath;	//StringUtil.decodeFTPPath(ftpPath, connInfo.getCharset());
		LOGGER.debug("开始下载：{}", decodedFTPPath);
		InputStream in = this.downRemoteFile(ftpPath);
		//如果多次重试均无法获取FTP连接流 则直接异常退出
		if(in == null)
			throw new Exception(connInfo.getRetryTimes() + "次重试下载失败,放弃此文件：" + decodedFTPPath);
		LOGGER.debug("获取{}FTP文件流成功", decodedFTPPath);
		this.currIn = in;
		return this.toAccessOutObject(in, decodedFTPPath, path.getSize());
	}
	
	public AccessOutObject toAccessOutObject(InputStream in, String rawName, long len){
		StreamAccessOutObject out = new StreamAccessOutObject();
		out.setOutObject(in);
		out.setLen(len);
		out.setRawAccessName(rawName);
		return out;
	}

	@Override
	public void close(){
		try {
			if(currIn != null){
				IoUtil.closeQuietly(currIn);
			}
			
			//删除本地的temp文件
			if (this.tmpFile != null && this.tmpFile.length()>0) {
				File file = new File(this.tmpFile);
				if (file.exists())
					file.delete();
			}
			
			super.close();
		} finally {
			// 将sftpClient归还到sftpClientPool中;
			if(this.sftpClient != null){
				this.sftpClientPool.returnSftpChannel(sftpClient);
			}			
		}
	}

	protected InputStream downRemoteFile(String sftpPathFile) throws Exception {
		String fileName = FileUtil.getFileFullName(sftpPathFile);
		
		final int maxTryCount = 3;
		this.tmpFile = UcloudePathUtil.makePath("igp/temp") + this.getTask().getId() + "_"+ this.connInfo.getId() ;
		File tempDir = new File(this.tmpFile);
		if (!(tempDir.exists() && tempDir.isDirectory())) {
			if (!tempDir.mkdirs()) {
				throw new Exception("创建临时文件夹失败. path=" + this.tmpFile);
			}
		}
		this.tmpFile += "/" + fileName;
		
		int nTryCount = 0;
		while (nTryCount < maxTryCount) {
			++nTryCount;
			try {
				sftpClient.downRemoteFile(sftpPathFile, this.tmpFile);
				break;
			} catch (Exception e) {
				LOGGER.error("下载SFTP文件:" + sftpPathFile + "出错, 稍后将继续第" + (nTryCount+1) + "次重试下载.", e);
				
				this.sftpClient.close();
			}
		}
		if (nTryCount < maxTryCount) {
			FileInputStream is = new FileInputStream(this.tmpFile);
			return is;
		}
		
		throw new Exception("下载SFTP文件:" + sftpPathFile + "出错，尝试次数超限，将放弃.");
	}
	
}
