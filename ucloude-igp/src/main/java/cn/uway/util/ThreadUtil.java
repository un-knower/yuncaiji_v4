package cn.uway.util;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 增加线程工具类<br>
 * 比如一些常见的线程休眠不用再捕获异常
 * 
 * @author chenrongqiang @ 2013-4-21
 */
public final class ThreadUtil {

	/**
	 *  日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(ThreadUtil.class); 

	/**
	 * 线程休眠类
	 * 
	 * @param millis 休眠毫秒数
	 */
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOGGER.error("线程：{}休眠异常", Thread.currentThread().getName(),e);
		}
	}
}
