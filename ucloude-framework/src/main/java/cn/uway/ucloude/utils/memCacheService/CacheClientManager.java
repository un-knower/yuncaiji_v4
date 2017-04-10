package cn.uway.ucloude.utils.memCacheService;

import java.util.Properties;


public class CacheClientManager {
	public static CacheClientManager instance;
	protected CacheClientPool clientPool;
	protected Properties property;
	
	public void init() {
		property = new Properties();
		property.setProperty("memcached.serverOpts.237", "192.168.15.237:11211");
		property.setProperty("redis.serverOpts.237", "192.168.15.237:6379");
		property.setProperty("rediscluster.serverOpts.237", "192.168.15.237:6379");
	}
	
	public CacheClientManager() {
		clientPool = new CacheClientPool();
	}
	
	public static CacheClientManager getInstance() {
		if (instance == null) {
			synchronized (CacheClientManager.class) {
				CacheClientManager manager = new CacheClientManager();
				manager.init();
				instance = manager;
			}
		}
		
		return instance;
	}
	
	public AbsCacheClient getClient(AbsCacheClient.memCacheClientType type, String serviceName) {
		return clientPool.getClient(type, serviceName, property);
	}
}
