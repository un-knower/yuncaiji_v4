package cn.uway.framework.task;

import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

/**
 * 延迟数据任务：“周期任务”达到采集时间点之后到达的数据对应的任务
 * 
 * @author tylerlee @ 2015年10月15日
 */
public class DelayTask extends PeriodTask {

	/* igp_cfg_delay_data_task表的数据 */
	
	/**
	 * 延迟任务的id号
	 */
	protected int delayId;


	/**
	 * 正常“周期任务”已经采集过的文件列表
	 */
	protected List<String> gatherObjs;

	/**
	 * 当前延迟任务扫描时间
	 */
	protected Date dataScanCurrTime;
	
	/**
	 * 当前延迟任务创建时间
	 */
	protected Date createTime;
	

	@Override
	public void loadTask(final ResultSet rs) throws Exception {
		super.loadTask(rs);
		this.setDelayId(rs.getInt("id"));
		this.setDataScanCurrTime(new Date(rs.getTimestamp("data_scan_curr_time").getTime()));
		this.setCreateTime(new Date(rs.getTimestamp("create_time").getTime()));
	}

	/**
	 * 判断时延信息 是否达到采集时间点
	 * 
	 * @return
	 */
	public boolean isReady() {
		// TODO 添加新的业务逻辑
		// return dataTime.getTime() + this.gatherTimeDelay * 60L * 1000L <= new Date().getTime();
		return true;
	}

	@Override
	public String toString() {
		return "DelayTask [delayId="+delayId+", dataScanCurrTime=" + dataScanCurrTime + ", delayDataScanPeriod=" + delayDataScanPeriod + ", delayDataTimeDelay="
				+ delayDataTimeDelay + "]";
	}

	public int getDelayId() {
		return delayId;
	}

	
	public void setDelayId(int delayId) {
		this.delayId = delayId;
	}

	
	public List<String> getGatherObjs() {
		return gatherObjs;
	}

	public void setGatherObjs(List<String> gatherObjs) {
		this.gatherObjs = gatherObjs;
	}

	public Date getDataScanCurrTime() {
		return dataScanCurrTime;
	}

	
	public void setDataScanCurrTime(Date dataScanCurrTime) {
		this.dataScanCurrTime = dataScanCurrTime;
	}

	
	public Date getCreateTime() {
		return createTime;
	}

	
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
