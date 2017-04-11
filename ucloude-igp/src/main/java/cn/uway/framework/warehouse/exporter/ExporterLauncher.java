package cn.uway.framework.warehouse.exporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.status.Status;
import cn.uway.framework.status.dao.StatusDAO;
import cn.uway.framework.task.ReTask;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.GenericWareHouse;
import cn.uway.framework.warehouse.exporter.template.ExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.TimeUtil;
import cn.uway.util.parquet.PWPool;
import cn.uway.util.parquet.PartPool;

/**
 * 输出线程调度器 ExporterLauncher
 * 
 * @author chenrongqiang 2012-12-17
 */
public class ExporterLauncher extends Thread {

	/**
	 * 仓库和输出器参数定义
	 */
	private ExporterArgs exporterArgs;

	private ExecutorService es;

	private CompletionService<ExportFuture> cs;

	private List<Exporter> exporters = new ArrayList<Exporter>();
	
	private List<ExporterGroupDispatcher> groupExporterDispatchers = null;

	Map<Integer, Integer> dataThreadCounter = new HashMap<Integer, Integer>();

	private StatusDAO statusDAO = AppContext.getBean("statusDAO", StatusDAO.class);
	
	// 仓库暂存数据块大小
	private static final String sWareHouseExporterMaxThread = AppContext.getBean("wareHouseExporterMaxThread", String.class);
	protected int maxWareWareHouseExporterThread = 0;

	// 外部是否发生异常
	private boolean exceptionFlag = false;
	
	private static final ILogger LOGGER = LoggerManager.getLogger(ExporterLauncher.class); // 日志

	public ExporterLauncher(ExporterArgs exporterArgs) {
		this.exporterArgs = exporterArgs;
		if (sWareHouseExporterMaxThread.indexOf("$") < 0) {
			try {
				maxWareWareHouseExporterThread = Integer.parseInt(sWareHouseExporterMaxThread);
			} catch (Exception e) {
				maxWareWareHouseExporterThread = 0;
			}
		}
		
		createExporters();
	}

	public synchronized void notifyException() {
		this.exceptionFlag = true;
	}

	/**
	 * 根据输出定义和模版 动态创建exporter
	 */
	void createExporters() {
		List<ExportTemplateBean> exportTempletBeans = exporterArgs.getExportTempletBeans();
		for (ExportTemplateBean templetBean : exportTempletBeans) {
			Integer dataType = templetBean.getDataType();
			Exporter exportor = ExporterFactory.createExporter(templetBean, exporterArgs);
			if (exportor != null) {
				exportor.setDataType(dataType);
				// List 中增加Exporter的一个引用，方便进行迭代和输出,以及线程池调用Exporter
				exporters.add(exportor);
				Integer dataExporterNum = dataThreadCounter.get(dataType);
				if (dataExporterNum == null) {
					dataThreadCounter.put(dataType, 1);
					continue;
				}
				dataThreadCounter.put(dataType, dataExporterNum + 1);
			}
		}
		
		if (exporterArgs.isDispatcher()) {
			// 当限定了输出线程时，将几个Exporter合并到一个分组中并发.
			int exporterNum = exporters.size();
			int threadCount = exporterNum;
			if (maxWareWareHouseExporterThread > 0 && threadCount > maxWareWareHouseExporterThread) {
				threadCount = maxWareWareHouseExporterThread;
			}
			
			// 每个输出线程管理几个Exporter
			int exporterGroupCount = exporterNum / threadCount;
			if (exporterNum % threadCount > 0)
				++exporterGroupCount;
			
			if (exporterGroupCount > 1) {
				groupExporterDispatchers = new ArrayList<ExporterGroupDispatcher> (threadCount);
				int groupID = 0;
				ExporterGroupDispatcher groupDispatcher = null;
				for (int i = 0; i < exporterNum; i++) {
					if ((i % exporterGroupCount)==0) {
						groupDispatcher = new ExporterGroupDispatcher(exporterArgs, groupID++);
						groupExporterDispatchers.add(groupDispatcher);
					}
					groupDispatcher.registerExporter(exporters.get(i));
				}
			}
		}
	}

	@Override
	public void run() {
		int exporterNum = exporters.size();
		if (exporterNum == 0) {
			throw new IllegalArgumentException("ExporterLauncher线程池无法创建，输出失败，请检查输出配置");
		}
		
		int threadCount = groupExporterDispatchers!=null?groupExporterDispatchers.size():exporterNum;
		es = Executors.newFixedThreadPool(threadCount);
		cs = new ExecutorCompletionService<ExportFuture>(es);
		LOGGER.debug("ExporterLauncher线程池创建。");
		
		for (int i = 0; i < threadCount; i++) {
			if (groupExporterDispatchers == null) {
				cs.submit(exporters.get(i));
			} else {
				cs.submit(groupExporterDispatchers.get(i));
			}
		}

		List<ExportReport> exportReports = new ArrayList<ExportReport>();
		for (int i = 0; i < threadCount; i++) {
			try {
				Future<ExportFuture> exportFuture = cs.take();
				if (exportFuture == null)
					continue;
				ExportFuture future = exportFuture.get();
				if (groupExporterDispatchers == null) {
					ExportReport exportReport = future.getExportReport();
					exportReports.add(exportReport);					
				} else {
					List<ExportReport> groupExportReports = future.getGroupExportReports();
					exportReports.addAll(groupExportReports);
				}
			} catch (InterruptedException e) {
				LOGGER.error("输出器线程中断!", e);
			} catch (ExecutionException e) {
				LOGGER.error("线程返回结果处理异常!", e);
			} catch(Exception e){
				LOGGER.error("输出器线程其它异常!", e);
			}
		}
		afterExport(exportReports);
				
		// 输出完毕后关闭线程池
		shutdown();
	}
	
	/**
	 * 输出完成后处理 更新采集记录表输出状态为1[表示输出成功]
	 * 
	 * @param exportReports
	 */
	private void afterExport(List<ExportReport> exportReports) {
		
		String taskfn = "";
		if(exporterArgs.getTask().getWorkerType() == 1)
		{
			taskfn = exporterArgs.getTask().getId() + TimeUtil.getDateString_yyyyMMddHHmmss(exporterArgs.getTask().getDataTime());
		}else
		{
			taskfn = exporterArgs.getTask().getId()+exporterArgs.entryNames.get(0);
		}
		
		LOGGER.debug("wKey. afterExport taskfn:" + taskfn);
		PWPool.removeAll(taskfn);
		PartPool.removeAll(exporterArgs.entryNames.get(0));
		// 通知仓库 当前任务使用输出线程使用warehouse完毕
		Task task = exporterArgs.getTask();
		long taskId = task.getId();
		if (task instanceof ReTask) {
			taskId = ((ReTask) task).getrTaskId();
		}
		GenericWareHouse.getInstance().shutdownNotice(taskId);
		String errorMsg = null;
		for (ExportReport exportReport : exportReports) {
			LOGGER.debug("exportReport=" + exportReport);
			if (exportReport.getCause() != null && errorMsg == null) {
				errorMsg = exportReport.getCause();
			}
		}
		// 如果是延迟入库，全部强制为0，然后在真正入库时根据条件更新为1
		if(exporterArgs.getIsDelayExport()){
			List<Status> status = exporterArgs.getObjStatus();
			int statusNum = status.size();
			for (int i = 0; i < statusNum; i++) {
				LOGGER.debug("延迟入库。statusId:{}",status.get(i).getId());
			}
		}
		// 修改状态记录表 标志输出完成 如果所有Exporter 失败原因均为空，则表示全部成功
		else if (errorMsg == null) {
			List<Status> status = exporterArgs.getObjStatus();
			int statusNum = status.size();
			for (int i = 0; i < statusNum; i++) {
				//statusDAO.updateExportStatus(status.get(i).getId(), exceptionFlag ? 0 : 1);
				status.get(i).updateExportStatusBySynchronized(statusDAO, status.get(i).getId(), exceptionFlag ? Status.EXPORT_ERROR : Status.FINISH_SUCCESS);
			}
		}
	}

	public List<Exporter> getExporters() {
		return exporters;
	}
	
	public List<ExporterGroupDispatcher> getGroupExporterDispatchers() {
		return groupExporterDispatchers;
	}

	void shutdown() {
		if (es != null) {
			es.shutdown();
			LOGGER.debug("ExporterLauncher线程池关闭。");
		}
	}

}
