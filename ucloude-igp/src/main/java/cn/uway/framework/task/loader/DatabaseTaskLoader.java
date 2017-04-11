package cn.uway.framework.task.loader;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.uway.framework.task.Task;
import cn.uway.framework.task.dao.TaskDAO;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 数据库扫描型任务加载器
 * 
 * @author chenrongqiang @ 2014-3-29
 */
public class DatabaseTaskLoader extends TaskLoader {

	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(DatabaseTaskLoader.class);

	/**
	 * 数据库扫描任务的周期，单位为分钟
	 */
	private int periodMinutes = 5;

	/**
	 * 任务扫描定时器
	 */
	private Timer timer;

	/**
	 * 任务查询DAO
	 */
	private TaskDAO taskDAO;

	public void setPeriodMinutes(int periodMinutes) {
		this.periodMinutes = periodMinutes;
	}

	public void setTaskDAO(TaskDAO taskDao) {
		this.taskDAO = taskDao;
	}

	@Override
	public void loadTask() {
		timer = new Timer("任务扫描器");
		startLoad();
		// 延迟periodMinutes分钟，每periodMinutes分钟执行一次
		long time = periodMinutes * 60 * 1000L;
		timer.schedule(new TimerLoader(), time, time);
	}

	/**
	 * 数据库扫描
	 */
	class TimerLoader extends TimerTask {

		@Override
		public void run() {
			startLoad();
		}
		
	}
	
	/**
	 * 开始加载任务
	 */
	public void startLoad() {
		if (taskDAO == null)
			assert(false);
		List<Task> tasks = taskDAO.loadTasks();
		if(tasks == null || tasks.isEmpty())
			LOGGER.debug("本次查找到任务数为0");
		for (Task task : tasks) {
			try {
				getTaskQueue().put(task);
			} catch (InterruptedException e) {
				LOGGER.warn("线程被中断。", e);
				return;
			}
		}
	}
}
