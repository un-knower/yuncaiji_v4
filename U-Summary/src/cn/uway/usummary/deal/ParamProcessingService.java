package cn.uway.usummary.deal;

import java.util.Date;

import cn.uway.usummary.entity.USummaryConfInfo;

public interface ParamProcessingService {
	
	public boolean checkParam();
	
	public USummaryConfInfo paramProcessing();
	
	public Date getDataTime() throws Exception;
}
