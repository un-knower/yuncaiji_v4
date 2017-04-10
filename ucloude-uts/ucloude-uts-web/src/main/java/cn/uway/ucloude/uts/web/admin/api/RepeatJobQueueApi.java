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
import cn.uway.ucloude.uts.core.support.JobUtils;
import cn.uway.ucloude.uts.web.admin.AbstractMVC;
import cn.uway.ucloude.uts.web.admin.support.Builder;
import cn.uway.ucloude.uts.web.admin.vo.RestfulResponse;
import cn.uway.ucloude.uts.web.cluster.BackendAppContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author magic.s.g.xie
 */
@RestController
public class RepeatJobQueueApi extends AbstractMVC {

    private static final ILogger LOGGER = LoggerManager.getLogger(RepeatJobQueueApi.class);
    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("/job-queue/repeat-job-get")
    public @ResponseBody Pagination<JobPo> repeatJobGet(JobQueueReq request) {
        Pagination<JobPo> paginationRsp = appContext.getRepeatJobQueue().pageSelect(request);
       
        return paginationRsp;
    }
    
    @RequestMapping("/job-queue/repeat-job-getById")
	public RestfulResponse repeatJobGetById(String jobId) {
		Assert.hasLength(jobId, "jobId不能为空!");
		JobPo jobInfo = appContext.getRepeatJobQueue().getJob(jobId);
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

    @RequestMapping("/job-queue/repeat-job-update")
    public RestfulResponse repeatJobUpdate(JobQueueReq request) {
        // 检查参数
        try {
            Assert.hasLength(request.getJobId(), "jobId不能为空!");
            Assert.notNull(request.getRepeatInterval(), "repeatInterval不能为空!");
            Assert.isTrue(request.getRepeatInterval() > 0, "repeatInterval必须大于0");
            Assert.isTrue(request.getRepeatCount() >= -1, "repeatCount必须>= -1");
        } catch (IllegalArgumentException e) {
            return Builder.build(false, e.getMessage());
        }
        request.setCronExpression(null);
        JobPo oldJobPo = appContext.getRepeatJobQueue().getJob(request.getJobId());
        boolean success = appContext.getRepeatJobQueue().selectiveUpdateByJobId(request);
        if (success) {
            try {
                JobPo newJobPo = appContext.getRepeatJobQueue().getJob(request.getJobId());

                boolean relyOnPrevCycleChanged = !newJobPo.getRelyOnPrevCycle().equals(oldJobPo.getRelyOnPrevCycle());
                // repeatInterval变了或者repeatCount变少了
                boolean repeatIntervalChanged = !newJobPo.getRepeatInterval().equals(oldJobPo.getRepeatInterval());
                boolean repeatIntervalOrCountDecChanged = repeatIntervalChanged
                        || (
                        (oldJobPo.getRepeatCount() == -1 && newJobPo.getRepeatCount() > 0)
                                || (oldJobPo.getRepeatCount() != -1 && newJobPo.getRepeatCount() != -1 && newJobPo.getRepeatCount() < oldJobPo.getRepeatCount()
                        )
                );
                if (oldJobPo.getRelyOnPrevCycle() && !relyOnPrevCycleChanged) {
                    // 如果repeatInterval有修改,需要把triggerTime也要修改下
                    if (repeatIntervalChanged) {
                        long nextTriggerTime = JobUtils.getRepeatNextTriggerTime(oldJobPo);
                        request.setTriggerTime(new Date(nextTriggerTime));
                    }
                    // 把等待执行的队列也更新一下
                    appContext.getExecutableJobQueue().selectiveUpdateByJobId(request);
                } else {
                    // 2. 需要对批量任务做处理
                    if (relyOnPrevCycleChanged) {
                        if (oldJobPo.getRelyOnPrevCycle()) {
                            // 之前是依赖的,现在不依赖,需要生成批量任务
                            appContext.getExecutableJobQueue().remove(oldJobPo.getTaskTrackerNodeGroup(), oldJobPo.getJobId());
                            appContext.getNoRelyJobGenerator().generateRepeatJobForInterval(newJobPo, new Date());
                        } else {
                            // 之前不依赖,现在依赖,需要删除批量任务
                            appContext.getExecutableJobQueue().removeBatch(oldJobPo.getRealTaskId(), oldJobPo.getTaskTrackerNodeGroup());
                            // 添加新的任务
                            newJobPo.setTriggerTime(JobUtils.getRepeatNextTriggerTime(oldJobPo));
                            try {
                                newJobPo.setInternalExtParam(ExtConfigKeys.EXE_SEQ_ID, JobUtils.generateExeSeqId(newJobPo));
                                appContext.getExecutableJobQueue().add(newJobPo);
                            } catch (DupEntryException ignored) {
                            }
                        }
                    } else {
                        // 如果relyOnPrevCycle 没有修改过, 表示relyOnPrevCycle=false, 那么要看repeatIntervalOrCountDecChanged,如果修改过,需要删除重新生成
                        if (repeatIntervalOrCountDecChanged) {
                            appContext.getExecutableJobQueue().removeBatch(oldJobPo.getRealTaskId(), oldJobPo.getTaskTrackerNodeGroup());
                            appContext.getNoRelyJobGenerator().generateRepeatJobForInterval(newJobPo, new Date());
                        } else {
                            appContext.getExecutableJobQueue().selectiveUpdateByTaskId(request);
                        }
                    }
                }

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return Builder.build(false, "更新等待执行的任务失败，请手动更新! error:" + e.getMessage());
            }
            JobLogUtils.log(LogType.UPDATE, oldJobPo, appContext.getJobLogger());
            return Builder.build(true);
        } else {
            return Builder.build(false, "该任务已经被删除或者执行完成");
        }
    }

    @RequestMapping("/job-queue/repeat-job-delete")
    public RestfulResponse repeatJobDelete(JobQueueReq request) {
        if (StringUtil.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }
        JobPo jobPo = appContext.getRepeatJobQueue().getJob(request.getJobId());
        boolean success = appContext.getRepeatJobQueue().remove(request.getJobId());
        if (success) {
            try {
                appContext.getExecutableJobQueue().removeBatch(jobPo.getRealTaskId(), jobPo.getTaskTrackerNodeGroup());
//                appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(), request.getJobId());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return Builder.build(false, "删除等待执行的任务失败，请手动删除! error:{}" + e.getMessage());
            }
        }
        JobLogUtils.log(LogType.DEL, jobPo, appContext.getJobLogger());
        return Builder.build(true);
    }

    @RequestMapping("/job-queue/repeat-job-suspend")
    public RestfulResponse repeatJobSuspend(JobQueueReq request) {
        if (StringUtil.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }
        JobPo jobPo = appContext.getRepeatJobQueue().getJob(request.getJobId());
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
            appContext.getRepeatJobQueue().remove(request.getJobId());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "删除Repeat任务失败，请手动删除! error:" + e.getMessage());
        }
        try {
            if (!jobPo.getRelyOnPrevCycle()) {
                appContext.getRepeatJobQueue().updateLastGenerateTriggerTime(jobPo.getJobId(), new Date().getTime());
                appContext.getExecutableJobQueue().removeBatch(jobPo.getRealTaskId(), jobPo.getTaskTrackerNodeGroup());
            } else {
                appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(), request.getJobId());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Builder.build(false, "删除等待执行的任务失败，请手动删除! error:" + e.getMessage());
        }

        JobLogUtils.log(LogType.SUSPEND, jobPo, appContext.getJobLogger());

        return Builder.build(true);
    }

}
