package cn.uway.framework.task;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.Date;

import cn.uway.util.DbUtil;
import cn.uway.util.StringUtil;

/**
 * 补采任务实体定义
 * 
 * @author chenrongqiang @ 2014-3-29
 */
/**
 * @author yuy @ 6 May, 2014
 */
public class ReTask extends PeriodTask {

	/**
	 * 补采任务ID，此ID是补采表中的ID列。
	 */
	private long rTaskId;

	/**
	 * 补采路径或者templetIds
	 */
	private String regather_path;

	/**
	 * 补采时间点
	 */
	private Date regather_datetime;

	/**
	 * 已补采次数
	 */
	private int times;

	/**
	 * 补采状态 1：成功；2：失败
	 */
	private int status;

	/**
	 * 成功补采时间
	 */
	private Date successDate;

	/**
	 * 采集失败原因
	 */
	private String cause;

	/**
	 * 成功采集状态定义
	 */
	public static final int SUCCESS_COLLECT_STATUS = 1;

	/**
	 * 失败采集状态定义
	 */
	public static final int FAIL_COLLECT_STATUS = 0;

	public ReTask() {
		super();
	}

	public ReTask(long taskId, String regather_path, Date regather_datetime, String cause) {
		super();
		super.id = taskId;
		this.regather_path = regather_path;
		this.regather_datetime = regather_datetime;
		this.cause = cause;
	}

	@Override
	public void loadTask(final ResultSet rs) throws Exception {
		super.loadTask(rs);
		this.setrTaskId(rs.getInt("id"));
		ResultSetMetaData resultSetMetaData = rs.getMetaData();
		int index = findCloumnIndex(resultSetMetaData, "regather_path");
		if (-1 != index && (resultSetMetaData.getColumnType(index) == Types.BLOB || resultSetMetaData.getColumnType(index) == Types.CLOB)) {
			this.setRegatherPath(DbUtil.ClobParse(rs.getClob("regather_path")));
		} else {
			this.setRegatherPath(rs.getString("regather_path"));
		}
		// 采集路径为空时，表示补采全部。
		if (StringUtil.isEmpty(this.getRegatherPath())) {
			this.setRegatherPath(super.getGatherPathDescriptor().getRawData());
		}

		this.setRegather_datetime(rs.getTimestamp("regather_datetime"));
		this.setTimes(rs.getInt("times"));
		this.setStatus(rs.getInt("status"));
		this.setSuccessDate(rs.getDate("success_date"));
		this.setCause(rs.getString("cause"));
	}

	private int findCloumnIndex(ResultSetMetaData resultSetMetaData, String name) throws Exception {
		int count = resultSetMetaData.getColumnCount();
		for (int i = 1; i <= count; i++) {
			String columnName = resultSetMetaData.getColumnName(i);
			if (columnName.equalsIgnoreCase(name)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean isReady() {
		// 数据时间加上采集时延和补采偏移时间 是否已经达到当前的时间
		return regather_datetime.getTime() + (this.gatherTimeDelay + this.regatherTimeOffsetMinutes * (times + 1)) * 60 * 1000 <= new Date()
				.getTime();
	}

	/**
	 * @return the rTaskId
	 */
	public long getrTaskId() {
		return rTaskId;
	}

	/**
	 * @param rTaskId
	 *            the rTaskId to set
	 */
	public void setrTaskId(long rTaskId) {
		this.rTaskId = rTaskId;
	}

	/**
	 * @return path
	 */
	public String getRegatherPath() {
		return regather_path;
	}

	@Override
	public GatherPathDescriptor getGatherPathDescriptor() {
		return new GatherPathDescriptor(this.getRegatherPath());
	}

	/**
	 * @param path
	 */
	public void setRegatherPath(String regather_path) {
		this.regather_path = regather_path;
	}

	/**
	 * @return regather_datetime
	 */
	public Date getRegather_datetime() {
		return regather_datetime;
	}

	/**
	 * @param regather_datetime
	 */
	public void setRegather_datetime(Date regather_datetime) {
		this.regather_datetime = regather_datetime;
	}

	@Override
	public Date getDataTime() {
		return this.getRegather_datetime();
	}

	/**
	 * successDate
	 * 
	 * @return the times
	 */
	public int getTimes() {
		return times;
	}

	/**
	 * @param times
	 *            the times to set
	 */
	public void setTimes(int times) {
		this.times = times;
	}

	/**
	 * @return status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return successDate
	 */
	public Date getSuccessDate() {
		return successDate;
	}

	/**
	 * @param successDate
	 */
	public void setSuccessDate(Date successDate) {
		this.successDate = successDate;
	}

	/**
	 * @return cause
	 */
	public String getCause() {
		return cause;
	}

	/**
	 * @param cause
	 */
	public void setCause(String cause) {
		this.cause = cause;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (rTaskId ^ (rTaskId >>> 32));
		result = prime * result + ((regather_datetime == null) ? 0 : regather_datetime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReTask other = (ReTask) obj;
		if (rTaskId != other.rTaskId)
			return false;
		if (regather_datetime == null) {
			if (other.regather_datetime != null)
				return false;
		} else if (!regather_datetime.equals(other.regather_datetime))
			return false;
		return true;
	}

}
