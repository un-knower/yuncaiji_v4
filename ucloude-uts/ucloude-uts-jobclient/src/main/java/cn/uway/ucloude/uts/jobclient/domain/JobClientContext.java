package cn.uway.ucloude.uts.jobclient.domain;

import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.rpc.RpcClientDelegate;
import cn.uway.ucloude.uts.jobclient.support.JobCompletedHandler;

public class JobClientContext extends UtsContext {
	private RpcClientDelegate rpcClient;
	
	private JobCompletedHandler jobCompletedHandler;

	public RpcClientDelegate getRpcClient() {
		return rpcClient;
	}

	public void setRpcClient(RpcClientDelegate rpcClient) {
		this.rpcClient = rpcClient;
	}

	public JobCompletedHandler getJobCompletedHandler() {
		return jobCompletedHandler;
	}

	public void setJobCompletedHandler(JobCompletedHandler jobCompletedHandler) {
		this.jobCompletedHandler = jobCompletedHandler;
	}
}
