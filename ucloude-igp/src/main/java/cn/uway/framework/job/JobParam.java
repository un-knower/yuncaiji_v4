package cn.uway.framework.job;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.solution.GatherSolution;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.Task;

/**
 * 作业参数<br>
 * 用于下发job之用
 * 
 * @author chenrongqiang @ 2014-3-30
 */
public class JobParam {

	/**
	 * 任务对象
	 */
	private final Task task;

	/**
	 * 任务对应的连接信息
	 */
	private final ConnectionInfo connInfo;

	/**
	 * 采集解决方案实体
	 */
	private final GatherSolution solution;

	/**
	 * 采集路径实体
	 */
	private final GatherPathEntry pathEntry;

	/**
	 * 构造函数
	 * 
	 * @param task 任务对象
	 * @param connInfo 采集数据源连接信息
	 * @param solution 采集解决方案
	 * @param pathEntry 采集路径实体
	 */
	public JobParam(Task task, ConnectionInfo connInfo, GatherSolution solution, GatherPathEntry pathEntry) {
		super();
		this.task = task;
		this.connInfo = connInfo;
		this.solution = solution;
		this.pathEntry = pathEntry;
	}

	/**
	 * 获取作业参数中绑定的任务对象
	 */
	public Task getTask() {
		return task;
	}

	/**
	 * 获取作业参数中绑定的采集路径实体数组
	 */
	public GatherPathEntry getPathEntry() {
		return pathEntry;
	}
	
	/**
	 * 获取作业参数中绑定的采集数据源连接信息
	 * 
	 * @return the connInfo
	 */
	public ConnectionInfo getConnInfo() {
		return connInfo;
	}

	/**
	 * 获取作业参数中绑定的采集解决方案
	 * 
	 * @return the solution
	 */
	public GatherSolution getSolution() {
		return solution;
	}
}
