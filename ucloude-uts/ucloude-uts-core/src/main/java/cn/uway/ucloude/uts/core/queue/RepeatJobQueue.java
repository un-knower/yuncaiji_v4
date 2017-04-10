package cn.uway.ucloude.uts.core.queue;

import cn.uway.ucloude.uts.core.queue.domain.JobPo;


/**
 * 重复任务队列
 * @author Uway-M3
 *
 */
public interface RepeatJobQueue extends SchedulerJobQueue {
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

    /**
     * 增加重复次数
     */
    int incRepeatedCount(String jobId);
}
