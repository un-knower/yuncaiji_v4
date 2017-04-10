package cn.uway.ucloude.uts.core.queue.implement;

import cn.uway.ucloude.ec.IEventCenter;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.queue.CronJobQueue;
import cn.uway.ucloude.uts.core.queue.ExecutableJobQueue;
import cn.uway.ucloude.uts.core.queue.ExecutingJobQueue;
import cn.uway.ucloude.uts.core.queue.JobFeedbackQueue;
import cn.uway.ucloude.uts.core.queue.JobQueueFactory;
import cn.uway.ucloude.uts.core.queue.NodeGroupStore;
import cn.uway.ucloude.uts.core.queue.PreLoader;
import cn.uway.ucloude.uts.core.queue.RepeatJobQueue;
import cn.uway.ucloude.uts.core.queue.SuspendJobQueue;

public class DbJobQueueFactory  implements JobQueueFactory {

	@Override
	public CronJobQueue getCronJobQueue() {
		// TODO Auto-generated method stub
		return new DbCronJobQueue(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public RepeatJobQueue getRepeatJobQueue() {
		return new DbRepeatJobQueue(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public ExecutableJobQueue getExecutableJobQueue() {
		return new DbExecutableJobQueue(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public ExecutingJobQueue getExecutingJobQueue() {
		return new DbExecutingJobQueue(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public JobFeedbackQueue getJobFeedbackQueue() {
		return new DbJobFeedbackQueue(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public NodeGroupStore getNodeGroupStore() {
		return new DbNodeGroupStore(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public SuspendJobQueue getSuspendJobQueue() {
		return new DbSuspendJobQueue(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public PreLoader getPreLoader(int loadSize, double factor, long interval, String identity,
			IEventCenter eventcenter) {
		// TODO Auto-generated method stub
		return new DbPreLoader(ExtConfigKeys.CONNECTION_KEY, loadSize, factor, interval, identity, eventcenter);
	}

}
