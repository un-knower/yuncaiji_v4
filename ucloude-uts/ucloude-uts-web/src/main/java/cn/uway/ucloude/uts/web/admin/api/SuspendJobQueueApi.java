package cn.uway.ucloude.uts.web.admin.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.data.dataaccess.exception.DupEntryException;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.utils.Assert;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.biz.logger.JobLogUtils;
import cn.uway.ucloude.uts.biz.logger.domain.LogType;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.domain.JobQueueReq;
import cn.uway.ucloude.uts.core.support.CronExpression;
import cn.uway.ucloude.uts.core.support.CronExpressionUtils;
import cn.uway.ucloude.uts.core.support.JobUtils;
import cn.uway.ucloude.uts.web.admin.AbstractMVC;
import cn.uway.ucloude.uts.web.admin.support.Builder;
import cn.uway.ucloude.uts.web.admin.vo.RestfulResponse;
import cn.uway.ucloude.uts.web.cluster.BackendAppContext;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author magic.s.g.xie
 */
@RestController
public class SuspendJobQueueApi extends AbstractMVC {

    private static final ILogger LOGGER = LoggerManager.getLogger(SuspendJobQueueApi.class);
    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("/job-queue/suspend-job-get")
    public @ResponseBody Pagination<JobPo> suspendJobGet(JobQueueReq request) {
        Pagination<JobPo> paginationRsp = appContext.getSuspendJobQueue().pageSelect(request);
        
        return paginationRsp;
    }
    
    @RequestMapping("/job-queue/suspend-job-getById")
	public RestfulResponse suspendJobGetById(String jobId) {
		Assert.hasLength(jobId, "jobId不能为空!");
		JobPo jobInfo = appContext.getSuspendJobQueue().getJob(jobId);
		RestfulResponse result = new RestfulResponse();
		if (jobInfo != null) {
			result.setSuccess(true);
			result.setMsg("获取成功");
			List<JobPo> rows = new ArrayList<JobPo>();
			rows.add(jobInfo);
			result.setRows(rows);
		} else {
			result.setSuccess(false);
			result.setMsg("无效的JobId");
		}
		return result;
	}

    @RequestMapping("/job-queue/suspend-job-update")
    public RestfulResponse suspendJobUpdate(String jobType, JobQueueReq request) {
        // 检查参数
        try {
            Assert.hasLength(request.getJobId(), "jobId不能为空!");
            Assert.hasLength(jobType, "jobType不能为空!");
        } catch (IllegalArgumentException e) {
            return Builder.build(false, e.getMessage());
        }
        try {

            JobPo jobPo = appContext.getSuspendJobQueue().getJob(request.getJobId());

            if ("CRON".equals(jobType)) {
                // 检查参数
                try {
                    Assert.hasLength(request.getCronExpression(), "cronExpression不能为空!");
                } catch (IllegalArgumentException e) {
                    return Builder.build(false, e.getMessage());
                }
                // 1. 检测 cronExpression是否是正确的
                CronExpression expression = new CronExpression(request.getCronExpression());
                if (expression.getTimeAfter(new Date()) == null) {
                    return Builder.build(false, StringUtil.format("该CronExpression={} 已经没有执行时间点! 请重新设置或者直接删除。", request.getCronExpression()));
                }
                // 看CronExpression是否有修改,如果有修改,需要更新triggerTime
                if (!request.getCronExpression().equals(jobPo.getCronExpression())) {
                    request.setTriggerTime(expression.getTimeAfter(new Date()));
                }
            } else {
                try {
                    Assert.notNull(request.getRepeatInterval(), "repeatInterval不能为空!");
                    Assert.isTrue(request.getRepeatInterval() > 0, "repeatInterval必须大于0");
                    Assert.isTrue(request.getRepeatCount() >= -1, "repeatCount必须>= -1");
                } catch (IllegalArgumentException e) {
                    return Builder.build(false, e.getMessage());
                }
                // 如果repeatInterval有修改,需要把triggerTime也要修改下
                if (!request.getRepeatInterval().equals(jobPo.getRepeatInterval())) {
                    long nextTriggerTime = JobUtils.getRepeatNextTriggerTime(jobPo);
                    request.setTriggerTime(new Date(nextTriggerTime));
                }
                request.setCronExpression(null);
            }

            boolean success = appContext.getSuspendJobQueue().selectiveUpdateByJobId(request);
            if (success) {
                JobLogUtils.log(LogType.UPDATE, jobPo, appContext.getJobLogger());
                return Builder.build(true);
            } else {
                return Builder.build(false, "该任务已经被删除或者执行完成");
            }
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "请输入正确的 CronExpression");
        }
    }


    @RequestMapping("/job-queue/suspend-job-delete")
    public RestfulResponse suspendJobDelete(JobQueueReq request) {
        if (StringUtil.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }
        boolean success = appContext.getSuspendJobQueue().remove(request.getJobId());
        if (success) {
            JobPo jobPo = appContext.getSuspendJobQueue().getJob(request.getJobId());
            JobLogUtils.log(LogType.DEL, jobPo, appContext.getJobLogger());
        }
        return Builder.build(success);
    }


    @RequestMapping("/job-queue/suspend-job-recovery")
    public RestfulResponse suspendJobRecovery(JobQueueReq request) {
        if (StringUtil.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }

        JobPo jobPo = appContext.getSuspendJobQueue().getJob(request.getJobId());
        if (jobPo == null) {
            return Builder.build(false, "任务不存在，或者已经删除");
        }

        // 判断是Cron任务还是Repeat任务
        if (jobPo.isCron()) {
            // 先恢复,才能删除
            Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCronExpression());
            if (nextTriggerTime != null) {
                jobPo.setGmtModified(SystemClock.now());
                try {
                    // 1.add to cron job queue
                    appContext.getCronJobQueue().add(jobPo);
                } catch (DupEntryException e) {
                    LOGGER.error(e.getMessage(), e);
                    return Builder.build(false, "Cron队列中任务已经存在，请检查");
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    return Builder.build(false, "插入Cron队列中任务错误, error:" + e.getMessage());
                }

                if (jobPo.getRelyOnPrevCycle()) {
                    try {
                        // 2. add to executable queue
                        jobPo.setTriggerTime(nextTriggerTime.getTime());
                        jobPo.setInternalExtParam(ExtConfigKeys.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
                        appContext.getExecutableJobQueue().add(jobPo);
                    } catch (DupEntryException e) {
                        LOGGER.error(e.getMessage(), e);
                        return Builder.build(false, "等待执行队列中任务已经存在，请检查");
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        return Builder.build(false, "插入等待执行队列中任务错误, error:" + e.getMessage());
                    }
                } else {
                    // 不依赖上一周期的
//                    Long lastGenerateTriggerTime = jobPo.getLastGenerateTriggerTime();
//                    if (lastGenerateTriggerTime == null || lastGenerateTriggerTime == 0) {
//                        lastGenerateTriggerTime = SystemClock.now();
//                    }
                    appContext.getNoRelyJobGenerator().generateCronJobForInterval(jobPo, new Date(SystemClock.now()));
                }

            } else {
                return Builder.build(false, "该任务已经无效, 或者已经没有下一轮执行时间点, 请直接删除");
            }
        } else if (jobPo.isRepeatable()) {
            // 先恢复,才能删除
            if (jobPo.getRepeatCount() == -1 || jobPo.getRepeatedCount() < jobPo.getRepeatCount()) {
                jobPo.setGmtModified(SystemClock.now());
                try {
                    // 1.add to cron job queue
                    appContext.getRepeatJobQueue().add(jobPo);
                } catch (DupEntryException e) {
                    LOGGER.error(e.getMessage(), e);
                    return Builder.build(false, "Repeat队列中任务已经存在，请检查");
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    return Builder.build(false, "插入Repeat队列中任务错误, error:" + e.getMessage());
                }

                if (jobPo.getRelyOnPrevCycle()) {
                    try {
                        // 2. add to executable queue
                        JobPo repeatJob = appContext.getRepeatJobQueue().getJob(request.getJobId());
                        long nextTriggerTime = JobUtils.getRepeatNextTriggerTime(repeatJob);
                        jobPo.setTriggerTime(nextTriggerTime);
                        jobPo.setInternalExtParam(ExtConfigKeys.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
                        appContext.getExecutableJobQueue().add(jobPo);
                    } catch (DupEntryException e) {
                        LOGGER.error(e.getMessage(), e);
                        return Builder.build(false, "等待执行队列中任务已经存在，请检查");
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        return Builder.build(false, "插入等待执行队列中任务错误, error:" + e.getMessage());
                    }
                } else {
                    // 不依赖上一周期的
//                    Long lastGenerateTriggerTime = jobPo.getLastGenerateTriggerTime();
//                    if (lastGenerateTriggerTime == null || lastGenerateTriggerTime == 0) {
//                        lastGenerateTriggerTime = SystemClock.now();
//                    }
                    appContext.getNoRelyJobGenerator().generateRepeatJobForInterval(jobPo, new Date(SystemClock.now()));
                }
            } else {
                return Builder.build(false, "该任务已经无效, 或者已经没有下一轮执行时间点, 请直接删除");
            }
        }

        // 从暂停表中移除
        if (!appContext.getSuspendJobQueue().remove(request.getJobId())) {
            return Builder.build(false, "恢复暂停任务失败，请重试");
        }

        JobLogUtils.log(LogType.RESUME, jobPo, appContext.getJobLogger());
        return Builder.build(true);
    }
}
