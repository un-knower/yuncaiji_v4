package cn.uway.ucloude.uts.core.protocol.command;

import java.util.List;

import cn.uway.ucloude.annotation.NotNull;
import cn.uway.ucloude.uts.core.domain.Job;

/**
 *  任务传递信息
 * @author uway
 *
 */
public class JobSubmitRequest extends AbstractRpcCommandBody {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7519106247014922432L;
	
	@NotNull
    private List<Job> jobs;

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

}
