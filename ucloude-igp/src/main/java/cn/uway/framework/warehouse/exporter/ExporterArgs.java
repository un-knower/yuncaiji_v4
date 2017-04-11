package cn.uway.framework.warehouse.exporter;

import java.util.Date;
import java.util.List;

import cn.uway.framework.status.Status;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.exporter.template.ExportTemplateBean;
import cn.uway.framework.warehouse.repository.Repository;

/**
 * 输出器参数封装类 ExporterArgs
 * 
 * @author chenrongqiang 2012-11-21
 */
public class ExporterArgs {

	// 解析模版 用于仓库创建输出器
	protected List<ExportTemplateBean> exportTempletBeans;

	/**
	 * 任务信息
	 */
	protected Task task;

	// 数据时间
	protected Date dataTime;

	// 采集对象名
	protected List<String> entryNames;

	// 采集对象名对应的数据库状态记录
	protected List<Status> objStatus;
	// 仓库名
	protected Repository repository;
	// 是否是自调度器
	protected boolean dispatcher = true;
	
	// 是否是延迟入库（hadoop入库是现在内存中缓存，当达到指定大小后持久化到磁盘。在exporter中指定）
	protected Boolean isDelayExport = false;

	public List<ExportTemplateBean> getExportTempletBeans() {
		return exportTempletBeans;
	}

	public void setExportTempletBeans(List<ExportTemplateBean> exportTempletBeans) {
		this.exportTempletBeans = exportTempletBeans;
	}

	public Date getDataTime() {
		return dataTime;
	}

	public void setDataTime(Date dataTime) {
		this.dataTime = dataTime;
	}

	public List<String> getEntryNames() {
		return entryNames;
	}

	public void setEntryNames(List<String> entryNames) {
		this.entryNames = entryNames;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public List<Status> getObjStatus() {
		return objStatus;
	}

	public void setObjStatus(List<Status> objStatus) {
		this.objStatus = objStatus;
	}
	
	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public Boolean getIsDelayExport() {
		return isDelayExport;
	}

	public void setIsDelayExport(Boolean isDelayExport) {
		this.isDelayExport = isDelayExport;
	}
	
	public boolean isDispatcher() {
		return dispatcher;
	}
	
	public void setDispatcher(boolean dispatcher) {
		this.dispatcher = dispatcher;
	}
}
