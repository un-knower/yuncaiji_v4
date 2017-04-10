package cn.uway.ucloude.utils.memCacheService;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.KeyIterator;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.ucloude.utils.StringUtil;

@SuppressWarnings("deprecation")
public class MCCacheClient extends AbsCacheClient {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MCCacheClient.class);
	
	/**
	 * list类型，key值分页符号．如:$key@@0,$key@@1,$key@@2,...
	 */
	protected final static String LIST_PAGE_SPLIT_CHAR = "@@";
	
	/**
	 * list类型，catalog key的符号
	 */
	protected final static String LIST_PAGE_CATALOG_WITH_KEY = "@@0";
	
	/**
	 * xmemcached客户端对象
	 */
	protected MemcachedClient cache;
	
	/**
	 * 服务器配置参数{ip:port ip:port ip:port ...}
	 */
	protected String serverOpts;

	public MCCacheClient(CacheClientPool cacheClientPool, String serviceName) {
		super(cacheClientPool, memCacheClientType.client_memCache, serviceName);
		//无限时（memcached设定30天,长了有问题)
		this.INFINITE_SECOND = (30*24*60*60);
	}

	@Override
	public void onDestory() {
		if (cache != null) {
			try {
				cache.shutdown();
			} catch (IOException e) {
				LOGGER.info("shutdown memcached has exception ocurred. msg:"
						+ e.getMessage());
			}
			cache = null;
		}
	}

	@Override
	public boolean set(String key, String value, int ttlSecond) {
		if (key == null)
			return false;
		
		try {
			boolean ret = cache.set(key, ttlSecond, value);
			this.errCount = 0;
			return ret;
		} catch (Exception e) {
			onException("MCCacheClient.set String has exception occured.", e);
		}

		return false;
	}

	@Override
	public boolean set(String key, byte[] value, int ttlSecond) {
		if (key == null)
			return false;

		try {
			boolean ret = cache.set(key, ttlSecond, value);
			this.errCount = 0;
			return ret;
		} catch (Exception e) {
			onException("MCCacheClient.set byte[] has exception occured.", e);
		}

		return false;
	}

	@Override
	public boolean set(String key, Serializable value, int ttlSecond) {
		if (key == null)
			return false;

		try {
			boolean ret = cache.set(key, ttlSecond, value);
			this.errCount = 0;
			return ret;
		} catch (Exception e) {
			onException(
					"MCCacheClient.set Serializable has exception occured.", e);
		}

		return false;
	}

	@Override
	public String get(String key) {
		return (String) getObject(key);
	}

	@Override
	public byte[] getBytes(String key) {
		return (byte[]) getObject(key);
	}

	@Override
	public Object getObject(String key) {
		if (key == null)
			return null;

		try {
			Object ret = cache.get(key);
			this.errCount = 0;
			return ret;
		} catch (Exception e) {
			onException("MCCacheClient.get has exception occured.", e);
		}

		return null;
	}

	@Override
	public long increase(String key, long value) {
		if (key == null)
			return -1;

		try {
			long ret = cache.incr(key, value, value);
			this.errCount = 0;
			return ret;
		} catch (Exception e) {
			onException("MCCacheClient.increase has exception occured.", e);
		}

		return -1;
	}

	@Override
	public long decrease(String key, long value) {
		if (key == null)
			return -1;

		try {
			long ret = cache.decr(key, value, value);
			this.errCount = 0;
			return ret;
		} catch (Exception e) {
			onException("MCCacheClient.decrease has exception occured.", e);
		}

		return -1;
	}

	@Override
	public boolean delete(String key) {
		if (key == null)
			return false;

		try {
			boolean ret = cache.delete(key);
			this.errCount = 0;
			return ret;
		} catch (Exception e) {
			onException("MCCacheClient.delete has exception occured.", e);
		}

		return false;
	}

	@Override
	public boolean lset(String key, List<Serializable> value, int ttlSecond,
			int pageSize) {
		if (key == null || value == null)
			return false;

		int nRecordCount = value.size();
		if (pageSize < 1)
			pageSize = DEF_PAGE_SIZE;

		StringBuilder sb = new StringBuilder();
		try {
			// 设置list catalog信息
			String catalogPageKey = StringUtil.concatWithSplitChars(
					LIST_PAGE_SPLIT_CHAR, key, "0");
			String catalogPageValue = StringUtil.concatWithSplitChars(",",
					String.valueOf(nRecordCount), String.valueOf(pageSize),
					String.valueOf(ttlSecond));
			cache.set(catalogPageKey, (int) ttlSecond, catalogPageValue);

			// 添加list　group内容
			int recordIndex = 0;
			int pageIndex = 0;
			String pageKey = null;
			Object[] pageObjs = new Object[Math.min(pageSize,
					(int) nRecordCount)];
			for (Object v : value) {
				int pageObjIndex = recordIndex % pageSize;
				if (pageObjIndex == 0) {
					pageIndex = (int) (recordIndex / pageSize) + 1;

					if (pageKey != null) {
						cache.set(pageKey, (int) ttlSecond, pageObjs);
					}

					// build page key;
					if (sb.length() > 0)
						sb.setLength(0);

					sb.append(key).append(LIST_PAGE_SPLIT_CHAR)
							.append(pageIndex);
					pageKey = sb.toString();
				}

				pageObjs[pageObjIndex] = v;
				recordIndex++;
			}

			if (pageKey != null) {
				int sizeLeft = pageSize - (recordIndex % pageSize);
				// 将剩余的空列置null，避免被系列化(pageSize range:0至pageSize-1)
				if (sizeLeft < pageSize) {
					while (sizeLeft > 0) {
						pageObjs[pageSize - sizeLeft] = null;
						--sizeLeft;
					}
				}

				cache.set(pageKey, (int) ttlSecond, pageObjs);
			}

			this.errCount = 0;
			return true;
		} catch (Exception e) {
			onException("MCCacheClient.lset has exception occured.", e);
		}

		return false;
	}

	@Override
	public boolean ladd(String key, Serializable value) {
		if (key == null || value == null)
			return false;

		List<Serializable> lst = new LinkedList<Serializable>();
		lst.add(value);
		return ladds(key, lst);
	}

	@Override
	public boolean ladds(String key, List<Serializable> value) {
		if (key == null || value == null)
			return false;

		StringBuilder sb = new StringBuilder();
		try {
			// 读取list的catalog信息
			String catalogPageKey = StringUtil.concatWithSplitChars(
					LIST_PAGE_SPLIT_CHAR, key, "0");
			String catalogPageValue = cache.get(catalogPageKey);
			String[] params = StringUtil.split(catalogPageValue, ",");
			if (params == null || params.length < 3)
				return false;

			int nRecordCount = Integer.parseInt(params[0]) + value.size();
			int pageSize = Integer.parseInt(params[1]);
			int ttlSecond = Integer.parseInt(params[2]);
			catalogPageValue = StringUtil.concatWithSplitChars(",",
					String.valueOf(nRecordCount), String.valueOf(pageSize),
					String.valueOf(ttlSecond));
			cache.set(catalogPageKey, ttlSecond, catalogPageValue);

			// 获取插入位置信息．
			int addStartRecordIndex = nRecordCount - value.size();
			int addStartPageIndex = (int) (addStartRecordIndex / pageSize) + 1;
			String addStartPageKey = StringUtil.concatWithSplitChars(
					LIST_PAGE_SPLIT_CHAR, key,
					String.valueOf(addStartPageIndex));
			Object[] pageObjs = null;
			//allocate new page.
			if ((addStartRecordIndex % pageSize) == 0)
				pageObjs = new Object[Math.min(pageSize, (int) nRecordCount)];
			else
				pageObjs = cache.get(addStartPageKey);

			if (pageObjs == null) {
				LOGGER.debug("can't acquire page of index:" + addStartPageKey);
				return false;
			}

			int recordIndex = addStartRecordIndex;
			int pageIndex = addStartPageIndex;
			String pageKey = addStartPageKey;
			for (Object v : value) {
				int pageObjIndex = recordIndex % pageSize;
				if (pageObjIndex == 0) {
					pageIndex = (int) (recordIndex / pageSize) + 1;

					if (pageKey != null) {
						cache.set(pageKey, (int) ttlSecond, pageObjs);
					}

					// build page key;
					if (sb.length() > 0)
						sb.setLength(0);

					sb.append(key).append(LIST_PAGE_SPLIT_CHAR)
							.append(pageIndex);
					pageKey = sb.toString();
				}

				pageObjs[pageObjIndex] = v;
				recordIndex++;
			}

			if (pageKey != null) {
				int sizeLeft = pageSize - (recordIndex % pageSize);
				// 将剩余的空列置null，避免被系列化(pageSize range:0至pageSize-1)
				if (sizeLeft < pageSize) {
					while (sizeLeft > 0) {
						pageObjs[pageSize - sizeLeft] = null;
						--sizeLeft;
					}
				}

				cache.set(pageKey, (int) ttlSecond, pageObjs);
			}

			this.errCount = 0;
			return true;
		} catch (Exception e) {
			onException("MCCacheClient.ladd has exception occured.", e);
		}

		return false;
	}

	@Override
	public List<Object> lget(String key, int start, int length) {
		if (key == null)
			return null;

		StringBuilder sb = new StringBuilder();
		try {
			// 读取list的catalog信息
			String catalogPageKey = StringUtil.concatWithSplitChars(
					LIST_PAGE_SPLIT_CHAR, key, "0");
			String catalogPageValue = cache.get(catalogPageKey);
			String[] params = StringUtil.split(catalogPageValue, ",");
			if (params == null || params.length < 3)
				return null;

			int nRecordCount = Integer.parseInt(params[0]);
			int pageSize = Integer.parseInt(params[1]);
			if (length < 1 || ((start+length)>nRecordCount))
				length = nRecordCount - start;
			
			int recordIndex = start;
			int pageIndex = 0;
			String pageKey = null;
			Object[] pageObjs = null;
			List<Object> lstResult = new ArrayList<Object>(length);
			for (int i = 0; i < length && i < nRecordCount; ++i) {
				int pageObjIndex = recordIndex % pageSize;
				if (pageObjs == null || pageObjIndex == 0) {
					pageIndex = (int) (recordIndex / pageSize) + 1;

					// build page key;
					if (sb.length() > 0)
						sb.setLength(0);

					sb.append(key).append(LIST_PAGE_SPLIT_CHAR)
							.append(pageIndex);
					pageKey = sb.toString();
					pageObjs = cache.get(pageKey);
					if (pageObjs == null) {
						LOGGER.debug("can't acquire page of index:" + pageKey);
						return null;
					}
				}

				lstResult.add(pageObjs[pageObjIndex]);
				recordIndex++;
			}

			this.errCount = 0;
			return lstResult;
		} catch (Exception e) {
			onException("MCCacheClient.lget has exception occured.", e);
		}

		return null;
	}

	@Override
	public Set<String> getAllKeys(String pattern) {
		Set<String> keys = new HashSet<String>();
		try {
			Collection<InetSocketAddress> addrs = cache.getAvailableServers();
			if (addrs == null || addrs.size() < 1)
				return keys;

			for (InetSocketAddress addr : addrs) {
				KeyIterator iter = cache.getKeyIterator(addr);
				while (iter.hasNext()) {
					String key = iter.next();
					if (key.contains(LIST_PAGE_SPLIT_CHAR)) {
						if (key.endsWith(LIST_PAGE_CATALOG_WITH_KEY)) {
							String lstKey = key.substring(0, key.length()
									- LIST_PAGE_CATALOG_WITH_KEY.length());

							if (pattern == null
									|| FilenameUtils
											.wildcardMatch(key, pattern)) {
								keys.add(lstKey);
							}
						}

						continue;
					}

					if (pattern == null
							|| FilenameUtils.wildcardMatch(key, pattern)) {
						keys.add(key);
					}
				}
			}

			this.errCount = 0;
			return keys;
		} catch (Exception e) {
			onException("MCCacheClient.getAllKeys fault.", e);
		}

		return null;
	}

	@Override
	public void connect(Properties property) {
		if (cache != null && !cache.isShutdown()) {
			return;
		}
		
		String propKey = "memcached.serverOpts." + this.serviceName;
		if (property != null && property.containsKey(propKey)) {
			this.serverOpts = property.getProperty(propKey);
		}
		
		List<InetSocketAddress> addrs = AddrUtil.getAddresses(this.serverOpts);
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(addrs);
		
		try {
			cache = builder.build();
			this.errCount = 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onException(String desc, Exception e) {
		if (e instanceof MemcachedException) {
			++this.errCount;
			LOGGER.debug(desc + " msg:" + e.getMessage());
		} else if (e instanceof InterruptedException) {
			++this.errCount;
			LOGGER.debug(desc + " msg:" + e.getMessage());
		} else if (e instanceof TimeoutException) {
			++this.errCount;
			LOGGER.debug(desc + " msg:" + e.getMessage());
		} else {
			LOGGER.debug(desc + " msg:" + e.getMessage());
		}

		if (this.errCount >= 5) {
			//重连
			onDestory();
			connect(null);
		}
	}
}
