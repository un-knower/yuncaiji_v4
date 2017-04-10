package cn.uway.ucloude.uts.core.queue;

import java.util.List;

import cn.uway.ucloude.uts.core.queue.domain.JobPo;


/**
 * 更新周期任务队列的最后执行时间
 * @author Uway-M3
 *
 */
public interface SchedulerJobQueue extends JobQueue {
	 /**
     * 更新 lastGenerateTriggerTime
     */
    boolean updateLastGenerateTriggerTime(String jobId, Long lastGenerateTriggerTime);

    List<JobPo> getNeedGenerateJobPos(Long checkTime, int topSize);
}
