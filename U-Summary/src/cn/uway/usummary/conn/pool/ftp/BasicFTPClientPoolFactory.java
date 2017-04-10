package cn.uway.usummary.conn.pool.ftp;

import cn.uway.usummary.entity.ConnectionInfo;

/**
 * FTP连接池工厂实现。
 * 
 */
public class BasicFTPClientPoolFactory implements FTPClientPoolFactory {

	@Override
	public FTPClientPool create(ConnectionInfo connectInfo) throws Exception {
		return new BasicFTPClientPool(connectInfo, connectInfo.getMaxidle(), connectInfo.getMaxwait());
	}

}
