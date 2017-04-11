package cn.uway.framework.warehouse.exporter;

import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.uway.framework.cache.AbstractCacher;
import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.framework.connection.pool.database.DbPoolManager;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.job.LogCdrInsert;
import cn.uway.framework.log.BadWriter;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.status.Status;
import cn.uway.framework.status.dao.StatusDAO;
import cn.uway.framework.warehouse.exporter.template.ColumnTemplateBean;
import cn.uway.framework.warehouse.exporter.template.DatabaseExporterBean;
import cn.uway.framework.warehouse.exporter.template.DbExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.DbUtil;
import cn.uway.util.TimeUtil;

/**
 * PooledFaultTolerantJdbcExporter使用连接池的支持容错的JDBC输出器<br>
 * 1、连接池使用DbPoolManage进行管理，see{DbPoolManager}<br>
 * 2、容错能力:支持类似sqldr的功能，写入失败的数据会记录到bad日志中<br>
 * 3、此输出器建议用在数据库中有主键等输出机制下,不要用于话单输出，但是可以用于话单问题定位<br>
 * 
 * 
 * @author chenrongqiang 2014-4-13
 */
public class PooledFaultTolerantJdbcExporter extends JdbcBatchExporter {

	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(PooledFaultTolerantJdbcExporter.class); // 日志

	/**
	 * 错误记录日志
	 */
	protected static final ILogger badWriter = BadWriter.getInstance().getBadWriter();

	private StatusDAO statusDAO = AppContext.getBean("statusDAO", StatusDAO.class);


	/**
	 * 以数据库批量提交数据量为最大大小的临时缓存数据
	 */
	protected List<ParseOutRecord> cacheElements;

	public PooledFaultTolerantJdbcExporter(DbExportTemplateBean dbExportTempletBean, ExporterArgs exporterArgs) {
		super(dbExportTempletBean, exporterArgs);
	}

	/**
	 * 创建数据库链接
	 */
	protected void initJdbc(ExporterArgs exporterArgs) {
		try {
			createCacher(AbstractCacher.BLOCK_CACHER);
			DatabaseExporterBean dbTargetBean = (DatabaseExporterBean) dbExportTempletBean.getExportTargetBean();
			this.batchNum = dbTargetBean.getBatchNum();
			// 如果批量入库条数非法[目前限制最大10000]
			if (batchNum <= 0 || batchNum > 10000)
				batchNum = DEFAULT_BATCH_NUM;
			LOGGER.debug("每次批量入库条数为：{}，exportId：{}", new Object[]{batchNum, dbExportTempletBean.getId()});
			this.cacheElements = new ArrayList<ParseOutRecord>(batchNum);
		} catch (Exception e) {
			exporterInitErrorFlag = true;
			// -1表示初始化失败
			this.errorCode = -1;
			this.cause = e.getMessage();
			LOGGER.error("数据入库时，数据库连接创建失败。", e);
		}
	}

	@Override
	public void close() {
		long start = System.currentTimeMillis();
		try {
			// 可能还有未提交的内容，最后要提交一下。
			if (cacheElements != null && cacheElements.size() > 0) {
				this.exportBatch(cacheElements);
				this.succ += cacheElements.size();
				this.breakPoint += cacheElements.size();
				// 增加一个非空判断，
				if (!exporterArgs.getObjStatus().isEmpty()) {
					Status status = exporterArgs.getObjStatus().get(0);
					status.breakPointProcess(exportId, breakPoint);
					// this.statusDAO.update(status, exporterArgs.getObjStatus().get(0).getId());
					status.updateBySynchronized(statusDAO, exporterArgs.getObjStatus().get(0).getId());
				}
			}
		} catch (Exception e) {
			this.fail += this.currNum;
			LOGGER.error("endExport导入数据库失败", e);
		} finally {
			endTime = new Date();

			// 优先根据config.ini中的system.logcltinsert.flag进行判断，如果开启（1）， 添加日志入库操作 log_clt_insert ,数据库汇总需要依赖此表,
			// 话单入 log_cdr_insert, 则需要将 system.logcltinsert.flag=0 关闭.
			if (isLogCltInsertFlag()) {
				logCltInsert();
			} else {
				if (this.dbLoggerFlag) {
					writeDbLog();
				}
			}

			long end = System.currentTimeMillis();
			totalTime += (end - start);
			LOGGER.debug("【入库时间统计】{}表入库耗时{}秒，入库成功{}条数据，pool入库失败{}条数据，{}任务，原始文件：{}，CITY：{}，OMC：{}，BSC：{}，VENDOR：{}", new Object[]{this.table, totalTime / 1000.00,
					this.succ, this.fail,task.getId(), (this.exporterArgs != null ? entryNames.get(0) : ""), task.getExtraInfo().getCityId(),
					task.getExtraInfo().getOmcId(), task.getExtraInfo().getBscId(), task.getExtraInfo().getVendor()});
		}
	}

	private void writeDbLog() {

		LogCdrInsert.getInstance().insert(task.getExtraInfo().getCityId(), task.getExtraInfo().getOmcId(), task.getExtraInfo().getBscId(), table,
				exporterArgs.getDataTime(), startTime, endTime, succ, fail, total, task.getExtraInfo().getVendor(), entryNames.get(0));

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
				// 提交完成一批后 更新状态表
				if (!exporterArgs.getObjStatus().isEmpty()) {
					Status status = exporterArgs.getObjStatus().get(0);
					status.breakPointProcess(exportId, breakPoint);
					// this.statusDAO.update(status, exporterArgs.getObjStatus().get(0).getId());
					status.updateBySynchronized(statusDAO, exporterArgs.getObjStatus().get(0).getId());
				}
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
		DatabaseExporterBean dbTargetBean = (DatabaseExporterBean) dbExportTempletBean.getExportTargetBean();
		DatabaseConnectionInfo connectionInfo = dbTargetBean.getConnectionInfo();
		Connection con = DbPoolManager.getConnection(connectionInfo);
		this.statement = con.prepareStatement(sql);
		Map<String, String> record = null;
		con.setAutoCommit(false);
		try {
			for (ParseOutRecord element : records) {
				record = element.getRecord();
				int colNum = this.columns.size();
				int inIndex = 0;
				for (int i = 0; i < colNum; i++) {
					ColumnTemplateBean column = this.columns.get(i);
					if (column.getIsSpan() != null && column.getIsSpan().equalsIgnoreCase("true") && column.getDefaultValue() != null) {
						// 如果字段的默认值作为sql拼接，则跳过对这个字段的赋值
						continue;
					}
					String prop = column.getPropertyName();
					String value = record.get(prop);
					if ((value != null && value.equalsIgnoreCase("nan"))||"".equalsIgnoreCase(value))
						value = null;
					if (value == null && column.getDefaultValue() != null) {
						statement.setString(++inIndex, column.getDefaultValue());
						continue;
					}
					if (column.getFormat() == null) {
						statement.setString(++inIndex, value);
						continue;
					}
					if (column.getFormat().equals("blob")) {// 处理大字段blob
						statement.setBlob(++inIndex, handleBlob(con, value));
						continue;
					} else {
						// 处理时间字符串中包含T，如'1980-01-06T00:00:19'
						if (value != null && value.indexOf("T") > -1)
							value = value.replace("T", " ");
					}

					if (column.getFormat().equals("Timestamp")) {// 处理Timestamp
						statement.setTimestamp(++inIndex, new Timestamp(TimeUtil.getDate(value).getTime()));
						continue;
					}
					statement.setString(++inIndex, value);
				}
				statement.addBatch();
			}
			// 批量提交一次.成功后更新断点表
			statement.executeBatch();
			con.commit();
		} catch (Exception e) {
			LOGGER.warn("PooledFaultTolerant exportBatch() con.commit(), Exception:{}", e.getMessage());
			super.con = con;
			String errSql = buildErrRecordSql(record);

			// 如果提交失败.则事务回滚,采用折半法查找异常的数据记录<br>
			try {
				con.rollback();
			} catch (Exception er) {
				LOGGER.warn("PooledFaultTolerant exportBatch() con.rollback(), Exception:{}", er.getMessage());
			}
			DbUtil.close(null, statement, con);
			if (records.size() > 1) {
				onException(records);
				return;
			}
			// StringBuilder sb = new StringBuilder();
			// if (record != null) {
			// // for (int i = 0; i < this.props.size(); i++) {
			// // String col = this.props.get(i);
			// // String prop = dbExportTempletBean.getTable().getColumn(col).getPropertyName();
			// int colNum = this.columns.size();
			// for (int i = 0; i < colNum; i++) {
			// ColumnTemplateBean column = this.columns.get(i);
			// String prop = column.getPropertyName();
			// String value = record.get(prop);
			// if (value != null && value.equalsIgnoreCase("nan"))
			// value = null;
			// sb.append((value != null ? "'" + value + "'" : "null")).append(",");
			// }
			// }
			// 如果碰到失败一条，则成功条数-1，失败条数+1.
			this.succ--;
			this.fail++;
			// badWriter.error("入库失败,Table={},异常原因={}。\n记录详情:{}\nsql:{}\nvalue:{}", new Object[]{this.table, e.getMessage(),
			// records.get(0).getRecord(),
			// this.sql, sb.toString()});
			badWriter.error("入库失败,Table={},异常原因={}。记录详情:{}\nsql:{}", new Object[]{this.table, e.getMessage(), records.get(0).getRecord(), errSql});
		} finally {
			DbUtil.close(null, statement, con);
			long end = System.currentTimeMillis();
			totalTime += (end - start);
			// LOGGER.debug("OracleJdbcExporter close a connection");
		}
	}

	/**
	 * 当发生异常时执行的操作<br>
	 * 折半法进行入库，将入库失败的记录都提取出来
	 * 
	 * @param elements
	 *            异常的批次数据
	 * @throws ParseException
	 * @throws SQLException
	 */
	public void onException(List<ParseOutRecord> records) throws SQLException {
		if (records == null || records.isEmpty())
			return;
		int size = records.size();
		if (size == 1) {
			exportBatch(records);
			return;
		}
		// 折半处理
		exportBatch(records.subList(0, size / 2));
		exportBatch(records.subList(size / 2, size));
	}

	/**
	 * 处理blob字段
	 * 
	 * @param con
	 * @param value
	 * @return
	 */
	public Blob handleBlob(Connection con, String value) {
		Blob blob = null;
		try {
			blob = con.createBlob();
			OutputStream out = blob.setBinaryStream(1);
			out.write(value.getBytes());
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return blob;
	}
}
