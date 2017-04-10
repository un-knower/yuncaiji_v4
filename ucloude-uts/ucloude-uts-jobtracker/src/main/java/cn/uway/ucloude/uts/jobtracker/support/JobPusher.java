package cn.uway.ucloude.uts.jobtracker.support;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.uway.ucloude.common.UCloudeConstants;
import cn.uway.ucloude.common.Holder;
import cn.uway.ucloude.data.dataaccess.exception.DupEntryException;
import cn.uway.ucloude.log.DotLogUtils;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.AsyncCallback;
import cn.uway.ucloude.rpc.ResponseFuture;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.exception.RequestTimeoutException;
import cn.uway.ucloude.uts.core.exception.RpcSendException;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.JobPullRequest;
import cn.uway.ucloude.uts.core.protocol.command.JobPushRequest;
import cn.uway.ucloude.uts.core.protocol.command.JobPushResponse;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.rpc.RpcServerDelegate;
import cn.uway.ucloude.uts.core.support.JobDomainConverter;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.domain.TaskTrackerNode;
import cn.uway.ucloude.uts.jobtracker.monitor.JobTrackerMStatReporter;
import cn.uway.ucloude.uts.jobtracker.sender.JobPushResult;
import cn.uway.ucloude.uts.jobtracker.sender.JobSender;

/**
 * 任务分发管理
 * @author uway
 *
 */
public class JobPusher {
	private final ILogger LOGGER = LoggerManager.getLogger(JobPusher.class);
	private JobTrackerContext context;
	private final ExecutorService executorService;
	private final ExecutorService pushExecutorService;
	private JobTrackerMStatReporter stat;
	private RpcServerDelegate rpcServer;
    private int jobPushBatchSize = 10;
    private ConcurrentHashMap<String, AtomicBoolean> PUSHING_FLAG = new ConcurrentHashMap<String, AtomicBoolean>();
    
    public JobPusher(JobTrackerContext context){
    	this.context = context;
    	this.executorService = Executors.newFixedThreadPool(UCloudeConstants.AVAILABLE_PROCESSOR*5,
    			new NamedThreadFactory(JobPusher.class.getSimpleName()+"-Executor", true));
    	int processorSize = context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_PUSHER_THREAD_NUM, ExtConfigKeys.DEFAULT_JOB_TRACKER_PUSHER_THREAD_NUM);
        this.pushExecutorService = Executors.newFixedThreadPool(processorSize,
                new NamedThreadFactory(JobPusher.class.getSimpleName() + "-AsyncPusher", true));
        this.stat = (JobTrackerMStatReporter)context.getMStatReporter();
        this.rpcServer = context.getRpcServer();
        this.jobPushBatchSize = context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_PUSH_BATCH_SIZE, ExtConfigKeys.DEFAULT_JOB_TRACKER_PUSH_BATCH_SIZE);
        
    }
    
    public void push(final JobPullRequest request){
    	this.executorService.submit(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				 LOGGER.info("Job push begin"+JsonConvert.serialize(request));
				try {
					pushRequest(request);
                } catch (Exception e) {
                    LOGGER.error("Job push failed!", e);
                }
			}
    		
    	});
    }
    
    /**
     * 是否正在推送
     */
    private AtomicBoolean getPushingFlag(TaskTrackerNode taskTrackerNode) {
        AtomicBoolean flag = PUSHING_FLAG.get(taskTrackerNode.getIdentity());
        if (flag == null) {
            flag = new AtomicBoolean(false);
            AtomicBoolean exist = PUSHING_FLAG.putIfAbsent(taskTrackerNode.getIdentity(), flag);
            if (exist != null) {
                flag = exist;
            }
        }
        return flag;
    }
    private void pushRequest(final JobPullRequest request){
    	String nodeGroup = request.getNodeGroup();
        String identity = request.getIdentity();
        /**
         * 更新taskTacker可用线程数
         */
        context.getTaskTrackerManager().updateTaskTrackerAvailableThreads(nodeGroup, identity, request.getAvailableThreads(), request.getTimestamp());
        final TaskTrackerNode taskTrackerNode = context.getTaskTrackerManager().getTaskTrackerNode(nodeGroup, identity);
        if(taskTrackerNode == null){
        	if(LOGGER.isDebugEnabled())
        	     LOGGER.debug("taskTrackerNodeGroup:{}, taskTrackerIdentity:{} , didn't have node.", nodeGroup, identity);
        	return;
        }
        
        int availableThread = taskTrackerNode.getAvailableThread().get();
        if (availableThread <= 0) {
            return;
        }
        AtomicBoolean pushingFlag = getPushingFlag(taskTrackerNode);
        if (pushingFlag.compareAndSet(false, true)) {
        	try {
                final int batchSize = jobPushBatchSize;

                int it = availableThread % batchSize == 0 ? availableThread / batchSize : availableThread / batchSize + 1;

                final CountDownLatch latch = new CountDownLatch(it);

                for (int i = 1; i <= it; i++) {
                    int size = batchSize;
                    if (i == it) {
                        size = availableThread - batchSize * (it - 1);
                    }
                    final int finalSize = size;
                    pushExecutorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 推送任务
                                send(rpcServer, finalSize, taskTrackerNode);
                            } catch (Throwable t) {
                                LOGGER.error("Error on Push Job to {}", taskTrackerNode, t);
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                }

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                DotLogUtils.dot("taskTrackerNodeGroup:{}, taskTrackerIdentity:{} , pushing finished. batchTimes:{}, size:{}", nodeGroup, identity, it, availableThread);
            } finally {
                pushingFlag.compareAndSet(true, false);
            }
        }
    }
    
    /**
     * 是否推送成功
     */
    private JobPushResult send(final RpcServerDelegate remotingServer, int size, final TaskTrackerNode taskTrackerNode) {

        final String nodeGroup = taskTrackerNode.getNodeGroup();
        final String identity = taskTrackerNode.getIdentity();

        JobSender.SendResult sendResult = context.getJobSender().send(nodeGroup, identity, size, new JobSender.SendInvoker() {
            @Override
            public JobSender.SendResult invoke(final List<JobPo> jobPos) {

                // 发送给TaskTracker执行
                JobPushRequest body = context.getCommandBodyWrapper().wrapper(new JobPushRequest());
                body.setJobMetaList(JobDomainConverter.convert(jobPos));
                RpcCommand commandRequest = RpcCommand.createRequestCommand(JobProtos.RequestCode.PUSH_JOB.code(), body);

                // 是否分发推送任务成功
                final Holder<Boolean> pushSuccess = new Holder<Boolean>(false);

                final CountDownLatch latch = new CountDownLatch(1);
                try {
                	rpcServer.invokeAsync(taskTrackerNode.getChannel().getChannel(), commandRequest, new AsyncCallback() {
                        @Override
                        public void onComplete(ResponseFuture responseFuture) {
                            try {
                                RpcCommand responseCommand = responseFuture.getResponseCommand();
                                if (responseCommand == null) {
                                    LOGGER.warn("Job push failed! response command is null!");
                                    return;
                                }
                                if (responseCommand.getCode() == JobProtos.ResponseCode.JOB_PUSH_SUCCESS.code()) {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("Job push success! nodeGroup=" + nodeGroup + ", identity=" + identity + ", jobList=" + JsonConvert.serialize(jobPos));
                                    }
                                    pushSuccess.set(true);
                                    stat.incPushJobNum(jobPos.size());
                                } else if (responseCommand.getCode() == JobProtos.ResponseCode.NO_AVAILABLE_JOB_RUNNER.code()) {
                                    JobPushResponse jobPushResponse = responseCommand.getBody();
                                    if (jobPushResponse != null && CollectionUtil.isNotEmpty(jobPushResponse.getFailedJobIds())) {
                                        // 修复任务
                                        for (String jobId : jobPushResponse.getFailedJobIds()) {
                                            for (JobPo jobPo : jobPos) {
                                                if (jobId.equals(jobPo.getJobId())) {
                                                    resumeJob(jobPo);
                                                    break;
                                                }
                                            }
                                        }
                                        stat.incPushJobNum(jobPos.size() - jobPushResponse.getFailedJobIds().size());
                                    } else {
                                        stat.incPushJobNum(jobPos.size());
                                    }
                                    pushSuccess.set(true);
                                }

                            } finally {
                                latch.countDown();
                            }
                        }
                    });

                } catch (RpcSendException e) {
                    LOGGER.error("Rpc send error, jobPos={}", JsonConvert.serialize(jobPos), e);
                    return new JobSender.SendResult(false, JobPushResult.SENT_ERROR);
                }

                try {
                    latch.await(ExtConfigKeys.LATCH_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new RequestTimeoutException(e);
                }

                if (!pushSuccess.get()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Job push failed! nodeGroup=" + nodeGroup + ", identity=" + identity + ", jobs=" + JsonConvert.serialize(jobPos));
                    }
                    for (JobPo jobPo : jobPos) {
                        resumeJob(jobPo);
                    }
                    return new JobSender.SendResult(false, JobPushResult.SENT_ERROR);
                }

                return new JobSender.SendResult(true, JobPushResult.SUCCESS);
            }
        });

        return (JobPushResult) sendResult.getReturnValue();
    }

    private void resumeJob(JobPo jobPo) {

        // 队列切回来
        boolean needResume = true;
        try {
            jobPo.setIsRunning(true);
            context.getExecutableJobQueue().add(jobPo);
        } catch (DupEntryException e) {
            LOGGER.warn("ExecutableJobQueue already exist:" + JsonConvert.serialize(jobPo));
            needResume = false;
        }
        context.getExecutingJobQueue().remove(jobPo.getJobId());
        if (needResume) {
            context.getExecutableJobQueue().resume(jobPo.getJobId(),jobPo.getTaskTrackerNodeGroup());
        }
    }
}
