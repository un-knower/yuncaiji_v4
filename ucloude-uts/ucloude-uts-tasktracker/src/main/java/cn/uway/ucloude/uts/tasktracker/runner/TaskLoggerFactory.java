package cn.uway.ucloude.uts.tasktracker.runner;

import cn.uway.ucloude.uts.tasktracker.logger.BizLogger;

/**
 * 任务执行日志反馈器
 * @author uway
 *
 */
public final class TaskLoggerFactory {
	private static final ThreadLocal<BizLogger> THREAD_LOCAL = new ThreadLocal<BizLogger>();

	public static BizLogger getBizLogger() {
		return THREAD_LOCAL.get();
	}

	protected static void setLogger(BizLogger logger) {
		THREAD_LOCAL.set(logger);
	}

	protected static void remove() {
		THREAD_LOCAL.remove();
	}
}
