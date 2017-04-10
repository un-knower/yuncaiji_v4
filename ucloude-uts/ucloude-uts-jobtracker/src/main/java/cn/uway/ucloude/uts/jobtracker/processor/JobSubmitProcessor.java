package cn.uway.ucloude.uts.jobtracker.processor;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.uts.core.exception.JobReceiveException;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.command.JobSubmitRequest;
import cn.uway.ucloude.uts.core.protocol.command.JobSubmitResponse;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

/**
 * 客户端提交任务的处理器
 * @author uway
 *
 */
public class JobSubmitProcessor extends AbstractRpcProcessor{
	private static final ILogger LOGGER = LoggerManager.getLogger(JobSubmitProcessor.class);

    public JobSubmitProcessor(JobTrackerContext context) {
        super(context);
    }

    @Override
    public RpcCommand processRequest(Channel channel, RpcCommand request) throws RpcCommandException {

        JobSubmitRequest jobSubmitRequest = request.getBody();

        JobSubmitResponse jobSubmitResponse = context.getCommandBodyWrapper().wrapper(new JobSubmitResponse());
        RpcCommand response;
        try {
            context.getJobReceiver().receive(jobSubmitRequest);

            response = RpcCommand.createResponseCommand(
                    JobProtos.ResponseCode.JOB_RECEIVE_SUCCESS.code(), "job submit success!", jobSubmitResponse);

        } catch (JobReceiveException e) {
            LOGGER.error("Receive job failed , jobs = " + jobSubmitRequest.getJobs(), e);
            jobSubmitResponse.setSuccess(false);
            jobSubmitResponse.setMsg(e.getMessage());
            jobSubmitResponse.setFailedJobs(e.getJobs());
            response = RpcCommand.createResponseCommand(
                    JobProtos.ResponseCode.JOB_RECEIVE_FAILED.code(), e.getMessage(), jobSubmitResponse);
        }

        return response;
    }
}
