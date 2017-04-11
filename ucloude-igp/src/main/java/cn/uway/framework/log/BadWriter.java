package cn.uway.framework.log;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 错误数据日志写入<br>
 * 将由问题的日志写入到badwriter.log中，单例实现
 * 
 * @author chenrongqiang @ 2014-3-30
 */
public class BadWriter {

	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger("badWriter");

	/**
	 * 单例对象
	 */
	private static BadWriter instance = new BadWriter();

	private BadWriter(){
		super();
	}
	
	public static BadWriter getInstance() {
		return instance;
	}

	public ILogger getBadWriter() {
		return LOGGER;
	}
}
