package cn.uway.ucloude.uts.jobtracker.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.rpc.protocal.RpcProtos;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.commons.concurrent.limiter.RateLimiter;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.JobProtos.RequestCode;
import cn.uway.ucloude.uts.core.protocol.command.AbstractRpcCommandBody;
import cn.uway.ucloude.uts.jobtracker.channel.ChannelWrapper;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

/**
 * Rpc 分发
 * @author uway
 *
 */
public class RpcDispatcher extends AbstractRpcProcessor {
	 public RpcDispatcher(JobTrackerContext context) {
		super(context);
		// TODO Auto-generated constructor stub
		 processors.put(RequestCode.SUBMIT_JOB, new JobSubmitProcessor(context));
	        processors.put(RequestCode.JOB_COMPLETED, new JobCompletedProcessor(context));
	        processors.put(RequestCode.JOB_PULL, new JobPullProcessor(context));
	        processors.put(RequestCode.BIZ_LOG_SEND, new JobBizLogProcessor(context));
	        processors.put(RequestCode.CANCEL_JOB, new JobCancelProcessor(context));

	        this.reqLimitEnable = context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_RPC_REQ_LIMIT_ENABLE, false);
	        Integer maxQPS = context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_RPC_REQ_LIMIT_MAX_QPS, 5000);
	        this.rateLimiter = RateLimiter.create(maxQPS);
	        this.reqLimitAcquireTimeout = context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_RPC_REQ_LIMIT_ACQUIRE_TIMEOUT, 50);
	}
	private final Map<RequestCode, RpcProcessor> processors = new HashMap<RequestCode, RpcProcessor>();
    private RateLimiter rateLimiter;
    private int reqLimitAcquireTimeout = 50;
    private boolean reqLimitEnable = false;
    
	@Override
	public RpcCommand processRequest(Channel channel, RpcCommand request) throws RpcCommandException {
		// TODO Auto-generated method stub
		
		if(request.getCode() == JobProtos.RequestCode.HEART_BEAT.code()){
			offerHandler(channel, request);
			return RpcCommand.createResponseCommand(JobProtos.ResponseCode.HEART_BEAT_SUCCESS.code(),"");
		}
		
		if(reqLimitEnable){
			return doBizWithReqLimit(channel,request);
		}
		return doBiz(channel,request);
	}
	
	private RpcCommand doBizWithReqLimit(Channel channel, RpcCommand request) throws RpcCommandException{
		if(this.rateLimiter.tryAcquire(this.reqLimitAcquireTimeout, TimeUnit.MILLISECONDS)){
			return doBiz(channel, request);
		}
		return RpcCommand.createResponseCommand(RpcProtos.ResponseCode.SYSTEM_BUSY.code(), "remoting server is busy!");
	}
	
	private RpcCommand doBiz(Channel channel,RpcCommand request) throws RpcCommandException{
		RequestCode code = RequestCode.valueOf(request.getCode());
		RpcProcessor processor = processors.get(code);
		if(processor == null)
			return RpcCommand.createResponseCommand(RpcProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(), "request code not supported!");
		offerHandler(channel, request);
		return processor.processRequest(channel, request);
		
	}
	
	/**
	 * 1.将channel纳入管理中(不存在就加入)
	 * 2.更新 TaskTracker 节点信息(可用线程数)
	 * @param channel
	 * @param request
	 */
	private void offerHandler(Channel channel,RpcCommand request){
		AbstractRpcCommandBody commandBody = request.getBody();
		String nodeGroup = commandBody.getNodeGroup();
		String identity = commandBody.getIdentity();
		NodeType nodeType = NodeType.valueOf(commandBody.getNodeType());
		context.getChannelManager().offerChannel(new ChannelWrapper(channel,nodeType,nodeGroup,identity));
	}
	
	
	

}
