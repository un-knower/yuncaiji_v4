package cn.uway.framework.warehouse.exporter;

import java.util.Date;

/**
 * 仓库报告 ExportReport
 * 
 * @author chenrongqiang 2012-11-1
 */
public class ExportReport {

	// 输出模版ID
	private int exportId;

	// 输出类型
	private int exportType;

	// 总共条数
	private long total;

	// 成功条数
	private long succ;

	private long breakPoint;

	// 失败条数
	private long fail;

	// 失败码
	private long errorCode;

	// 失败原因
	private String cause;

	// 输出目的地
	private String dest;

	// 输出开始时间
	private Date startTime;

	// 输出结束时间
	private Date endTime;

	public int getExportType() {
		return exportType;
	}

	public int getExportId() {
		return exportId;
	}

	public void setExportId(int exportId) {
		this.exportId = exportId;
	}

	public long getTotal() {
		return total;
	}

	public long getSucc() {
		return succ;
	}

	public long getFail() {
		return fail;
	}

	public long getBreakPoint() {
		return breakPoint;
	}

	public void setBreakPoint(long breakPoint) {
		this.breakPoint = breakPoint;
	}

	public long getErrorCode() {
		return errorCode;
	}

	public String getCause() {
		return cause;
	}

	public String getDest() {
		return dest;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setExportType(int exportType) {
		this.exportType = exportType;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public void setSucc(long succ) {
		this.succ = succ;
	}

	public void setFail(long fail) {
		this.fail = fail;
	}

	public void setErrorCode(long errorCode) {
		this.errorCode = errorCode;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "dest=" + this.dest + ",exportId=" + this.exportId + ",exportType=" + this.exportType + ",total=" + this.total + ",succ=" + this.succ
				+ ",fail=" + this.fail + ",breakPoint=" + this.breakPoint + ",errorCode=" + this.errorCode + ",cause=" + this.cause + ",startTime="
				+ this.startTime + ",endTime=" + this.endTime + ",cost=" + (this.endTime.getTime() - this.startTime.getTime()) / 1000L;
	}
}
