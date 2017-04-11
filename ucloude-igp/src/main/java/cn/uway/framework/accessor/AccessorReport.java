package cn.uway.framework.accessor;

import java.util.Date;

/**
 * AccessorReport 接入器报告
 * 
 * @author chenrongqiang 2012-11-8
 */
public class AccessorReport {

	// 采集对象
	private String gatherObj;

	// 失败原因
	private String cause;

	// 接入开始时间
	private Date startTime;

	// 接入结束时间
	private Date endTime;

	public String getGatherObj() {
		return gatherObj;
	}

	public String getCause() {
		return cause;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setGatherObj(String gatherObj) {
		this.gatherObj = gatherObj;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

}
