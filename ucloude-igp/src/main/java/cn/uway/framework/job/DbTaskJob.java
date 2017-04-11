package cn.uway.framework.job;

import cn.uway.framework.status.Status;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.exporter.ExporterSummaryArgs;
import cn.uway.framework.warehouse.repository.SyncNoStatusJdbcRepository;
import cn.uway.util.StringUtil;

public class DbTaskJob extends AdaptiveStreamJob {

	public DbTaskJob(JobParam jobParam) {
		super(jobParam);
	}

	@Override
	protected JobFuture afterJob(Status status, boolean jobProcessRet) {
		return new JobFuture(0, "");
	}

	@Override
	protected Status createGatherObjStatus(Task task) {
		Status gatherObjStatus = new Status();
		// 此处用task.getId()，不用rightTaskId，防止向状态表写入过多记录，且便于控制
		gatherObjStatus.setTaskId(task.getId());
		gatherObjStatus.setGatherObj(getPathEntry());
		gatherObjStatus.setPcName(task.getPcName());
		// 周期性任务需要加上数据时间进行判断
		if (task instanceof PeriodTask)
			gatherObjStatus.setDataTime(currentDateTime);
		gatherObjStatus.initDataAccess();
		return gatherObjStatus;
	}

	@Override
	protected Status createGatherObjStatus(Task task, String pathEntry) {
		Status gatherObjStatus = new Status();
		gatherObjStatus = new Status();
		gatherObjStatus.setTaskId(task.getId());
		gatherObjStatus.setGatherObj(StringUtil.getFilename(pathEntry));
		gatherObjStatus.setPcName(task.getPcName());
		// 周期性任务需要加上数据时间进行判断
		if (task instanceof PeriodTask)
			gatherObjStatus.setDataTime(task.getDataTime());
		gatherObjStatus.initDataAccess();
		return gatherObjStatus;
	}

	@Override
	void startExport() {
		exporterArgs = new ExporterSummaryArgs();
		exporterArgs.setExportTempletBeans(exportTemplateBeans);
		exporterArgs.setTask(task);
		exporterArgs.setEntryNames(entryNames);
		exporterArgs.setDataTime(parser.getCurrentDataTime() == null ? jobParam.getPathEntry().getDateTime() : parser.getCurrentDataTime());
		exporterArgs.setObjStatus(statusList);
		((ExporterSummaryArgs) exporterArgs).setRepair(repairJob);
		repository = new SyncNoStatusJdbcRepository(exporterArgs, this.task);
	}
}
