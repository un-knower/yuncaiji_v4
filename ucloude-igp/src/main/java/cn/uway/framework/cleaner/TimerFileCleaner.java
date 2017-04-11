package cn.uway.framework.cleaner;

import java.util.Date;
import java.util.Timer;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 定时文件清理器<br>
 * 
 * @author chenrongqiang @ 2013-9-5
 */
public class TimerFileCleaner extends Thread {

	/**
	 * 定时清理的频率 单位分钟
	 */
	private int period;

	/**
	 * 文件清理器<br>
	 */
	private FileCleaner cleaner;

	/**
	 * @param period
	 *            the period to set
	 */
	public void setPeriod(int period) {
		this.period = period;
	}

	/**
	 * @param cleaner
	 *            the cleaner to set
	 */
	public void setCleaner(FileCleaner cleaner) {
		this.cleaner = cleaner;
	}

	/**
	 * 日志<br>
	 */
	private static ILogger LOGGER = LoggerManager.getLogger(TimerFileCleaner.class); // 日志

	/**
	 * 定时调度Timer类<br>
	 */
	private Timer timer = new Timer();

	@Override
	public void run() {
		Thread.currentThread().setName("【文件清理定时线程】");
		LOGGER.debug("启动：清理频率{}分钟", period);
		timer.schedule(cleaner, new Date(), period * 60 * 1000);
	}
}
