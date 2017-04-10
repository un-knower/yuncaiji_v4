package cn.uway.ucloude.uts.core.protocol;

import cn.uway.ucloude.uts.core.protocol.command.AbstractRpcCommandBody;

public class JobPullRequest extends AbstractRpcCommandBody {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7550301576883611955L;
	
	private Integer availableThreads;

    public Integer getAvailableThreads() {
        return availableThreads;
    }

    public void setAvailableThreads(Integer availableThreads) {
        this.availableThreads = availableThreads;
    }

}
