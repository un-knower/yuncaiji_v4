package cn.uway.ucloude.uts.tasktracker.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.AsyncCallback;
import cn.uway.ucloude.rpc.Channel;
import cn.uway.ucloude.rpc.ResponseFuture;
import cn.uway.ucloude.rpc.exception.RpcCommandException;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.rpc.protocal.RpcProtos;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.utils.Callable;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.domain.JobMeta;
import cn.uway.ucloude.uts.core.domain.JobRunResult;
import cn.uway.ucloude.uts.core.exception.JobTrackerNotFoundException;
import cn.uway.ucloude.uts.core.exception.RequestTimeoutException;
import cn.uway.ucloude.uts.core.failstore.FailStorePathBuilder;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.command.JobCompletedRequest;
import cn.uway.ucloude.uts.core.protocol.command.JobPushRequest;
import cn.uway.ucloude.uts.core.protocol.command.JobPushResponse;
import cn.uway.ucloude.uts.core.rpc.RpcClientDelegate;
import cn.uway.ucloude.uts.core.support.NodeShutdownHook;
import cn.uway.ucloude.uts.core.support.RetryScheduler;
import cn.uway.ucloude.uts.tasktracker.domain.Response;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;
import cn.uway.ucloude.uts.tasktracker.exception.NoAvailableJobRunnerException;
import cn.uway.ucloude.uts.tasktracker.runner.RunnerCallback;

public class JobPushProcessor extends AbstractProcessor {
	  private static final ILogger LOGGER = LoggerManager.getLogger(JobPushProcessor.class);

	    private RetryScheduler<JobRunResult> retryScheduler;
	    private JobRunnerCallback jobRunnerCallback;
	    private RpcClientDelegate rpcClient;
	public JobPushProcessor(TaskTrackerContext context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.rpcClient = context.getRpcClient();
        retryScheduler = new RetryScheduler<JobRunResult>(JobPushProcessor.class.getSimpleName(), context,
                FailStorePathBuilder.getJobFeedbackPath(context), 3) {
            @Override
            protected boolean isRpcEnable() {
                return rpcClient.isServerEnable();
            }

            @Override
            protected boolean retry(List<JobRunResult> results) {
                return retrySendJobResults(results);
            }
        };
        retryScheduler.start();

        // 线程安全的
        jobRunnerCallback = new JobRunnerCallback();

        NodeShutdownHook.registerHook(context.getEventCenter(),context.getConfiguration().getIdentity(), this.getClass().getName(), new Callable() {
            @Override
            public void call() throws Exception {
                retryScheduler.stop();
            }
        });
	}

	@Override
	public RpcCommand processRequest(Channel channel, RpcCommand request) throws RpcCommandException {
		LOGGER.info("processRequest");
		// TODO Auto-generated method stub
		JobPushRequest requestBody = request.getBody();

        // JobTracker 分发来的 job
        final List<JobMeta> jobMetaList = requestBody.getJobMetaList();
        List<String> failedJobIds = null;

        for (JobMeta jobMeta : jobMetaList) {
            try {
                context.getRunnerPool().execute(jobMeta, jobRunnerCallback);
            } catch (NoAvailableJobRunnerException e) {
                if (failedJobIds == null) {
                    failedJobIds = new ArrayList<String>();
                }
                failedJobIds.add(jobMeta.getJobId());
            }
        }
        if (CollectionUtil.isNotEmpty(failedJobIds)) {
            // 任务推送失败
            JobPushResponse jobPushResponse = new JobPushResponse();
            jobPushResponse.setFailedJobIds(failedJobIds);
            return RpcCommand.createResponseCommand(JobProtos.ResponseCode.NO_AVAILABLE_JOB_RUNNER.code(), jobPushResponse);
        }

        // 任务推送成功
        return RpcCommand.createResponseCommand(JobProtos
                .ResponseCode.JOB_PUSH_SUCCESS.code(), "job push success!");
	}

	 /**
     * 任务执行的回调(任务执行完之后线程回调这个函数)
     */
    private class JobRunnerCallback implements RunnerCallback {
        @Override
        public JobMeta runComplete(Response response) {
            // 发送消息给 JobTracker
            final JobRunResult jobRunResult = new JobRunResult();
            jobRunResult.setTime(SystemClock.now());
            jobRunResult.setJobMeta(response.getJobMeta());
            jobRunResult.setAction(response.getAction());
            jobRunResult.setMsg(response.getMsg());
            JobCompletedRequest requestBody = context.getCommandBodyWrapper().wrapper(new JobCompletedRequest());
            requestBody.addJobResult(jobRunResult);
            requestBody.setReceiveNewJob(response.isReceiveNewJob());     // 设置可以接受新任务

            int requestCode = JobProtos.RequestCode.JOB_COMPLETED.code();

            RpcCommand request = RpcCommand.createRequestCommand(requestCode, requestBody);

            final Response returnResponse = new Response();

            try {
                final CountDownLatch latch = new CountDownLatch(1);
                rpcClient.invokeAsync(request, new AsyncCallback() {
                    @Override
                    public void onComplete(ResponseFuture responseFuture) {
                        try {
                        	RpcCommand commandResponse = responseFuture.getResponseCommand();

                            if (commandResponse != null && commandResponse.getCode() == RpcProtos.ResponseCode.SUCCESS.code()) {
                                JobPushRequest jobPushRequest = commandResponse.getBody();
                                if (jobPushRequest != null) {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("Get new job :{}", JsonConvert.serialize(jobPushRequest.getJobMetaList()));
                                    }
                                    if (CollectionUtil.isNotEmpty(jobPushRequest.getJobMetaList())) {
                                        returnResponse.setJobMeta(jobPushRequest.getJobMetaList().get(0));
                                    }
                                }
                            } else {
                                if (LOGGER.isInfoEnabled()) {
                                    LOGGER.info("Job feedback failed, save local files。{}", jobRunResult);
                                }
                                try {
                                    retryScheduler.inSchedule(
                                            jobRunResult.getJobMeta().getJobId().concat("_") + SystemClock.now(),
                                            jobRunResult);
                                } catch (Exception e) {
                                    LOGGER.error("Job feedback failed", e);
                                }
                            }
                        } finally {
                            latch.countDown();
                        }
                    }
                });

                try {
                    latch.await(ExtConfigKeys.LATCH_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new RequestTimeoutException(e);
                }
            } catch (JobTrackerNotFoundException e) {
                try {
                    LOGGER.warn("No job tracker available! save local files.");
                    retryScheduler.inSchedule(
                            jobRunResult.getJobMeta().getJobId().concat("_") + SystemClock.now(),
                            jobRunResult);
                } catch (Exception e1) {
                    LOGGER.error("Save files failed, {}", jobRunResult.getJobMeta(), e1);
                }
            }

            return returnResponse.getJobMeta();
        }
    }

    /**
     * 发送JobResults
     */
    private boolean retrySendJobResults(List<JobRunResult> results) {
        // 发送消息给 JobTracker
        JobCompletedRequest requestBody = context.getCommandBodyWrapper().wrapper(new JobCompletedRequest());
        requestBody.setJobRunResults(results);
        requestBody.setReSend(true);

        int requestCode = JobProtos.RequestCode.JOB_COMPLETED.code();
        RpcCommand request = RpcCommand.createRequestCommand(requestCode, requestBody);

        try {
            // 这里一定要用同步，不然异步会发生文件锁，死锁
        	RpcCommand commandResponse = rpcClient.invokeSync(request);
            if (commandResponse != null && commandResponse.getCode() == RpcProtos.ResponseCode.SUCCESS.code()) {
                return true;
            } else {
                LOGGER.warn("Send job failed, {}", commandResponse);
                return false;
            }
        } catch (JobTrackerNotFoundException e) {
            LOGGER.error("Retry send job result failed! jobResults={}", results, e);
        }
        return false;
    }
}
