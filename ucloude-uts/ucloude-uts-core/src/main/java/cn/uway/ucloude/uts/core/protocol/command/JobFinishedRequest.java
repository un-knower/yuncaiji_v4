package cn.uway.ucloude.uts.core.protocol.command;

import java.util.ArrayList;
import java.util.List;

import cn.uway.ucloude.annotation.NotNull;
import cn.uway.ucloude.uts.core.domain.JobResult;

public class JobFinishedRequest extends AbstractRpcCommandBody {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3072744481600366654L;

	/**
     * 是否接受新任务
     */
    private boolean receiveNewJob = false;

    @NotNull
    private List<JobResult> jobResults;

    // 是否是重发(重发是批量发)
    private boolean reSend = false;

    public boolean isReSend() {
        return reSend;
    }

    public void setReSend(boolean reSend) {
        this.reSend = reSend;
    }

    public boolean isReceiveNewJob() {
        return receiveNewJob;
    }

    public void setReceiveNewJob(boolean receiveNewJob) {
        this.receiveNewJob = receiveNewJob;
    }

    public List<JobResult> getJobResults() {
        return jobResults;
    }

    public void setJobResults(List<JobResult> jobResults) {
        this.jobResults = jobResults;
    }

    public void addJobResult(JobResult jobResult) {
        if (jobResults == null) {
            jobResults = new ArrayList<JobResult>();
        }
        jobResults.add(jobResult);
    }

    public void addJobResults(List<JobResult> jobResults) {
        if (this.jobResults == null) {
            this.jobResults = new ArrayList<JobResult>();
        }
        this.jobResults.addAll(jobResults);
    }
}
