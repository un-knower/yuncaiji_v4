package cn.uway.ucloude.uts.jobtracker.complete.biz;

import java.util.List;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.rpc.protocal.RpcProtos;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.LogType;
import cn.uway.ucloude.uts.core.domain.Action;
import cn.uway.ucloude.uts.core.domain.JobRunResult;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.protocol.command.JobCompletedRequest;
import cn.uway.ucloude.uts.core.support.JobDomainConverter;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.monitor.JobTrackerMStatReporter;

public class JobStatBiz implements JobCompletedBiz {
    private final static ILogger LOGGER = LoggerManager.getLogger(JobStatBiz.class);
	private JobTrackerContext context;
    private JobTrackerMStatReporter stat;

    public JobStatBiz(JobTrackerContext context) {
        this.context = context;
        this.stat = (JobTrackerMStatReporter) context.getMStatReporter();

    }

	@Override
	public RpcCommand doBiz(JobCompletedRequest request) {
		 List<JobRunResult> results = request.getJobRunResults();

	        if (CollectionUtil.isEmpty(results)) {
	            return RpcCommand.createResponseCommand(RpcProtos
	                            .ResponseCode.REQUEST_PARAM_ERROR.code(),
	                    "JobResults can not be empty!");
	        }

	        if (LOGGER.isDebugEnabled()) {
	            LOGGER.debug("Job execute completed : {}", results);
	        }

	        LogType logType = request.isReSend() ? LogType.RESEND : LogType.FINISHED;

	        for (JobRunResult result : results) {

	            // 记录日志
	            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(result.getJobMeta());
	            jobLogPo.setMsg(result.getMsg());
	            jobLogPo.setLogType(logType);
	            jobLogPo.setSuccess(Action.EXECUTE_SUCCESS.equals(result.getAction()));
	            jobLogPo.setTaskTrackerIdentity(request.getIdentity());
	            jobLogPo.setLevel(Level.INFO);
	            jobLogPo.setLogTime(result.getTime());
	            context.getJobLogger().log(jobLogPo);

	            // 监控数据统计
	            if (result.getAction() != null) {
	                switch (result.getAction()) {
	                    case EXECUTE_SUCCESS:
	                        stat.incExeSuccessNum();
	                        break;
	                    case EXECUTE_FAILED:
	                        stat.incExeFailedNum();
	                        break;
	                    case EXECUTE_LATER:
	                        stat.incExeLaterNum();
	                        break;
	                    case EXECUTE_EXCEPTION:
	                        stat.incExeExceptionNum();
	                        break;
	                }
	            }
	        }
	        return null;
	}

}
