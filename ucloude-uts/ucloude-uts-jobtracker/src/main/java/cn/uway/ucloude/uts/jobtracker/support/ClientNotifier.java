package cn.uway.ucloude.uts.jobtracker.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cn.uway.ucloude.common.Holder;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.rpc.AsyncCallback;
import cn.uway.ucloude.rpc.ResponseFuture;
import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.domain.Action;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.domain.JobResult;
import cn.uway.ucloude.uts.core.domain.JobRunResult;
import cn.uway.ucloude.uts.core.exception.RequestTimeoutException;
import cn.uway.ucloude.uts.core.exception.RpcSendException;
import cn.uway.ucloude.uts.core.protocol.JobProtos;
import cn.uway.ucloude.uts.core.protocol.command.JobFinishedRequest;
import cn.uway.ucloude.uts.core.rpc.RpcServerDelegate;
import cn.uway.ucloude.uts.core.support.JobUtils;
import cn.uway.ucloude.uts.jobtracker.domain.JobClientNode;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
@SuppressWarnings({"rawtypes","unchecked"})
public class ClientNotifier {
	private static final ILogger logger = LoggerManager.getLogger(ClientNotifier.class);
	
	private ClientNotifyHandler clientNotifyHandler;
	
	private JobTrackerContext context;
	
	public ClientNotifier(JobTrackerContext context,ClientNotifyHandler clientNotifyHandler){
		this.context = context; 
		this.clientNotifyHandler = clientNotifyHandler;
		
	}
	
	/**
     * 发送给客户端
     * @return 返回成功的个数
     */
	public <T extends JobRunResult> int send(List<T> jobResults) {
        if (CollectionUtil.isEmpty(jobResults)) {
            return 0;
        }

        // 单个 就不用 分组了
        if (jobResults.size() == 1) {

            JobRunResult result = jobResults.get(0);
            if (!send0(result.getJobMeta().getJob().getSubmitNodeGroup(), Collections.singletonList(result))) {
                // 如果没有完成就返回
                clientNotifyHandler.handleFailed(jobResults);
                return 0;
            }
        } else if (jobResults.size() > 1) {

            List<JobRunResult> failedJobRunResult = new ArrayList<JobRunResult>();

            // 有多个要进行分组 (出现在 失败重发的时候)
            Map<String/*nodeGroup*/, List<JobRunResult>> groupMap = new HashMap<String, List<JobRunResult>>();

            for (T jobResult : jobResults) {
                List<JobRunResult> results = groupMap.get(jobResult.getJobMeta().getJob().getSubmitNodeGroup());
                if (results == null) {
                    results = new ArrayList<JobRunResult>();
                    groupMap.put(jobResult.getJobMeta().getJob().getSubmitNodeGroup(), results);
                }
                results.add(jobResult);
            }
            for (Map.Entry<String, List<JobRunResult>> entry : groupMap.entrySet()) {

                if (!send0(entry.getKey(), entry.getValue())) {
                    failedJobRunResult.addAll(entry.getValue());
                }
            }
            clientNotifyHandler.handleFailed(failedJobRunResult);
            return jobResults.size() - failedJobRunResult.size();
        }
        return jobResults.size();
    }

    /**
     * 发送给客户端
     * 返回是否发送成功还是失败
     */
    private boolean send0(String nodeGroup, final List<JobRunResult> results) {
        // 得到 可用的客户端节点
        JobClientNode jobClientNode = context.getJobClientManager().getAvailableJobClient(nodeGroup);

        if (jobClientNode == null) {
            return false;
        }
        List<JobResult> jobResults = new ArrayList<JobResult>(results.size());
        for (JobRunResult result : results) {
            JobResult jobResult = new JobResult();

            Job job = JobUtils.copy(result.getJobMeta().getJob());
            job.setTaskId(result.getJobMeta().getRealTaskId());
            jobResult.setJob(job);
            jobResult.setSuccess(Action.EXECUTE_SUCCESS.equals(result.getAction()));
            jobResult.setMsg(result.getMsg());
            jobResult.setTime(result.getTime());
            jobResult.setExeSeqId(result.getJobMeta().getInternalExtParam(ExtConfigKeys.EXE_SEQ_ID));
            jobResults.add(jobResult);
        }

        JobFinishedRequest requestBody = context.getCommandBodyWrapper().wrapper(new JobFinishedRequest());
        requestBody.setJobResults(jobResults);
        RpcCommand commandRequest = RpcCommand.createRequestCommand(JobProtos.RequestCode.JOB_COMPLETED.code(), requestBody);

        final Holder<Boolean> result = new Holder<Boolean>();
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            getRpcServer().invokeAsync(jobClientNode.getChannel().getChannel(), commandRequest, new AsyncCallback() {
                @Override
                public void onComplete(ResponseFuture responseFuture) {
                    try {
                    	RpcCommand commandResponse = responseFuture.getResponseCommand();

                        if (commandResponse != null && commandResponse.getCode() == JobProtos.ResponseCode.JOB_NOTIFY_SUCCESS.code()) {
                            clientNotifyHandler.handleSuccess(results);
                            result.set(true);
                        } else {
                            result.set(false);
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

        } catch (RpcSendException e) {
            logger.error("Notify client failed!", e);
        }
        return result.get() == null ? false : result.get();
    }

    private RpcServerDelegate getRpcServer() {
        return context.getRpcServer();
    }

}
