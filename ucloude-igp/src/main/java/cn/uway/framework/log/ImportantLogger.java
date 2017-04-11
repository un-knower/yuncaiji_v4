package cn.uway.framework.log;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 重要信息日志打印类<br>
 * 使用ImportantLogger打印的日志会输出到控制台上<br>
 * 注：除了重要的错误，需要人工立马干预的，请不要使用ImportantLogger来打印日志<br>
 * 
 * @author chenrongqiang @ 2014-3-31
 */
public class ImportantLogger {

	private static final ILogger LOGGER = LoggerManager.getLogger("importantLogger");

	/**
	 * 私有化构造方法<br>
	 */
	private ImportantLogger() {
		super();
	}

	/**
	 * 工厂方法
	 * 
	 * @return 重要日志打印Logger
	 */
	public static ILogger getLogger() {
		return LOGGER;
	}
}
