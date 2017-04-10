package cn.uway.ucloude.uts.core.queue;

import cn.uway.ucloude.uts.core.queue.domain.JobPo;

/**
 * 周期性任务队列
 * @author Uway-M3
 *
 */
public interface CronJobQueue extends SchedulerJobQueue {
	/**
     * 添加任务
     *
     * @throws DupEntryException
     */
    boolean add(JobPo jobPo);

    /**
     * 完成某一次执行，返回队列中的这条记录
     */
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
