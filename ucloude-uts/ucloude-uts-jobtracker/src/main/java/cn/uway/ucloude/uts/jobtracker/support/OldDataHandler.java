package cn.uway.ucloude.uts.jobtracker.support;

import cn.uway.ucloude.uts.core.queue.JobFeedbackQueue;
import cn.uway.ucloude.uts.core.queue.domain.JobFeedbackPo;

/**
 * 老数据处理handler（像那种JobClient）
 * @author uway
 *
 */
public interface OldDataHandler {
	boolean handle(JobFeedbackQueue jobFeedbackQueue, JobFeedbackPo jobFeedbackPo, JobFeedbackPo po);
}
