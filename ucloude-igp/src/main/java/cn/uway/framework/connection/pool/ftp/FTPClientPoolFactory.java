package cn.uway.framework.connection.pool.ftp;

import cn.uway.framework.connection.FTPConnectionInfo;

/**
 * FTP连接池工厂。
 * 
 * @author ChenSijiang 2012-11-1
 */
public interface FTPClientPoolFactory {

	/**
	 * 创建FTP连接池。
	 * 
	 * @param connectInfo
	 *            FTP连接信息。
	 * @param maxConnection
	 *            FTP最大连接数量。
	 * @param maxWaitSecond
	 *            等待空闲FTP连接的最大时间（秒）。
	 * @return FTP连接池。
	 * @throws Exception
	 *             创建连接池失败。
	 */
	FTPClientPool create(FTPConnectionInfo connectInfo) throws Exception;
}
