package cn.uway.framework.log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.dbcp2.BasicDataSource;

import cn.uway.framework.warehouse.exporter.ExporterSummaryArgs;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.DbUtil;

/**
 * 汇总输出日志记录
 * 
 * @author yuy
 * @date 2014.04.11
 */
public final class SummaryDBLogger {

	private static final ILogger logger = LoggerManager.getLogger(SummaryDBLogger.class);

	/** 数据库连接池。 */
	private BasicDataSource dataSource;

	/** 插入输出日志表记录sql。 */
	private String sqlForInsertSummaryExportLogRecords;

	/** 查询汇总输出日志表记录sql。 */
	private String sqlForGetSummaryExportLogRecords;

	/**
	 * 构造方法。
	 */
	private SummaryDBLogger() {
		super();
	}

	/**
	 * 记录一条数据库日志到DS_LOG_CLT_TO_GROUP表中。
	 * 
	 * @param omcId
	 *            omcId
	 * @param tableName
	 *            clt原始表名
	 * @param stampTime
	 *            入库时间
	 * @param count
	 *            入库条数
	 * @param taskID
	 *            任务编号
	 */
	public void insert(ExporterSummaryArgs exporterArgs, String tableName, Date beginTime, long totalNum, long insertNum, long failNum) {
		String sql = this.sqlForInsertSummaryExportLogRecords;
		logger.debug("插入汇总输出日志表记录的SQL为：{}", sql);

		Connection con = null;
		PreparedStatement ps = null;
		try {
			logger.debug("准备获取Oracle连接……当前oracle连接数：{}，最大oracle连接数：{}", new Object[]{dataSource.getNumActive(), dataSource.getNumActive()});
			con = this.dataSource.getConnection();
			logger.debug("Oracle连接获取成功，当前oracle连接数：{}，最大oracle连接数：{}", new Object[]{dataSource.getNumActive(), dataSource.getNumActive()});
			ps = con.prepareStatement(sql);
			int index = 1;
			ps.setLong(index++, exporterArgs.getTask().getExtraInfo().getOmcId());
			ps.setLong(index++, exporterArgs.getTask().getId());
			ps.setTimestamp(index++, new Timestamp(exporterArgs.getDataTime().getTime()));
			ps.setLong(index++, exporterArgs.getDateType());
			ps.setString(index++, tableName);
			ps.setTimestamp(index++, new Timestamp(beginTime.getTime()));
			ps.setTimestamp(index++, new Timestamp(new Date().getTime()));
			ps.setLong(index++, totalNum);
			ps.setLong(index++, insertNum);
			ps.setLong(index++, failNum);
			ps.setLong(index++, exporterArgs.isRepair() ? 1 : 0);
			ps.setLong(index++, exporterArgs.getTask().getExtraInfo().getNetType());
			ps.execute();
		} catch (Exception e) {
			logger.warn("插入DS_LOG_CLT_TO_GROUP表异常，dataTime=" + exporterArgs.getDataTime(), e);
		} finally {
			DbUtil.close(null, ps, con);
		}
	}

	/**
	 * 查询汇总输出日志表记录
	 * 
	 * @return 正常任务列表
	 */
	public int getSummaryExportLogRecords(long taskId, int dataType, String data_time) {
		String sql = this.sqlForGetSummaryExportLogRecords;
		logger.debug("查询汇总输出日志表记录的SQL为：{}", sql);

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = this.dataSource.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, taskId);
			pstmt.setInt(2, dataType);
			pstmt.setString(3, data_time);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("从汇总输出日志表中读取日志信息时异常。", e);
		} finally {
			DbUtil.close(rs, pstmt, conn);
		}

		logger.debug("任务id={},dataType={},data_time={},从汇总输出日志表中读取日志信息条数为: {}", new Object[]{taskId, dataType, data_time, count});
		return count;
	}

	/**
	 * @param dataSource
	 */
	public void setDataSource(BasicDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @param sqlForInsertSummaryExportLogRecords
	 */
	public void setSqlForInsertSummaryExportLogRecords(String sqlForInsertSummaryExportLogRecords) {
		this.sqlForInsertSummaryExportLogRecords = sqlForInsertSummaryExportLogRecords;
	}

	/**
	 * @param sqlForGetSummaryExportLogRecords
	 */
	public void setSqlForGetSummaryExportLogRecords(String sqlForGetSummaryExportLogRecords) {
		this.sqlForGetSummaryExportLogRecords = sqlForGetSummaryExportLogRecords;
	}
}
