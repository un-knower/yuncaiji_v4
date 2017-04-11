package cn.uway.framework.warehouse.exporter;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.framework.connection.pool.database.DbPoolManager;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.external.AbstractCache;
import cn.uway.framework.log.SummaryDBLogger;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.solution.GatherSolution;
import cn.uway.framework.solution.SolutionLoader;
import cn.uway.framework.warehouse.exporter.template.ColumnTemplateBean;
import cn.uway.framework.warehouse.exporter.template.DatabaseExporterBean;
import cn.uway.framework.warehouse.exporter.template.DbExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.ExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.DbUtil;
import cn.uway.util.StringUtil;
import cn.uway.util.TimeUtil;

/**
 * 汇总数据输出 增加网元关联
 * 
 * @author yuy @ 11 Apr, 2014
 */
public class SummaryDataJdbcExporter extends PooledFaultTolerantJdbcExporter {

	/** 日志 **/
	private static final ILogger LOGGER = LoggerManager.getLogger(SummaryDataJdbcExporter.class);

	/** MME_ID **/
	public String[] mmeIds;

	/** groupBy关键字 **/
	public String[] groupByKeys;

	/** 网元缓存 **/
	public AbstractCache neInfoCache;

	/** 数据源标识，用于组装关联网元keys **/
	public static String SOURCEFLAG = "SOURCE";

	/** 汇总输出器参数封装类实例 **/
	public ExporterSummaryArgs exporterSummaryArgs;

	public List<ParseOutRecord> notRelatedRecords;

	public SummaryDataJdbcExporter(DbExportTemplateBean dbExportTempletBean, ExporterSummaryArgs exporterArgs) {
		super(dbExportTempletBean, exporterArgs);
		this.exporterSummaryArgs = exporterArgs;
	}

	/**
	 * 验证（网元信息、汇总模板）
	 */
	public boolean validate() {
		neInfoCache = AppContext.getBean("neInfoCache", AbstractCache.class);
		if (neInfoCache != null && neInfoCache.isNotEmpty()) {
			this.mmeIds = getMmeids();
			this.groupByKeys = getGroupByKeys();
			// 验证是否汇总
			if (!isSummary()) {
				LOGGER.error("OracleJdbcSummaryExporter创建失败，原因：", new Exception("不是汇总输出，配置有误"));
				return false;
			}
		} else {
			LOGGER.error("OracleJdbcSummaryExporter创建失败，原因：", new Exception("网元信息(neInfoCache)为空"));
			return false;
		}
		return true;
	}

	/**
	 * 获取需要汇总的mmeIds
	 * 
	 * @return String[]
	 */
	protected String[] getMmeids() {
		String[] strArray = null;
		String str = AppContext.getBean("mmeIds", String.class);
		str = str.replace("{", "").replace("}", "");
		strArray = StringUtil.split(str, ",");
		return strArray;
	}

	/**
	 * 获取groupBy字段
	 * 
	 * @return String[]
	 */
	protected String[] getGroupByKeys() {
		String[] strArray = null;
		GatherSolution solution = SolutionLoader.getSolution(task);
		List<ExportTemplateBean> exportTempletBeans = solution.getExportDefinition().getExportTemplatePojo().getExportTemplates();
		for (ExportTemplateBean templetBean : exportTempletBeans) {
//			if (dbExportTempletBean.getDataType() == templetBean.getDataType() && !ExportTemplate.isDBExoprt(templetBean.getType())) {
//				SummaryFileExportTemplateBean fileExportTemplateBean = (SummaryFileExportTemplateBean) templetBean;
//				strArray = StringUtil.split(fileExportTemplateBean.getGroupBy(), ",");
//				break;
//			}
		}
		return strArray;
	}

	/**
	 * 验证是否汇总
	 */
	protected boolean isSummary() {
		if (mmeIds == null || mmeIds.length == 0)
			return false;
		if (groupByKeys == null || groupByKeys.length == 0)
			return false;
		for (String mmeId : mmeIds) {
			if (mmeId.equals(String.valueOf(task.getExtraInfo().getOmcId()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * delete原有记录
	 */
	public void deleteRecords() throws SQLException {
		long start = System.currentTimeMillis();
		StringBuilder deleteSql = new StringBuilder();
		deleteSql.append(" delete from ").append(table).append(" where MME_ID = ? and STAMPTIME = ?");
		DatabaseExporterBean dbTargetBean = (DatabaseExporterBean) dbExportTempletBean.getExportTargetBean();
		DatabaseConnectionInfo connectionInfo = dbTargetBean.getConnectionInfo();
		Connection con = DbPoolManager.getConnection(connectionInfo);
		PreparedStatement statement = null;
		try {
			con.setAutoCommit(false);
			statement = con.prepareStatement(deleteSql.toString());
			statement.setInt(1, exporterArgs.getTask().getExtraInfo().getOmcId());
			statement.setTimestamp(2, new Timestamp(exporterArgs.getDataTime().getTime()));
			statement.executeUpdate();
			con.commit();
		} catch (SQLException e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				LOGGER.error("回滚失败", e);
			}
			LOGGER.error("补汇时删除记录失败，MME_ID = " + exporterArgs.getTask().getExtraInfo().getOmcId() + "，STAMPTIME = " + exporterArgs.getDataTime(), e);
		} finally {
			DbUtil.close(null, statement, con);
			long time = System.currentTimeMillis() - start;
			LOGGER.debug("补汇时删除记录成功，耗时{}s,MME_ID = {},STAMPTIME = {}。", new Object[]{time / 1000, exporterArgs.getTask().getExtraInfo().getOmcId(),
					exporterArgs.getDataTime()});
		}
	}

	@Override
	public void export(BlockData blockData) throws Exception {
		List<ParseOutRecord> outData = blockData.getData();
		if (outData == null || outData.isEmpty())
			return;
		// 将数据加入到临时批量缓存中
		for (ParseOutRecord outRecord : outData) {
			this.current++;
			if (this.current <= this.breakPoint) {
				this.fail++;
				continue;
			}
			if (cacheElements.size() <= batchNum - 1) {
				cacheElements.add(outRecord);
			} else {
				exportBatch(cacheElements);
				this.breakPoint += batchNum;
				this.succ += batchNum;
				cacheElements = new ArrayList<ParseOutRecord>(batchNum);
				cacheElements.add(outRecord);
			}
		}
	}

	/**
	 * 将数据添加到批处理队列中
	 * 
	 * @param record
	 * @param statement
	 * @throws SQLException
	 * @throws ParseException
	 */
	public void exportBatch(List<ParseOutRecord> records) throws SQLException {
		if (records == null || records.isEmpty())
			return;
		long start = System.currentTimeMillis();
		Map<String, String> record = null;
		DatabaseExporterBean dbTargetBean = (DatabaseExporterBean) dbExportTempletBean.getExportTargetBean();
		DatabaseConnectionInfo connectionInfo = dbTargetBean.getConnectionInfo();
		Connection con = DbPoolManager.getConnection(connectionInfo);
		this.statement = con.prepareStatement(sql);
		con.setAutoCommit(false);
		try {
			for (ParseOutRecord element : records) {
				record = element.getRecord();
				// 关联网元
				if (!relateNeInfo(record, dbExportTempletBean.getSummaryDataType())) {
					if (notRelatedRecords == null)
						notRelatedRecords = new ArrayList<ParseOutRecord>();
					notRelatedRecords.add(element);
					this.succ--;
					this.fail++;
					continue;
				}
				int colNum = this.columns.size();
				for (int i = 0; i < colNum; i++) {
					ColumnTemplateBean column = this.columns.get(i);
					String prop = column.getPropertyName();
					String value = record.get(prop);
					if (value == null || value.equals("null"))
						value = "";
					if (value != null && value.equalsIgnoreCase("nan"))
						value = null;
					if (column.getFormat() == null) {
						statement.setString(i + 1, value);
						continue;
					}
					if (column.getFormat().equals("blob")) {// 处理大字段blob
						statement.setBlob(i + 1, handleBlob(con, value));
						continue;
					}  else {
						// 处理时间字符串中包含T，如'1980-01-06T00:00:19'
						if (value != null && value.indexOf("T") > -1)
							value = value.replace("T", " ");
					}
					
					if (column.getFormat().equals("Timestamp")) {// 处理Timestamp
						statement.setTimestamp(i + 1, new Timestamp(TimeUtil.getDate(value).getTime()));
						continue;
					}
					statement.setString(i + 1, value);
				}
				statement.addBatch();
			}
			// 批量提交一次.成功后更新断点表
			statement.executeBatch();
			con.commit();
		} catch (Exception e) {
			// 如果提交失败.则事务回滚,采用折半法查找异常的数据记录<br>
			con.rollback();
			DbUtil.close(null, statement, con);
			// 没关联上的移除
			if (notRelatedRecords != null && notRelatedRecords.size() > 0) {
				records.removeAll(notRelatedRecords);
				notRelatedRecords.clear();
				notRelatedRecords = null;
			}
			if (records.size() > 1) {
				onException(records);
				return;
			}
			StringBuilder sb = new StringBuilder();
			if (record != null) {
//				for (int i = 0; i < this.props.size(); i++) {
//					String col = this.props.get(i);
//					String prop = dbExportTempletBean.getTable().getColumn(col).getPropertyName();
				int colNum = this.columns.size();
				for (int i = 0; i < colNum; i++) {
					ColumnTemplateBean column = this.columns.get(i);
					String prop = column.getPropertyName();
					String value = record.get(prop);
					if (value != null && value.equalsIgnoreCase("nan"))
						value = null;
					sb.append((value != null ? "'" + value + "'" : "null")).append(",");
				}
			}
			// 如果碰到失败一条，则成功条数-1，失败条数+1.
			this.succ--;
			this.fail++;
			badWriter.error("入库失败,Table={},异常原因={}。\n记录详情:{}\nsql:{}\nvalue:{}", new Object[]{this.table, e.getMessage(), records.get(0).getRecord(),
					this.sql, sb.toString()});
		} finally {
			DbUtil.close(null, statement, con);
			long end = System.currentTimeMillis();
			totalTime += (end - start);
		}
	}

	/**
	 * 关联网元(关联不上参数数据也要入库)
	 * 
	 * @param record
	 */
	public boolean relateNeInfo(Map<String, String> record, String summaryDataType) {
		// 获取网元信息
		String key = neInfoCache.getMyKey(record, groupByKeys);
		Map<String, String> map = neInfoCache.getNeInfo(key);
		if (map == null) {
			badWriter.debug("关联不上网元，关联关键字key={}，record={}", key, record);
			// 关联不上网元，参数数据需要入库
			if (DbExportTemplateBean.PM_SUMMARY.equals(summaryDataType.toUpperCase())) {
				return false;
			}
		} else {
			putAll(record, map);
		}
		return true;
	}

	/**
	 * 同HashMap.putAll(Map map)
	 * 
	 * @param destMap
	 * @param srcMap
	 */
	public void putAll(Map<String, String> destMap, Map<String, String> srcMap) {
		for (Map.Entry<String, String> e : srcMap.entrySet()) {
			if (StringUtil.isEmpty(destMap.get(e.getKey())))
				destMap.put(e.getKey(), e.getValue());
		}
	}

	/**
	 * 写入汇总日志表
	 */
	protected void writeDbSummaryLog() {
		SummaryDBLogger summaryDBLogger = AppContext.getBean("summaryDBLogger", SummaryDBLogger.class);
		summaryDBLogger.insert(exporterSummaryArgs, table, startTime, total, succ, fail);
	}

	@Override
	public void close() {
		long start = System.currentTimeMillis();
		try {
			// 可能还有未提交的内容，最后要提交一下。
			if (cacheElements != null && cacheElements.size() > 0) {
				this.exportBatch(cacheElements);
				this.succ += cacheElements.size();
			}
		} catch (Exception e) {
			this.fail += this.currNum;
			LOGGER.error("endExport导入数据库失败", e);
		} finally {
			// 写日志
			writeDbSummaryLog();
			// 删除文件
			deleteFiles();

			endTime = new Date();
			long end = System.currentTimeMillis();
			totalTime += (end - start);
			LOGGER.debug("【入库时间统计】{}表入库耗时{}秒，入库成功{}条数据，sum入库失败{}条数据，{}任务，groupBy：{}，CITY：{}，OMC：{}，BSC：{}，VENDOR：{}", new Object[]{this.table,
					totalTime / 1000.00, this.succ,this.fail, task.getId(), groupByKeys, task.getExtraInfo().getCityId(), task.getExtraInfo().getOmcId(),
					task.getExtraInfo().getBscId(), task.getExtraInfo().getVendor()});
		}
	}

	/**
	 * 删除文件
	 */
	public void deleteFiles() {
		List<String> bigFilesList = exporterSummaryArgs.getBigFileList();
		List<String> indexFilesList = exporterSummaryArgs.getIndexFileList();
		if (bigFilesList != null && bigFilesList.size() > 0)
			deleteFile(bigFilesList);
		if (indexFilesList != null && indexFilesList.size() > 0)
			deleteFile(indexFilesList);
	}

	/**
	 * @param list
	 */
	public void deleteFile(List<String> list) {
		for (String filePath : list) {
			new File(filePath).delete();
		}
	}

}
