package cn.uway.ucloude.uts.tasktracker.processor;

import java.util.List;

import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.rpc.protocal.RpcProtos;
import cn.uway.ucloude.uts.core.protocol.command.CommandBodyWrapper;
import cn.uway.ucloude.uts.core.protocol.command.JobAskRequest;
import cn.uway.ucloude.uts.core.protocol.command.JobAskResponse;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;

public class JobAskProcessor extends AbstractProcessor {

	public JobAskProcessor(TaskTrackerContext context) {
		super(context);
		// TODO Auto-generated constructor stub

	}

	@Override
	public RpcCommand processRequest(Channel channel, RpcCommand request) throws RpcCommandException {
		// TODO Auto-generated method stub
		JobAskRequest requestBody = request.getBody();
		List<String> jobIds = requestBody.getJobIds();
        List<String> notExistJobIds = context.getRunnerPool()
                .getRunningJobManager().getNotExists(jobIds);
        
        JobAskResponse responseBody = CommandBodyWrapper.wrapper(context, new JobAskResponse());
        responseBody.setJobIds(notExistJobIds);

        return RpcCommand.createResponseCommand(
                RpcProtos.ResponseCode.SUCCESS.code(), "查询成功", responseBody);

	}

}
