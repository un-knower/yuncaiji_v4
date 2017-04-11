package cn.uway.framework.warehouse.exporter;

import java.util.List;

/**
 * 汇总输出器参数封装类 ExporterSummaryArgs
 * 
 * @author yuyi
 * @date 2014-04-15
 */
public class ExporterSummaryArgs extends ExporterArgs {

	/** 数据类型 **/
	protected int dateType;

	/** 是否补汇，默认为false，即正常采集汇总 **/
	protected boolean isRepair = false;

	/** 大文件列表 **/
	protected List<String> bigFileList;

	/** 索引文件列表 **/
	protected List<String> indexFileList;

	/**
	 * @return dateType 数据类型
	 */
	public int getDateType() {
		return dateType;
	}

	/**
	 * @param dateType
	 *            数据类型
	 */
	public void setDateType(int dateType) {
		this.dateType = dateType;
	}

	/**
	 * @return isRepair <boolean>
	 */
	public boolean isRepair() {
		return isRepair;
	}

	/**
	 * @param isRepair
	 *            <boolean>
	 */
	public void setRepair(boolean isRepair) {
		this.isRepair = isRepair;
	}

	/**
	 * @return bigFileList <List<String>>
	 */
	public List<String> getBigFileList() {
		return bigFileList;
	}

	/**
	 * @param bigFileList
	 *            <List<String>>
	 */
	public void setBigFileList(List<String> bigFileList) {
		this.bigFileList = bigFileList;
	}

	/**
	 * @return indexFileList <List<String>>
	 */
	public List<String> getIndexFileList() {
		return indexFileList;
	}

	/**
	 * @param indexFileList
	 *            <List<String>>
	 */
	public void setIndexFileList(List<String> indexFileList) {
		this.indexFileList = indexFileList;
	}

}
