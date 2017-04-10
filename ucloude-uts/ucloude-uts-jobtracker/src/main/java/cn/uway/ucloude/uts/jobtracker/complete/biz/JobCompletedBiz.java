package cn.uway.ucloude.uts.jobtracker.complete.biz;

import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.uts.core.protocol.command.JobCompletedRequest;

public interface JobCompletedBiz {
	 /**
     * 如果返回空表示继续执行
     */
    RpcCommand doBiz(JobCompletedRequest request);
}
