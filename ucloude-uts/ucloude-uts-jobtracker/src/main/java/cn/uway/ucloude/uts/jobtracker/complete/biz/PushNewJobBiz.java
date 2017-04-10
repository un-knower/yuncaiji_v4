package cn.uway.ucloude.uts.jobtracker.complete.biz;

import java.util.List;

import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.rpc.protocal.RpcProtos;
import cn.uway.ucloude.uts.core.protocol.command.JobCompletedRequest;
import cn.uway.ucloude.uts.core.protocol.command.JobPushRequest;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.support.JobDomainConverter;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.sender.JobSender;

public class PushNewJobBiz implements JobCompletedBiz {

    private JobTrackerContext context;
	public PushNewJobBiz(JobTrackerContext context){
		this.context = context;
	}
	@Override
	public RpcCommand doBiz(JobCompletedRequest request) {
		if (request.isReceiveNewJob()) {
            try {
                // 查看有没有其他可以执行的任务
                JobPushRequest jobPushRequest = getNewJob(request.getNodeGroup(), request.getIdentity());
                // 返回 新的任务
                return RpcCommand.createResponseCommand(RpcProtos.ResponseCode.SUCCESS.code(), jobPushRequest);
            } catch (Exception ignored) {
            }
        }
        return null;
	}
	
	 /**
     * 获取新任务去执行
     */
    private JobPushRequest getNewJob(String taskTrackerNodeGroup, String taskTrackerIdentity) {

        JobSender.SendResult sendResult = context.getJobSender().send(taskTrackerNodeGroup, taskTrackerIdentity, 1, new JobSender.SendInvoker() {
            @Override
            public JobSender.SendResult invoke(List<JobPo> jobPos) {

                JobPushRequest jobPushRequest = context.getCommandBodyWrapper().wrapper(new JobPushRequest());
                jobPushRequest.setJobMetaList(JobDomainConverter.convert(jobPos));

                return new JobSender.SendResult(true, jobPushRequest);
            }
        });

        if (sendResult.isSuccess()) {
            return (JobPushRequest) sendResult.getReturnValue();
        }
        return null;
    }

}
