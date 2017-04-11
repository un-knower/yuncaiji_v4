package cn.uway.framework.external;

import java.util.Map;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class AbstractCache {

	// 日志
	protected static final ILogger LOGGER = LoggerManager.getLogger(AbstractCache.class);

	// 每隔多少毫秒检查一次当前时间点是否要加载
	protected static final long CHECK_PERIOD_MILLS = 60 * 60 * 1000;

	// 任务时间周期
	protected static final long period = 24 * 60 * 60 * 1000;

	// 连接的最大等待时间，单位：秒
	protected static final int maxWaitSecond = 600;

	// 是否打开此处初始化设置
	protected static final boolean isTurnOnMaxWaitSecond = true;

	public AbstractCache() {
		super();
	}

	/**
	 * 加载一次，然后开启定时线程(静态)
	 */
	public static void startLoad() {
		return;
	}

	/**
	 * 加载一次，然后开启定时线程(动态)
	 */
	public void start() {
		return;
	}

	/**
	 * 判断是否为空
	 * 
	 * @return
	 */
	public boolean isNotEmpty() {
		return true;
	}

	/**
	 * 获取enodb网元信息。
	 * 
	 * @param key
	 * @return NeInfo
	 */
	public Map<String, String> getNeInfo(String key) {
		return null;
	}

	/**
	 * @param map
	 * @param groupByKey
	 * @return key
	 */
	public String getMyKey(Map<String, String> map, String[] groupByKey) {
		return null;
	}
	
	/**
	 * 通过indexKeys找到正确的indexKeys的值组合
	 * 
	 * @param map
	 * @param indexKeys
	 * @return key
	 */
	public String getMyKey(Map<String, String> map, String indexKeys) {
		return null;
	}

}
