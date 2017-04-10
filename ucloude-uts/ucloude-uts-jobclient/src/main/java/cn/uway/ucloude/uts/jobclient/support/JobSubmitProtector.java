package cn.uway.ucloude.uts.jobclient.support;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.commons.concurrent.limiter.RateLimiter;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.exception.JobSubmitException;
import cn.uway.ucloude.uts.jobclient.domain.JobClientContext;
import cn.uway.ucloude.uts.jobclient.domain.Response;

/**
 * 用来处理客户端请求过载问题
 * @author uway
 *
 */
public class JobSubmitProtector {
	private int maxQPS;
    // 用信号量进行过载保护
    RateLimiter rateLimiter;
    private int acquireTimeout = 100;
    private String errorMsg;

    public JobSubmitProtector(JobClientContext appContext) {

        this.maxQPS = appContext.getConfiguration().getParameter(ExtConfigKeys.JOB_SUBMIT_MAX_QPS,
        		ExtConfigKeys.DEFAULT_JOB_SUBMIT_MAX_QPS);
        if (this.maxQPS < 10) {
            this.maxQPS = ExtConfigKeys.DEFAULT_JOB_SUBMIT_MAX_QPS;
        }

        this.errorMsg = "the maxQPS is " + maxQPS +
                " , submit too fast , use " + ExtConfigKeys.JOB_SUBMIT_MAX_QPS +
                " can change the concurrent size .";
        this.acquireTimeout = appContext.getConfiguration().getParameter(ExtConfigKeys.JOB_SUBMIT_LOCK_ACQUIRE_TIMEOUT, 100);

        this.rateLimiter = RateLimiter.create(this.maxQPS);
    }

    public Response execute(final List<Job> jobs, final JobSubmitExecutor<Response> jobSubmitExecutor) throws JobSubmitException {
        if (!rateLimiter.tryAcquire(acquireTimeout, TimeUnit.MILLISECONDS)) {
            throw new JobSubmitProtectException(maxQPS, errorMsg);
        }
        return jobSubmitExecutor.execute(jobs);
    }

    public int getMaxQPS() {
        return maxQPS;
    }
}
