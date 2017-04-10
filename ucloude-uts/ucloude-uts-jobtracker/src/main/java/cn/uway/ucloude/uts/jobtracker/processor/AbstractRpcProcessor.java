package cn.uway.ucloude.uts.jobtracker.processor;

import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

public abstract class AbstractRpcProcessor implements RpcProcessor {

    protected JobTrackerContext context;

    
    public AbstractRpcProcessor(JobTrackerContext context) {
        this.context = context;
    }

}
