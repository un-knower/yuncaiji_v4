package cn.uway.ucloude.uts.web.access.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import cn.uway.ucloude.data.dataaccess.ResultSetHandler;
import cn.uway.ucloude.data.dataaccess.builder.DeleteSql;
import cn.uway.ucloude.data.dataaccess.builder.SelectSql;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.monitor.access.db.DbTaskTrackerMAccess;
import cn.uway.ucloude.uts.monitor.access.domain.TaskTrackerMDataPo;
import cn.uway.ucloude.uts.web.access.face.BackendTaskTrackerMAccess;
import cn.uway.ucloude.uts.web.admin.vo.NodeInfo;
import cn.uway.ucloude.uts.web.request.MDataRequest;

class DbBackendTaskTrackerMAccess extends DbTaskTrackerMAccess implements BackendTaskTrackerMAccess {

	public DbBackendTaskTrackerMAccess(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<TaskTrackerMDataPo> querySum(MDataRequest request) {
		SelectSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql();
		List<Object> params = new ArrayList<Object>();
		sql.select().all().from().table(getTabName()).where(request.getWhereSql(params), params);
		return sql.list(new ResultSetHandler<List<TaskTrackerMDataPo>>() {
			@Override
			public List<TaskTrackerMDataPo> handle(ResultSet rs) throws SQLException {
				List<TaskTrackerMDataPo> list = new ArrayList<TaskTrackerMDataPo>();
				while (rs.next()) {
					TaskTrackerMDataPo mData = new TaskTrackerMDataPo();
					mData.setId(rs.getString("ID"));
					mData.setNodeGroup(rs.getString("NODE_GROUP"));
					mData.setNodeType(NodeType.getNodeType(rs.getInt("NODE_TYPE")));
					mData.setIdentity(rs.getString("IDENTITY"));
					mData.setGmtCreated(rs.getTimestamp("CREATE_TIME").getTime());
					mData.setTimestamp(rs.getTimestamp("TIME_STAMP").getTime());
					mData.setExeSuccessNum(rs.getLong("exe_Success_Num"));
					mData.setExeFailedNum(rs.getLong("exe_Failed_Num"));
					mData.setExeLaterNum(rs.getLong("exe_Later_Num"));
					mData.setExeExceptionNum(rs.getLong("exe_Exception_Num"));
					mData.setTotalRunningTime(rs.getLong("total_Running_Time"));
					list.add(mData);
				}
				return list;
			}
		});
	}

	@Override
	public void delete(MDataRequest request) {
		DeleteSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql();
		List<Object> params = new ArrayList<Object>();
		sql.delete().from().table(getTabName()).where(request.getWhereSql(params), params).doDelete();
	}

	@Override
	public List<NodeInfo> getTaskTrackers() {
		SelectSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql();
		return sql.select().all().from().table(getTabName()).list(new ResultSetHandler<List<NodeInfo>>() {
			@Override
			public List<NodeInfo> handle(ResultSet rs) throws SQLException {
				List<NodeInfo> list = new ArrayList<NodeInfo>();
				while (rs.next()) {
					NodeInfo node = new NodeInfo();
					node.setIdentity(rs.getString("IDENTITY"));
					node.setNodeGroup(rs.getString("NODE_GROUP"));
					list.add(node);
				}
				return list;
			}
		});
	}

}
