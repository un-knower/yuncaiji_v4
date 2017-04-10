package cn.uway.ucloude.uts.core.protocol.command;

import java.util.List;

public class JobPushResponse extends AbstractRpcCommandBody {
	   /**
	 * 
	 */
	private static final long serialVersionUID = -1632660726080326260L;
	private List<String> failedJobIds;

	    public List<String> getFailedJobIds() {
	        return failedJobIds;
	    }

	    public void setFailedJobIds(List<String> failedJobIds) {
	        this.failedJobIds = failedJobIds;
	    }
}
