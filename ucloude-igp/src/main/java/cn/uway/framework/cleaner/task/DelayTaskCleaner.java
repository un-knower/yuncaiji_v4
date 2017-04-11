package cn.uway.framework.cleaner.task;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.task.dao.TaskDAO;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 延迟任务清理器
 * 
 * @author tylerlee @ 2016年8月11日
 */
public class DelayTaskCleaner implements Runnable {

	private static final int DELETE_HOUR = 3;
	private static final long ONE_DAY = 24 * 60 * 60 * 1000;// 一天

	private Timer timer = new Timer();

	private TaskDAO taskDAO = AppContext.getBean("taskDAO", TaskDAO.class);

	private static ILogger LOGGER = LoggerManager.getLogger(DelayTaskCleaner.class); // 日志

	@Override
	public void run() {
		Calendar c = Calendar.getInstance();
		Date now = new Date();
		c.setTime(now);
		c.set(Calendar.HOUR_OF_DAY, DELETE_HOUR);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		// 判断当前时间是否超过凌晨3点，如果超过，就到第二天凌晨3点首次执行
		if (c.getTimeInMillis() < now.getTime()) {
			c.add(Calendar.DAY_OF_YEAR, 1);
		}
		LOGGER.info("延迟任务当前时间为{},首次执行时间为{}", now, c.getTime());
		// 首次执行时间为凌晨三点，此后每隔一天执行一次；
		timer.schedule(new DelayTaskTimerTask(), c.getTime(), ONE_DAY);
	}

	public void deleteExpiredDelayTask() {
		taskDAO.deleteExpiredDelayTask();
	}

	class DelayTaskTimerTask extends TimerTask {

		public DelayTaskTimerTask() {
			Thread.currentThread().setName("【延迟任务清理线程】");
		}

		@Override
		public void run() {
			LOGGER.info("开始执行延迟任务删除操作。");
			deleteExpiredDelayTask();
		}

	}
}
