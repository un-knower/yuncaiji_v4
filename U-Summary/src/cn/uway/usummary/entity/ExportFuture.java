package cn.uway.usummary.entity;

import java.util.Date;

public class ExportFuture {
	
	private int exportId;

	// 总共条数
	private long total;

	// 成功条数
	private long succ;

	// 失败条数
	private long fail;

	// 失败码
	private int errorCode;

	// 失败原因
	private String cause;

	// 输出目的地
	private String dest;

	// 输出开始时间
	private Date startTime;

	// 输出结束时间
	private Date endTime;

	public int getExportId() {
		return exportId;
	}

	public void setExportId(int exportId) {
		this.exportId = exportId;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getSucc() {
		return succ;
	}

	public void setSucc(long succ) {
		this.succ = succ;
	}

	public long getFail() {
		return fail;
	}

	public void setFail(long fail) {
		this.fail = fail;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	@Override
	public String toString() {
		return "dest=" + this.dest  + ",total=" + this.total + ",succ=" + this.succ
				+ ",fail=" + this.fail + ",errorCode=" + this.errorCode + ",cause=" + this.cause + ",startTime="
				+ this.startTime + ",endTime=" + this.endTime + ",cost=" + (this.endTime.getTime() - this.startTime.getTime()) / 1000L;
	}
}
