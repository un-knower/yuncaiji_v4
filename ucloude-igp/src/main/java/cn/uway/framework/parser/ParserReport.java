package cn.uway.framework.parser;

import java.util.Date;

/**
 * ParserReport 解析器报表
 * 
 * @author chenrongqiang 2012-11-8
 */
public class ParserReport {

	// 失败原因
	private String cause;

	// 接入开始时间
	private Date startTime;

	// 接入结束时间
	private Date endTime;

	// 解析后总条数
	private long totalNum;

	private long invalideNum;

	// 解析失败总条数
	private long parseFailNum;

	// 解析成功总条数
	private long parseSucNum;

	// 文件行数。
	private long fileLines;

	public long getInvalideNum() {
		return invalideNum;
	}

	public void setInvalideNum(long invalideNum) {
		this.invalideNum = invalideNum;
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

	public long getTotalNum() {
		return totalNum;
	}

	public long getParseFailNum() {
		return parseFailNum;
	}

	public long getParseSucNum() {
		return parseSucNum;
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

	public void setTotalNum(long totalNum) {
		this.totalNum = totalNum;
	}

	public void setParseFailNum(long parseFailNum) {
		this.parseFailNum = parseFailNum;
	}

	public void setParseSucNum(long parseSucNum) {
		this.parseSucNum = parseSucNum;
	}

	public long getFileLines() {
		return fileLines;
	}

	public void setFileLines(long fileLines) {
		this.fileLines = fileLines;
	}

	public String toString() {
		return "startTime=" + this.startTime + ",endTime=" + this.endTime + ",total=" + this.totalNum + ",cost="
				+ (this.endTime.getTime() - this.startTime.getTime()) / 1000L;
	}
}
