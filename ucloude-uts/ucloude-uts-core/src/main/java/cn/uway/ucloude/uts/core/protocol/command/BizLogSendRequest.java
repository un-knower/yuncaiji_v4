package cn.uway.ucloude.uts.core.protocol.command;

import java.util.List;

import cn.uway.ucloude.uts.core.domain.BizLog;

public class BizLogSendRequest extends AbstractRpcCommandBody {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7572494621722805090L;
	
	private List<BizLog> bizLogs;

	public List<BizLog> getBizLogs() {
		return bizLogs;
	}

	public void setBizLogs(List<BizLog> getBizLogs) {
		this.bizLogs = getBizLogs;
	}
	
	

}
