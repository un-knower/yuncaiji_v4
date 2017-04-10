package cn.uway.ucloude.uts.jobtracker.processor;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.LogType;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.command.JobCancelRequest;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.support.JobDomainConverter;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

public class JobCancelProcessor extends AbstractRpcProcessor {

	
	public JobCancelProcessor(JobTrackerContext context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	private final ILogger LOGGER = LoggerManager.getLogger(JobCancelProcessor.class);
	@Override
	public RpcCommand processRequest(Channel channel, RpcCommand request) throws RpcCommandException {
		// TODO Auto-generated method stub
		JobCancelRequest jobCancelRequest = request.getBody();

        String taskId = jobCancelRequest.getTaskId();
        String taskTrackerNodeGroup = jobCancelRequest.getTaskTrackerNodeGroup();
        JobPo jobPo = context.getCronJobQueue().getJob(taskTrackerNodeGroup, taskId);
        if (jobPo == null) {
            jobPo = context.getRepeatJobQueue().getJob(taskTrackerNodeGroup, taskId);
        }
        if (jobPo == null) {
            jobPo = context.getExecutableJobQueue().getJob(taskTrackerNodeGroup, taskId);
        }
        if (jobPo == null) {
            jobPo = context.getSuspendJobQueue().getJob(taskTrackerNodeGroup, taskId);
        }

        if (jobPo != null) {
            // 队列都remove下吧
            context.getExecutableJobQueue().removeBatch(jobPo.getRealTaskId(), jobPo.getTaskTrackerNodeGroup());
            if (jobPo.isCron()) {
                context.getCronJobQueue().remove(jobPo.getJobId());
            } else if (jobPo.isRepeatable()) {
                context.getRepeatJobQueue().remove(jobPo.getJobId());
            }
            context.getSuspendJobQueue().remove(jobPo.getJobId());

            // 记录日志
            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
            jobLogPo.setSuccess(true);
            jobLogPo.setLogType(LogType.DEL);
            jobLogPo.setLogTime(SystemClock.now());
            jobLogPo.setLevel(Level.INFO);
            context.getJobLogger().log(jobLogPo);

            LOGGER.info("Cancel Job success , jobId={}, taskId={}, taskTrackerNodeGroup={}", jobPo.getJobId(), taskId, taskTrackerNodeGroup);
            return RpcCommand.createResponseCommand(JobProtos
                    .ResponseCode.JOB_CANCEL_SUCCESS.code());
        }

        return RpcCommand.createResponseCommand(JobProtos
                .ResponseCode.JOB_CANCEL_FAILED.code(), "Job maybe running");
	}

}
