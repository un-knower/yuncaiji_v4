package cn.uway.ucloude.uts.jobtracker.complete;

import java.util.Date;
import java.util.List;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.data.dataaccess.exception.DupEntryException;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.LogType;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.domain.JobMeta;
import cn.uway.ucloude.uts.core.domain.JobRunResult;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.support.CronExpressionUtils;
import cn.uway.ucloude.uts.core.support.JobDomainConverter;
import cn.uway.ucloude.uts.core.support.JobUtils;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

public class JobFinishHandler {
    private static final ILogger LOGGER = LoggerManager.getLogger(JobFinishHandler.class);
    
    private JobTrackerContext context;
    
    public JobFinishHandler(JobTrackerContext context){
    	this.context = context;
    }
    
    
    public void onComplete(List<JobRunResult> results){
    	if(CollectionUtil.isEmpty(results)){
    		return;
    	}
    	
    	for(JobRunResult result:results){
    		JobMeta jobMeta = result.getJobMeta();
    		// 当前完成的job是否是重试的
    		boolean isRetryForThisTime = Boolean.TRUE.toString().equals(jobMeta.getInternalExtParam(ExtConfigKeys.IS_RETRY_JOB));
    		boolean isOnce = Boolean.TRUE.toString().equals(jobMeta.getInternalExtParam(ExtConfigKeys.ONCE));
    		if(jobMeta.getJob().isCron()){// 是 Cron任务
    			
    			if(isOnce == true){
    				finishNoReplyPrevCronJob(jobMeta);
    			}
    			else{
    				finishCronJob(jobMeta.getJobId());
    			}
    		} else if (jobMeta.getJob().isRepeatable()) {
                if (isOnce) {
                    finishNoReplyPrevRepeatJob(jobMeta, isRetryForThisTime);
                } else {
                    finishRepeatJob(jobMeta.getJobId(), isRetryForThisTime);
                }
            }
    		// 从正在执行的队列中移除
    		context.getExecutingJobQueue().remove(jobMeta.getJobId());
    	}
    	
    	
    }
    
    private void finishCronJob(String jobId) {
        JobPo jobPo = context.getCronJobQueue().getJob(jobId);
        if (jobPo == null) {
            // 可能任务队列中改条记录被删除了
            return;
        }
        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCronExpression());
        if (nextTriggerTime == null) {
            // 从CronJob队列中移除
            context.getCronJobQueue().remove(jobId);
            jobRemoveLog(jobPo, "Cron");
            return;
        }
        // 表示下次还要执行
        try {
            jobPo.setTaskTrackerIdentity(null);
            jobPo.setIsRunning(false);
            jobPo.setTriggerTime(nextTriggerTime.getTime());
            jobPo.setGmtModified(SystemClock.now());
            jobPo.setInternalExtParam(ExtConfigKeys.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
            context.getExecutableJobQueue().add(jobPo);
        } catch (DupEntryException e) {
            LOGGER.warn("ExecutableJobQueue already exist:" + JsonConvert.serialize(jobPo));
        }
    }

    private void finishNoReplyPrevCronJob(JobMeta jobMeta) {
        JobPo jobPo = context.getCronJobQueue().getJob(jobMeta.getJob().getTaskTrackerNodeGroup(), jobMeta.getRealTaskId());
        if (jobPo == null) {
            // 可能任务队列中改条记录被删除了
            return;
        }
        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCronExpression());
        if (nextTriggerTime == null) {
            // 检查可执行队列中是否还有
            if (context.getExecutableJobQueue().countJob(jobPo.getRealTaskId(), jobPo.getTaskTrackerNodeGroup()) == 0) {
                // TODO 检查执行中队列是否还有
                // 从CronJob队列中移除
                context.getCronJobQueue().remove(jobPo.getJobId());
                jobRemoveLog(jobPo, "Cron");
            }
        }
    }

    private void finishNoReplyPrevRepeatJob(JobMeta jobMeta, boolean isRetryForThisTime) {
        JobPo jobPo = context.getRepeatJobQueue().getJob(jobMeta.getJob().getTaskTrackerNodeGroup(), jobMeta.getRealTaskId());
        if (jobPo == null) {
            // 可能任务队列中改条记录被删除了
            return;
        }
        if (jobPo.getRepeatCount() != -1 && jobPo.getRepeatedCount() >= jobPo.getRepeatCount()) {
            // 已经重试完成, 那么删除, 这里可以不用check可执行队列是否还有,因为这里依赖的是计数
            context.getRepeatJobQueue().remove(jobPo.getJobId());
            jobRemoveLog(jobPo, "Repeat");
            return;
        }

        // 如果当前完成的job是重试的,那么不要增加repeatedCount
        if (!isRetryForThisTime) {
            // 更新repeatJob的重复次数
            context.getRepeatJobQueue().incRepeatedCount(jobPo.getJobId());
        }
    }

    private void finishRepeatJob(String jobId, boolean isRetryForThisTime) {
        JobPo jobPo = context.getRepeatJobQueue().getJob(jobId);
        if (jobPo == null) {
            // 可能任务队列中改条记录被删除了
            return;
        }
        if (jobPo.getRepeatCount() != -1 && jobPo.getRepeatedCount() >= jobPo.getRepeatCount()) {
            // 已经重试完成, 那么删除
            context.getRepeatJobQueue().remove(jobId);
            jobRemoveLog(jobPo, "Repeat");
            return;
        }

        int repeatedCount = jobPo.getRepeatedCount();
        // 如果当前完成的job是重试的,那么不要增加repeatedCount
        if (!isRetryForThisTime) {
            // 更新repeatJob的重复次数
            repeatedCount = context.getRepeatJobQueue().incRepeatedCount(jobId);
        }
        if (repeatedCount == -1) {
            // 表示任务已经被删除了
            return;
        }
        long nexTriggerTime = JobUtils.getRepeatNextTriggerTime(jobPo);
        try {
            jobPo.setRepeatedCount(repeatedCount);
            jobPo.setTaskTrackerIdentity(null);
            jobPo.setIsRunning(false);
            jobPo.setTriggerTime(nexTriggerTime);
            jobPo.setGmtModified(SystemClock.now());
            jobPo.setInternalExtParam(ExtConfigKeys.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
            context.getExecutableJobQueue().add(jobPo);
        } catch (DupEntryException e) {
            LOGGER.warn("ExecutableJobQueue already exist:" + JsonConvert.serialize(jobPo));
        }
    }

    private void jobRemoveLog(JobPo jobPo, String type) {
        JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
        jobLogPo.setSuccess(true);
        jobLogPo.setLogType(LogType.DEL);
        jobLogPo.setLogTime(SystemClock.now());
        jobLogPo.setLevel(Level.INFO);
        jobLogPo.setMsg(type + " Job Finished");
        context.getJobLogger().log(jobLogPo);
    }
    
    
}
