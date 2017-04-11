package cn.uway.framework.connection.pool.ftp;

import cn.uway.framework.connection.FTPConnectionInfo;

/**
 * FTP连接池工厂实现。
 * 
 * @author ChenSijiang 2012-11-1
 */
public class BasicFTPClientPoolFactory implements FTPClientPoolFactory {

	@Override
	public FTPClientPool create(FTPConnectionInfo connectInfo) throws Exception {
		return new BasicFTPClientPool(connectInfo, connectInfo.getMaxConnections(), connectInfo.getMaxWaitSecond());
	}

}
