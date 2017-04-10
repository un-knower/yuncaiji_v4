package cn.uway.ucloude.uts.jobclient.support;

import cn.uway.ucloude.rpc.protocal.RpcCommand;

public interface SubmitCallback {
	void call(final RpcCommand responseCommand);
}
