package cn.uway.ucloude.uts.jobclient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.AsyncCallback;
import cn.uway.ucloude.rpc.ResponseFuture;
import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.utils.Assert;
import cn.uway.ucloude.utils.BatchUtils;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.cluster.AbstractClientNode;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.exception.JobSubmitException;
import cn.uway.ucloude.uts.core.exception.JobTrackerNotFoundException;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.command.CommandBodyWrapper;
import cn.uway.ucloude.uts.core.protocol.command.JobCancelRequest;
import cn.uway.ucloude.uts.core.protocol.command.JobSubmitRequest;
import cn.uway.ucloude.uts.core.protocol.command.JobSubmitResponse;
import cn.uway.ucloude.uts.jobclient.cmd.JobClientReadFileHttpCmd;
import cn.uway.ucloude.uts.jobclient.domain.JobClientContext;
import cn.uway.ucloude.uts.jobclient.domain.JobClientNode;
import cn.uway.ucloude.uts.jobclient.domain.Response;
import cn.uway.ucloude.uts.jobclient.domain.ResponseCode;
import cn.uway.ucloude.uts.jobclient.processor.RpcDispatcher;
import cn.uway.ucloude.uts.jobclient.support.JobClientMStatReporter;
import cn.uway.ucloude.uts.jobclient.support.JobCompletedHandler;
import cn.uway.ucloude.uts.jobclient.support.JobSubmitExecutor;
import cn.uway.ucloude.uts.jobclient.support.JobSubmitProtector;
import cn.uway.ucloude.uts.jobclient.support.SubmitCallback;

public class JobClient<T extends JobClientNode, Context extends UtsContext> extends AbstractClientNode<JobClientNode, JobClientContext> {


    protected static final ILogger LOGGER = LoggerManager.getLogger(JobClient.class);

    private static final int BATCH_SIZE = 10;

    // 过载保护的提交者
    private JobSubmitProtector protector;
    protected JobClientMStatReporter stat;
    public JobClient() {
        this.stat = new JobClientMStatReporter(context);
        // 监控中心
        context.setMStatReporter(stat);
    }
    
    
    


	@Override
	protected void beforeStart() {
		// TODO Auto-generated method stub
	    context.setRpcClient(rpcClient);
        protector = new JobSubmitProtector(context);
        context.getHttpCmdServer().registerCommands(new JobClientReadFileHttpCmd(context)); //读日志
	}

	@Override
	protected void afterStart() {
		// TODO Auto-generated method stub
		context.getMStatReporter().start();
	}

	@Override
	protected void afterStop() {
		// TODO Auto-generated method stub
		context.getMStatReporter().stop();
	}

	@Override
	protected void beforeStop() {
		// TODO Auto-generated method stub
		
	}
	
	public Response submitJob(Job job) throws JobSubmitException {
        checkStart();
        return protectSubmit(Collections.singletonList(job));
    }

    private Response protectSubmit(List<Job> jobs) throws JobSubmitException {
        return protector.execute(jobs, new JobSubmitExecutor<Response>() {
            @Override
            public Response execute(List<Job> jobs) throws JobSubmitException {
                return submitJob(jobs, SubmitType.ASYNC);
            }
        });
    }

    /**
     * 取消任务
     */
    public Response cancelJob(String taskId, String taskTrackerNodeGroup) {
        checkStart();

        final Response response = new Response();

        Assert.hasText(taskId, "taskId can not be empty");
        Assert.hasText(taskTrackerNodeGroup, "taskTrackerNodeGroup can not be empty");

        JobCancelRequest request = CommandBodyWrapper.wrapper(context, new JobCancelRequest());
        request.setTaskId(taskId);
        request.setTaskTrackerNodeGroup(taskTrackerNodeGroup);

        RpcCommand requestCommand = RpcCommand.createRequestCommand(
                JobProtos.RequestCode.CANCEL_JOB.code(), request);

        try {
        	RpcCommand remotingResponse = rpcClient.invokeSync(requestCommand);

            if (JobProtos.ResponseCode.JOB_CANCEL_SUCCESS.code() == remotingResponse.getCode()) {
                LOGGER.info("Cancel job success taskId={}, taskTrackerNodeGroup={} ", taskId, taskTrackerNodeGroup);
                response.setSuccess(true);
                return response;
            }

            response.setSuccess(false);
            response.setCode(JobProtos.ResponseCode.valueOf(remotingResponse.getCode()).name());
            response.setMsg(remotingResponse.getRemark());
            LOGGER.warn("Cancel job failed: taskId={}, taskTrackerNodeGroup={}, msg={}", taskId,
                    taskTrackerNodeGroup, remotingResponse.getRemark());
            return response;

        } catch (JobTrackerNotFoundException e) {
            response.setSuccess(false);
            response.setCode(ResponseCode.JOB_TRACKER_NOT_FOUND);
            response.setMsg("Can not found JobTracker node!");
            return response;
        }
    }

    private void checkFields(List<Job> jobs) {
        // 参数验证
        if (CollectionUtil.isEmpty(jobs)) {
            throw new JobSubmitException("Job can not be null!");
        }
        for (Job job : jobs) {
            if (job == null) {
                throw new JobSubmitException("Job can not be null!");
            } else {
                job.checkField();
            }
        }
    }

    protected Response submitJob(final List<Job> jobs, SubmitType type) throws JobSubmitException {
        // 检查参数
        checkFields(jobs);

        final Response response = new Response();
        try {
            JobSubmitRequest jobSubmitRequest = CommandBodyWrapper.wrapper(context, new JobSubmitRequest());
            jobSubmitRequest.setJobs(jobs);

            RpcCommand requestCommand = RpcCommand.createRequestCommand(
                    JobProtos.RequestCode.SUBMIT_JOB.code(), jobSubmitRequest);

            SubmitCallback submitCallback = new SubmitCallback() {
                @Override
                public void call(RpcCommand responseCommand) {
                    if (responseCommand == null) {
                        response.setFailedJobs(jobs);
                        response.setSuccess(false);
                        response.setMsg("Submit Job failed: JobTracker is broken");
                        LOGGER.warn("Submit Job failed: {}, {}", jobs, "JobTracker is broken");
                        return;
                    }

                    if (JobProtos.ResponseCode.JOB_RECEIVE_SUCCESS.code() == responseCommand.getCode()) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Submit Job success: {}", jobs);
                        }
                        response.setSuccess(true);
                        return;
                    }
                    // 失败的job
                    JobSubmitResponse jobSubmitResponse = responseCommand.getBody();
                    response.setFailedJobs(jobSubmitResponse.getFailedJobs());
                    response.setSuccess(false);
                    response.setCode(JobProtos.ResponseCode.valueOf(responseCommand.getCode()).name());
                    response.setMsg("Submit Job failed: " + responseCommand.getRemark() + " " + jobSubmitResponse.getMsg());
                    LOGGER.warn("Submit Job failed: {}, {}, {}", jobs, responseCommand.getRemark(), jobSubmitResponse.getMsg());
                }
            };

            if (SubmitType.ASYNC.equals(type)) {
                asyncSubmit(requestCommand, submitCallback);
            } else {
                syncSubmit(requestCommand, submitCallback);
            }
        } catch (JobTrackerNotFoundException e) {
            response.setSuccess(false);
            response.setCode(ResponseCode.JOB_TRACKER_NOT_FOUND);
            response.setMsg("Can not found JobTracker node!");
        } catch (Exception e) {
            response.setSuccess(false);
            response.setCode(ResponseCode.SYSTEM_ERROR);
            response.setMsg(StringUtil.toString(e));
        } finally {
            // 统计
            if (response.isSuccess()) {
                stat.incSubmitSuccessNum(jobs.size());
            } else {
                stat.incSubmitFailedNum(CollectionUtil.sizeOf(response.getFailedJobs()));
            }
        }

        return response;
    }

    /**
     * 异步提交任务
     */
    private void asyncSubmit(RpcCommand requestCommand, final SubmitCallback submitCallback)
            throws JobTrackerNotFoundException {
        final CountDownLatch latch = new CountDownLatch(1);
        rpcClient.invokeAsync(requestCommand, new AsyncCallback() {
            @Override
            public void onComplete(ResponseFuture responseFuture) {
                try {
                    submitCallback.call(responseFuture.getResponseCommand());
                } finally {
                    latch.countDown();
                }
            }
        });
        try {
            latch.await(ExtConfigKeys.LATCH_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new JobSubmitException("Submit job failed, async request timeout!", e);
        }
    }

    /**
     * 同步提交任务
     */
    private void syncSubmit(RpcCommand requestCommand, final SubmitCallback submitCallback)
            throws JobTrackerNotFoundException {
        submitCallback.call(rpcClient.invokeSync(requestCommand));
    }

    public Response submitJob(List<Job> jobs) throws JobSubmitException {
        checkStart();
        final Response response = new Response();
        response.setSuccess(true);
        int size = jobs.size();

        BatchUtils.batchExecute(size, BATCH_SIZE, jobs, new BatchUtils.Executor<Job>() {
            @Override
            public boolean execute(List<Job> list) {
                Response subResponse = protectSubmit(list);
                if (!subResponse.isSuccess()) {
                    response.setSuccess(false);
                    response.addFailedJobs(list);
                    response.setMsg(subResponse.getMsg());
                }
                return true;
            }
        });
        return response;
    }

    @Override
    protected RpcProcessor getDefaultProcessor() {
        return new RpcDispatcher(context);
    }

    /**
     * 设置任务完成接收器
     */
    public void setJobCompletedHandler(JobCompletedHandler jobCompletedHandler) {
        context.setJobCompletedHandler(jobCompletedHandler);
    }

    enum SubmitType {
        SYNC,   // 同步
        ASYNC   // 异步
    }

    private void checkStart() {
        if (!started.get()) {
            throw new JobSubmitException("JobClient did not started");
        }
    }

}
