package cn.uway.ucloude.uts.core.queue.domain;

import cn.uway.ucloude.uts.core.domain.JobRunResult;

public class JobFeedbackPo {
	private String id;

    private Long gmtCreated;

    private JobRunResult jobRunResult;
    
    private String nodeGroup;

    public String getNodeGroup() {
		return nodeGroup;
	}

	public void setNodeGroup(String nodeGroup) {
		this.nodeGroup = nodeGroup;
	}

	public JobRunResult getJobRunResult() {
        return jobRunResult;
    }

    public void setJobRunResult(JobRunResult jobRunResult) {
        this.jobRunResult = jobRunResult;
    }

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
