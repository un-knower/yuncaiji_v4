package cn.uway.util;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.commons.net.ftp.FTPClient;

import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.framework.connection.pool.ftp.BasicFTPClientPoolFactory;
import cn.uway.framework.connection.pool.ftp.FTPClientPool;
import cn.uway.framework.connection.pool.ftp.FTPClientPoolMgr;
import cn.uway.framework.context.AppContext;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class FTPClientUtil {

	protected static final ILogger LOGGER = LoggerManager.getLogger(FTPClientUtil.class);

	public static String isUsedFTPPool = AppContext.getBean("isUsedFTPPool", String.class);

	/**
	 * 根据任务配置FTP信息 获取FTP连接
	 * 
	 * @param ftpInfo
	 * @return FTPClient
	 * @throws NoSuchElementException
	 * @throws Exception
	 */
	public static FTPClient connectFTP(FTPConnectionInfo ftpInfo) throws NoSuchElementException, Exception {
		// 创建FTP连接池。
		LOGGER.debug("准备连接到FTP：{}，最大连接数：{}，最大等待时间（秒）：{}", new Object[]{ftpInfo, ftpInfo.getMaxConnections(), ftpInfo.getMaxWaitSecond()});
		@SuppressWarnings("unchecked")
		FTPClientPoolMgr<Integer> syncStringKeyFTPClientPoolMgr = AppContext.getBean("syncStringKeyFTPClientPoolMgr", FTPClientPoolMgr.class);
		FTPClientPool ftpPool = syncStringKeyFTPClientPoolMgr.getFTPClientPool(ftpInfo.getId());
		if (ftpPool == null) {
			try {
				BasicFTPClientPoolFactory basicFTPClientPoolFactory = AppContext
						.getBean("basicFTPClientPoolFactory", BasicFTPClientPoolFactory.class);
				ftpPool = basicFTPClientPoolFactory.create(ftpInfo);
				syncStringKeyFTPClientPoolMgr.putFTPClientPool(ftpInfo.getId(), ftpPool);
			} catch (Exception e) {
				LOGGER.error("创建FTP连接池过程中异常。", e);
				return null;
			}
		}
		LOGGER.debug("等待获取FTP连接......KEY={}，当前池中活动连接数={}，最大连接数={}",
				new Object[]{ftpInfo.getId(), ftpPool.getCurrentActiveCount(), ftpPool.getMaxConnection()});
		FTPClient cl = ftpPool.getFTPClient();
		LOGGER.debug("获取FTP连接成功，KEY={}，当前池中活动连接数={}，最大连接数={}",
				new Object[]{ftpInfo.getId(), ftpPool.getCurrentActiveCount(), ftpPool.getMaxConnection()});
		return cl;
	}

	public static final FTPClient createFTPClient(FTPConnectionInfo ftpInfo, int timeoutSeconds) throws Exception {
		FTPClient ftp = null;
		ftp = new FTPClient();
		ftp.setRemoteVerificationEnabled(false);
		ftp.setDefaultTimeout(60 * 1000);
		ftp.setConnectTimeout(60 * 1000);
		LOGGER.debug("准备连接到FTP，IP：{}，端口：{}，用户名：{}，密码：{}，超时时间：{}秒。",
				new Object[]{ftpInfo.getIp(), ftpInfo.getPort(), ftpInfo.getUserName(), ftpInfo.getPassword(), timeoutSeconds});
		ftp.connect(ftpInfo.getIp(), ftpInfo.getPort());
		LOGGER.debug("FTP已经连接。");
		ftp.setSoTimeout(20 * 1000);
		ftp.setDataTimeout(timeoutSeconds <= 0 ? 20 * 60 * 1000 : timeoutSeconds * 1000);
		if (ftp.login(ftpInfo.getUserName(), ftpInfo.getPassword())) {
			LOGGER.debug("FTP登录成功：{}", ftp.getReplyString().trim());
		} else {
			closeConnections(ftp);
			throw new Exception("FTP登录失败：" + ftp.getReplyString().trim());
		}
		ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
		if (ftpInfo.isPassiveFlag()) {
			ftp.enterLocalPassiveMode();
			LOGGER.debug("FTP进入被动模式。");
		}
		return ftp;
	}

	public static final void closeConnections(FTPClient ftp) {
		if (ftp != null)
			try {
				ftp.disconnect();
			} catch (IOException e) {
			}
	}

}
