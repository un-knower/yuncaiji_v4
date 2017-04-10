package cn.uway.ucloude.uts.core.protocol.command;

import java.util.ArrayList;
import java.util.List;

import cn.uway.ucloude.annotation.NotNull;
import cn.uway.ucloude.uts.core.domain.JobRunResult;

public class JobCompletedRequest extends AbstractRpcCommandBody {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2714442093694132106L;
	/**
     * 是否接受新任务
     */
    private boolean receiveNewJob = false;

    @NotNull
    private List<JobRunResult> jobRunResults;

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

    public List<JobRunResult> getJobRunResults() {
        return jobRunResults;
    }

    public void setJobRunResults(List<JobRunResult> jobRunResults) {
        this.jobRunResults = jobRunResults;
    }

    public void addJobResult(JobRunResult jobRunResult) {
        if (jobRunResults == null) {
            jobRunResults = new ArrayList<JobRunResult>();
        }
        jobRunResults.add(jobRunResult);
    }
}
