package cn.uway.framework.job;

import java.util.concurrent.Callable;

import cn.uway.framework.task.Task;

/**
 * 作业接口
 * <p>
 * 任务{@link Task}下所有的行为称为{@link Job}.<br>
 * 运行返回值为{@link JobFuture}.
 * </p>
 * 
 * @author chenrongqiang
 * @Date 2012-10-27
 * @version 1.0
 * @since 3.0
 * @see JobFuture
 */
public interface Job extends Callable<JobFuture> {

	/**
	 * JOB执行方法
	 */
	JobFuture call();
}
