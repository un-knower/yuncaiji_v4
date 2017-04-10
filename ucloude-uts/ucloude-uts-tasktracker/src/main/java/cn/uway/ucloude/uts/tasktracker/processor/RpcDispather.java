package cn.uway.ucloude.uts.tasktracker.processor;

import java.util.HashMap;
import java.util.Map;

import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.rpc.protocal.RpcProtos;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;

/**
 * task tracker 总的处理器, 每一种命令对应不同的处理器
 * @author uway
 *
 */
public class RpcDispather extends AbstractProcessor {

	   private final Map<JobProtos.RequestCode, RpcProcessor> processors = new HashMap<JobProtos.RequestCode, RpcProcessor>();

	public RpcDispather(TaskTrackerContext context) {
		super(context);
		// TODO Auto-generated constructor stub
		processors.put(JobProtos.RequestCode.PUSH_JOB, new JobPushProcessor(context));
        processors.put(JobProtos.RequestCode.JOB_ASK, new JobAskProcessor(context));
	}

	@Override
	public RpcCommand processRequest(Channel channel, RpcCommand request) throws RpcCommandException {
		// TODO Auto-generated method stub
		
		  JobProtos.RequestCode code = JobProtos.RequestCode.valueOf(request.getCode());
	        RpcProcessor processor = processors.get(code);
	        if (processor == null) {
	            return RpcCommand.createResponseCommand(RpcProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(),
	                    "request code not supported!");
	        }
	        return processor.processRequest(channel, request);
	}

}
