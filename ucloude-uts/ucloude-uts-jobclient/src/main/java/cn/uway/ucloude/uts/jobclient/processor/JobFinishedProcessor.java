package cn.uway.ucloude.uts.jobclient.processor;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.command.JobFinishedRequest;
import cn.uway.ucloude.uts.jobclient.domain.JobClientContext;
import cn.uway.ucloude.uts.jobclient.support.JobClientMStatReporter;

public class JobFinishedProcessor implements RpcProcessor {
	private static final ILogger LOGGER = LoggerManager.getLogger(JobFinishedProcessor.class);

	private JobClientContext context;
	private JobClientMStatReporter stat;

	public JobFinishedProcessor(JobClientContext context) {
		super();
		this.context = context;
		this.stat = (JobClientMStatReporter) context.getMStatReporter();
	}

	@Override
	public RpcCommand processRequest(Channel channel, RpcCommand request) throws RpcCommandException {
		// TODO Auto-generated method stub
		JobFinishedRequest requestBody = request.getBody();
		try {
			if (context.getJobCompletedHandler() != null) {
				context.getJobCompletedHandler().onComplete(requestBody.getJobResults());
				stat.incHandleFeedbackNum(CollectionUtil.sizeOf(requestBody.getJobResults()));

			}
		} catch (Exception t) {
			LOGGER.error(t.getMessage(), t);
		}

		return RpcCommand.createResponseCommand(JobProtos.ResponseCode.JOB_NOTIFY_SUCCESS.code(),
				"received successful");
	}

}
