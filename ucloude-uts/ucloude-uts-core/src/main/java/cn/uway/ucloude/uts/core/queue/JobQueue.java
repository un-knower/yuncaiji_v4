package cn.uway.ucloude.uts.core.queue;

import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.domain.JobQueueReq;

/**
 * 任务队列基类
 * 
 * @author Uway-M3
 *
 */
public interface JobQueue {
	Pagination<JobPo> pageSelect(JobQueueReq request);

	boolean selectiveUpdateByJobId(JobQueueReq request);

	boolean selectiveUpdateByTaskId(JobQueueReq request);

	JobPo getJob(String jobId);
}
