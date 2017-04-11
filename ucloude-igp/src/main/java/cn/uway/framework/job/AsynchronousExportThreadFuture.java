package cn.uway.framework.job;

import cn.uway.framework.warehouse.WarehouseReport;

/**
 * 输出线程报告，用于异步输出线程
 * 
 * @author chenrongqiang @ 2013-4-29
 */
public class AsynchronousExportThreadFuture {

	private int status = -1;

	private String cause;

	private WarehouseReport warehouseReport;

	public int getStatus() {
		return status;
	}

	public AsynchronousExportThreadFuture() {
	}

	public AsynchronousExportThreadFuture(int status, String cause) {
		this.status = status;
		this.cause = cause;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public WarehouseReport getWarehouseReport() {
		return warehouseReport;
	}

	public void setWarehouseReport(WarehouseReport warehouseReport) {
		this.warehouseReport = warehouseReport;
	}
}
