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
import cn.uway.ucloude.uts.monitor.access.db.DbJVMThreadAccess;
import cn.uway.ucloude.uts.monitor.access.domain.JVMThreadDataPo;
import cn.uway.ucloude.uts.web.access.face.BackendJVMThreadAccess;
import cn.uway.ucloude.uts.web.request.JvmDataRequest;
import cn.uway.ucloude.uts.web.request.MDataRequest;

class DbBackendJVMThreadAccess extends DbJVMThreadAccess implements BackendJVMThreadAccess {

	public DbBackendJVMThreadAccess(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void delete(JvmDataRequest request) {
		DeleteSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql();
		List<Object> params = new ArrayList<Object>();
		sql.delete().from().table(getTabName()).where(request.getWhereSql(params), params).doDelete();
	}

	@Override
	public List<JVMThreadDataPo> queryAvg(MDataRequest request) {
		SelectSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql();
		List<Object> params = new ArrayList<Object>();
		sql.select().all().from().table(getTabName()).where(request.getWhereSql(params), params);
		return sql.list(new ResultSetHandler<List<JVMThreadDataPo>>() {
			@Override
			public List<JVMThreadDataPo> handle(ResultSet rs) throws SQLException {
				List<JVMThreadDataPo> list = new ArrayList<JVMThreadDataPo>();
				while (rs.next()) {
					JVMThreadDataPo mData = new JVMThreadDataPo();
					mData.setId(rs.getString("ID"));
					mData.setNodeGroup(rs.getString("NODE_GROUP"));
					mData.setNodeType(NodeType.getNodeType(rs.getInt("NODE_TYPE")));
					mData.setIdentity(rs.getString("IDENTITY"));
					mData.setGmtCreated(rs.getTimestamp("CREATE_TIME").getTime());
					mData.setTimestamp(rs.getTimestamp("TIME_STAMP").getTime());
					mData.setDaemonThreadCount(rs.getInt("DAEMON_THREAD_COUNT"));
					mData.setThreadCount(rs.getInt("THREAD_COUNT"));
					mData.setTotalStartedThreadCount(rs.getLong("TOTAL_STARTED_THREAD_COUNT"));
					mData.setDeadLockedThreadCount(rs.getInt("DEAD_LOCKED_THREAD_COUNT"));
					mData.setProcessCpuTimeRate(rs.getDouble("PROCESS_CPU_TIME_RATE"));
					list.add(mData);
				}
				return list;
			}
		});
	}

}
