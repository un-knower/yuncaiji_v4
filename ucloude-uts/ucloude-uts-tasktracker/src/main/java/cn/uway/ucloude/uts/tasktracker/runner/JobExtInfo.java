package cn.uway.ucloude.uts.tasktracker.runner;

import cn.uway.ucloude.uts.core.domain.JobType;

public class JobExtInfo {
	 // 已经重试的次数 (用户不要设置)
    private int retryTimes = 0;

    /**
     * 已经重复的次数, (用户不要设置)
     */
    private int repeatedCount = 0;
    /**
     * 是否是重试
     */
    private boolean retry;
    /**
     * 任务类型
     */
    private JobType jobType;
    /**
     * 执行的时序 (每个执行周期都不一样，但是修复死任务，重试等不会改变)
     */
    private String  seqId;
	public int getRetryTimes() {
		return retryTimes;
	}
	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}
	public int getRepeatedCount() {
		return repeatedCount;
	}
	public void setRepeatedCount(int repeatedCount) {
		this.repeatedCount = repeatedCount;
	}
	public boolean isRetry() {
		return retry;
	}
	public void setRetry(boolean retry) {
		this.retry = retry;
	}
	public JobType getJobType() {
		return jobType;
	}
	public void setJobType(JobType jobType) {
		this.jobType = jobType;
	}
	public String getSeqId() {
		return seqId;
	}
	public void setSeqId(String seqId) {
		this.seqId = seqId;
	}
}
