package cn.uway.ucloude.message.pack;

import cn.uway.ucloude.message.SMCData;

/**
 * 包接口类
 */
public abstract class AbsSmcPackage {
	protected SMCData smcData;
	
	public String getPackDesc() {
		if (smcData != null)
			return smcData.getId() + "";
		
		return "";
	}
	
	public AbsSmcPackage(SMCData smcData) {
		this.smcData = smcData;
	}
}
