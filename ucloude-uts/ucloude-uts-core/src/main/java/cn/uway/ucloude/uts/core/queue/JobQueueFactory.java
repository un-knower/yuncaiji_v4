package cn.uway.ucloude.uts.core.queue;

import cn.uway.ucloude.container.SPI;
import cn.uway.ucloude.ec.IEventCenter;
import cn.uway.ucloude.uts.core.ExtConfigKeys;

@SPI(key = ExtConfigKeys.JOB_QUEUE, dftValue = "DataBase")
public interface JobQueueFactory {

    CronJobQueue getCronJobQueue();

    RepeatJobQueue getRepeatJobQueue();

    ExecutableJobQueue getExecutableJobQueue();

    ExecutingJobQueue getExecutingJobQueue();

    JobFeedbackQueue getJobFeedbackQueue();

    NodeGroupStore getNodeGroupStore();

    SuspendJobQueue getSuspendJobQueue();

    PreLoader getPreLoader(int loadSize, double factor, long interval, String identity, IEventCenter eventCenter);
}

