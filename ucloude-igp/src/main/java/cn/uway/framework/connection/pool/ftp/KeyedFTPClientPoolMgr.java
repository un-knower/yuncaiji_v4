package cn.uway.framework.connection.pool.ftp;

/**
 * 以键管理的FTP连接池管理器。通过一个KEY来存放及获取FTP连接池。
 * 
 * @author ChenSijiang 2012-11-1
 */
public interface KeyedFTPClientPoolMgr<K> extends Iterable<K> {

	/**
	 * 将一个FTP连接池放入管理器。如果指定的KEY已存在，将返回之前的FTP连接池。
	 * 
	 * @param key
	 *            FTP连接池的KEY.
	 * @param pool
	 *            FTP连接池。
	 * @return 之前的FTP连接池。
	 * @throws NullPointerException
	 *             FTP连接池的KEY或FTP连接池为<code>null</code>.
	 */
	FTPClientPool putFTPClientPool(K key, FTPClientPool pool);

	/**
	 * 获取FTP连接池。如果不存在，将返回<code>null</code>.
	 * 
	 * @param key
	 *            FTP连接池的KEY.
	 * @return FTP连接池。
	 */
	FTPClientPool getFTPClientPool(K key);

	/**
	 * 从管理器中移除一个FTP连接池，并作为返回值返回。如果不存在，将返回<code>null</code>.
	 * 
	 * @param key
	 *            FTP连接池的KEY.
	 * @return 被移除的FTP连接池。
	 */
	FTPClientPool removeFTPClientPool(K key);

	/**
	 * 移除所有FTP连接池。
	 */
	void clear();
}
