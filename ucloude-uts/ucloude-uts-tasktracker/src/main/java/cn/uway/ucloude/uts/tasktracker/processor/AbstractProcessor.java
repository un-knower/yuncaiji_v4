package cn.uway.ucloude.uts.tasktracker.processor;

import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;

public abstract class AbstractProcessor implements RpcProcessor {

	protected TaskTrackerContext context;
	 
	
	public AbstractProcessor(TaskTrackerContext context) {
		super();
		this.context = context;
	}




}
