package cn.uway.framework.warehouse.exporter;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import cn.uway.framework.cache.AbstractCacher;
import cn.uway.framework.connection.HBaseHelper;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.job.LogCdrInsert;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.status.Status;
import cn.uway.framework.status.dao.StatusDAO;
import cn.uway.framework.warehouse.exporter.HBaseExporterTargetTableManager.HBaseTargetTable;
import cn.uway.framework.warehouse.exporter.hbaseExporterConf.HBaseExportDBConf;
import cn.uway.framework.warehouse.exporter.hbaseExporterConf.HBaseExportTableProperty;
import cn.uway.framework.warehouse.exporter.hbaseExporterConf.HBaseExportTableProperty.HBaseExportField;
import cn.uway.framework.warehouse.exporter.hbaseExporterConf.HBaseExportTableProperty.HBaseSubTablePropery;
import cn.uway.framework.warehouse.exporter.hbaseExporterConf.HBaseExportTableProperty.HBaseTabPrimaryKeyProperty;
import cn.uway.framework.warehouse.exporter.template.ColumnTemplateBean;
import cn.uway.framework.warehouse.exporter.template.DatabaseExporterBean;
import cn.uway.framework.warehouse.exporter.template.DbExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.StringUtil;

/**
 * HBASE数据库输出器
 */
public class HBaseExporter extends AbstractExporter {

	/**
	 * 日志
	 */
	protected static final ILogger LOGGER = LoggerManager
			.getLogger(HBaseExporter.class);

	/**
	 * 默认批次条数
	 */
	protected static final int DEFAULT_BATCH_NUM = 1000;

	/**
	 * 每多少条向数据库提交一次。
	 */
	protected int batchNum;

	/**
	 * 记录当前已缓存了多少条。
	 */
	protected int currNum;

	/**
	 * 数据库输出模版
	 */
	protected DbExportTemplateBean dbExportTempletBean;

	/**
	 * 输出字段列表
	 */
	protected List<ColumnTemplateBean> columns;

	/**
	 * 数据库输出类型
	 */
	protected String storageType;

	/**
	 * 输出表名
	 */
	protected String table;

	/**
	 * 状态表操作DAO
	 */
	protected StatusDAO statusDAO = AppContext.getBean("statusDAO",
			StatusDAO.class);


	/**
	 * 缓存文件名
	 */
	protected String cacheFileName;

	/**
	 * 输出总时间
	 */
	protected long totalTime;

	/**
	 * HBase数据服务器连接助手
	 */
	protected HBaseHelper hbaseHelper = null;

	/**
	 * HBASE配置文件
	 */
	protected HBaseExportTableProperty exportTargetTableProperty = null;

	/**
	 * HBase目标表管理器
	 */
	protected HBaseExporterTargetTableManager hbaseExportTargetTableManager = null;

	/**
	 * 默认的列族名Bytes
	 */
	private byte[] defColumnFamilyBytes = Bytes.toBytes("C");

	/**
	 * 默认列族字段名Bytes
	 */
	private byte[] defColumnFamilyFieldBytes = Bytes.toBytes("V");

	/**
	 * 构造函数
	 * 
	 * @param dbExportTempletBean
	 *            数据库输出实体定义
	 * @param exporterArgs
	 *            输出参数
	 */
	public HBaseExporter(DbExportTemplateBean dbExportTempletBean,
			ExporterArgs exporterArgs) {
		super(exporterArgs, dbExportTempletBean.getId());
		this.exportId = dbExportTempletBean.getId();
		setBreakPoint();
		this.dbExportTempletBean = dbExportTempletBean;
		this.dest = dbExportTempletBean.getTable().getTableName().toUpperCase();
		this.table = dest;
		this.exportType = 1;

		this.storageType = dbExportTempletBean.getTable().storageType;
		this.columns = dbExportTempletBean.getTable().getColumnsList();

		initParameter(exporterArgs);

		LOGGER.debug("HBASE输出器初始化完成: dataType="
				+ dbExportTempletBean.getDataType() + ", 目的地:"
				+ dbExportTempletBean.getTable().getTableName() + ",输出开始断点:"
				+ this.breakPoint);
	}

	/**
	 * 创建数据库链接
	 */
	protected void initParameter(ExporterArgs exporterArgs) {
		boolean bErrOccuredFlag = false;
		try {
			while (true) {
				createCacher(AbstractCacher.BLOCK_CACHER);
				DatabaseExporterBean dbTargetBean = (DatabaseExporterBean) dbExportTempletBean
						.getExportTargetBean();
				this.batchNum = dbTargetBean.getBatchNum();

				// 加载本次输出表相关配置文件
				HBaseExportDBConf hbaseExporDBConf = (HBaseExportDBConf) AppContext
						.getBean("HbaseConfig");
				if (hbaseExporDBConf == null) {
					bErrOccuredFlag = true;
					this.cause = "未加载到HBASE数据库输出配置文件";
					LOGGER.error(this.cause);
					break;
				}
				this.exportTargetTableProperty = hbaseExporDBConf
						.getExportTableConf(this.table);
				if (this.exportTargetTableProperty == null) {
					bErrOccuredFlag = true;
					this.cause = "HBASE配置文件中未对表：" + this.table + "作输出配置.";
					LOGGER.error(this.cause);
					break;
				}

				// 连接HBASE数据库
				hbaseHelper = HBaseHelper.getHelper("hbase-site.xml");

				// 如果批量入库条数非法[目前限制最大5000]
				if (batchNum <= 0 || batchNum > 5000)
					batchNum = 5000;
				LOGGER.debug("HBASE每次批量入库条数为：{}，exportId：{}", new Object[]{
						batchNum, dbExportTempletBean.getId()});

				hbaseExportTargetTableManager = new HBaseExporterTargetTableManager(
						hbaseHelper, exportTargetTableProperty, batchNum);

				break;
			}
		} catch (Exception e) {
			bErrOccuredFlag = true;
			this.cause = e.getMessage();
			LOGGER.error("HBASE数据连接初始化失败。", e);
		}

		if (bErrOccuredFlag) {
			exporterInitErrorFlag = true;
			// -1表示初始化失败
			this.errorCode = -1;
		}
	}

	public void export(Map<String, String> record) throws Exception {
		long start = System.currentTimeMillis();
		this.current++;
		if (this.current <= this.breakPoint) {
			this.fail++;
			return;
		}
		try {
			List<HBaseSubTablePropery> indexTabProps = this.exportTargetTableProperty
					.getIndexTableProperty();
			// 入主表
			{
				HBaseSubTablePropery tableProp = this.exportTargetTableProperty
						.getMainTablePropery();

				// 入库表名
				String targetHbaseFullDBName = getTargetHBaseFullTableName(
						record, tableProp);
				if (targetHbaseFullDBName == null) {
					this.fail++;
					return;
				}

				// 获取入库表缓存对象
				HBaseTargetTable hbaseTargetTable = hbaseExportTargetTableManager
						.getHBaseTargetTable(targetHbaseFullDBName);
				if (hbaseTargetTable == null) {
					this.fail++;
					return;
				}

				// 主键
				byte[] rowKey = buildRowKey(record, tableProp);
				if (rowKey == null) {
					this.fail++;
					return;
				}

				// 入库内容(整行变成CSV格式入到一个单元格)
				String cellText = buildCSVFormatLine(record);

				Put put = new Put(rowKey);
				put.add(defColumnFamilyBytes, defColumnFamilyFieldBytes,
						Bytes.toBytes(cellText));
				// 入库附加输出字段
				if (tableProp.exportFields != null && tableProp.exportFields.size()>0) {
					for(HBaseExportField exportField : tableProp.exportFields) {
						String fieldCellText = record.get(exportField.propertyName);
						if (fieldCellText != null && fieldCellText.length() > 0) {
							put.add(defColumnFamilyBytes, exportField.columnFamilyFieldBytes,
									Bytes.toBytes(fieldCellText));
						}
					}
				}
				
				hbaseTargetTable.put(put);
			}

			// 入索引表
			for (HBaseSubTablePropery tableProp : indexTabProps) {
				// 入库表名
				String targetHbaseFullDBName = getTargetHBaseFullTableName(
						record, tableProp);
				if (targetHbaseFullDBName == null)
					continue;

				// 获取入库表缓存对象
				HBaseTargetTable hbaseTargetTable = hbaseExportTargetTableManager
						.getHBaseTargetTable(targetHbaseFullDBName);
				if (hbaseTargetTable == null) {
					this.fail++;
					continue;
				}
				// 主键
				byte[] rowKey = buildRowKey(record, tableProp);
				// 用来建立索引的rowkey不能为null，否则忽略
				if (rowKey == null)
					continue;

				Put put = new Put(rowKey);
				// 索引表不添加任务何内容
				put.add(defColumnFamilyBytes, defColumnFamilyFieldBytes, null);
				// 入库附加输出字段
				if (tableProp.exportFields != null && tableProp.exportFields.size()>0) {
					for(HBaseExportField exportField : tableProp.exportFields) {
						String fieldCellText = record.get(exportField.propertyName);
						if (fieldCellText != null && fieldCellText.length() > 0) {
							put.add(defColumnFamilyBytes, exportField.columnFamilyFieldBytes,
									Bytes.toBytes(fieldCellText));
						}
					}
				}
				hbaseTargetTable.put(put);
			}

			this.currNum++;
			if (this.currNum >= batchNum) {
				this.hbaseExportTargetTableManager.flushCommits();

				this.currNum = 0;
				this.breakPoint += batchNum;
				this.succ += batchNum;
				Status objStatus = breakPointProcess();
//				this.statusDAO.update(objStatus, exporterArgs.getObjStatus()
//						.get(0).getId());
				//hbase不需要实时更新日志表(只要key相同，数据会覆盖)
				//objStatus.updateBySynchronized(statusDAO, exporterArgs.getObjStatus().get(0).getId());
			}
			long end = System.currentTimeMillis();
			totalTime += (end - start);
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < this.columns.size(); i++) {
				String value = record.get(columns.get(i).getPropertyName());
				if (value != null && value.equalsIgnoreCase("nan"))
					value = null;
				sb.append((value != null ? "'" + value + "'" : "null")).append(
						",");
			}
			LOGGER.error(sb.toString());
			this.cause = e.getMessage();
			LOGGER.error("HBASE数据库输出发生异常,入库线程停止,当前断点=" + breakPoint + ",文件："
					+ entryNames.get(0) + "，任务：" + task.getId(), e);
			throw new Exception("HBASE写入数据库失败：" + table, e);
		}
	}

	/**
	 * 断点信息处理
	 * 
	 * @return
	 */
	public Status breakPointProcess() {
		Status objStatus = exporterArgs.getObjStatus().get(0);
		String oldWarehouseBreakPoint = objStatus.getWarehousePoint();
		if (oldWarehouseBreakPoint == null
				|| oldWarehouseBreakPoint.trim().isEmpty()) {
			objStatus.setWarehousePoint(this.exportId + ":" + this.breakPoint
					+ ";");
			return objStatus;
		}
		String regex = this.exportId + ":\\d*";
		String pattern = StringUtil.getPattern(objStatus.getWarehousePoint(),
				regex);
		if (pattern == null || pattern.trim().isEmpty()) {
			oldWarehouseBreakPoint = oldWarehouseBreakPoint + this.exportId
					+ ":" + this.breakPoint + ";";
			objStatus.setWarehousePoint(oldWarehouseBreakPoint);
			return objStatus;
		}
		oldWarehouseBreakPoint = oldWarehouseBreakPoint.replace(pattern,
				this.exportId + ":" + this.breakPoint);
		objStatus.setWarehousePoint(oldWarehouseBreakPoint);
		return objStatus;
	}

	@Override
	public void close() {
		long start = System.currentTimeMillis();
		try {
			// 可能还有未提交的内容，最后要提交一下。
			if (this.hbaseExportTargetTableManager != null) {
				this.hbaseExportTargetTableManager.flushCommits();
				this.hbaseExportTargetTableManager.close();
				this.hbaseExportTargetTableManager = null;
			}
			this.succ += this.currNum;
			this.breakPoint += this.currNum;
			Status status = breakPointProcess();
//			this.statusDAO.update(status, exporterArgs.getObjStatus().get(0)
//					.getId());
			status.updateBySynchronized(statusDAO, exporterArgs.getObjStatus().get(0).getId());
		} catch (Exception e) {
			this.fail += this.currNum;
			LOGGER.error("endExport导入数据库失败", e);
		} finally {
			endTime = new Date();
			
			//优先根据config.ini中的system.logcltinsert.flag进行判断，如果开启（1），   添加日志入库操作 log_clt_insert ,数据库汇总需要依赖此表,    
			//话单入  log_cdr_insert, 则需要将 system.logcltinsert.flag=0 关闭. 
			if (isLogCltInsertFlag()) {
				logCltInsert();
			} else {
				if (this.dbLoggerFlag) {
					writeDbLog();
				}
			}
			long end = System.currentTimeMillis();
			totalTime += (end - start);
			LOGGER.debug(
					"【入库时间统计】{}表入库耗时{}秒，入库成功{}条数据，hbase入库失败{}条数据，{}任务，原始文件：{}，CITY：{}，OMC：{}，BSC：{}，VENDOR：{}",
					new Object[]{
							this.table,
							totalTime / 1000.00,
							this.succ,
							this.fail,
							task.getId(),
							(this.exporterArgs != null ? entryNames.get(0) : ""),
							task.getExtraInfo().getCityId(),
							task.getExtraInfo().getOmcId(),
							task.getExtraInfo().getBscId(),
							task.getExtraInfo().getVendor()});
		}
	}

	private void writeDbLog() {
		LogCdrInsert.getInstance()
				.insert(task.getExtraInfo().getCityId(),
						task.getExtraInfo().getOmcId(),
						task.getExtraInfo().getBscId(), table,
						exporterArgs.getDataTime(), startTime, endTime, succ,
						fail, total, task.getExtraInfo().getVendor(),
						entryNames.get(0));
	}

	@Override
	public void endExportOnException() {
		close();
	}

	@Override
	public void export(BlockData blockData) throws Exception {
		List<ParseOutRecord> outData = blockData.getData();
		if (outData == null || outData.isEmpty())
			return;
		for (ParseOutRecord outRecord : outData) {
			Map<String, String> record = outRecord.getRecord();
			export(record);
		}
	}

	@Override
	public void buildExportPropertysList(Set<String> propertysSet) {
		int colNum = this.columns.size();
		for (int i = 0; i < colNum; i++) {
			ColumnTemplateBean column = this.columns.get(i);
			String prop = column.getPropertyName();
			propertysSet.add(prop);
		}
		
		// 加两个固定的key位置，以便ArrayMap使用
		propertysSet.add("RAW_FILE_KEY1");
		propertysSet.add("RAW_FILE_KEY2");
	}

	/**
	 * 获取完整的HBASE入库表名
	 * 
	 * @param record
	 * @param tableProp
	 * @return
	 * @throws ParseException
	 */
	String getTargetHBaseFullTableName(Map<String, String> record,
			HBaseSubTablePropery tableProp) throws ParseException {
		if (tableProp.splitTabKeyProp != null) {
			String splitTabKeyValue = record
					.get(tableProp.splitTabKeyProp.keyName);
			if (splitTabKeyValue != null && tableProp.splitTabKeyProp != null) {
				splitTabKeyValue = tableProp.splitTabKeyProp
						.transToSplitTabKeyValue(splitTabKeyValue);
			}

			// 如果分表字段的值为NULL,则忽略这条记录
			if (splitTabKeyValue == null)
				return null;

			return tableProp.exportTableName + "_" + splitTabKeyValue;
		}

		return tableProp.exportTableName;
	}

	/**
	 * 创建记录ROWKEY
	 * 
	 * @param record
	 *            输出记录内容
	 * @param tableProp
	 *            HBASE表属性
	 * @return
	 * @throws ParseException
	 */
	byte[] buildRowKey(Map<String, String> record,
			HBaseSubTablePropery tableProp) throws ParseException {
		byte[][] indexKeys = new byte[tableProp.primaryKeys.size()][];
		int i = 0;
		for (HBaseTabPrimaryKeyProperty keyProp : tableProp.primaryKeys) {
			String keyValue = record.get(keyProp.keyName);
			if (keyValue != null) {
				indexKeys[i] = keyProp.transToKeyBytes(keyValue);
				if (indexKeys[i] == null)
					return null;
			} else {
				return null;
			}

			++i;
		}
		
		Integer nRawFileKey1 = null; 
		Integer nRawFileKey2 = null;
		String rawFileKey1 = record.get("RAW_FILE_KEY1");
		String rawFileKey2 = record.get("RAW_FILE_KEY2");
		try {
			if (rawFileKey1 != null) 
				nRawFileKey1 = Integer.parseInt(rawFileKey1);
			if (rawFileKey2 != null) 
				nRawFileKey2 = Integer.parseInt(rawFileKey2);
			
		} catch (NumberFormatException e) {}
		
		if (nRawFileKey1 == null)
			nRawFileKey1 = this.getCityBscKey();
		
		if (nRawFileKey2 == null)
			nRawFileKey2 = this.getRawFileTimeKey();
	
		byte[] rowKey = exportTargetTableProperty.getKeyBuilder().buildKey(
				(byte) tableProp.tabIndex, nRawFileKey1,
				nRawFileKey2, (int) this.current,
				exportTargetTableProperty.getPartitionNum(), indexKeys);
		return rowKey;
	}

	/**
	 * 创建一条输出记录
	 * 
	 * @param record
	 * @return
	 */
	protected String buildCSVFormatLine(Map<String, String> record) {
		StringBuffer stringBuffer = new StringBuffer();
		for (ColumnTemplateBean bean : this.columns) {
			String val = record.get(bean.getPropertyName());
			if (val == null)
				val = "";
			if (val.equalsIgnoreCase("NaN")) {
				val = "";
			}

			if (stringBuffer.length() > 0)
				stringBuffer.append(",");

			stringBuffer.append(val);
		}
		return stringBuffer.toString();
	}

	/**
	 * 获取城市BSC的Key key = cityid * 10000 + bscid，bsc/rnc最大9999
	 * 
	 * @return
	 */
	public int getCityBscKey() {
		return exporterArgs.getTask().getExtraInfo().getCityId() * 10000
				+ exporterArgs.getTask().getExtraInfo().getBscId();
	}

	/**
	 * 获取文件序号
	 * 
	 * @return
	 */
	public int getRawFileTimeKey() {
		/**
		 * 原始文件键值 = 原始文件的时间，精确到分钟。
		 * 
		 * <pre>
		 * 在目前的IGP框架中，如果同一时间有多个文件，会分成一组解码，
		 * 所以不会有多个相同的时间的多个原始文件，在不同的Parser中输出，否则exort生成文件也会出现文件名重复的问题。
		 * </pre>
		 */
		return (int) (exporterArgs.dataTime.getTime() / 1000 / 60);
	}
}
