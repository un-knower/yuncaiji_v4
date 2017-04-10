package cn.uway.ucloude.uts.core.protocol.command;

import java.util.List;

public class JobAskResponse extends AbstractRpcCommandBody {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5663422281326209343L;
	/**
     * 返回不在执行中的jobIds(死掉的)
     */
    List<String> jobIds;

    public List<String> getJobIds() {
        return jobIds;
    }

    public void setJobIds(List<String> jobIds) {
        this.jobIds = jobIds;
    }
}
