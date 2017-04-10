package cn.uway.ucloude.uts.core.queue;

import java.util.List;

import cn.uway.ucloude.uts.core.queue.domain.JobPo;

/**
 * 等待执行的任务队列 (可以有多个)
 * @author uway
 *
 */
public interface ExecutableJobQueue extends JobQueue {
	/**
	 * 创建一个队列
	 */
	boolean createQueue(String taskTrackerNodeGroup);

	/**
	 * 删除
	 */
	boolean removeQueue(String taskTrackerNodeGroup);

	/**
	 * 入队列
	 */
	boolean add(JobPo jobPo);

	/**
	 * 出队列
	 */
	boolean remove(String taskTrackerNodeGroup, String jobId);

	/**
	 * 获取任务数量
	 * @param realTaskId
	 * @param taskTrackerNodeGroup
	 * @return
	 */
	long countJob(String realTaskId, String taskTrackerNodeGroup);

	/**
	 * 批量删除任务
	 * @param realTaskId
	 * @param taskTrackerNodeGroup
	 * @return
	 */
	boolean removeBatch(String realTaskId, String taskTrackerNodeGroup);

	/**
	 * 重新开始，继续,修改IsRunning为false,修改TaskTrackerIdentity为空，修改gmtModified时间，根据JobID
	 * reset , runnable
	 */
	void resume(String jobId,String taskTrackerGroup);

	/**
	 * 得到死任务
	 */
	List<JobPo> getDeadJob(String taskTrackerNodeGroup, long deadline);
	
	/**
	 * 得到JobPo
	 */
	JobPo getJob(String taskTrackerNodeGroup, String taskId);
}
