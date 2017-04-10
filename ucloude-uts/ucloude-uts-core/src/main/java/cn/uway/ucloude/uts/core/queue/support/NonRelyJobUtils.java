package cn.uway.ucloude.uts.core.queue.support;

import java.util.Date;

import cn.uway.ucloude.data.dataaccess.exception.DupEntryException;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.DateUtil;
import cn.uway.ucloude.utils.DateUtil.TimePattern;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.queue.CronJobQueue;
import cn.uway.ucloude.uts.core.queue.ExecutableJobQueue;
import cn.uway.ucloude.uts.core.queue.RepeatJobQueue;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.support.CronExpressionUtils;
import cn.uway.ucloude.uts.core.support.JobUtils;

public class NonRelyJobUtils {
	private static final ILogger LOGGER = LoggerManager.getLogger(NonRelyJobUtils.class);

    /**
     * 生成一个小时的任务
     */
    public static void addCronJobForInterval(ExecutableJobQueue executableJobQueue,
                                             CronJobQueue cronJobQueue,
                                             int scheduleIntervalMinute,
                                             final JobPo finalJobPo,
                                             Date lastGenerateTime) {
        JobPo jobPo = JobUtils.copy(finalJobPo);

        String cronExpression = jobPo.getCronExpression();
        long endTime = DateUtil.addMinutes(lastGenerateTime, scheduleIntervalMinute).getTime();
        Date timeAfter = lastGenerateTime;
        boolean stop = false;
        while (!stop) {
            Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(cronExpression, timeAfter);
            if (nextTriggerTime == null) {
                stop = true;
            } else {
                if (nextTriggerTime.getTime() <= endTime) {
                    // 添加任务
                    jobPo.setTriggerTime(nextTriggerTime.getTime());
                    jobPo.setJobId(JobUtils.generateJobId());
                    
                    jobPo.setTaskId(finalJobPo.getTaskId() + "_" + DateUtil.formatNonException(nextTriggerTime,TimePattern.yyyyMMdd_HHmmss));
                    jobPo.setInternalExtParam(ExtConfigKeys.ONCE, Boolean.TRUE.toString());
                    try {
                        jobPo.setInternalExtParam(ExtConfigKeys.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
                        executableJobQueue.add(jobPo);
                    } catch (DupEntryException e) {
                        LOGGER.warn("Cron Job[taskId={}, taskTrackerNodeGroup={}] Already Exist in ExecutableJobQueue",
                                jobPo.getTaskId(), jobPo.getTaskTrackerNodeGroup());
                    }
                } else {
                    stop = true;
                }
            }
            timeAfter = nextTriggerTime;
        }
        cronJobQueue.updateLastGenerateTriggerTime(finalJobPo.getJobId(), endTime);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Add CronJob {} to {}", jobPo, DateUtil.formatNonException(new Date(endTime), TimePattern.yyyyMMdd_HHmmss));
        }
    }

    public static void addRepeatJobForInterval(
            ExecutableJobQueue executableJobQueue,
            RepeatJobQueue repeatJobQueue,
            int scheduleIntervalMinute, final JobPo finalJobPo, Date lastGenerateTime) {
        JobPo jobPo = JobUtils.copy(finalJobPo);
        long firstTriggerTime = Long.valueOf(jobPo.getInternalExtParam(ExtConfigKeys.FIRST_FIRE_TIME));

        Long repeatInterval = jobPo.getRepeatInterval();
        Integer repeatCount = jobPo.getRepeatCount();

        long endTime = DateUtil.addMinutes(lastGenerateTime, scheduleIntervalMinute).getTime();
        if (endTime <= firstTriggerTime) {
            return;
        }
        // 计算出应该重复的次数
        int repeatedCount = Long.valueOf((lastGenerateTime.getTime() - firstTriggerTime) / jobPo.getRepeatInterval()).intValue();

        boolean stop = false;
        while (!stop) {
            Long nextTriggerTime = firstTriggerTime + repeatedCount * repeatInterval;

            if (nextTriggerTime <= endTime &&
                    (repeatCount == -1 || repeatedCount <= repeatCount)) {
                // 添加任务
                jobPo.setTriggerTime(nextTriggerTime);
                jobPo.setJobId(JobUtils.generateJobId());
                jobPo.setTaskId(finalJobPo.getTaskId() + "_" + DateUtil.formatNonException(new Date(nextTriggerTime), TimePattern.yyyyMMdd_HHmmss));
                jobPo.setRepeatedCount(repeatedCount);
                jobPo.setInternalExtParam(ExtConfigKeys.ONCE, Boolean.TRUE.toString());
                try {
                    jobPo.setInternalExtParam(ExtConfigKeys.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
                    executableJobQueue.add(jobPo);
                } catch (DupEntryException e) {
                    LOGGER.warn("Repeat Job[taskId={}, taskTrackerNodeGroup={}] Already Exist in ExecutableJobQueue",
                            jobPo.getTaskId(), jobPo.getTaskTrackerNodeGroup());
                }
                repeatedCount++;
            } else {
                stop = true;
            }
        }
        // 更新时间
        repeatJobQueue.updateLastGenerateTriggerTime(finalJobPo.getJobId(), endTime);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Add RepeatJob {} to {}", jobPo, DateUtil.formatNonException(new Date(endTime),TimePattern.yyyyMMdd_HHmmss));
        }
    }
}
