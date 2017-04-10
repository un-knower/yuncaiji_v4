package cn.uway.ucloude.uts.jobtracker.processor;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.JobPullRequest;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.support.JobPusher;

public class JobPullProcessor extends AbstractRpcProcessor {

	public JobPullProcessor(JobTrackerContext context) {
		super(context);
		// TODO Auto-generated constructor stub
		jobPusher = new JobPusher(context);
	}



	private JobPusher jobPusher;
	private static final ILogger LOGGER = LoggerManager.getLogger(JobPullProcessor.class);
	
	
	
	@Override
	public RpcCommand processRequest(Channel channel, RpcCommand request) throws RpcCommandException {
		// TODO Auto-generated method stub
		 JobPullRequest requestBody = request.getBody();

	        if (LOGGER.isDebugEnabled()) {
	            LOGGER.debug("taskTrackerNodeGroup:{}, taskTrackerIdentity:{} , availableThreads:{}", requestBody.getNodeGroup(), requestBody.getIdentity(), requestBody.getAvailableThreads());
	        }
	        jobPusher.push(requestBody);

	        return RpcCommand.createResponseCommand(JobProtos.ResponseCode.JOB_PULL_SUCCESS.code(),"");
	}

}
