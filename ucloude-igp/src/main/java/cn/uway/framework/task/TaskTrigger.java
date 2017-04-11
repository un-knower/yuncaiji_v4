package cn.uway.framework.task;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.uway.framework.log.ImportantLogger;
import cn.uway.framework.task.worker.AbstractTaskWorker;
import cn.uway.framework.task.worker.ITaskDeliver;
import cn.uway.framework.task.worker.TaskWorker;
import cn.uway.framework.task.worker.TaskWorkerFactory;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 任务触发器
 * <p>
 * 从任务队列{@link TaskQueue}中提取任务对象，并创建相应的{@link TaskWorker}.<br>
 * 任务的运行启动以及运行结束后的返回值处理等都在此类中完成.
 * </p>
 * 
 */
public class TaskTrigger extends Thread {

	/**
	 * 任务队列
	 */
	private TaskQueue taskQueue;

	/**
	 * 触发器开关 当triggerFlag=false时 不会有新的任务提交
	 */
	private volatile boolean triggerFlag = true;

	/**
	 * 任务线程池
	 */
	private ExecutorService threadPool;

	/**
	 * 任务线程池包装类
	 */
	private CompletionService<TaskFuture> service;

	/**
	 * 正在运行的任务的Map<br>
	 * 同时使用workingTasks来进行并发控制,而不是使用线程池的线程并发控制,目的在于将运行队列提供给控制台用于显示<br>
	 * 
	 */
	private Set<Task> workingTasks = new HashSet<Task>();

	/**
	 * 当前正在运行的所有补采任务。
	 */
	private Set<ReTask> workingReTasks = new HashSet<ReTask>();

	/**
	 * 当前正在运行的所有延迟数据任务，因为这种数据不会太多，所以线程数量和正常采集任务的数量一致。
	 */
	private Set<DelayTask> workingDelayTasks = new HashSet<DelayTask>();
	
	/**
	 * 最大并发任务线程数。
	 */
	private int maxRunningTaskNum;

	private String strMaxRunningReTaskNum;

	/**
	 * 最大并发补采任务线程数。
	 */
	private int maxRunningReTaskNum;

	/**
	 * 任务执行线程监听器<br>
	 */
	private Listener listener;
	
	private ITaskDeliver taskDeliver;
	
	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(TaskTrigger.class);

	public TaskTrigger() {
		super("任务触发器");
	}

	public void setTaskQueue(TaskQueue taskQueue) {
		if (taskQueue == null)
			throw new NullPointerException("任务队列没有配置");
		this.taskQueue = taskQueue;
	}
	
	private void initialize() {
		// maxRunningTaskNum通过spring注入 如注入值非法 则默认为10
		if (maxRunningTaskNum <= 0)
			maxRunningTaskNum = 10;
		try {
			maxRunningReTaskNum = Integer.parseInt(strMaxRunningReTaskNum);
		} catch (Exception ex) {
		}
		if (maxRunningReTaskNum <= 0)
			maxRunningReTaskNum = 3;
		threadPool = Executors.newFixedThreadPool(maxRunningTaskNum);
		service = new ExecutorCompletionService<TaskFuture>(threadPool);
		// 启动任务执行完毕后处理线程
		listener = new Listener();
		// 设置为守护线程
		listener.setDaemon(true);
		listener.start();
	}

	/**
	 * 任务触发器线程工作方法<br>
	 * 1、从任务队列中提取任务.如没有任务,则等待任务加载<br>
	 * 2、将扫描到的任务提交至运行线程池中<br>
	 */
	public void run() {
		LOGGER.debug(" 已启动");
		initialize();
		long taskId = 0;
		while (triggerFlag) {
			try {
				Task task = taskQueue.take();

				taskId = getRightTaskId(task);
				if (running(task)) {
					LOGGER.debug(" 任务已经在运行中. [task-id={},task-name={}]", taskId, task.getName());
					continue;
				}

				if (task instanceof DelayTask) {
					synchronized (workingDelayTasks) {
						DelayTask delayTask = (DelayTask) task;
						if (workingDelayTasks.size() >= this.maxRunningReTaskNum) {
							LOGGER.warn("延迟数据任务{}（{}）{}，暂时无法运行，因为当前运行的任务数量已达到配置的最大任务数量：{}.", new Object[]{delayTask.getId(), delayTask.getName(),delayTask.getDelayId(),
							 this.maxRunningReTaskNum});
							taskQueue.put(delayTask);
							Thread.sleep(100);
							continue;
						} else {
							workingDelayTasks.add(delayTask);
						}
					}
				} else if (task instanceof ReTask) {
					synchronized (workingReTasks) {
						ReTask reTask = (ReTask) task;
						if (workingReTasks.size() >= this.maxRunningReTaskNum) {
							 LOGGER.warn("补采任务{}（“{}”的补采任务，正常任务id为），暂时无法运行，因为当前运行的补采任务数量已达到配置的最大补采任务数量：{}.",
							 new Object[]{reTask.getrTaskId(), reTask.getName(), reTask.getId(), this.maxRunningReTaskNum});
							 taskQueue.put(reTask);
							 Thread.sleep(100);
							continue;
						} else {
							workingReTasks.add(reTask);
						}
					}
				} else {
					synchronized (workingTasks) {
						if (workingTasks.size() >= this.maxRunningTaskNum) {
							LOGGER.warn("任务{}（{}），暂时无法运行，因为当前运行的任务数量已达到配置的最大任务数量：{}.", new Object[]{task.getId(), task.getName(),
							 this.maxRunningTaskNum});
							taskQueue.put(task);
							Thread.sleep(100);
							continue;
						} else {
							workingTasks.add(task);
						}
					}
				}

				// 设置任务本次启动时间
				task.setBeginRuntime(new Date());
				AbstractTaskWorker taskWorker = TaskWorkerFactory.getTaskWorkerFactory(task);
				taskWorker.setDeliver(this.taskDeliver);
				service.submit(taskWorker);
				LOGGER.debug("taskId={},taskName={}已提交至运行队列中,当前任务运行队列大小={},最大任务运行队列大小={}", new Object[]{taskId, task.getName(), workingTasks.size(),
						maxRunningTaskNum});
			} catch (InterruptedException e) {
				ImportantLogger.getLogger().error("从任务队列TaskQueue中提取任务异常,请检查程序", e);
				continue;
			}
		}
		LOGGER.warn("由于触发器已停止，任务不再启动。");
		LOGGER.warn(" 线程已停止，不再接受新的任务");
	}

	/**
	 * 判断任务是否在运行 周期性任务 判断task_id和数据时间 非周期性任务 直接通过task_id进行判断
	 * 
	 * @param task
	 * @return 任务是否已经在运行 true表示已经在运行 false 表示未运行
	 */
	protected boolean running(Task task) {
		if (task instanceof DelayTask) {
			synchronized (workingDelayTasks) {
				return workingDelayTasks.contains(task);
			}
		} else if (task instanceof ReTask) {
			synchronized (workingReTasks) {
				return workingReTasks.contains(task);
			}
		} else {
			synchronized (workingTasks) {
				return workingTasks.contains(task);
			}
		}
	}

	/**
	 * 获取正在运行的任务表
	 * 
	 * @return Set<任务对象>
	 */
	public Set<Task> getWorkingTaskList() {
		Set<Task> copy = new HashSet<Task>();
		synchronized (workingTasks) {
			copy.addAll(workingTasks);
		}
		synchronized (workingReTasks) {
			copy.addAll(workingReTasks);
		}
		synchronized (workingDelayTasks) {
			copy.addAll(workingDelayTasks);
		}
		return copy;
	}

	public void setMaxRunningTaskNum(int maxRunningTaskNum) {
		this.maxRunningTaskNum = maxRunningTaskNum;
	}

	public void setMaxRunningReTaskNum(int maxRunningReTaskNum) {
		this.maxRunningReTaskNum = maxRunningReTaskNum;
	}

	public void setStrMaxRunningReTaskNum(String strMaxRunningReTaskNum) {
		this.strMaxRunningReTaskNum = strMaxRunningReTaskNum;
	}

	public void setTriggerFalse() {
		this.triggerFlag = false;
	}

	/**
	 * 停止任务触发
	 */
	public synchronized void stopTrigger() {
		triggerFlag = false;
		listener.interrupt();
		threadPool.shutdownNow();
		this.interrupt();
		synchronized (workingTasks) {
			workingTasks.clear();
		}
		synchronized (workingReTasks) {
			workingReTasks.clear();
		}
		synchronized (workingDelayTasks) {
			workingDelayTasks.clear();
		}
		LOGGER.warn(" 将被外部停止");
	}

	/**
	 * 将任务从运行队列中移除 同时唤醒trigger线程
	 * 
	 * @param task
	 */
	public void removeTask(Task task) {
		if (task instanceof DelayTask) {
			synchronized (workingDelayTasks) {
				DelayTask delayTask = (DelayTask) task;
				if (!workingDelayTasks.remove(task)) {
					ImportantLogger.getLogger().error("延迟数据任务已从运行队列移除失败,延迟数据任务Id={}在运行任务队列中不存在", delayTask.getDelayId());
					return;
				}
				LOGGER.debug("延迟数据任务已从运行队列移除：{}", delayTask.getDelayId());
			}
		} else if (task instanceof ReTask) {
			synchronized (workingReTasks) {
				ReTask reTask = (ReTask) task;
				if (!workingReTasks.remove(task)) {
					ImportantLogger.getLogger().error("补采任务已从运行队列移除失败,补采任务Id={}在运行任务队列中不存在", reTask.getrTaskId());
					return;
				}
				LOGGER.debug("补采任务已从运行队列移除：{}", reTask.getrTaskId());
			}
		} else {
			synchronized (workingTasks) {
				if (!workingTasks.remove(task)) {
					ImportantLogger.getLogger().error("任务已从运行队列移除失败,taskId={}在运行任务队列中不存在", task.getId());
					return;
				}
				LOGGER.debug("任务已从运行队列移除：{}", task.getId());
			}
		}

	}

	/**
	 * 任务执行结果处理监听器<br>
	 * 监听任务的执行结果
	 */
	class Listener extends Thread {

		Listener() {
			super("任务结果处理器");
		}

		@Override
		public void run() {
			TaskFuture taskFuture = null;
			long taskId = 0;
			LOGGER.debug("任务运行结果提取线程启动。");
			while (true) {
				try {
					// 取出任务运行结果 如果没有返回 则线程会挂起
					Future<TaskFuture> future = service.take();
					if (future == null) {
						ImportantLogger.getLogger().error("提取线程返回结果异常.Future==null");
						continue;
					}
					taskFuture = future.get();
					if (taskFuture == null) {
						ImportantLogger.getLogger().error("提取线程返回结果异常.TaskFuture==null");
						continue;
					}
					int code = taskFuture.getCode();
					Task task = taskFuture.getTask();
					taskId = getRightTaskId(task);
					LOGGER.debug("任务运行{},[taskId={},taskName={},{}]", new Object[]{code == 0 ? "成功" : "失败", taskId, task.getName(),
							code == 0 ? null : "cause=" + taskFuture.getCause()});
				} catch (InterruptedException e) {
					ImportantLogger.getLogger().error("提取任务线程运行结果失败", e);
					continue;
				} catch (ExecutionException e) {
					ImportantLogger.getLogger().error("提取任务线程运行结果失败", e);
					continue;
				} finally {
					// 无论返回成功与否都必须从 当前运行任务表 中清除掉
					if (taskFuture != null && taskFuture.getTask() != null)
						removeTask(taskFuture.getTask());
				}
			}
		}
	}

	/**
	 * @param task
	 * @return taskId
	 */
	public long getRightTaskId(Task task) {
		long taskId = task.getId();
		if (task instanceof ReTask) {
			taskId = ((ReTask) task).getrTaskId();
		}else if(task instanceof DelayTask){
			taskId =  ((DelayTask) task).getDelayId();
		}
		return taskId;
	}
	
	public ITaskDeliver getTaskDeliver() {
		return taskDeliver;
	}
	
	public void setTaskDeliver(ITaskDeliver taskDeliver) {
		this.taskDeliver = taskDeliver;
	}
}
