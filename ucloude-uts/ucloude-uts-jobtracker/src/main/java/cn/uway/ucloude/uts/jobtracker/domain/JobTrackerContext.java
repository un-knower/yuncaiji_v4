package cn.uway.ucloude.uts.jobtracker.domain;

import cn.uway.ucloude.uts.biz.logger.JobLogger;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.queue.*;
import cn.uway.ucloude.uts.core.rpc.RpcServerDelegate;
import cn.uway.ucloude.uts.jobtracker.channel.ChannelManager;
import cn.uway.ucloude.uts.jobtracker.sender.JobSender;
import cn.uway.ucloude.uts.jobtracker.support.JobReceiver;
import cn.uway.ucloude.uts.jobtracker.support.NonRelyOnPrevCycleJobScheduler;
import cn.uway.ucloude.uts.jobtracker.support.OldDataHandler;
import cn.uway.ucloude.uts.jobtracker.support.checker.ExecutableDeadJobChecker;
import cn.uway.ucloude.uts.jobtracker.support.checker.ExecutingDeadJobChecker;
import cn.uway.ucloude.uts.jobtracker.support.checker.FeedbackJobSendChecker;
import cn.uway.ucloude.uts.jobtracker.support.cluster.JobClientManager;
import cn.uway.ucloude.uts.jobtracker.support.cluster.TaskTrackerManager;

public class JobTrackerContext extends UtsContext {
	
	   // dead job checker
    private ExecutingDeadJobChecker executingDeadJobChecker;
    private FeedbackJobSendChecker feedbackJobSendChecker;
    private ExecutableDeadJobChecker executableDeadJobChecker;

    // old data handler, dirty data
    private OldDataHandler oldDataHandler;
    
	public ExecutingDeadJobChecker getExecutingDeadJobChecker() {
		return executingDeadJobChecker;
	}

	public void setExecutingDeadJobChecker(ExecutingDeadJobChecker executingDeadJobChecker) {
		this.executingDeadJobChecker = executingDeadJobChecker;
	}

	public FeedbackJobSendChecker getFeedbackJobSendChecker() {
		return feedbackJobSendChecker;
	}

	public void setFeedbackJobSendChecker(FeedbackJobSendChecker feedbackJobSendChecker) {
		this.feedbackJobSendChecker = feedbackJobSendChecker;
	}

	public ExecutableDeadJobChecker getExecutableDeadJobChecker() {
		return executableDeadJobChecker;
	}

	public void setExecutableDeadJobChecker(ExecutableDeadJobChecker executableDeadJobChecker) {
		this.executableDeadJobChecker = executableDeadJobChecker;
	}

	public OldDataHandler getOldDataHandler() {
		return oldDataHandler;
	}

	public void setOldDataHandler(OldDataHandler oldDataHandler) {
		this.oldDataHandler = oldDataHandler;
	}

	private RpcServerDelegate rpcServer;
	// JobClient manager for job tracker
	private JobClientManager jobClientManager;
	
    // channel manager
    private ChannelManager channelManager;
    

    // executable job queue（waiting for exec）
    private ExecutableJobQueue executableJobQueue;
    // executing job queue
    private ExecutingJobQueue executingJobQueue;
    // store the connected node groups
    private NodeGroupStore nodeGroupStore;

    // Cron Job queue
    private CronJobQueue cronJobQueue;
    // feedback queue
    private JobFeedbackQueue jobFeedbackQueue;
	private SuspendJobQueue suspendJobQueue;
    private RepeatJobQueue repeatJobQueue;
    
    private PreLoader preLoader;
    
    private JobSender jobSender;
    
    private JobReceiver jobReceiver;
    
    public JobReceiver getJobReceiver() {
		return jobReceiver;
	}

	public void setJobReceiver(JobReceiver jobReceiver) {
		this.jobReceiver = jobReceiver;
	}

	// biz logger
    private JobLogger jobLogger;

    private NonRelyOnPrevCycleJobScheduler nonRelyOnPrevCycleJobScheduler;
    
	public NonRelyOnPrevCycleJobScheduler getNonRelyOnPrevCycleJobScheduler() {
		return nonRelyOnPrevCycleJobScheduler;
	}

	public void setNonRelyOnPrevCycleJobScheduler(NonRelyOnPrevCycleJobScheduler nonRelyOnPrevCycleJobScheduler) {
		this.nonRelyOnPrevCycleJobScheduler = nonRelyOnPrevCycleJobScheduler;
	}

	public JobLogger getJobLogger() {
		return jobLogger;
	}

	public void setJobLogger(JobLogger jobLogger) {
		this.jobLogger = jobLogger;
	}

	public PreLoader getPreLoader() {
		return preLoader;
	}

	public void setPreLoader(PreLoader preLoader) {
		this.preLoader = preLoader;
	}

	public JobSender getJobSender() {
		return jobSender;
	}

	public void setJobSender(JobSender jobSender) {
		this.jobSender = jobSender;
	}

	public ExecutableJobQueue getExecutableJobQueue() {
		return executableJobQueue;
	}

	public void setExecutableJobQueue(ExecutableJobQueue executableJobQueue) {
		this.executableJobQueue = executableJobQueue;
	}

	public ExecutingJobQueue getExecutingJobQueue() {
		return executingJobQueue;
	}

	public void setExecutingJobQueue(ExecutingJobQueue executingJobQueue) {
		this.executingJobQueue = executingJobQueue;
	}

	public NodeGroupStore getNodeGroupStore() {
		return nodeGroupStore;
	}

	public void setNodeGroupStore(NodeGroupStore nodeGroupStore) {
		this.nodeGroupStore = nodeGroupStore;
	}

	public CronJobQueue getCronJobQueue() {
		return cronJobQueue;
	}

	public void setCronJobQueue(CronJobQueue cronJobQueue) {
		this.cronJobQueue = cronJobQueue;
	}

	public JobFeedbackQueue getJobFeedbackQueue() {
		return jobFeedbackQueue;
	}

	public void setJobFeedbackQueue(JobFeedbackQueue jobFeedbackQueue) {
		this.jobFeedbackQueue = jobFeedbackQueue;
	}

	public SuspendJobQueue getSuspendJobQueue() {
		return suspendJobQueue;
	}

	public void setSuspendJobQueue(SuspendJobQueue suspendJobQueue) {
		this.suspendJobQueue = suspendJobQueue;
	}

	public RepeatJobQueue getRepeatJobQueue() {
		return repeatJobQueue;
	}

	public void setRepeatJobQueue(RepeatJobQueue repeatJobQueue) {
		this.repeatJobQueue = repeatJobQueue;
	}

	public ChannelManager getChannelManager() {
		return channelManager;
	}

	public void setChannelManager(ChannelManager channelManager) {
		this.channelManager = channelManager;
	}

	public JobClientManager getJobClientManager() {
		return jobClientManager;
	}

	public void setJobClientManager(JobClientManager jobClientManager) {
		this.jobClientManager = jobClientManager;
	}

	public TaskTrackerManager getTaskTrackerManager() {
		return taskTrackerManager;
	}

	public void setTaskTrackerManager(TaskTrackerManager taskTrackerManager) {
		this.taskTrackerManager = taskTrackerManager;
	}

	// TaskTracker manager for job tracker
	private TaskTrackerManager taskTrackerManager;

	public RpcServerDelegate getRpcServer() {
		return rpcServer;
	}

	public void setRpcServer(RpcServerDelegate rpcServer) {
		this.rpcServer = rpcServer;
	}
}
