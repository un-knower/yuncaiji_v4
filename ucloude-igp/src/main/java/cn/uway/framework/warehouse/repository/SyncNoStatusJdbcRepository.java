package cn.uway.framework.warehouse.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.log.DBLogger;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.WarehouseReport;
import cn.uway.framework.warehouse.WarehouseReport.TableReport;
import cn.uway.framework.warehouse.exporter.AbstractExporter;
import cn.uway.framework.warehouse.exporter.BlockData;
import cn.uway.framework.warehouse.exporter.Exporter;
import cn.uway.framework.warehouse.exporter.ExporterArgs;
import cn.uway.framework.warehouse.exporter.NoStatusJdbcBatchExport;
import cn.uway.framework.warehouse.exporter.template.DbExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.ExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class SyncNoStatusJdbcRepository implements Repository {

	private static final ILogger LOG = LoggerManager.getLogger(SyncNoStatusJdbcRepository.class);

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

	// 出现“表不存在”这种错误的数据类型，记下来，下一批次开始，不入库了，因为再入库也一样入不进去。
	protected Set<Integer> errorExportDataTypes;

	protected static final int REGITSTER_SIZE = AppContext.getBean("tempRegisterSize", Integer.class);

	public SyncNoStatusJdbcRepository(ExporterArgs exporterArgs, Task task) {
		super();
		this.exporterArgs = exporterArgs;
		this.task = task;
		this.exports = new HashMap<>();
		this.repositoryId = RepositoryIDGenerator.generatId();
		this.cachedRecordsMap = new HashMap<Integer, List<ParseOutRecord>>();
		this.errorExportDataTypes = new HashSet<Integer>();
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
			cachedRecords = new ArrayList<>(REGITSTER_SIZE);
			this.cachedRecordsMap.put(type, cachedRecords);
		}
		cachedRecords.add(outRecord);
		if (cachedRecords.size() < REGITSTER_SIZE) {
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
		this.cachedRecordsMap.clear();
		Map<String, TableReport> tableInfo = new HashMap<String, TableReport>();
		for (List<Exporter> exporterList : this.exports.values()) {
			for (Exporter exporter : exporterList) {
				exporter.close();
				// 20151105 add by tyler.lee for TableReport begin
				AbstractExporter ae = (AbstractExporter) exporter;
				TableReport tr = new TableReport();
				tr.setDataType(ae.getType());
				tr.setTableName(ae.getDest());
				tr.setCause(ae.getCause());
				tr.setStartTime(ae.getStartTime());
				tr.setEndTime(ae.getEndTime());
				tr.setFail(ae.getFail());
				tr.setSucc(ae.getSucc());
				tr.setTotal(ae.getTotal());
				tableInfo.put(ae.getDest(), tr);
				// 20151105 add by tyler.lee for TableReport end
				NoStatusJdbcBatchExport impl = (NoStatusJdbcBatchExport) exporter;
				DBLogger.getInstance().insertMinute(this.task.getId(), this.task.getExtraInfo().getOmcId(), impl.getDest().toUpperCase(),
						this.task.getDataTime(), impl.getSucc(),
						impl.getExporterArgs().getEntryNames() != null ? impl.getExporterArgs().getEntryNames().get(0) : null);
			}
		}
		this.exports.clear();
		this.warehouseReport = new WarehouseReport();
		this.warehouseReport.setStartTime(this.startTime);
		this.warehouseReport.setEndTime(new Date());
		this.warehouseReport.setCause("");
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
		if (this.errorExportDataTypes.contains(type)) {
			LOG.warn("数据类型dataType为{}的数据，上次入库时出现了不可恢复的异常，此次不再尝试入库。", type);
			return 0;
		}
		int currSuccCount = 0;
		List<Exporter> exporterList = this.getExporter(type);
		if (exporterList != null && exporterList.isEmpty()) {
			LOG.error("dataType为{}的数据，无法找到相应的输出目的地。", type);
			return 0;
		}
		BlockData bd;
		try {
			for (Exporter export : exporterList) {
				AbstractExporter impl = ((AbstractExporter) export);
				impl.setTotal(impl.getTotal() + cachedRecords.size());
				bd = new BlockData(cachedRecords, type);
				export.export(bd);
			}
			currSuccCount = cachedRecords.size();
			this.succCount += currSuccCount;
		} catch (Exception e) {
			LOG.warn("入库出错。", e);
			this.failCount += currSuccCount;
			for (Exporter export : exporterList) {
				if (export instanceof NoStatusJdbcBatchExport) {
					NoStatusJdbcBatchExport jdbcExport = (NoStatusJdbcBatchExport) export;
					// ORA-00942：表不存在。
					if (jdbcExport.getLastSQLException() != null) {
						this.errorExportDataTypes.add(type);
						LOG.warn("数据类型dataType={}，templateId={}的数据，本次入库时出现了不可恢复的异常，下次开始将不再尝试入库。", type, jdbcExport.getExportId());
					}
				}
			}
		} finally {
			cachedRecords.clear();
		}
		// 没找到匹配的Exporter，也要把缓存的清空，不然越来越大，产生内存泄漏。
		if (!cachedRecords.isEmpty())
			cachedRecords.clear();
		return currSuccCount;
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

	private List<Exporter> createExporter(int type) {
		List<ExportTemplateBean> exportTempletBeans = exporterArgs.getExportTempletBeans();
		List<Exporter> exporterList = new ArrayList<Exporter>();
		for (ExportTemplateBean templetBean : exportTempletBeans) {
			Integer dataType = templetBean.getDataType();
			if (dataType.intValue() == type) {
				DbExportTemplateBean dbtempletBean = (DbExportTemplateBean) templetBean;
				Exporter exportor = new NoStatusJdbcBatchExport(dbtempletBean, exporterArgs);
				if (exportor != null) {
					exportor.setDataType(dataType);
					AbstractExporter expImpl = (AbstractExporter) exportor;
					if (expImpl != null && expImpl.getStartTime() == null)
						expImpl.setStartTime(new Date());
					exporterList.add(exportor);
				}
			}
		}
		return exporterList;
	}
}
