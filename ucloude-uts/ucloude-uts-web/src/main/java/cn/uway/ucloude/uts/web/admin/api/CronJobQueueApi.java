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
public class CronJobQueueApi extends AbstractMVC {

    private static final ILogger LOGGER = LoggerManager.getLogger(CronJobQueueApi.class);
    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("/job-queue/cron-job-get")
    public @ResponseBody Pagination<JobPo> cronJobGet(JobQueueReq request) {
        Pagination<JobPo> paginationRsp = appContext.getCronJobQueue().pageSelect(request);
       
        return paginationRsp;
    }
    
    @RequestMapping("/job-queue/cron-job-getById")
	public RestfulResponse cronJobGetById(String jobId) {
		Assert.hasLength(jobId, "jobId不能为空!");
		JobPo jobInfo = appContext.getCronJobQueue().getJob(jobId);
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

    @RequestMapping("/job-queue/cron-job-update")
    public RestfulResponse cronJobUpdate(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();
        // 检查参数
        try {
            Assert.hasLength(request.getJobId(), "jobId不能为空!");
            Assert.hasLength(request.getCronExpression(), "cronExpression不能为空!");
        } catch (IllegalArgumentException e) {
            return Builder.build(false, e.getMessage());
        }
        try {
            // 1. 检测 cronExpression是否是正确的
            CronExpression expression = new CronExpression(request.getCronExpression());
            Date nextTriggerTime = expression.getTimeAfter(new Date());
            if (nextTriggerTime == null) {
                return Builder.build(false, StringUtil.format("该CronExpression={} 已经没有执行时间点! 请重新设置或者直接删除。", request.getCronExpression()));
            }
            JobPo oldJobPo = appContext.getCronJobQueue().getJob(request.getJobId());
            boolean success = appContext.getCronJobQueue().selectiveUpdateByJobId(request);
            if (success) {
                JobPo newJobPo = appContext.getCronJobQueue().getJob(request.getJobId());
                try {
                    // 判断是否有relyOnPrevCycle变更
                    boolean relyOnPrevCycleChanged = !newJobPo.getRelyOnPrevCycle().equals(oldJobPo.getRelyOnPrevCycle());
                    boolean cronExpressionChanged = !newJobPo.getCronExpression().equals(oldJobPo.getCronExpression());

                    // 1. 修改前relyOnPrevCycle=true,并且修改后也是true
                    if (oldJobPo.getRelyOnPrevCycle() && !relyOnPrevCycleChanged) {
                        // 看CronExpression是否有修改,如果有修改,需要更新triggerTime
                        if (cronExpressionChanged) {
                            request.setTriggerTime(nextTriggerTime);
                        }
                        appContext.getExecutableJobQueue().selectiveUpdateByJobId(request);
                    } else {
                        // 2. 需要对批量任务做处理
                        if (relyOnPrevCycleChanged) {
                            // 如果relyOnPrevCycle 修改过
                            if (oldJobPo.getRelyOnPrevCycle()) {
                                // 之前是依赖的,现在不依赖,需要生成批量任务
                                appContext.getExecutableJobQueue().remove(oldJobPo.getTaskTrackerNodeGroup(), oldJobPo.getJobId());
                                appContext.getNoRelyJobGenerator().generateCronJobForInterval(newJobPo, new Date());
                            } else {
                                // 之前不依赖,现在依赖,需要删除批量任务
                                appContext.getExecutableJobQueue().removeBatch(oldJobPo.getRealTaskId(), oldJobPo.getTaskTrackerNodeGroup());
                                // 添加新的任务
                                newJobPo.setTriggerTime(nextTriggerTime.getTime());
                                try {
                                    newJobPo.setInternalExtParam(ExtConfigKeys.EXE_SEQ_ID, JobUtils.generateExeSeqId(newJobPo));
                                    appContext.getExecutableJobQueue().add(newJobPo);
                                } catch (DupEntryException ignored) {
                                }
                            }
                        } else {
                            // 如果relyOnPrevCycle 没有修改过, 表示relyOnPrevCycle=false, 那么要看cronExpression是否修改过,如果修改过,需要删除重新生成
                            if (cronExpressionChanged) {
                                appContext.getExecutableJobQueue().removeBatch(oldJobPo.getRealTaskId(), oldJobPo.getTaskTrackerNodeGroup());
                                appContext.getNoRelyJobGenerator().generateCronJobForInterval(newJobPo, new Date());
                            } else {
                                appContext.getExecutableJobQueue().selectiveUpdateByTaskId(request);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    return Builder.build(false, "更新等待执行的任务失败，请手动更新! error:" + e.getMessage());
                }
                response.setSuccess(true);
            } else {
                return Builder.build(false, "该任务已经被删除或者执行完成");
            }
            JobLogUtils.log(LogType.UPDATE, oldJobPo, appContext.getJobLogger());
            return response;
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "请输入正确的 CronExpression!" + e.getMessage());
        }
    }

    @RequestMapping("/job-queue/cron-job-delete")
    public RestfulResponse cronJobDelete(JobQueueReq request) {
        if (StringUtil.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }
        JobPo jobPo = appContext.getCronJobQueue().getJob(request.getJobId());
        if (jobPo == null) {
            return Builder.build(true, "已经删除");
        }
        boolean success = appContext.getCronJobQueue().remove(request.getJobId());
        if (success) {
            try {
                appContext.getExecutableJobQueue().removeBatch(jobPo.getRealTaskId(), jobPo.getTaskTrackerNodeGroup());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return Builder.build(false, "删除等待执行的任务失败，请手动删除! error:{}" + e.getMessage());
            }
        }
        JobLogUtils.log(LogType.DEL, jobPo, appContext.getJobLogger());

        return Builder.build(true, "ok");
    }

    @RequestMapping("/job-queue/cron-job-suspend")
    public RestfulResponse cronJobSuspend(JobQueueReq request) {
        if (StringUtil.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }
        JobPo jobPo = appContext.getCronJobQueue().getJob(request.getJobId());
        if (jobPo == null) {
            return Builder.build(false, "任务不存在，或者已经删除");
        }
        try {
            jobPo.setGmtModified(SystemClock.now());
            appContext.getSuspendJobQueue().add(jobPo);
        } catch (DupEntryException e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "该任务已经被暂停, 请检查暂停队列");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "移动任务到暂停队列失败, error:" + e.getMessage());
        }
        try {
            appContext.getCronJobQueue().remove(request.getJobId());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "删除Cron任务失败，请手动删除! error:" + e.getMessage());
        }
        try {
            if (!jobPo.getRelyOnPrevCycle()) {
                appContext.getCronJobQueue().updateLastGenerateTriggerTime(jobPo.getJobId(), new Date().getTime());
                appContext.getExecutableJobQueue().removeBatch(jobPo.getRealTaskId(), jobPo.getTaskTrackerNodeGroup());
            } else {
                appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(), request.getJobId());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "删除等待执行的任务失败，请手动删除! error:" + e.getMessage());
        }

        // 记录日志
        JobLogUtils.log(LogType.SUSPEND, jobPo, appContext.getJobLogger());

        return Builder.build(true, "ok");
    }
}
