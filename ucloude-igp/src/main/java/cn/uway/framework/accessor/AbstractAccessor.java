package cn.uway.framework.accessor;

import java.util.Date;

import cn.uway.framework.task.Task;

/**
 * 抽象接入器。
 * 
 * @author ChenSijiang 2012-11-1
 */
public abstract class AbstractAccessor implements Accessor {

	/**
	 * 采集对象
	 */
	protected String gatherObj;

	/**
	 * 失败原因
	 */
	protected String cause;

	/**
	 * 接入开始时间
	 */
	protected Date startTime;

	/**
	 * 接入结束时间
	 */
	protected Date endTime;

	protected Task task;
	
	public AbstractAccessor() {
		super();
	}

	@Override
	public boolean beforeAccess() {
		return true;
	}

	@Override
	public void close() {
		this.endTime = new Date();
	}
	
	

	@Override
	public Task getTask() {
		return task;
	}

	@Override
	public void setTask(Task task) {
		this.task = task;
	}

	/**
	 * 获取解析报告 具体的报告信息由子类实现
	 */
	public AccessorReport report() {
		AccessorReport accessorReport = new AccessorReport();
		accessorReport.setStartTime(startTime);
		accessorReport.setEndTime(endTime);
		accessorReport.setGatherObj(gatherObj);
		accessorReport.setCause(cause);
		return accessorReport;
	}
}
