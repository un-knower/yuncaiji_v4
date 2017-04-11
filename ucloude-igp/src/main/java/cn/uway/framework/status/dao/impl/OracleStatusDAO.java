package cn.uway.framework.status.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.uway.framework.status.Status;
import cn.uway.framework.status.dao.StatusDAO;
import cn.uway.util.DbUtil;

/**
 * 基于oracle数据库的状态表操作DAO
 * 
 * @author chenrongqiang
 * 
 */
public class OracleStatusDAO extends StatusDAO {

	@Override
	public long log(Status gatherObjStatus) {
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		long id = -1;
		try {
			conn = datasource.getConnection();
			statement = conn.prepareStatement(getGatherObjStatusIdFromOracle);
			rs = statement.executeQuery();
			while (rs.next()) {
				id = rs.getLong(1);
				break;
			}
			if (id == -1) {
				logger.error("从数据库获取IGP_GATHER_OBJ_STATUS_ID序列值失败");
			}
			DbUtil.close(rs, statement, null);
			int i = 1;
			statement = conn.prepareStatement(sqlForInsertOracleGatherObjStatus);
			statement.setLong(i++, id);
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
		} catch (SQLException e) {
			logger.error("采集报表记录失败!", e);
			logger.error("GatherObjStatus={}", gatherObjStatus.toString());
		} finally {
			DbUtil.close(rs, statement, conn);
		}
		return id;
	}

}
