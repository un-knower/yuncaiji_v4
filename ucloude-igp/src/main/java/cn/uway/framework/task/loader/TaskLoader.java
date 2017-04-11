package cn.uway.framework.task.loader;

import cn.uway.framework.task.TaskQueue;

/**
 * 任务加载器接口
 * <p>
 * 通过任务加载器把任务加载到任务队列{@link TaskQueue}中.
 * </p>
 * 
 * @author chenrongqiang @ 2014-3-29
 */
public abstract class TaskLoader {

	/**
	 * 任务队列<br>
	 */
	protected TaskQueue taskQueue;

	/**
	 * 加载任务,把任务加载到任务队列中。
	 */
	public abstract void loadTask();

	protected TaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(TaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}
}
