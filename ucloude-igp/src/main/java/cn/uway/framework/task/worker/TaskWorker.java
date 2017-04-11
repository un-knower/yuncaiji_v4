package cn.uway.framework.task.worker;

import java.util.concurrent.Callable;

import cn.uway.framework.task.TaskFuture;

/**
 * TaskWorker 每一个TaskWorker都是一个线程
 * 
 * @author chenrongqiang 2012-12-2
 */
public interface TaskWorker extends Callable<TaskFuture> {

	/**
	 * 在task开始工作前执行操作
	 */
	void beforeWork();

	/**
	 * 执行task。并且返回执行结果
	 * 
	 * @return
	 */
	TaskFuture work() throws Exception;

	/**
	 * 在task执行完成或者异常或执行操作
	 */
	void afterWork(TaskFuture taskFuture);
}
