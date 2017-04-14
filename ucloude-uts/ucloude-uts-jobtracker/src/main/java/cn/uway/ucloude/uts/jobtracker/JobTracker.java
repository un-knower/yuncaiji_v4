package cn.uway.ucloude.uts.jobtracker;

import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.uts.biz.logger.SmartJobLogger;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.cluster.AbstractServerNode;
import cn.uway.ucloude.uts.core.queue.JobQueueFactory;
import cn.uway.ucloude.uts.jobtracker.channel.ChannelManager;
import cn.uway.ucloude.uts.jobtracker.cmd.AddJobHttpCmd;
import cn.uway.ucloude.uts.jobtracker.cmd.JobTrackerReadFileHttpCmd;
import cn.uway.ucloude.uts.jobtracker.cmd.LoadJobHttpCmd;
import cn.uway.ucloude.uts.jobtracker.cmd.TriggerJobManuallyHttpCmd;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerNode;
import cn.uway.ucloude.uts.jobtracker.monitor.JobTrackerMStatReporter;
import cn.uway.ucloude.uts.jobtracker.processor.RpcDispatcher;
import cn.uway.ucloude.uts.jobtracker.sender.JobSender;
import cn.uway.ucloude.uts.jobtracker.support.JobReceiver;
import cn.uway.ucloude.uts.jobtracker.support.NonRelyOnPrevCycleJobScheduler;
import cn.uway.ucloude.uts.jobtracker.support.OldDataHandler;
import cn.uway.ucloude.uts.jobtracker.support.checker.ExecutableDeadJobChecker;
import cn.uway.ucloude.uts.jobtracker.support.checker.ExecutingDeadJobChecker;
import cn.uway.ucloude.uts.jobtracker.support.checker.FeedbackJobSendChecker;
import cn.uway.ucloude.uts.jobtracker.support.cluster.JobClientManager;
import cn.uway.ucloude.uts.jobtracker.support.cluster.TaskTrackerManager;
import cn.uway.ucloude.uts.jobtracker.support.listener.JobNodeChangeListener;
import cn.uway.ucloude.uts.jobtracker.support.listener.JobTrackerMasterChangeListener;
import cn.uway.ucloude.uts.jobtracker.support.policy.OldDataDeletePolicy;

/**
 * Job Tracker
 * 
 * @author uway
 *
 */
public class JobTracker extends AbstractServerNode<JobTrackerNode, JobTrackerContext> {
	private String nodeName;//节点名称

	public JobTracker() {
		// 添加节点变化监听器
		addNodeChangeListener(new JobNodeChangeListener(context));
		// 添加master节点变化监听器
		addMasterChangeListener(new JobTrackerMasterChangeListener(context));
	}

	@Override
	protected RpcProcessor getDefaultProcessor() {
		// TODO Auto-generated method stub
		return new RpcDispatcher(context);
	}

	@Override
	protected void beforeStart() {
		// TODO Auto-generated method stub
		/**
		 * 监控中心
		 */
		context.setMStatReporter(new JobTrackerMStatReporter(context));

		/**
		 * channel管理者
		 */
		context.setChannelManager(new ChannelManager());

		/**
		 * JobClient manager
		 */
		context.setJobClientManager(new JobClientManager(context));

		/**
		 * Task Tracker管理者
		 */
		context.setTaskTrackerManager(new TaskTrackerManager(context));

		/**
		 * 远程服务器
		 */
		context.setRpcServer(rpcServer);

		/**
		 * 日志记录信息
		 */
		context.setJobLogger(new SmartJobLogger(context));

		JobQueueFactory factory = ServiceFactory.load(JobQueueFactory.class, configuration);

		context.setExecutableJobQueue(factory.getExecutableJobQueue());
		context.setExecutingJobQueue(factory.getExecutingJobQueue());
		context.setCronJobQueue(factory.getCronJobQueue());
		context.setRepeatJobQueue(factory.getRepeatJobQueue());
		context.setSuspendJobQueue(factory.getSuspendJobQueue());
		context.setJobFeedbackQueue(factory.getJobFeedbackQueue());
		context.setNodeGroupStore(factory.getNodeGroupStore());
		context.setPreLoader(factory.getPreLoader(
				context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_PRELOADER_SIZE, 300),
				context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_PRELOADER_FACTOR, 0.2),
				context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_PRELOADER_SIGNAL_CHECK_INTERVAL, 100),
				context.getConfiguration().getIdentity(), context.getEventCenter()));
		context.setJobReceiver(new JobReceiver(context));
		context.setJobSender(new JobSender(context));
		context.setNonRelyOnPrevCycleJobScheduler(new NonRelyOnPrevCycleJobScheduler(context));
		context.setExecutableDeadJobChecker(new ExecutableDeadJobChecker(context));
		context.setExecutingDeadJobChecker(new ExecutingDeadJobChecker(context));
		context.setFeedbackJobSendChecker(new FeedbackJobSendChecker(context));

		context.getHttpCmdServer().registerCommands(new LoadJobHttpCmd(context), // 手动加载任务
				new AddJobHttpCmd(context), new TriggerJobManuallyHttpCmd(context), // 添加任务;
				new JobTrackerReadFileHttpCmd(context)); // 读日志
		if (context.getOldDataHandler() == null) {
			setOldDataHandler(new OldDataDeletePolicy());
		}
	}

	@Override
	protected void afterStart() {
		// TODO Auto-generated method stub
		context.getChannelManager().start();
		context.getMStatReporter().start();
	}

	@Override
	protected void afterStop() {
		// TODO Auto-generated method stub
		context.getChannelManager().stop();
		context.getMStatReporter().stop();
		context.getHttpCmdServer().stop();
	}

	@Override
	protected void beforeStop() {
		// TODO Auto-generated method stub

	}

	public void setOldDataHandler(OldDataHandler oldDataHandler) {
		context.setOldDataHandler(oldDataHandler);
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

}
