package cn.uway.ucloude.utils.memCacheService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.ucloude.utils.StringUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

public class RedisCacheClient extends AbsCacheClient {
	/**
	 * redis service client;
	 */
	public Jedis cache;
	
	/**
	 * 服务器配置信息{ip:port}
	 */
	protected String serverOpts;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RedisCacheClient.class);

	public RedisCacheClient(CacheClientPool cacheClientPool, String serviceName) {
		super(cacheClientPool, memCacheClientType.client_redis, serviceName);
		this.INFINITE_SECOND = -1;
	}

	@Override
	public void onDestory() {
		close();

		try {
			if (this.cache != null && this.cache.isConnected()) {
				this.cache.close();
				this.cache = null;
			}
		} catch (Exception e) {
			LOGGER.debug("RedisCacheClient.destory() has error occured. msg:"
					+ e.getMessage());
		}
	}

	@Override
	public boolean set(String key, String value, int ttlSecond) {
		if (key == null)
			return false;

		try {
			cache.set(key, value);
			if (ttlSecond > 0) {
				cache.expire(key, ttlSecond);
			}

			this.errCount = 0;
			return true;
		} catch (Exception e) {
			onException("RedisCacheClient.set String has exception occured.", e);
		}

		return false;
	}

	@Override
	public boolean set(String key, byte[] value, int ttlSecond) {
		if (key == null)
			return false;

		try {
			cache.set(key.getBytes(), value);
			if (ttlSecond > 0) {
				cache.expire(key, ttlSecond);
			}
			this.errCount = 0;
			return true;
		} catch (Exception e) {
			onException("RedisCacheClient.set byte[] has exception occured.", e);
		}

		return false;
	}

	@Override
	public boolean set(String key, Serializable value, int ttlSecond) {
		if (key == null)
			return false;

		try {
			cache.set(key.getBytes(), encodeObject(value));
			if (ttlSecond > 0) {
				cache.expire(key, ttlSecond);
			}
			this.errCount = 0;
			return true;
		} catch (Exception e) {
			onException(
					"RedisCacheClient.set serializbale has exception occured.",
					e);
		}

		return false;
	}

	@Override
	public String get(String key) {
		if (key == null)
			return null;

		try {
			String ret = cache.get(key);
			this.errCount = 0;
			return ret;
		} catch (Exception e) {
			onException("RedisCacheClient.get has exception occured.", e);
		}

		return null;
	}

	@Override
	public byte[] getBytes(String key) {
		if (key == null)
			return null;

		try {
			byte[] ret = cache.get(key.getBytes());
			this.errCount = 0;
			return ret;
		} catch (Exception e) {
			onException("RedisCacheClient.getBytes has exception occured.", e);
		}

		return null;
	}

	@Override
	public Object getObject(String key) {
		if (key == null)
			return null;

		try {
			byte[] retBytes = cache.get(key.getBytes());
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
			cache.incrBy(key, value);
		} catch (Exception e) {
			onException(
					"RedisCacheClient.increase serializbale has exception occured.",
					e);
		}

		return 0;
	}

	@Override
	public long decrease(String key, long value) {
		if (key == null)
			return -1;

		try {
			cache.decrBy(key, value);
		} catch (Exception e) {
			onException(
					"RedisCacheClient.decrease serializbale has exception occured.",
					e);
		}

		return 0;
	}

	@Override
	public boolean delete(String key) {
		if (key == null)
			return false;

		try {
			cache.del(key);
		} catch (Exception e) {
			onException(
					"RedisCacheClient.delete serializbale has exception occured.",
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
			if (cache.exists(keyBytes))
				cache.del(keyBytes);

			for (Serializable serialObj : value) {
				cache.rpush(keyBytes, encodeObject(serialObj));
			}

			if (ttlSecond > 0) {
				cache.expire(keyBytes, ttlSecond);
			}
		} catch (Exception e) {
			onException("RedisCacheClient.lset has exception occured.", e);
		}

		return false;
	}

	@Override
	public boolean ladd(String key, Serializable value) {
		if (key == null)
			return false;

		try {
			byte[] keyBytes = key.getBytes();
			cache.rpush(keyBytes, encodeObject(value));
		} catch (Exception e) {
			onException("RedisCacheClient.ladd has exception occured.", e);
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
				cache.rpush(keyBytes, encodeObject(v));
		} catch (Exception e) {
			onException("RedisCacheClient.ladd has exception occured.", e);
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
				Long size = cache.llen(keyBytes);
				if (size != null) {
					length = (int) (long) (size) - start;
				}
			}

			List<byte[]> retByteObjs = cache.lrange(keyBytes, start, start
					+ length - 1);
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
			onException("RedisCacheClient.lget range has exception occured.", e);
		}

		return null;
	}

	@Override
	public Set<String> getAllKeys(String pattern) {
		if (pattern == null)
			pattern = "*";

		try {
			Set<String> retSets = cache.keys(pattern);

			return retSets;
		} catch (Exception e) {
			onException("RedisCacheClient.getAllKeys has exception occured.", e);
		}

		return null;
	}

	@Override
	public void connect(Properties property) {
		if (this.cache != null && this.cache.isConnected()) {
			return;
		}
		
		String propKey = "redis.serverOpts." + this.serviceName;
		if (property != null && property.containsKey(propKey)) {
			this.serverOpts = property.getProperty(propKey);
		}
		
		String host = "";
		Integer port = 6379;
		String password = null;
		String[] params = StringUtil.split(serverOpts, ':');
		if (params != null && params.length > 1) {
			host = params[0];
			port = Integer.parseInt(params[1]);
			if (params.length > 2) {
				password = params[2].trim();
			}
		}
				
		this.cache = new Jedis(host, port);
		if (password != null) {
			this.cache.auth(password);
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
