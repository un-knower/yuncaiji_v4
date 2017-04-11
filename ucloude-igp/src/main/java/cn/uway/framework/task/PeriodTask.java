package cn.uway.framework.task;

import java.sql.ResultSet;
import java.util.Date;

/**
 * 周期性任务
 * 
 * @author chenrongqiang @ 2014-3-29
 */
public class PeriodTask extends Task{

	/**
	 * 周期，单位为分钟
	 */
	protected int periodMinutes;

	/**
	 * 采集时间延迟，单位为分钟
	 */
	protected int gatherTimeDelay;

	/**
	 * 补采时间偏移量，单位为分钟
	 */
	protected int regatherTimeOffsetMinutes;

	/**
	 * 最多采集次数，如果尝试超过这些次数，则会放弃采集
	 */
	protected int maxGatherTime;

	/**
	 * 获取任务周期，单位分钟
	 */
	public int getPeriodMinutes(){
		return periodMinutes;
	}

	public void setPeriodMinutes(int periodMinutes){
		this.periodMinutes = periodMinutes;
	}

	/**
	 * 获取采集延迟时间，单位为分钟
	 */
	public int getGatherTimeDelay(){
		return gatherTimeDelay;
	}

	public void setGatherTimeDelay(int gatherTimeDelay){
		this.gatherTimeDelay = gatherTimeDelay;
	}

	/**
	 * 获取补采时时间偏移量，单位分钟
	 */
	public int getRegatherTimeOffsetMinutes(){
		return regatherTimeOffsetMinutes;
	}

	public void setRegatherTimeOffsetMinutes(int regatherTimeOffsetMinutes){
		this.regatherTimeOffsetMinutes = regatherTimeOffsetMinutes;
	}

	/**
	 * @return the maxGatherTime
	 */
	public int getMaxGatherTime(){
		return maxGatherTime;
	}

	/**
	 * @param maxGatherTime the maxGatherTime to set
	 */
	public void setMaxGatherTime(int maxGatherTime){
		this.maxGatherTime = maxGatherTime;
	}

	@Override
	public void loadTask(final ResultSet rs) throws Exception{
		super.loadTask(rs);
		this.setGatherTimeDelay(rs.getInt("gather_time_delay"));
		this.setPeriodMinutes(rs.getInt("period"));
		this.setMaxGatherTime(rs.getInt("max_gather_count"));
		this.setRegatherTimeOffsetMinutes(rs.getInt("regather_time_offset"));
	}

	/**
	 * 判断时延信息 是否达到采集时间点
	 * 
	 * @return
	 */
	public boolean isReady(){
		return dataTime.getTime() + this.gatherTimeDelay * 60L * 1000L <= new Date().getTime();
	}

	@Override
	public String toString(){
		return "PeriodTask [periodMinutes=" + periodMinutes + ", gatherTimeDelay=" + gatherTimeDelay
				+ ", regatherTimeOffsetMinutes=" + regatherTimeOffsetMinutes + ", maxGatherTime=" + maxGatherTime + "]";
	}
}
