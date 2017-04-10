package cn.uway.ucloude.uts.jobtracker.processor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.rpc.protocal.RpcProtos;
import cn.uway.ucloude.uts.core.protocol.command.JobCompletedRequest;
import cn.uway.ucloude.uts.jobtracker.complete.biz.JobCompletedBiz;
import cn.uway.ucloude.uts.jobtracker.complete.biz.JobProcBiz;
import cn.uway.ucloude.uts.jobtracker.complete.biz.JobStatBiz;
import cn.uway.ucloude.uts.jobtracker.complete.biz.PushNewJobBiz;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

public class JobCompletedProcessor extends AbstractRpcProcessor {

	private List<JobCompletedBiz> bizChain;
	public JobCompletedProcessor(JobTrackerContext context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.bizChain = new CopyOnWriteArrayList<JobCompletedBiz>();
        this.bizChain.add(new JobStatBiz(context));        // 统计
        this.bizChain.add(new JobProcBiz(context));          // 完成处理
        this.bizChain.add(new PushNewJobBiz(context));           // 获取新任务
	}

	@Override
	public RpcCommand processRequest(Channel channel, RpcCommand request) throws RpcCommandException {
		// TODO Auto-generated method stub
		JobCompletedRequest requestBody = request.getBody();

        for (JobCompletedBiz biz : bizChain) {
        	RpcCommand remotingCommand = biz.doBiz(requestBody);
            if (remotingCommand != null) {
                return remotingCommand;
            }
        }
        return RpcCommand.createResponseCommand(RpcProtos.ResponseCode.SUCCESS.code());
	}

}
