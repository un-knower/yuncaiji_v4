package cn.uway.framework.warehouse.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.status.Status;
import cn.uway.framework.status.dao.StatusDAO;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.GenericWareHouse;
import cn.uway.framework.warehouse.WarehouseReport;
import cn.uway.framework.warehouse.WarehouseReport.TableReport;
import cn.uway.framework.warehouse.exporter.AbstractExporter;
import cn.uway.framework.warehouse.exporter.BlockData;
import cn.uway.framework.warehouse.exporter.Exporter;
import cn.uway.framework.warehouse.exporter.ExporterArgs;
import cn.uway.framework.warehouse.exporter.ExporterFactory;
import cn.uway.framework.warehouse.exporter.template.ExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class SyncDirectExportRepository implements Repository {

	private static final ILogger LOG = LoggerManager.getLogger(SyncDirectExportRepository.class);
	
	private StatusDAO statusDAO = AppContext.getBean("statusDAO", StatusDAO.class);

	protected Map<Integer, List<Exporter>> exports;

	protected final long repositoryId;

	protected Map<Integer, List<ParseOutRecord>> cachedRecordsMap;

	protected WarehouseReport warehouseReport;

	protected Date startTime;

	protected int succCount;

	protected int failCount;

	protected int totalCount;

	protected Task task;

	protected ExporterArgs exporterArgs;
	
	protected String cause;
	
	// 已经发生错误的exporter列表 如果已经发生错误 则从输出列表中清楚
	private Map<Integer, Exporter> errorExporters = new HashMap<Integer, Exporter>();

	/**
	 * 内存中保存的最大记录条数
	 */
	protected static final int MAX_QUEUE_SIZE = AppContext.getBean(
			"elementsInMemery", java.lang.Integer.class);

	public SyncDirectExportRepository(ExporterArgs exporterArgs) {
		super();
		this.exporterArgs = exporterArgs;
		this.exporterArgs.setDispatcher(false);
		this.task = exporterArgs.getTask();
		this.exports = new HashMap<>();
		this.repositoryId = RepositoryIDGenerator.generatId();
		this.cachedRecordsMap = new HashMap<Integer, List<ParseOutRecord>>();
		this.startTime = new Date();
	}

	@Override
	public long getReposId() {
		return repositoryId;
	}

	@Override
	public int transport(ParseOutRecord[] outRecords) {
		if (outRecords == null || outRecords.length == 0)
			return 0;
		int count = 0;
		for (ParseOutRecord out : outRecords) {
			if (out != null)
				count += this.transport(out);
		}
		return count;
	}

	@Override
	public int transport(ParseOutRecord outRecord) {
		if (outRecord == null || outRecord.getRecord() == null)
			return 0;
		this.totalCount++;
		int type = outRecord.getType();
		List<ParseOutRecord> cachedRecords = this.cachedRecordsMap.get(type);
		if (cachedRecords == null) {
			cachedRecords = new ArrayList<>(MAX_QUEUE_SIZE);
			this.cachedRecordsMap.put(type, cachedRecords);
		}
		cachedRecords.add(outRecord);
		if (cachedRecords.size() < MAX_QUEUE_SIZE) {
			return 0;
		}

		return this.export(type, cachedRecords);
	}

	@Override
	public void commit(boolean exceptionFlag) {
		for (Entry<Integer, List<ParseOutRecord>> entry : this.cachedRecordsMap.entrySet()) {
			int type = entry.getKey();
			List<ParseOutRecord> cachedRecords = entry.getValue();
			this.export(type, cachedRecords);
		}
		
		String errMsg = null;
		this.cachedRecordsMap.clear();
		Map<String, TableReport> tableInfo = new HashMap<String, TableReport>();
		for (List<Exporter> exporterList : this.exports.values()) {
			for (Exporter exporter : exporterList) {
				if (errMsg == null) {
					AbstractExporter expr = (AbstractExporter)exporter;
					errMsg = expr.getCause();
				}
				
				exporter.close();
			}
		}
		
		shutdown(errMsg, exceptionFlag);
		
		this.exports.clear();
		this.warehouseReport = new WarehouseReport();
		this.warehouseReport.setStartTime(this.startTime);
		this.warehouseReport.setEndTime(new Date());
		this.warehouseReport.setCause(this.cause);
		this.warehouseReport.setDistributedNum(String.valueOf(this.totalCount));
		this.warehouseReport.setFail(this.failCount);
		this.warehouseReport.setSucc(this.succCount);
		this.warehouseReport.setTotal(this.totalCount);
		this.warehouseReport.setTableInfo(tableInfo);
		this.failCount = 0;
		this.succCount = 0;
		this.totalCount = 0;

	}

	@Override
	public void rollBack() {
	}

	@Override
	public WarehouseReport getReport() {
		return this.warehouseReport;
	}

	private int export(int type, List<ParseOutRecord> cachedRecords) {
		List<Exporter> exporterList = this.getExporter(type);
		if (exporterList != null && exporterList.isEmpty()) {
			LOG.error("dataType为{}的数据，无法找到相应的输出目的地。", type);
			cachedRecords.clear();
			return 0;
		}
		
		BlockData bd = new BlockData(cachedRecords, type);
		int recordSize = cachedRecords.size();
		Integer exporterId = -1;
		AbstractExporter exporter = null;
		try {
			for (Exporter export : exporterList) {
				try {
					exporter = ((AbstractExporter) export);
					exporterId = export.getExportId();
					if (exporter.breakProcessFlag)
						continue;
					
					exporter.setTotal(exporter.getTotal() + cachedRecords.size());
					export.export(bd);
					this.succCount += recordSize;
				} catch (Exception e) {
					this.failCount += recordSize;
					errorExporters.put(exporterId, export);
					exporter.breakProcess(e.getMessage());
					LOG.warn("任务ID={},仓库ID={},输出模版ID={}输出器发生异常已关闭", new Object[]{task.getId(), this.repositoryId, exporterId}, e);
				}
			}
			
		} catch (Exception e) {
			LOG.warn("入库出错。", e);
		}
		
		cachedRecords.clear();
		
		return recordSize;
	}

	private List<Exporter> getExporter(int type) {
		if (this.exports.containsKey(type))
			return this.exports.get(type);
		List<Exporter> exporterList = this.createExporter(type);
		if (!exporterList.isEmpty()) {
			this.exports.put(type, exporterList);
			return exporterList;
		}
		return null;
	}
	
	/**
	 * 实例化export,只创建指定类型的exporter
	 * @param type
	 * @return
	 */
	private List<Exporter> createExporter(int type) {
		List<ExportTemplateBean> exportTempletBeans = exporterArgs.getExportTempletBeans();
		List<Exporter> exporterList = new ArrayList<Exporter>();
		for (ExportTemplateBean templetBean : exportTempletBeans) {
			Integer dataType = templetBean.getDataType();
			if (dataType.intValue() != type) 
				continue;
		
			Exporter exportor = ExporterFactory.createExporter(templetBean, exporterArgs);
			if (exportor != null) {
				exportor.setDataType(dataType);
				exporterList.add(exportor);
			}
		}
		return exporterList;
	}
	
	public void shutdown(String errorMsg, boolean exceptionFlag) {
		GenericWareHouse.getInstance().shutdownNotice(exporterArgs.getTask().getId());
		
		// 如果是延迟入库，全部强制为0，然后在真正入库时根据条件更新为1
		if(exporterArgs.getIsDelayExport()){
			List<Status> status = exporterArgs.getObjStatus();
			int statusNum = status.size();
			for (int i = 0; i < statusNum; i++) {
				LOG.debug("延迟入库。statusId:{}",status.get(i).getId());
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
}
