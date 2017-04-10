package cn.uway.ucloude.uts.jobtracker.complete.biz;

import java.util.ArrayList;
import java.util.List;

import cn.uway.ucloude.rpc.protocal.RpcCommand;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.protocol.command.JobCompletedRequest;
import cn.uway.ucloude.uts.core.queue.domain.JobFeedbackPo;
import cn.uway.ucloude.uts.core.support.JobDomainConverter;
import cn.uway.ucloude.uts.jobtracker.complete.JobFinishHandler;
import cn.uway.ucloude.uts.jobtracker.complete.JobRetryHandler;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.support.ClientNotifier;
import cn.uway.ucloude.uts.core.domain.Action;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.domain.JobRunResult;
import cn.uway.ucloude.uts.jobtracker.support.ClientNotifyHandler;

public class JobProcBiz implements JobCompletedBiz {

	 private ClientNotifier clientNotifier;
    private final JobRetryHandler retryHandler;
    private final JobFinishHandler jobFinishHandler;
    // 任务的最大重试次数
    private final Integer globalMaxRetryTimes;
	public JobProcBiz(final JobTrackerContext context) {
		 this.retryHandler = new JobRetryHandler(context);
	        this.jobFinishHandler = new JobFinishHandler(context);

	        this.globalMaxRetryTimes = context.getConfiguration().getParameter(ExtConfigKeys.JOB_MAX_RETRY_TIMES,
	        		ExtConfigKeys.DEFAULT_JOB_MAX_RETRY_TIMES);

	        this.clientNotifier = new ClientNotifier(context, new ClientNotifyHandler<JobRunResult>() {
	        	@Override
	            public void handleSuccess(List<JobRunResult> results) {
	                jobFinishHandler.onComplete(results);
	            }

	            @Override
	            public void handleFailed(List<JobRunResult> results) {
	                if (CollectionUtil.isNotEmpty(results)) {
	                    List<JobFeedbackPo> jobFeedbackPos = new ArrayList<JobFeedbackPo>(results.size());

	                    for (JobRunResult result : results) {
	                        JobFeedbackPo jobFeedbackPo = JobDomainConverter.convert(result);
	                        jobFeedbackPos.add(jobFeedbackPo);
	                    }
	                    // 2. 失败的存储在反馈队列
	                    context.getJobFeedbackQueue().add(jobFeedbackPos);
	                    // 3. 完成任务 
	                    jobFinishHandler.onComplete(results);
	                }
	            }
	        });
	}
	
	@Override
	public RpcCommand doBiz(JobCompletedRequest request) {
		// TODO Auto-generated method stub
		  List<JobRunResult> results = request.getJobRunResults();

	        if (CollectionUtil.sizeOf(results) == 1) {
	            singleResultsProcess(results);
	        } else {
	            multiResultsProcess(results);
	        }
	        return null;
	}

	private void singleResultsProcess(List<JobRunResult> results) {
        JobRunResult result = results.get(0);

        if (!needRetry(result)) {
            // 这种情况下，如果要反馈客户端的，直接反馈客户端，不进行重试
            if (isNeedFeedback(result.getJobMeta().getJob())) {
                clientNotifier.send(results);
            } else {
                jobFinishHandler.onComplete(results);
            }
        } else {
            // 需要retry
            retryHandler.onComplete(results);
        }
    }

    /**
     * 判断任务是否需要加入重试队列
     */
    private boolean needRetry(JobRunResult result) {
        // 判断类型
        if (!(Action.EXECUTE_LATER.equals(result.getAction())
                || Action.EXECUTE_EXCEPTION.equals(result.getAction()))) {
            return false;
        }

        // 判断重试次数
        Job job = result.getJobMeta().getJob();
        Integer retryTimes = result.getJobMeta().getRetryTimes();
        int jobMaxRetryTimes = job.getMaxRetryTimes();
        return !(retryTimes >= globalMaxRetryTimes || retryTimes >= jobMaxRetryTimes);
    }

    /**
     * 这里情况一般是发送失败，重新发送的
     */
    private void multiResultsProcess(List<JobRunResult> results) {

        List<JobRunResult> retryResults = null;
        // 过滤出来需要通知客户端的
        List<JobRunResult> feedbackResults = null;
        // 不需要反馈的
        List<JobRunResult> finishResults = null;

        for (JobRunResult result : results) {

            if (needRetry(result)) {
                // 需要加入到重试队列的
                retryResults = CollectionUtil.newArrayListOnNull(retryResults);
                retryResults.add(result);
            } else if (isNeedFeedback(result.getJobMeta().getJob())) {
                // 需要反馈给客户端
                feedbackResults = CollectionUtil.newArrayListOnNull(feedbackResults);
                feedbackResults.add(result);
            } else {
                // 不用反馈客户端，也不用重试，直接完成处理
                finishResults = CollectionUtil.newArrayListOnNull(finishResults);
                finishResults.add(result);
            }
        }

        // 通知客户端
        clientNotifier.send(feedbackResults);

        // 完成任务
        jobFinishHandler.onComplete(finishResults);

        // 将任务加入到重试队列
        retryHandler.onComplete(retryResults);
    }

    private boolean isNeedFeedback(Job job) {
        if (job == null) {
            return false;
        }
        // 容错,如果没有提交节点组,那么不反馈
        return !StringUtil.isEmpty(job.getSubmitNodeGroup()) && job.isNeedFeedback();
    }
}
