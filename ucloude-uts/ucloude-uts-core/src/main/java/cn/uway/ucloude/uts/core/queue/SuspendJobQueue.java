package cn.uway.ucloude.uts.core.queue;

import cn.uway.ucloude.uts.core.queue.domain.JobPo;

/**
 * 暂停队列  存储
 * @author uway
 *
 */
public interface SuspendJobQueue extends JobQueue {
	/**
     * 添加任务
     *
     * @throws DupEntryException
     */
    boolean add(JobPo jobPo);

    JobPo getJob(String jobId);

    /**
     * 移除Cron Job
     */
    boolean remove(String jobId);

    /**
     * 得到JobPo
     */
    JobPo getJob(String taskTrackerNodeGroup, String taskId);
}
