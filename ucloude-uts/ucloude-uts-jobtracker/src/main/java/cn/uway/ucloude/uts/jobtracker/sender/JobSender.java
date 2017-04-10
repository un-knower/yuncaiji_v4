package cn.uway.ucloude.uts.jobtracker.sender;

import java.util.ArrayList;
import java.util.List;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.data.dataaccess.exception.DupEntryException;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.LogType;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.support.JobDomainConverter;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

public class JobSender {
	private final ILogger LOGGER  = LoggerManager.getLogger(JobSender.class);
	
	private JobTrackerContext context;
	
	public JobSender(JobTrackerContext context){
		this.context = context;
	}
	
	public SendResult send(String taskTrackerNodeGroup, String taskTrackerIdentity, int size, SendInvoker invoker) {

        List<JobPo> jobPos = fetchJob(taskTrackerNodeGroup, taskTrackerIdentity, size);
        if (jobPos.size() == 0) {
            return new SendResult(false, JobPushResult.NO_JOB);
        }

        SendResult sendResult = invoker.invoke(jobPos);

        if (sendResult.isSuccess()) {
            List<JobLogPo> jobLogPos = new ArrayList<JobLogPo>(jobPos.size());
            for (JobPo jobPo : jobPos) {
                // 记录日志
                JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
                jobLogPo.setSuccess(true);
                jobLogPo.setLogType(LogType.SENT);
                jobLogPo.setLogTime(SystemClock.now());
                jobLogPo.setLevel(Level.INFO);
                jobLogPos.add(jobLogPo);
            }
            context.getJobLogger().log(jobLogPos);
        }
        return sendResult;
    }

    private List<JobPo> fetchJob(String taskTrackerNodeGroup, String taskTrackerIdentity, int size) {
        List<JobPo> jobPos = new ArrayList<JobPo>(size);

        for (int i = 0; i < size; i++) {
            // 从mongo 中取一个可运行的job
            final JobPo jobPo = context.getPreLoader().take(taskTrackerNodeGroup, taskTrackerIdentity);
            if (jobPo == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Job push failed: no job! nodeGroup=" + taskTrackerNodeGroup + ", identity=" + taskTrackerIdentity);
                }
                break;
            }

            // IMPORTANT: 这里要先切换队列
            try {
            	context.getExecutingJobQueue().add(jobPo);
            } catch (DupEntryException e) {
                LOGGER.warn("ExecutingJobQueue already exist:" + JsonConvert.serialize(jobPo));
                context.getExecutableJobQueue().resume(jobPo.getJobId(),jobPo.getTaskTrackerNodeGroup());
                continue;
            }
            context.getExecutableJobQueue().remove(jobPo.getTaskTrackerNodeGroup(), jobPo.getJobId());

            jobPos.add(jobPo);
        }
        return jobPos;
    }

    public interface SendInvoker {
        SendResult invoke(List<JobPo> jobPos);
    }

    public static class SendResult {
        private boolean success;
        private Object returnValue;

        public SendResult(boolean success, Object returnValue) {
            this.success = success;
            this.returnValue = returnValue;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Object getReturnValue() {
            return returnValue;
        }

        public void setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
        }
    }
}
