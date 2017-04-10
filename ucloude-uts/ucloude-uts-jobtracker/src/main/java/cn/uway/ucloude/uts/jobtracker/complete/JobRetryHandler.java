package cn.uway.ucloude.uts.jobtracker.complete;

import java.util.Date;
import java.util.List;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.data.dataaccess.exception.DupEntryException;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.domain.JobMeta;
import cn.uway.ucloude.uts.core.domain.JobRunResult;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.support.CronExpressionUtils;
import cn.uway.ucloude.uts.core.support.JobUtils;
import cn.uway.ucloude.uts.jobtracker.complete.retry.DefaultJobRetryTimeGenerator;
import cn.uway.ucloude.uts.jobtracker.complete.retry.JobRetryTimeGenerator;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

public class JobRetryHandler {
    private static final ILogger LOGGER = LoggerManager.getLogger(JobRetryHandler.class);
    
    private JobTrackerContext context;
    
    public JobRetryHandler(JobTrackerContext context){
    	this.context = context;
    	 this.retryInterval = context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_JOB_RETRY_INTERVAL_MILLIS, 30 * 1000);
         this.jobRetryTimeGenerator = ServiceFactory.load(JobRetryTimeGenerator.class, context.getConfiguration());
    }
    
    private int retryInterval = 30 * 1000;     // 默认30s
    private JobRetryTimeGenerator jobRetryTimeGenerator;

   

    public void onComplete(List<JobRunResult> results) {

        if (CollectionUtil.isEmpty(results)) {
            return;
        }
        for (JobRunResult result : results) {

            JobMeta jobMeta = result.getJobMeta();
            // 1. 加入到重试队列
            JobPo jobPo = context.getExecutingJobQueue().getJob(jobMeta.getJobId());
            if (jobPo == null) {    // 表示已经被删除了
                continue;
            }

            Job job = jobMeta.getJob();
            if (!(jobRetryTimeGenerator instanceof DefaultJobRetryTimeGenerator)) {
                job = JobUtils.copy(jobMeta.getJob());
                job.setTaskId(jobMeta.getRealTaskId());     // 这个对于用户需要转换为用户提交的taskId
            }
            // 得到下次重试时间
            Long nextRetryTriggerTime = jobRetryTimeGenerator.getNextRetryTriggerTime(job, jobPo.getRetryTimes(), retryInterval);
            // 重试次数+1
            jobPo.setRetryTimes((jobPo.getRetryTimes() == null ? 0 : jobPo.getRetryTimes()) + 1);

            if (jobPo.isCron()) {
                // 如果是 cron Job, 判断任务下一次执行时间和重试时间的比较
                JobPo cronJobPo = context.getCronJobQueue().getJob(jobMeta.getJobId());
                if (cronJobPo != null) {
                    Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(cronJobPo.getCronExpression());
                    if (nextTriggerTime != null && nextTriggerTime.getTime() < nextRetryTriggerTime) {
                        // 表示下次还要执行, 并且下次执行时间比下次重试时间要早, 那么不重试，直接使用下次的执行时间
                        nextRetryTriggerTime = nextTriggerTime.getTime();
                        jobPo = cronJobPo;
                    } else {
                        jobPo.setInternalExtParam(ExtConfigKeys.IS_RETRY_JOB, Boolean.TRUE.toString());
                    }
                }
            } else if (jobPo.isRepeatable()) {
                JobPo repeatJobPo = context.getRepeatJobQueue().getJob(jobMeta.getJobId());
                if (repeatJobPo != null) {
                    // 比较下一次重复时间和重试时间
                    if (repeatJobPo.getRepeatCount() == -1 || (repeatJobPo.getRepeatedCount() < repeatJobPo.getRepeatCount())) {
                        long nexTriggerTime = JobUtils.getRepeatNextTriggerTime(jobPo);
                        if (nexTriggerTime < nextRetryTriggerTime) {
                            // 表示下次还要执行, 并且下次执行时间比下次重试时间要早, 那么不重试，直接使用下次的执行时间
                            nextRetryTriggerTime = nexTriggerTime;
                            jobPo = repeatJobPo;
                        } else {
                            jobPo.setInternalExtParam(ExtConfigKeys.IS_RETRY_JOB, Boolean.TRUE.toString());
                        }
                    }
                }
            } else {
                jobPo.setInternalExtParam(ExtConfigKeys.IS_RETRY_JOB, Boolean.TRUE.toString());
            }

            // 加入到队列, 重试
            jobPo.setTaskTrackerIdentity(null);
            jobPo.setIsRunning(false);
            jobPo.setGmtModified(SystemClock.now());
            // 延迟重试时间就等于重试次数(分钟)
            jobPo.setTriggerTime(nextRetryTriggerTime);
            try {
                context.getExecutableJobQueue().add(jobPo);
            } catch (DupEntryException e) {
                LOGGER.warn("ExecutableJobQueue already exist:" + JsonConvert.serialize(jobPo));
            }
            // 从正在执行的队列中移除
            context.getExecutingJobQueue().remove(jobPo.getJobId());
        }
    }
    
}
