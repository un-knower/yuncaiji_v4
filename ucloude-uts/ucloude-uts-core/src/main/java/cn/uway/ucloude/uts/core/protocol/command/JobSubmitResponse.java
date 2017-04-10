package cn.uway.ucloude.uts.core.protocol.command;

import java.util.List;

import cn.uway.ucloude.uts.core.domain.Job;

/**
 * 任务传递信息
 * @author uway
 *
 */
public class JobSubmitResponse extends AbstractRpcCommandBody {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2321173837307423990L;
	private Boolean success = true;

    private String msg;

    // 失败的jobs
    private List<Job> failedJobs;

    public List<Job> getFailedJobs() {
        return failedJobs;
    }

    public void setFailedJobs(List<Job> failedJobs) {
        this.failedJobs = failedJobs;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
