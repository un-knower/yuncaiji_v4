package cn.uway.framework.warehouse.exporter.template;

/**
 * 归类数据bean(北京/河北infoBright入库数据按时间分类)
 * 
 * @author yuy 
 * @date 2014-01-23
 */
public class SortedDataRule {

	/**
	 * 数据归类关键字，必须是时间字段
	 */
	public String timeKey;

	/**
	 * 数据分离维度，月/天
	 */
	public String dimension;

	/**
	 * 开始归类时间
	 */
	public String beginTime;

	/**
	 * 结束归类时间
	 */
	public String endTime;
	
	/**
	 * 中间时间即分类时间点
	 */
	public String middleTime;

	/**
	 * get value of timeKey
	 * @return timeKey
	 */
	public String getTimeKey() {
		return timeKey;
	}

	/**
	 * set value of timeKey
	 * @param timeKey
	 */
	public void setTimeKey(String timeKey) {
		this.timeKey = timeKey;
	}

	/**
	 *  get value of dimension
	 * @return
	 */
	public String getDimension() {
		return dimension;
	}

	/**
	 * set value of dimension
	 * @param dimension
	 */
	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	/**
	 * get value of beginTime
	 * @return beginTime
	 */
	public String getBeginTime() {
		return beginTime;
	}

	/**
	 * set value of beginTime
	 * @param beginTime
	 */
	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}

	/**
	 * get value of endTime
	 * @return endTime
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * set value of endTime
	 * @param endTime
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	/**
	 * get value of middleTime
	 * @return middleTime
	 */
	public String getMiddleTime() {
		return middleTime;
	}

	/**
	 * set value of middleTime
	 * @param middleTime
	 */
	public void setMiddleTime(String middleTime) {
		this.middleTime = middleTime;
	}

}
