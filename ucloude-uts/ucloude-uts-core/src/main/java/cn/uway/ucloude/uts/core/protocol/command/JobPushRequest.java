package cn.uway.ucloude.uts.core.protocol.command;

import java.util.List;

import cn.uway.ucloude.annotation.NotNull;
import cn.uway.ucloude.uts.core.domain.JobMeta;

public class JobPushRequest extends AbstractRpcCommandBody {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6795797200596478119L;
	@NotNull
    private List<JobMeta> jobMetaList;

    public List<JobMeta> getJobMetaList() {
        return jobMetaList;
    }

    public void setJobMetaList(List<JobMeta> jobMetaList) {
        this.jobMetaList = jobMetaList;
    }

}
