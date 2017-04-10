package cn.uway.ucloude.utils.memCacheService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.ucloude.utils.StringUtil;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisDataException;

public class RedisClusterCacheClient extends AbsCacheClient {
	/**
	 * redis cluster service client;
	 */
	protected JedisCluster redisCluster;
	
	/**
	 * 服务器集群配置信息{ip:port ip:port ip:port ...}
	 */
	protected String serverOpts;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RedisClusterCacheClient.class);

	public RedisClusterCacheClient(CacheClientPool cacheClientPool, String serviceName) {
		super(cacheClientPool, memCacheClientType.client_redisCluster, serviceName);
		this.INFINITE_SECOND = -1;
	}

	@Override
	public void onDestory() {
		close();

		try {
			if (this.redisCluster != null) {
				this.redisCluster.close();
				this.redisCluster = null;
			}
		} catch (Exception e) {
			LOGGER.debug("RedisClusterCacheClient.destory() has error occured. msg:"
					+ e.getMessage());
		}
	}

	@Override
	public boolean set(String key, String value, int ttlSecond) {
		if (key == null)
			return false;

		try {
			redisCluster.set(key, value);
			if (ttlSecond > 0) {
				redisCluster.expire(key, ttlSecond);
			}

			this.errCount = 0;
			return true;
		} catch (Exception e) {
			onException(
					"RedisClusterCacheClient.set String has exception occured.",
					e);
		}

		return false;
	}

	@Override
	public boolean set(String key, byte[] value, int ttlSecond) {
		if (key == null)
			return false;

		try {
			redisCluster.set(key.getBytes(), value);
			if (ttlSecond > 0) {
				redisCluster.expire(key, ttlSecond);
			}
			this.errCount = 0;
			return true;
		} catch (Exception e) {
			onException(
					"RedisClusterCacheClient.set byte[] has exception occured.",
					e);
		}

		return false;
	}

	@Override
	public boolean set(String key, Serializable value, int ttlSecond) {
		if (key == null)
			return false;

		try {
			redisCluster.set(key.getBytes(), encodeObject(value));
			if (ttlSecond > 0) {
				redisCluster.expire(key, ttlSecond);
			}
			this.errCount = 0;
			return true;
		} catch (Exception e) {
			onException(
					"RedisClusterCacheClient.set serializbale has exception occured.",
					e);
		}

		return false;
	}

	@Override
	public String get(String key) {
		if (key == null)
			return null;

		try {
			String ret = redisCluster.get(key);
			this.errCount = 0;
			return ret;
		} catch (Exception e) {
			onException("RedisClusterCacheClient.get has exception occured.", e);
		}

		return null;
	}

	@Override
	public byte[] getBytes(String key) {
		if (key == null)
			return null;

		try {
			byte[] ret = redisCluster.get(key.getBytes());
			this.errCount = 0;
			return ret;
		} catch (Exception e) {
			onException(
					"RedisClusterCacheClient.getBytes has exception occured.",
					e);
		}

		return null;
	}

	@Override
	public Object getObject(String key) {
		if (key == null)
			return null;

		try {
			byte[] retBytes = redisCluster.get(key.getBytes());
			Object obj = decodeObject(retBytes);

			this.errCount = 0;
			return obj;
		} catch (Exception e) {
			onException("MCClient.getObject has exception occured.", e);
		}

		return null;
	}

	@Override
	public long increase(String key, long value) {
		if (key == null)
			return -1;

		try {
			redisCluster.incrBy(key, value);
		} catch (Exception e) {
			onException(
					"RedisClusterCacheClient.increase serializbale has exception occured.",
					e);
		}

		return 0;
	}

	@Override
	public long decrease(String key, long value) {
		if (key == null)
			return -1;

		try {
			redisCluster.decrBy(key, value);
		} catch (Exception e) {
			onException(
					"RedisClusterCacheClient.decrease serializbale has exception occured.",
					e);
		}

		return 0;
	}

	@Override
	public boolean delete(String key) {
		if (key == null)
			return false;

		try {
			redisCluster.del(key);
		} catch (Exception e) {
			onException(
					"RedisClusterCacheClient.delete serializbale has exception occured.",
					e);
		}

		return false;
	}

	@Override
	public boolean lset(String key, List<Serializable> value, int ttlSecond,
			int pageSize) {
		if (key == null)
			return false;

		try {
			byte[] keyBytes = key.getBytes();
			if (redisCluster.exists(keyBytes))
				redisCluster.del(keyBytes);

			for (Serializable serialObj : value) {
				redisCluster.rpush(keyBytes, encodeObject(serialObj));
			}

			if (ttlSecond > 0) {
				redisCluster.expire(keyBytes, ttlSecond);
			}
		} catch (Exception e) {
			onException("RedisClusterCacheClient.lset has exception occured.",
					e);
		}

		return false;
	}

	@Override
	public boolean ladd(String key, Serializable value) {
		if (key == null)
			return false;

		try {
			byte[] keyBytes = key.getBytes();
			redisCluster.rpush(keyBytes, encodeObject(value));
		} catch (Exception e) {
			onException("RedisClusterCacheClient.ladd has exception occured.",
					e);
		}

		return false;
	}

	@Override
	public boolean ladds(String key, List<Serializable> value) {
		if (key == null || value == null)
			return false;

		try {
			byte[] keyBytes = key.getBytes();
			for (Serializable v : value)
				redisCluster.rpush(keyBytes, encodeObject(v));
		} catch (Exception e) {
			onException("RedisClusterCacheClient.ladd has exception occured.",
					e);
		}

		return false;
	}

	@Override
	public List<Object> lget(String key, int start, int length) {
		if (key == null)
			return null;

		try {
			byte[] keyBytes = key.getBytes();
			if (length < 1) {
				Long size = redisCluster.llen(keyBytes);
				if (size != null) {
					length = (int) (long) (size) - start;
				}
			}

			List<byte[]> retByteObjs = redisCluster.lrange(keyBytes, start,
					start + length - 1);
			if (retByteObjs == null)
				return null;

			List<Object> lstObj = new ArrayList<Object>(retByteObjs.size());
			for (byte[] byteObj : retByteObjs) {
				if (byteObj == null)
					continue;

				lstObj.add(decodeObject(byteObj));
			}

			return lstObj;
		} catch (Exception e) {
			onException(
					"RedisClusterCacheClient.lget range has exception occured.",
					e);
		}

		return null;
	}

	@Override
	public Set<String> getAllKeys(String pattern) {
		if (pattern == null)
			pattern = "*";

		try {
			TreeSet<String> keys = new TreeSet<>();
			Map<String, JedisPool> clusterNodes = redisCluster
					.getClusterNodes();
			for (Entry<String, JedisPool> entry : clusterNodes.entrySet()) {
				JedisPool jedisPool = entry.getValue();
				Jedis jedisNode = jedisPool.getResource();
				try {
					keys.addAll(jedisNode.keys(pattern));
				} catch (Exception e) {

				} finally {
					if (jedisNode != null && jedisNode.isConnected()) {
						// must close node by jedisPool.getResource();
						jedisNode.close();
					}
				}
			}

			return keys;
		} catch (Exception e) {
			onException(
					"RedisClusterCacheClient.getAllKeys has exception occured.",
					e);
		}

		return null;
	}

	@Override
	public void connect(Properties property) {
		String propKey = "rediscluster.serverOpts." + this.serviceName;
		if (property != null && property.containsKey(propKey)) {
			this.serverOpts = property.getProperty(propKey);
		}
		
		String password = null;
		Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
		String[] servOpts = StringUtil.split(this.serverOpts, ' ');
		for (String servOpt : servOpts) {
			String[] params = StringUtil.split(servOpt, ':');
			String host = params[0];
			Integer port = 6379;
			
			if (params.length>1) 
				port = Integer.parseInt(params[1]);
			if (params.length > 2)
				password = params[2].trim();
			
			jedisClusterNodes.add(new HostAndPort(host, port));
		}
		
		// Jedis Cluster will attempt to discover cluster nodes automatically
		this.redisCluster = new JedisCluster(jedisClusterNodes);
		if (password != null) {
			//this.redisCluster.auth(password);
		}
	}

	@Override
	public void onException(String desc, Exception e) {
		if (e instanceof JedisDataException) {
			++this.errCount;
			LOGGER.debug(desc + " msg:" + e.getMessage());
		} else if (e instanceof JedisDataException) {
			++this.errCount;
			LOGGER.debug(desc + " msg:" + e.getMessage());
		} else {
			LOGGER.debug(desc + " msg:" + e.getMessage());
		}
		
		if (this.errCount >= 5) {
			onDestory();
			connect(null);
		}
	}

}
