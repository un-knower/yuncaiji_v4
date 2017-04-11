package cn.uway.framework.connection.pool.ftp;

import java.util.NoSuchElementException;

import org.apache.commons.net.ftp.FTPClient;

/**
 * FTP连接池，线程安全。
 * 
 * @author ChenSijiang 2012-10-28
 */
public interface FTPClientPool {

	/**
	 * 设置最大FTP连接数量。
	 * 
	 * @param maxConnection
	 *            最大FTP连接数量。
	 */
	void setMaxConnection(int maxConnection);

	/**
	 * 获取最大FTP连接数量。
	 * 
	 * @return 最大FTP连接数量。
	 */
	int getMaxConnection();

	/**
	 * 设置等待空闲FTP连接的时间（以秒为单位）。
	 * 
	 * @param maxWaitSecond
	 *            等待空闲FTP连接的时间（以秒为单位）。
	 */
	void setMaxWaitSecond(int maxWaitSecond);

	/**
	 * 获取等待空闲FTP连接的时间（以秒为单位）。
	 * 
	 * @return 等待空闲FTP连接的时间（以秒为单位）。
	 */
	int getMaxWaitSecond();

	/**
	 * 获取当前活动连接数量。
	 * 
	 * @return 当前活动连接数量。
	 */
	int getCurrentActiveCount();

	/**
	 * 从池中获取一个FTP连接。
	 * 
	 * @return FTP连接。
	 * @throws Exception
	 *             获取失败。
	 * @throws NoSuchElementException
	 *             当前没有足够的FTP连接。
	 */
	FTPClient getFTPClient() throws Exception, NoSuchElementException;

	/**
	 * 关闭连接池。关闭后，并不会马上断开池中已建立的FTP连接，而是在连接归还到连接池中时，会被关闭。
	 */
	void close();
}
