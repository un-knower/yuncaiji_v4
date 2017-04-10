package cn.uway.ucloude.uts.jobtracker.support.policy;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.uts.core.queue.JobFeedbackQueue;
import cn.uway.ucloude.uts.core.queue.domain.JobFeedbackPo;
import cn.uway.ucloude.uts.jobtracker.support.OldDataHandler;

public class OldDataDeletePolicy implements OldDataHandler {

	private long expired = 30 * 24 * 60 * 60 * 1000L; // 默认30 天

	public OldDataDeletePolicy() {
	}

	public OldDataDeletePolicy(long expired) {
		this.expired = expired;
	}

	@Override
	public boolean handle(JobFeedbackQueue jobFeedbackQueue, JobFeedbackPo jobFeedbackPo, JobFeedbackPo po) {
		// TODO Auto-generated method stub
		if (SystemClock.now() - jobFeedbackPo.getGmtCreated() > expired) {
			// delete
			jobFeedbackQueue.remove(po.getJobRunResult().getJobMeta().getJob().getTaskTrackerNodeGroup(), po.getId());
			return true;
		}

		return false;
	}

}
