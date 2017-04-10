package cn.uway.ucloude.utils.memCacheService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;

public abstract class AbsCacheClient {

	public static enum memCacheClientType {
		client_memCache, client_redis, client_redisCluster,
	};

	/**
	 * 默认list每个页面存放的记录条数
	 */
	public static final int DEF_PAGE_SIZE = 50;

	/**
	 * memCacheClient类型，在AbsCacheClient.memCacheClientType中任一种
	 */
	protected memCacheClientType clientType;
	
	protected int INFINITE_SECOND = (60*24*60*60);

	/**
	 * 连续出错次数
	 */
	protected int errCount;
	
	/**
	 * 服务名称
	 */
	protected String serviceName;
	
	protected CacheClientPool cacheClientPool;
	
	public AbsCacheClient(CacheClientPool cacheClientPool, memCacheClientType clientType, String serviceName) {
		this.cacheClientPool = cacheClientPool;
		this.clientType = clientType;
		this.serviceName = serviceName;
	}

	/**
	 * 关闭client，每次使用完后，客户端必须关闭，在本例中实际上并不关闭，只向连接池通知释放资源泉
	 */
	public void close() {
		if (this.cacheClientPool != null) {
			cacheClientPool.unRegisterClient(this, false);
		}
	}

	/**
	 * 销毁一个client．
	 */
	public void destory() {
		onDestory();
		if (this.cacheClientPool != null) {
			cacheClientPool.unRegisterClient(this, true);
		}
	}
	
	/**
	 * 销毁client回调事件．
	 */
	public abstract void onDestory();

	/**
	 * 增加或修改一个key值
	 * 
	 * @param key
	 * @param value
	 * @param ttlSecond
	 *            生存时间，单位秒，如果值等于INFINITE_SECOND是无限时
	 * @return
	 */
	public abstract boolean set(String key, String value, int ttlSecond);

	public boolean set(String key, String value) {
		return set(key, value, INFINITE_SECOND);
	}

	public abstract boolean set(String key, byte[] value, int ttlSecond);

	public boolean set(String key, byte[] value) {
		return set(key, value, INFINITE_SECOND);
	}

	public abstract boolean set(String key, Serializable value, int ttlSecond);

	public boolean set(String key, Serializable value) {
		return set(key, value, INFINITE_SECOND);
	}

	/**
	 * 获取key对应的值
	 * 
	 * @param key
	 * @return
	 */
	public abstract String get(String key);

	public abstract byte[] getBytes(String key);

	public abstract Object getObject(String key);

	/**
	 * 原子增加一个计数器value个值
	 * 
	 * @param key
	 * @param value
	 *            每调用一次增加多个值
	 * @return
	 */
	public abstract long increase(String key, long value);

	public abstract long decrease(String key, long value);

	/**
	 * 删除某一个key
	 * 
	 * @param key
	 * @return
	 */
	public abstract boolean delete(String key);

	/**
	 * 设置一个value是list列表
	 * 
	 * @param key
	 * @param value
	 * @param ttlSecond
	 *            生存时间，单位秒，如果值等于INFINITE_SECOND是无限时
	 * @param pageSize
	 *            每页的分片个数，对于redis无效
	 * @return
	 */
	public abstract boolean lset(String key, List<Serializable> value,
			int ttlSecond, int pageSize);

	public boolean lset(String key, List<Serializable> value) {
		return lset(key, value, INFINITE_SECOND, DEF_PAGE_SIZE);
	}

	/**
	 * 向一个已存在的list添加一个值，如果list不存在，则创建一个
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public abstract boolean ladd(String key, Serializable value);

	public abstract boolean ladds(String key, List<Serializable> value);

	/**
	 * 获取key对应的list从start开始的length条记录
	 * 
	 * @param key
	 * @param start
	 * @param length
	 * @return
	 */
	public abstract List<Object> lget(String key, int start, int length);

	public List<Object> lget(String key) {
		return lget(key, 0, 0);
	}

	/**
	 * 获取指定pattern匹配的所有key值
	 * 
	 * @param pattern
	 * @return
	 */
	public abstract Set<String> getAllKeys(String pattern);

	public Set<String> getAllKeys() {
		return getAllKeys("*");
	}

	/**
	 * 连接内存服务器
	 */
	public abstract void connect(Properties property);

	/**
	 * exception公共处理方法
	 * 
	 * @param desc
	 *            发生问题位置描述
	 * @param e
	 *            异常
	 */
	public abstract void onException(String desc, Exception e);
	

	public memCacheClientType getClientType() {
		return clientType;
	}
	
	public int getErrCount() {
		return errCount;
	}
	
	public CacheClientPool getCacheClientPool() {
		return cacheClientPool;
	}

	/**
	 * 对多个Object转换成二进制
	 * 
	 * @param elements
	 * @return
	 * @throws IOException
	 */
	public static byte[] encodeObjects(Serializable... elements)
			throws IOException {
		ByteArrayOutputStream byteOut = null;
		ObjectOutputStream objOut = null;
		try {
			byteOut = new ByteArrayOutputStream(512);
			objOut = new ObjectOutputStream(byteOut);
			objOut.write(elements.length);
			for (Serializable element : elements)
				objOut.writeObject(element);
			objOut.flush();
			byteOut.flush();
			return byteOut.toByteArray();
		} finally {
			IOUtils.closeQuietly(byteOut);
			IOUtils.closeQuietly(objOut);
		}
	}

	/**
	 * 对单个Object转换成二进制
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public static byte[] encodeObject(Serializable element) throws IOException {
		ByteArrayOutputStream byteOut = null;
		ObjectOutputStream objOut = null;
		try {
			byteOut = new ByteArrayOutputStream(512);
			objOut = new ObjectOutputStream(byteOut);
			objOut.writeObject(element);
			objOut.flush();
			byteOut.flush();
			return byteOut.toByteArray();
		} finally {
			IOUtils.closeQuietly(byteOut);
			IOUtils.closeQuietly(objOut);
		}
	}

	/**
	 * 从二进制码流中，转换成Object对象
	 * 
	 * @param bytes
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object decodeObject(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream byteIn = null;
		ObjectInputStream objIn = null;
		try {
			byteIn = new ByteArrayInputStream(bytes);
			objIn = new ObjectInputStream(byteIn);

			return objIn.readObject();
		} finally {
			IOUtils.closeQuietly(byteIn);
			IOUtils.closeQuietly(objIn);
		}
	}
}
