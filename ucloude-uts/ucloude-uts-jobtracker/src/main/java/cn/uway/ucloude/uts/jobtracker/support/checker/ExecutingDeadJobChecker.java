package cn.uway.ucloude.uts.jobtracker.support.checker;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.data.dataaccess.exception.DupEntryException;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.AsyncCallback;
import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.ResponseFuture;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.rpc.protocal.RpcProtos;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.QuietUtils;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.LogType;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.exception.RpcSendException;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.command.JobAskRequest;
import cn.uway.ucloude.uts.core.protocol.command.JobAskResponse;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.rpc.RpcServerDelegate;
import cn.uway.ucloude.uts.core.support.JobDomainConverter;
import cn.uway.ucloude.uts.jobtracker.channel.ChannelWrapper;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.monitor.JobTrackerMStatReporter;
import cn.uway.ucloude.uts.jobtracker.support.cluster.JobClientManager;

/**
 * 死掉的任务
 * 1. 分发出去的，并且执行节点不存在的任务
 * 2. 分发出去，执行节点还在, 但是没有在执行的任务
 * @author uway
 *
 */
public class ExecutingDeadJobChecker {
	private static final ILogger LOGGER = LoggerManager.getLogger(JobClientManager.class);
	
	private final ScheduledExecutorService FIXED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1, new NamedThreadFactory("UTS-ExecutingJobQueue-Fix-Executor", true));
	
	private JobTrackerContext context;
	
	private JobTrackerMStatReporter stat;
	
	private AtomicBoolean start = new AtomicBoolean(false);
	
	private ScheduledFuture<?> scheduledFuture;
	
	public ExecutingDeadJobChecker(JobTrackerContext context){
		this.context = context;
		this.stat = (JobTrackerMStatReporter)context.getMStatReporter();
	}
	
	
	public void start(){
		if(start.compareAndSet(false, true)){
			int fixedCheckPeriodSeconds = context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_EXECUTING_JOB_FIX_CHECK_INTERVAL_SECONDS, 30);
			if (fixedCheckPeriodSeconds < 5) {
				fixedCheckPeriodSeconds = 5;
            } else if (fixedCheckPeriodSeconds > 5 * 60) {
            	fixedCheckPeriodSeconds = 5 * 60;
            }
			
			scheduledFuture = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(context.getRegistryStatMonitor().isAvailable()){
						return;
					}
					
					checkAndFix();
					
				}
				
			}, fixedCheckPeriodSeconds, fixedCheckPeriodSeconds, TimeUnit.SECONDS);
			
		}
	}
	
	private void checkAndFix(){
		int maxDeadCheckedTime = context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_EXECUTING_JOB_FIX_DEADLINE_SECONDS, 20);
		if(maxDeadCheckedTime < 10)
			maxDeadCheckedTime = 10;
		else if(maxDeadCheckedTime > 5* 60){
			maxDeadCheckedTime = 5*60;
		}
		
		 // 查询出所有死掉的任务 (其实可以直接在数据库中fix的, 查询出来主要是为了日志打印)
        // 一般来说这个是没有多大的，我就不分页去查询了
        List<JobPo> maybeDeadJobPos = context.getExecutingJobQueue().getDeadJobs(
                SystemClock.now() - maxDeadCheckedTime * 1000);
        
        if(CollectionUtil.isNotEmpty(maybeDeadJobPos)){
        	Map<String/*taskTrackerIdentity*/, List<JobPo>> jobMap = new HashMap<String, List<JobPo>>();
        	for(JobPo jobPo:maybeDeadJobPos){
        		List<JobPo> jobPos = jobMap.get(jobPo.getTaskTrackerIdentity());
                if (jobPos == null) {
                    jobPos = new ArrayList<JobPo>();
                    jobMap.put(jobPo.getTaskTrackerIdentity(), jobPos);
                }
                jobPos.add(jobPo);
        	}
        	
        	for (Map.Entry<String, List<JobPo>> entry : jobMap.entrySet()) {
                String taskTrackerNodeGroup = entry.getValue().get(0).getTaskTrackerNodeGroup();
                String taskTrackerIdentity = entry.getKey();
                // 去查看这个TaskTrackerIdentity是否存活
                ChannelWrapper channelWrapper = context.getChannelManager().getChannel(taskTrackerNodeGroup, NodeType.TASK_TRACKER, taskTrackerIdentity);
                if (channelWrapper == null && taskTrackerIdentity != null) {
                    Long offlineTimestamp = context.getChannelManager().getOfflineTimestamp(taskTrackerIdentity);
                    // 已经离线太久，直接修复
                    if (offlineTimestamp == null || SystemClock.now() - offlineTimestamp > ExtConfigKeys.DEFAULT_TASK_TRACKER_OFFLINE_LIMIT_MILLIS) {
                        // fixDeadJob
                        fixDeadJob(entry.getValue());
                    }
                } else {
                    // 去询问是否在执行该任务
                    if (channelWrapper != null && channelWrapper.getChannel() != null && channelWrapper.isOpen()) {
                        askTimeoutJob(channelWrapper.getChannel(), entry.getValue());
                    }
                }
            }
        }
        
        
	}
	
	/**
     * 向taskTracker询问执行中的任务
     */
    private void askTimeoutJob(Channel channel, final List<JobPo> jobPos) {
        try {
            RpcServerDelegate remotingServer = context.getRpcServer();
            List<String> jobIds = new ArrayList<String>(jobPos.size());
            for (JobPo jobPo : jobPos) {
                jobIds.add(jobPo.getJobId());
            }
            JobAskRequest requestBody = context.getCommandBodyWrapper().wrapper(new JobAskRequest());
            requestBody.setJobIds(jobIds);
            RpcCommand request = RpcCommand.createRequestCommand(JobProtos.RequestCode.JOB_ASK.code(), requestBody);
            remotingServer.invokeAsync(channel, request, new AsyncCallback() {
                @Override
                public void onComplete(ResponseFuture responseFuture) {
                    RpcCommand response = responseFuture.getResponseCommand();
                    if (response != null && RpcProtos.ResponseCode.SUCCESS.code() == response.getCode()) {
                        JobAskResponse responseBody = response.getBody();
                        List<String> deadJobIds = responseBody.getJobIds();
                        if (CollectionUtil.isNotEmpty(deadJobIds)) {

                            // 睡了1秒再修复, 防止任务刚好执行完正在传输中. 1s可以让完成的正常完成
                            QuietUtils.sleep(context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_FIX_EXECUTING_JOB_WAITING_MILLS, 1000L));

                            for (JobPo jobPo : jobPos) {
                                if (deadJobIds.contains(jobPo.getJobId())) {
                                    fixDeadJob(jobPo);
                                }
                            }
                        }
                    }
                }
            });
        } catch (RpcSendException e) {
            LOGGER.error("Ask timeout Job error, ", e);
        }

    }

    private void fixDeadJob(List<JobPo> jobPos) {
        for (JobPo jobPo : jobPos) {
            fixDeadJob(jobPo);
        }
    }

    private void fixDeadJob(JobPo jobPo) {
        try {

            // 已经被移除了
            if (context.getExecutingJobQueue().getJob(jobPo.getJobId()) == null) {
                return;
            }

            jobPo.setGmtModified(SystemClock.now());
            jobPo.setTaskTrackerIdentity(null);
            jobPo.setIsRunning(false);
            // 1. add to executable queue
            try {
                context.getExecutableJobQueue().add(jobPo);
            } catch (DupEntryException e) {
                LOGGER.warn("ExecutableJobQueue already exist:" + JsonConvert.serialize(jobPo));
            }

            // 2. remove from executing queue
            context.getExecutingJobQueue().remove(jobPo.getJobId());

            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
            jobLogPo.setLogTime(SystemClock.now());
            jobLogPo.setSuccess(true);
            jobLogPo.setLevel(Level.WARN);
            jobLogPo.setLogType(LogType.FIXED_DEAD);
            context.getJobLogger().log(jobLogPo);

            stat.incFixExecutingJobNum();

        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
        LOGGER.info("checkAndFix dead job ! {}", JsonConvert.serialize(jobPo));
    }

    public void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                FIXED_EXECUTOR_SERVICE.shutdown();
            }
            LOGGER.info("Executing dead job checker stopped!");
        } catch (Throwable t) {
            LOGGER.error("Executing dead job checker stop failed!", t);
        }
    }
	
}
