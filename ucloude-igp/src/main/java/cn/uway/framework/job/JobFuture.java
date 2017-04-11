package cn.uway.framework.job;

import cn.uway.framework.accessor.AccessorReport;
import cn.uway.framework.parser.ParserReport;
import cn.uway.framework.warehouse.WarehouseReport;

/**
 * 作业执行结果
 * 
 * @author MikeYang
 * @Date 2012-10-30 update by 陈荣强 2012-11-08 增加接入、解析、仓库报表信息
 * @version 1.0
 * @since 3.0
 */
public class JobFuture {

	private int code = 0; // 执行结果码

	private String cause; // 失败原因

	// 接入器报告
	private AccessorReport accessorReport;

	// 解析器报告
	private ParserReport parserReport;

	// 数据仓库报告
	private WarehouseReport warehouseReport;

	public AccessorReport getAccessorReport() {
		return accessorReport;
	}

	public ParserReport getParserReport() {
		return parserReport;
	}

	public WarehouseReport getWarehouseReport() {
		return warehouseReport;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public void setAccessorReport(AccessorReport accessorReport) {
		this.accessorReport = accessorReport;
	}

	public void setParserReport(ParserReport parserReport) {
		this.parserReport = parserReport;
	}

	public void setWarehouseReport(WarehouseReport warehouseReport) {
		this.warehouseReport = warehouseReport;
	}

	/**
	 * 构造操作成功的结果对象
	 */
	public JobFuture() {
		super();
	}

	/**
	 * 构建任务执行的结果对象
	 * 
	 * @param code
	 *            操作结果码
	 * @param cause
	 *            如果失败，可以填写一下原因
	 */
	public JobFuture(int code, String cause) {
		super();
		this.code = code;
		this.cause = cause;
	}

	/**
	 * 获取操作结果码
	 */
	public int getCode() {
		return code;
	}

	/**
	 * 获取失败原因
	 */
	public String getCause() {
		return cause;
	}

}
