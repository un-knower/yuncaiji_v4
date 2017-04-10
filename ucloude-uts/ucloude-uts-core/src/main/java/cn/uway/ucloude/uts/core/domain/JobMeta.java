package cn.uway.ucloude.uts.core.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import cn.uway.ucloude.serialize.JsonConvert;

public class JobMeta  implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3873273525067120860L;
	private Job job;

    private String jobId;
    private Map<String, String> internalExtParams;
    // 已经重试的次数
    private int retryTimes;
    // 已经重复的次数
    private Integer repeatedCount;
    private String realTaskId;
    private JobType jobType;

    public JobMeta() {
    }

    public JobType getJobType() {
        return jobType;
    }
    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Map<String, String> getInternalExtParams() {
        return internalExtParams;
    }

    public void setInternalExtParams(Map<String, String> internalExtParams) {
        this.internalExtParams = internalExtParams;
    }

    public String getInternalExtParam(String key) {
        if (internalExtParams == null) {
            return null;
        }
        return internalExtParams.get(key);
    }

    public void setInternalExtParam(String key, String value) {
        if (internalExtParams == null) {
            internalExtParams = new HashMap<String, String>();
        }
        internalExtParams.put(key, value);
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Integer getRepeatedCount() {
        return repeatedCount;
    }

    public void setRepeatedCount(Integer repeatedCount) {
        this.repeatedCount = repeatedCount;
    }

    public String getRealTaskId() {
        return realTaskId;
    }

    public void setRealTaskId(String realTaskId) {
        this.realTaskId = realTaskId;
    }

    @Override
    public String toString() {
        return JsonConvert.serialize(this);
    }
}
