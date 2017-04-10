package cn.uway.usummary.conn.pool.ftp;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FTPClientPoolMgr<T> implements KeyedFTPClientPoolMgr<T> {

	private Map<T, FTPClientPool> pool;

	public FTPClientPoolMgr() {
		super();
		this.pool = new ConcurrentHashMap<T, FTPClientPool>();
	}

	@Override
	public Iterator<T> iterator() {
		return this.pool.keySet().iterator();
	}

	@Override
	public FTPClientPool putFTPClientPool(T key, FTPClientPool pool) {
		if (key == null)
			throw new NullPointerException("key为null.");
		if (pool == null)
			throw new NullPointerException("pool为null.");

		return this.pool.put(key, pool);
	}

	@Override
	public FTPClientPool getFTPClientPool(T key) {
		if (key == null)
			return null;
		return this.pool.get(key);
	}

	@Override
	public FTPClientPool removeFTPClientPool(T key) {
		if (key == null)
			return null;
		return this.pool.remove(key);
	}

	@Override
	public void clear() {
		this.pool.clear();
	}

}
