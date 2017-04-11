package cn.uway.framework.connection.pool.sftp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;


public class SFTPClientPool {
	private int maxPoolSize;
	private LinkedList<SFTPClient> sftpClientList = new LinkedList<SFTPClient>();
	private static final ILogger LOGGER = LoggerManager.getLogger(SFTPClientPool.class);
	
	private static Map<Integer, SFTPClientPool> sftpClientPools;
	public synchronized static SFTPClientPool getSFTPClientPool(int connID, int maxPoolSize) {
		if (sftpClientPools == null)
			sftpClientPools = new HashMap<Integer, SFTPClientPool>(maxPoolSize);
		
		SFTPClientPool pool = sftpClientPools.get(connID);
		if (pool == null) {
			pool = new SFTPClientPool(maxPoolSize);
			sftpClientPools.put(connID, pool);
		}
		
		return pool;
	}
	
	
	public SFTPClientPool(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}
	
	public synchronized SFTPClient getSftpClient(String host, int port, String user, String pass, String fileNameEncodeing) {
		SFTPClient sftp = null;
		while (sftpClientList.size() > 0) {
			sftp = sftpClientList.removeFirst();
			if (sftp.isAvaliable()) {
				LOGGER.debug("SFTPClientPool::getSftpClient(), 本次从连接池中成功获取。连接池还有可用客户端数：{}, 最大可存放客户端数：{}", sftpClientList.size(), this.maxPoolSize);
				return sftp;
			}
			
			sftp.close();
		}
		
		sftp = new SFTPClient(host, port, user, pass);
		if (sftp.connectServer(fileNameEncodeing))
			return sftp;
		
		return null;
	}
	
	/**
	 * 归还SFTPClient;
	 * @param sftp
	 */
	public synchronized  void returnSftpChannel(SFTPClient sftp) {
		try {
			LOGGER.debug("归还SFTPClient到连接池, 当前池已有client个数：{}, 连接池最大可存放client容量个数:{}", sftpClientList.size(), this.maxPoolSize);
			if (!sftp.isAvaliable() 
					|| (sftpClientList.size() >= this.maxPoolSize)) {
				LOGGER.debug("当前SFTPClient已失效，或池个数已超过最大限度值,　将销毁当前的SFTPClient. 当前池已有client个数：{},连接池最大可存放client容量个数:{}", sftpClientList.size(), this.maxPoolSize);
				sftp.close();
				return;
			}
			
			sftp.beforeReturnPool();
			sftpClientList.add(sftp);
			LOGGER.debug("已成功归还SFTPClient到连接池, 当前池已有client个数：{}, 连接池最大可存放client容量个数:{}", sftpClientList.size(), this.maxPoolSize);
		} catch (Exception e) {
			LOGGER.error("将SFTPClient归还到pool发生了异常", e);
		}
	}
	
}
