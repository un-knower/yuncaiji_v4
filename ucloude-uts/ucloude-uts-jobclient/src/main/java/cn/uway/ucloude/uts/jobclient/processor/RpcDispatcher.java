package cn.uway.ucloude.uts.jobclient.processor;

import java.util.HashMap;
import java.util.Map;

import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.rpc.protocal.RpcProtos;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.jobclient.domain.JobClientContext;

import static cn.uway.ucloude.uts.core.protocol.JobProtos.RequestCode.JOB_COMPLETED;
import static cn.uway.ucloude.uts.core.protocol.JobProtos.RequestCode.valueOf;

public class RpcDispatcher implements RpcProcessor {
	private final Map<JobProtos.RequestCode,RpcProcessor> processors = new HashMap<JobProtos.RequestCode, RpcProcessor>();
	
	public RpcDispatcher(JobClientContext context) {
		processors.put(JOB_COMPLETED, new JobFinishedProcessor(context));
		// TODO Auto-generated constructor stub
	}

	@Override
	public RpcCommand processRequest(Channel channel, RpcCommand request) throws RpcCommandException {
		JobProtos.RequestCode code = valueOf(request.getCode());
		RpcProcessor processor = processors.get(code);
		if(processor == null){
			return RpcCommand.createResponseCommand(RpcProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(),"request code not supported!");
		}
        return processor.processRequest(channel, request);
	}

}
