package cn.uway.framework.status.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.uway.framework.status.Status;
import cn.uway.framework.status.dao.StatusDAO;
import cn.uway.util.DbUtil;

/**
 * @author chenrongqiang
 * 
 */
public class MysqlStatusDAO extends StatusDAO {

	/**
	 * 往igp_data_gather_obj_status表写入记录
	 * 
	 * @param gatherObjStatus
	 * @return ID 返回当前插入的记录在数据表中的主键 以方面更新 select @@identity as ID 只在一次连接会话内是准确的 chenrongqiang 2012-11-12
	 */
	public long log(Status gatherObjStatus) {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(sqlForInsertGatherObjStatus);
			int i = 1;
			statement.setString(i++, gatherObjStatus.getGatherObj());
			statement.setString(i++, gatherObjStatus.getSubGatherObj());
			statement.setTimestamp(i++, dateToTimestamp(gatherObjStatus.getDataTime()));
			statement.setLong(i++, gatherObjStatus.getTaskId());
			statement.setTimestamp(i++, dateToTimestamp(gatherObjStatus.getAccessStartTime()));
			statement.setTimestamp(i++, dateToTimestamp(gatherObjStatus.getAccessEndTime()));
			statement.setString(i++, gatherObjStatus.getAccessCause());
			statement.setTimestamp(i++, dateToTimestamp(gatherObjStatus.getParseStartTime()));
			statement.setTimestamp(i++, dateToTimestamp(gatherObjStatus.getParseEndTime()));
			statement.setString(i++, gatherObjStatus.getParseCause());
			statement.setTimestamp(i++, dateToTimestamp(gatherObjStatus.getWarehouseStartTime()));
			statement.setTimestamp(i++, dateToTimestamp(gatherObjStatus.getWarehouseEndTime()));
			statement.setString(i++, gatherObjStatus.getWarehousePoint());
			statement.setString(i++, gatherObjStatus.getWarehouseCause());
			statement.setInt(i++, gatherObjStatus.getStatus());
			statement.setString(i++, gatherObjStatus.getPcName());
			statement.setInt(i++, gatherObjStatus.getExportStatus());
			statement.execute();
			logger.debug("采集记录写入成功,GatherObjStatus={}", gatherObjStatus);
			DbUtil.close(null, statement, null);
			// select @@identity as ID在一次连接会话中是准确的
			statement = conn.prepareStatement(sqlForGetId);
			rs = statement.executeQuery();
			while (rs.next()) {
				return rs.getLong("ID");
			}
		} catch (SQLException e) {
			logger.warn("采集报表记录失败!", e);
		} finally {
			DbUtil.close(rs, statement, conn);
		}
		return -1;
	}

}
