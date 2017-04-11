package cn.uway.framework.warehouse.exporter;

import java.util.List;


/**
 * ExportFuture 返回Exporter线程报告
 * 
 * @author chenrongqiang 2012-10-31
 */
public class ExportFuture {

	/**
	 * Exporter线程报告对象
	 */
	private ExportReport exportReport;
	private List<ExportReport> groupExportReports;
	
	public ExportReport getExportReport() {
		return exportReport;
	}

	public void setExportReport(ExportReport exportReport) {
		this.exportReport = exportReport;
	}
	
	public List<ExportReport> getGroupExportReports() {
		return groupExportReports;
	}
	
	public void setGroupExportReports(List<ExportReport> exportReports) {
		this.groupExportReports = exportReports;
	}
	
}
