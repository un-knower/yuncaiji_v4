package cn.uway.usummary.entity;

import java.util.Date;


public class WarehouseReport {

	/**
	 * 开始时间
	 */
	private Date startTime;

	/**
	 * 结束时间
	 */
	private Date endTime;

	/**
	 * 总条数
	 */
	private long total;

	/**
	 *  成功条数
	 */
	private long succ;

	/**
	 *  失败条数
	 */
	private long fail;

	/**
	 *  分发至输出模块记录条数 多个数据类型用;隔开
	 */
	private String distributedNum;
	
	private int errCode = 1;

	/**
	 *  失败原因
	 */
	private String cause;
	

	/*getters and setters*/
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

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public String getDistributedNum() {
		return distributedNum;
	}

	public void setDistributedNum(String distributedNum) {
		this.distributedNum = distributedNum;
	}

	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}
	
	
	
}
