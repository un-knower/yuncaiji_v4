package cn.uway.ucloude.uts.jobtracker.support.listener;

import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.listener.MasterChangeListener;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

public class JobTrackerMasterChangeListener implements MasterChangeListener {
	private JobTrackerContext context;
	public JobTrackerMasterChangeListener(JobTrackerContext context){
		this.context = context;

	}
	
	@Override
	public void change(Node master, boolean isMaster) {
		// TODO Auto-generated method stub
		if (context.getConfiguration().getIdentity().equals(master.getIdentity())) {
            // 如果 master 节点是自己
            // 2. 启动通知客户端失败检查重发的定时器
			context.getFeedbackJobSendChecker().start();
			context.getExecutableDeadJobChecker().start();
			context.getExecutingDeadJobChecker().start();
			context.getNonRelyOnPrevCycleJobScheduler().start();
        } else {
            // 如果 master 节点不是自己

            // 2. 关闭通知客户端失败检查重发的定时器
        	context.getFeedbackJobSendChecker().stop();
        	context.getExecutableDeadJobChecker().stop();
        	context.getExecutingDeadJobChecker().stop();
        	context.getNonRelyOnPrevCycleJobScheduler().stop();
        }
	}

}
