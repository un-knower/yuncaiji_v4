package cn.uway.framework.status.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.dbcp2.BasicDataSource;

import cn.uway.framework.status.Status;
import cn.uway.framework.task.worker.FileNamesCache;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.DbUtil;
import cn.uway.util.TimeUtil;

/**
 * igp_data_gather_obj_status 表记录写入 方法由具体的Job来调用 GatherObjStatusDAO
 * 
 * @author chenrongqiang 2012-11-8
 */
public abstract class StatusDAO {

	/**
	 * MySQL连接池
	 */

	protected BasicDataSource datasource;

	/**
	 * SQL语句
	 */
	protected String sqlForInsertGatherObjStatus;

	/**
	 * 查询当前会话操作生成的最新的自增长ID字段
	 */
	protected String sqlForGetId;

	private String sqlForCheckGatherObj;
	
	private String sqlForInitGatherObj;

	/**
	 * 根据ID更新数据采集表记录
	 */
	private String sqlForUpdateGatherObjStatus;

	/**
	 * 根据采集对象名称和任务Id来搜索采集对象状态信息
	 */
	private String sqlForSearchGatherObjStatus;

	private String sqlForAfterFinishExport;
	
	private String sqlForUpdateBreakPoint;

	private String sqlForGatherObjStatusRevert;

	private String sqlForGetMaxTaskDataTime;

	protected String getGatherObjStatusIdFromOracle;

	protected String sqlForInsertOracleGatherObjStatus;

	/**
	 * 根据采集对象名称,任务Id和pc name来搜索采集对象状态信息
	 */
	private String sqlForSearchGatherObjStatusWithPCName;

	protected static final ILogger logger = LoggerManager.getLogger(StatusDAO.class); // 日志

	public String getSqlForGetId() {
		return sqlForGetId;
	}

	public BasicDataSource getDatasource() {
		return datasource;
	}

	public void setDatasource(BasicDataSource datasource) {
		this.datasource = datasource;
	}

	public void setSqlForGetId(String sqlForGetId) {
		this.sqlForGetId = sqlForGetId;
	}

	public String getSqlForCheckGatherObj() {
		return sqlForCheckGatherObj;
	}

	public void setSqlForCheckGatherObj(String sqlForCheckGatherObj) {
		this.sqlForCheckGatherObj = sqlForCheckGatherObj;
	}
		
	public String getSqlForInitGatherObj() {
		return sqlForInitGatherObj;
	}
	
	public void setSqlForInitGatherObj(String sqlForInitGatherObj) {
		this.sqlForInitGatherObj = sqlForInitGatherObj;
	}

	public String getSqlForUpdateGatherObjStatus() {
		return sqlForUpdateGatherObjStatus;
	}

	public void setSqlForUpdateGatherObjStatus(String sqlForUpdateGatherObjStatus) {
		this.sqlForUpdateGatherObjStatus = sqlForUpdateGatherObjStatus;
	}

	public String getSqlForSearchGatherObjStatus() {
		return sqlForSearchGatherObjStatus;
	}

	public void setSqlForSearchGatherObjStatus(String sqlForSearchGatherObjStatus) {
		this.sqlForSearchGatherObjStatus = sqlForSearchGatherObjStatus;
	}

	public String getSqlForSearchGatherObjStatusWithPCName() {
		return sqlForSearchGatherObjStatusWithPCName;
	}

	public void setSqlForSearchGatherObjStatusWithPCName(String sqlForSearchGatherObjStatusWithPCName) {
		this.sqlForSearchGatherObjStatusWithPCName = sqlForSearchGatherObjStatusWithPCName;
	}

	public String getSqlForAfterFinishExport() {
		return sqlForAfterFinishExport;
	}

	public void setSqlForAfterFinishExport(String sqlForAfterFinishExport) {
		this.sqlForAfterFinishExport = sqlForAfterFinishExport;
	}

	public String getSqlForUpdateBreakPoint() {
		return sqlForUpdateBreakPoint;
	}

	public void setSqlForUpdateBreakPoint(String sqlForUpdateBreakPoint) {
		this.sqlForUpdateBreakPoint = sqlForUpdateBreakPoint;
	}

	public String getSqlForGatherObjStatusRevert() {
		return sqlForGatherObjStatusRevert;
	}

	public void setSqlForGatherObjStatusRevert(String sqlForGatherObjStatusRevert) {
		this.sqlForGatherObjStatusRevert = sqlForGatherObjStatusRevert;
	}

	public void setSqlForGetMaxTaskDataTime(String sqlForGetMaxTaskDataTime) {
		this.sqlForGetMaxTaskDataTime = sqlForGetMaxTaskDataTime;
	}

	public String getSqlForInsertGatherObjStatus() {
		return sqlForInsertGatherObjStatus;
	}

	public void setSqlForInsertGatherObjStatus(String sqlForInsertGatherObjStatus) {
		this.sqlForInsertGatherObjStatus = sqlForInsertGatherObjStatus;
	}

	public String getGetGatherObjStatusIdFromOracle() {
		return getGatherObjStatusIdFromOracle;
	}

	public void setGetGatherObjStatusIdFromOracle(String getGatherObjStatusIdFromOracle) {
		this.getGatherObjStatusIdFromOracle = getGatherObjStatusIdFromOracle;
	}

	public String getSqlForInsertOracleGatherObjStatus() {
		return sqlForInsertOracleGatherObjStatus;
	}

	public void setSqlForInsertOracleGatherObjStatus(String sqlForInsertOracleGatherObjStatus) {
		this.sqlForInsertOracleGatherObjStatus = sqlForInsertOracleGatherObjStatus;
	}

	public String getSqlForGetMaxTaskDataTime() {
		return sqlForGetMaxTaskDataTime;
	}

	/**
	 * Date格式转换为Timestamp
	 * 
	 * @param date
	 * @return Timestamp
	 */
	public Timestamp dateToTimestamp(Date date) {
		return date == null ? null : new Timestamp(date.getTime());
	}

	/**
	 * 根据ID更新采集状态表
	 * 
	 * @param status
	 * @param ID
	 */
	public void updateUnsynchronized(Status status, long id) {
		if (id == -1) {
			logger.debug("更新采集记录表失败!无效的ID");
			return;
		}
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(sqlForUpdateGatherObjStatus);
			int i = 1;
			statement.setTimestamp(i++, dateToTimestamp(status.getAccessStartTime()));
			statement.setTimestamp(i++, dateToTimestamp(status.getAccessEndTime()));
			statement.setString(i++, status.getAccessCause());
			statement.setTimestamp(i++, dateToTimestamp(status.getParseStartTime()));
			statement.setTimestamp(i++, dateToTimestamp(status.getParseEndTime()));
			statement.setString(i++, status.getParseCause());
			statement.setTimestamp(i++, dateToTimestamp(status.getWarehouseStartTime()));
			statement.setTimestamp(i++, dateToTimestamp(status.getWarehouseEndTime()));
			statement.setString(i++, status.getWarehousePoint());
			statement.setString(i++, status.getWarehouseCause());
			statement.setInt(i++, status.getStatus());
			statement.setString(i++, status.getGatherNum());
			if (status.getSubGatherObj() != null)
				statement.setString(i++, status.getSubGatherObj());
			else
				statement.setString(i++, null);
			statement.setLong(i++, id);
			int ret = statement.executeUpdate();
			if (ret != 1) {
				logger.warn("未更新到记录");
			}
			logger.trace("采集记录更新成功。id={}，task_id={}，point={}，gather_obj={}",
					new Object[]{id, status.getTaskId(), status.getWarehousePoint(), status.getGatherObj()});
		} catch (SQLException e) {
			logger.warn("采集报表记录失败!", e);
		} finally {
			DbUtil.close(null, statement, conn);
		}
	}

	/**
	 * 如果采集对象成功采集,返回false,否则返回true
	 * 
	 * @param gatherObj
	 */
	public boolean needToGather(Status objStatus) {
		if (objStatus == null) {
			logger.debug("采集对象为空!");
			return true;
		}
		String sql = sqlForCheckGatherObj;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = datasource.getConnection();

			if (objStatus.getDataTime() != null) {
				sql = sql + " and data_time=?";
			}
			statement = conn.prepareStatement(sql);
			statement.setString(1, objStatus.getGatherObj());
			statement.setLong(2, objStatus.getTaskId());
			if (objStatus.getDataTime() != null) {
				statement.setTimestamp(3, dateToTimestamp(objStatus.getDataTime()));
			}
			rs = statement.executeQuery();

			while (rs.next()) {
				return false;
			}
		} catch (SQLException e) {
			logger.warn("查询报表对象失败!", e);
			return false;
		} finally {
			DbUtil.close(rs, statement, conn);
		}
		return true;
	}
	
	/**
	 * 根据任何起始时间
	 * @param taskID
	 * @param startTime
	 * @param endTime
	 */
	public boolean initTaskGatherEntryCache(FileNamesCache fileNameCache, long taskID, Date startTime, Date endTime) {
		if (startTime == null || endTime == null) {
			logger.warn("初始化任务状态表信息失败，　startTime == null或endTime == null");
			return false;
		}
		
		String sql = sqlForInitGatherObj;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = datasource.getConnection();

			statement = conn.prepareStatement(sql);
			statement.setLong(1, taskID);
			statement.setString(2, TimeUtil.getDateString(startTime));
			statement.setString(3, TimeUtil.getDateString(endTime));
			
			rs = statement.executeQuery();

			while (rs.next()) {
				String taskFile = rs.getString(1);
				if (taskFile == null || taskFile.length()<1)
					continue;
				
				fileNameCache.putToCache(taskFile, taskID);
			}
			
			return true;
		} catch (SQLException e) {
			logger.warn("查询报表对象失败!", e);
		} finally {
			DbUtil.close(rs, statement, conn);
		}
		return false;
	}

	/**
	 * 往igp_data_gather_obj_status表写入记录
	 * 
	 * @param gatherObjStatus
	 * @return ID chenrongqiang 2012-11-12
	 */
	public abstract long log(Status gatherObjStatus);

	/**
	 * 根据采集对象名称和任务Id （pc name可选）来返回一个采集对象状态,如果找不到则返回null
	 * 
	 * @param gatherObj
	 * @param taskId
	 * @param pcName
	 * @return
	 */
	public Status searchGatherObjStatus(Status objStatus) {
		Status gatherObjStatus = null;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = datasource.getConnection();
			String sql = sqlForSearchGatherObjStatus;
			if (objStatus.getDataTime() != null) {
				sql += " And data_time=?";
			}
			statement = conn.prepareStatement(sql);
			statement.setString(1, objStatus.getGatherObj());
			statement.setLong(2, objStatus.getTaskId());
			if (objStatus.getDataTime() != null) {
				statement.setTimestamp(3, dateToTimestamp(objStatus.getDataTime()));
			}
			rs = statement.executeQuery();

			while (rs.next()) {
				gatherObjStatus = new Status();
				gatherObjStatus.setId(rs.getLong("id"));
				gatherObjStatus.setGatherObj(objStatus.getGatherObj());
				gatherObjStatus.setSubGatherObj(rs.getString("sub_gather_obj"));
				gatherObjStatus.setTaskId(objStatus.getTaskId());
				gatherObjStatus.setDataTime(rs.getTimestamp("data_time"));
				gatherObjStatus.setAccessStartTime(rs.getTimestamp("access_start_time"));
				gatherObjStatus.setAccessEndTime(rs.getTimestamp("access_end_time"));
				gatherObjStatus.setAccessCause(rs.getString("access_cause"));
				gatherObjStatus.setParseStartTime(rs.getTimestamp("parse_start_time"));
				gatherObjStatus.setParseEndTime(rs.getTimestamp("parse_end_time"));
				gatherObjStatus.setParseCause(rs.getString("parse_cause"));
				gatherObjStatus.setWarehouseStartTime(rs.getTimestamp("warehouse_start_time"));
				gatherObjStatus.setWarehouseEndTime(rs.getTimestamp("warehouse_end_time"));
				gatherObjStatus.setWarehousePoint(rs.getString("warehouse_point"));
				gatherObjStatus.setWarehouseCause(rs.getString("warehouse_cause"));
				gatherObjStatus.setStatus(rs.getInt("status"));
				gatherObjStatus.setPcName(rs.getString("pc_name"));
				gatherObjStatus.setGatherNum(rs.getString("parse_num"));
				gatherObjStatus.setSubGatherObj(rs.getString("sub_gather_obj"));
				break;
			}
		} catch (SQLException e) {
			logger.error("查询采集对象状态失败!", e);
		} finally {
			DbUtil.close(rs, statement, conn);
		}

		return gatherObjStatus;
	}

	/**
	 * 更新采集表断点信息
	 * 
	 * @param id
	 * @param warehousePoint
	 * 
	 * @return 更新是否成功
	 */
	public Boolean updateBreakPointUnsynchronized(long id, String warehousePoint) {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(sqlForUpdateBreakPoint);
			statement.setString(1, warehousePoint);
			statement.setLong(2, id);
			statement.executeUpdate();
		} catch (Exception e) {
			logger.error("更新断点失败。id:{};warehousePoint:{}",id,warehousePoint);
			logger.error("更新采集表断点信息失败", e);
			return false;
		} finally {
			DbUtil.close(null, statement, conn);
		}
		return true;
	}

	/**
	 * 更新采集表输出状态
	 * 
	 * @param id
	 * @param exportStatus
	 */
	public void updateExportStatusUnsynchronized(long id, int exportStatus) {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = datasource.getConnection();
			// 如果数据时间不为空 则更新数据时间
			statement = conn.prepareStatement(sqlForAfterFinishExport);
			statement.setInt(1, exportStatus);
			statement.setLong(2, id);
			statement.executeUpdate();
		} catch (Exception e) {
			logger.error("更新采集表输出状态失败", e);
		} finally {
			DbUtil.close(null, statement, conn);
		}
	}

	/**
	 * 获取任务的最大数据时间
	 * 
	 * @param taskId
	 * @return 最大数据时间
	 */
	public Date getMaxTaskDataTime(long taskId) {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(sqlForGetMaxTaskDataTime);
			statement.setLong(1, taskId);
			rs = statement.executeQuery();
			while (rs.next()) {
				return rs.getTimestamp(1);
			}
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			DbUtil.close(rs, statement, conn);
		}
	}

	/**
	 * 更新采集表输出状态 <br>
	 * 在系统启动时调用，对采集解码成功但是入库未完成的记录状态进行还原
	 * 
	 * @param id
	 * @param exportStatus
	 */
	public int gatherObjStatusRevert(String pcName) {
		Connection conn = null;
		PreparedStatement statement = null;
		int revertNum = 0;
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(sqlForGatherObjStatusRevert);
			statement.setString(1, pcName);
			revertNum = statement.executeUpdate();
			logger.debug("程序启动执行状态初始化成功,sql={},pc_name={}", new Object[]{sqlForGatherObjStatusRevert, pcName});
			return revertNum;
		} catch (Exception e) {
			logger.error("执行错误.", e);
			return -1;
		} finally {
			DbUtil.close(null, statement, conn);
		}
	}
}
