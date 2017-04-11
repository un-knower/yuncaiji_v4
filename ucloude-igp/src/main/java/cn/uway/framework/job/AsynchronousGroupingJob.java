package cn.uway.framework.job;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.context.Vendor;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.status.Status;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.MultiElementGatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.GenericWareHouse;
import cn.uway.framework.warehouse.WarehouseReport;
import cn.uway.framework.warehouse.exporter.ExporterArgs;
import cn.uway.framework.warehouse.repository.BufferedMultiExportRepository;
import cn.uway.framework.warehouse.repository.Repository;
import cn.uway.util.StringUtil;

/**
 * 单Job多个采集对象实现类<br>
 * 一组采集对象同时处理，其中一个失败 则整体失败<br>
 * 
 * @author chenrongqiang @ 2013-4-29
 */
public class AsynchronousGroupingJob extends AbstractJob {

	// 异步提取线程 区别于AbstractJob 数据的提取parser.next()由单独的线程调用
	protected AsynchronousExportThread asynchronousExportThread = null;

	// 输出执行线程池
	protected CompletionService<AsynchronousExportThreadFuture> jobPool;

	protected ExecutorService es;

	/**
	 * @param jobParam
	 */
	public AsynchronousGroupingJob(JobParam jobParam) {
		super(jobParam);
	}

	public JobFuture call() {
		LOGGER.debug("AsynchronousGroupingJob线程开始。");
		Task task = jobParam.getTask();
		try {
			createExportTemplate();
		} catch (Exception e) {
			LOGGER.error("初始化数据库输出目的地失败", e);
			return new JobFuture(-1, "[初始化数据库输出目的地失败]");
		}
		accessBefore();
		AccessOutObject accessOutObject = null;
		LOGGER.debug("[access start]");
		// pathEntry是MultiElementGatherPathEntry 所以包含多个采集实体
		GatherPathEntry pathEntry = this.jobParam.getPathEntry();
		List<String> pathEntrys = null;
		boolean exceptionFlag = false;
		// 爱立信的文件是15分钟一组文件，15分钟文件处理一次。
		if (Vendor.VENDOR_ERICSSON.equalsIgnoreCase(this.task.getExtraInfo().getVendor())) {
			MultiElementGatherPathEntry multiElementGatherPathEntry = (MultiElementGatherPathEntry) pathEntry;
			pathEntrys = multiElementGatherPathEntry.getGatherPaths();
			int entryNum = pathEntrys.size();
			LOGGER.debug("本次异步解码线程共有{}个文件需要处理", entryNum);
			for (int i = 0; i < entryNum; i++) {
				String path = pathEntrys.get(i);
				try {
					LOGGER.debug("开始处理{}个文件，总共{}个文件", new Object[]{i + 1, entryNum});
					accessor.setConnectionInfo(jobParam.getConnInfo());
					accessOutObject = accessor.access(new GatherPathEntry(path));					
					accessOutObject.setTask(task);
					
					parser.parse(accessOutObject);
					// 创建采集对象 此时会进行状态更新 入库等
					Status gatherObj = this.createGatherObjStatus(task, path);
					statusList.add(gatherObj);
					entryNames.add(StringUtil.getFilename(path));
					// 如果输出线程未启动 则启动
				} catch (Exception e) {
					// 分组采集 异常可能在其中任何一个文件发生 accessor是同一个 parser里面不知道外部状态
					// 必须实现destory方法释放一些内部的资源
					exceptionFlag = true;
					parser.destory();
					LOGGER.error("AsynchronousGroupingJob异常,当前处理文件={}", path, e);
					return new JobFuture(-1, "[AsynchronousGroupingJob]异常");
				} finally {
					parser.close();
					accessor.close();
				}
			}
			startExport();
			try {
				parser.after();
			} catch (Exception e) {
				LOGGER.error("AsynchronousGroupingJob调用parser.after()异常.",  e);
			}
		} else {
			String path = pathEntry.getPath();
			try {
				LOGGER.debug("开始处理{}文件。", new Object[]{path});
				accessor.setConnectionInfo(jobParam.getConnInfo());
				accessOutObject = accessor.access(pathEntry);
				parser.parse(accessOutObject);
				// 创建采集对象 此时会进行状态更新 入库等
				Status gatherObj = this.createGatherObjStatus(task, path);
				statusList.add(gatherObj);
				entryNames.add(StringUtil.getFilename(path));
				// 如果输出线程未启动 则启动
				startExport();
				parser.after();
			} catch (Exception e) {
				// 分组采集 异常可能在其中任何一个文件发生 accessor是同一个 parser里面不知道外部状态
				// 必须实现destory方法释放一些内部的资源
				exceptionFlag = true;
				parser.destory();
				LOGGER.error("AsynchronousGroupingJob异常,当前处理文件={}", path, e);
				return new JobFuture(-1, "[AsynchronousGroupingJob]异常");
			} finally {
				parser.close();
				accessor.close();
			}
		}
		// 在parse完成后再启动输出 否则线程会很长时间空跑
		WarehouseReport warehouseReport = null;
		try {
			AsynchronousExportThreadFuture asynchronousExportThreadFuture = jobPool.take().get();
			warehouseReport = asynchronousExportThreadFuture.getWarehouseReport();
			asynchronousExportThread.commit(exceptionFlag);
			stopExport();
		} catch (Exception e) {
			LOGGER.debug("[AsynchronousExportThread]异常", e);
			return new JobFuture(-1, "[AsynchronousExportThread]异常");
		}
		JobFuture jobFuture = afterJob(warehouseReport, true);
		return jobFuture;
	}

	private JobFuture afterJob(WarehouseReport warehouseReport, boolean jobProcessRet) {
		int size = statusList.size();
		JobFuture jobFuture = null;
		for (int i = 0; i < size; i++) {
			Status status = statusList.get(i);
			jobFuture = dealReport(warehouseReport, status, jobProcessRet);
			//statusDAO.update(status, status.getId());
			status.updateBySynchronized(statusDAO, status.getId());
		}
		return jobFuture;
	}

	/**
	 * @param task
	 * @return
	 */
	protected Status createGatherObjStatus(Task task, String pathEntry) {
		Status gatherObjStatus = new Status();
		gatherObjStatus = new Status();
		gatherObjStatus.setTaskId(task.getId());
		gatherObjStatus.setGatherObj(StringUtil.getFilename(pathEntry));
		gatherObjStatus.setPcName(task.getPcName());
		// 周期性任务需要加上数据时间进行判断
		if (task instanceof PeriodTask)
			gatherObjStatus.setDataTime(task.getDataTime());
		gatherObjStatus = objStatusInitialize(gatherObjStatus);
		gatherObjStatus.initDataAccess();
		return gatherObjStatus;
	}

	protected void stopExport() {
		if (this.es != null) {
			this.es.shutdown();
			this.es = null;
			this.jobPool = null;
		}
	}

	protected void startExport() {
		if (asynchronousExportThread != null)
			return;
		asynchronousExportThread = new AsynchronousExportThread();
		es = Executors.newFixedThreadPool(1);
		jobPool = new ExecutorCompletionService<AsynchronousExportThreadFuture>(es);
		jobPool.submit(asynchronousExportThread);
		LOGGER.debug("AsynchronousGroupingJob异步提取线程启动");
	}

	class AsynchronousExportThread implements Callable<AsynchronousExportThreadFuture> {

		Repository repository = null;

		public AsynchronousExportThreadFuture call() throws Exception {
			LOGGER.debug("[AsynchronousExportThreadFuture]开始:");
			createRepository();
			AsynchronousExportThreadFuture exportFuture = new AsynchronousExportThreadFuture();
			if (parser == null)
				return exportFuture;
			while (parser.hasNextRecord()){
				ParseOutRecord outElement = null;
				try {
					outElement = parser.nextRecord();
				} catch (Exception e) {
					LOGGER.error("解析一条记录异常。", e);
					continue;
				}
				distribute(outElement);
			}
			exportFuture.setStatus(0);
			exportFuture.setWarehouseReport(repository.getReport());
			LOGGER.debug("[AsynchronousExportThreadFuture]结束:");
			return exportFuture;
		}

		public void createRepository() {
			if (repository == null)
				repository = new BufferedMultiExportRepository(createExportArgs());
			// 向warehouse注册一个使用
			GenericWareHouse.getInstance().applyNotice(jobParam.getTask().getId());
		}

		/**
		 * 输出提交方法
		 * 
		 * @param exceptionFlag
		 */
		public void commit(boolean exceptionFlag) {
			repository.commit(exceptionFlag);
		}

		/**
		 * 创建ExporterArgs
		 * 
		 * @return
		 */
		public ExporterArgs createExportArgs() {
			ExporterArgs exporterArgs = new ExporterArgs();
			exporterArgs.setExportTempletBeans(exportTemplateBeans);
			exporterArgs.setTask(jobParam.getTask());
			exporterArgs.setEntryNames(entryNames);
			// 文件名中的时间
			exporterArgs.setDataTime(parser.getCurrentDataTime());
			exporterArgs.setObjStatus(statusList);
			return exporterArgs;
		}

		/**
		 * 分发ParseOutRecord至仓库
		 * 
		 * @param outElement
		 */
		public void distribute(ParseOutRecord outElement) {
			if (outElement != null)
				repository.transport(outElement);
		}
	}

	@Override
	public void beforeAccess(String beforeAccessShell) {
	}

	@Override
	public void beforeParse(String beforeParseShell) {
	}
}
