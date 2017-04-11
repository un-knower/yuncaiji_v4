package cn.uway.framework.warehouse.exporter.template;

/**
 * infoBright file bean
 * 
 * @author yuy
 * @date 2014-01-23
 */
public class InfoBrightFileExportTemplateBean extends FileExportTemplateBean {

	/**
	 * SplitDataFormatBean
	 */
	public SortedDataRule splitDataFormatBean;

	/**
	 * get the quote of splitDataFormatBean
	 * @return splitDataFormatBean
	 */
	public SortedDataRule getSplitDataFormatBean() {
		return splitDataFormatBean;
	}

	/**
	 * set the quote of splitDataFormatBean
	 * @param splitDataFormatBean
	 */
	public void setSplitDataFormatBean(SortedDataRule splitDataFormatBean) {
		this.splitDataFormatBean = splitDataFormatBean;
	}
	
}
