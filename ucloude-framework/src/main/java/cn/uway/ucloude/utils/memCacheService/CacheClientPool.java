package cn.uway.ucloude.utils.memCacheService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CacheClientPool {
	public static class ClientList {
		public volatile int idleSize;
		public volatile int usedSize;
		
		ClientEntry topEntry;
		
		public ClientList() {
			idleSize = 0;
			usedSize = 0;
			topEntry = null;
		}
		
		public synchronized void addIdle(ClientEntry entry) {
			entry.next = topEntry;
			topEntry = entry;
			++idleSize;
		}
		
		public synchronized void increaseUsedSize() {
			++usedSize;
		}
		
		public synchronized void decreaseUsedSize() {
			--usedSize;
		}
		
		public synchronized ClientEntry pop() {
			ClientEntry entry = topEntry;
			if (entry != null) {
				topEntry = entry.next;
				--idleSize;
				++usedSize;
			}
			
			return entry;
		}
		
		public synchronized int getTotalSize() {
			return idleSize + usedSize;
		}
	}
	
	public static class ClientEntry {
		public AbsCacheClient client;
		public Date unRegisterTime;
		protected ClientEntry next;
		
		public ClientEntry(AbsCacheClient client, Date unRegisterTime) {
			this.client = client;
			this.unRegisterTime = unRegisterTime;
		}
	}
	
	private final static int POOL_MAX_SIZE = 10;
	private Map<String, ClientList> idleClientPoolMap;
	public void unRegisterClient(AbsCacheClient client, boolean isDestory) {
		String key = makeKey(client.clientType, client.serviceName);
		ClientList clientList = null;
		synchronized (this) {
			clientList = idleClientPoolMap.get(key);
			if (clientList == null) {
				clientList = new ClientList();
				idleClientPoolMap.put(key, clientList);
			}			
		}
		
		clientList.decreaseUsedSize();
		if (!isDestory) {
			ClientEntry entry = new ClientEntry(client, new Date());
			clientList.addIdle(entry);
		}
	}
	
	public CacheClientPool() {
		idleClientPoolMap = new HashMap<String, ClientList>();
	}

	public AbsCacheClient getClient(AbsCacheClient.memCacheClientType type, String serviceName, Properties property) {
		String key = makeKey(type, serviceName);
		ClientList clientList = null;
		synchronized (this) {
			clientList = idleClientPoolMap.get(key);
			if (clientList == null) {
				clientList = new ClientList();
				idleClientPoolMap.put(key, clientList);
			}			
		}
		
		ClientEntry clientEntry = clientList.pop();
		while (clientEntry == null) {
			synchronized (this) {
				if (clientList.getTotalSize() < POOL_MAX_SIZE) {
					AbsCacheClient client = createCleint(type, serviceName);
					if (client != null) {
						client.connect(property);
						clientList.increaseUsedSize();
						return client;
					}
					return null;
				} else {
					clientEntry = clientList.pop();
					if (clientEntry != null)
						break;
				}
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				break;
			}
		}
		
		if (clientEntry == null)
			return null;
		
		return clientEntry.client;
	}
	
	private String makeKey(AbsCacheClient.memCacheClientType type, String serviceName) {
		return type.name() + "@@" + serviceName;
	}
	
	private AbsCacheClient createCleint(AbsCacheClient.memCacheClientType type, String serviceName) {
		AbsCacheClient client = null;
		switch (type) {
			case client_memCache:
				client = new MCCacheClient(this, serviceName);
				break;
			case client_redis:
				client = new RedisCacheClient(this, serviceName);
				break;
			case client_redisCluster:
				client = new RedisClusterCacheClient(this, serviceName);
				break;
			default:
		}
		
		return client;
	}
}
