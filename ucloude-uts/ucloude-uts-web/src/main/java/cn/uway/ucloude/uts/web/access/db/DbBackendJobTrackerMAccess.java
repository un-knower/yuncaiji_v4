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
import cn.uway.ucloude.uts.monitor.access.db.DbJobTrackerMAccess;
import cn.uway.ucloude.uts.monitor.access.domain.JobTrackerMDataPo;
import cn.uway.ucloude.uts.web.access.face.BackendJobTrackerMAccess;
import cn.uway.ucloude.uts.web.request.MDataRequest;

class DbBackendJobTrackerMAccess extends DbJobTrackerMAccess implements BackendJobTrackerMAccess {

	public DbBackendJobTrackerMAccess(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<JobTrackerMDataPo> querySum(MDataRequest request) {
		SelectSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql();
		List<Object> params = new ArrayList<Object>();
		sql.select().all().from().table(getTabName()).where(request.getWhereSql(params), params);
		return sql.list(new ResultSetHandler<List<JobTrackerMDataPo>>() {
			@Override
			public List<JobTrackerMDataPo> handle(ResultSet rs) throws SQLException {
				List<JobTrackerMDataPo> list = new ArrayList<JobTrackerMDataPo>();
				while (rs.next()) {
					JobTrackerMDataPo mData = new JobTrackerMDataPo();
					mData.setId(rs.getString("ID"));
					mData.setNodeGroup(rs.getString("NODE_GROUP"));
					mData.setNodeType(NodeType.getNodeType(rs.getInt("NODE_TYPE")));
					mData.setIdentity(rs.getString("IDENTITY"));
					mData.setGmtCreated(rs.getTimestamp("CREATE_TIME").getTime());
					mData.setTimestamp(rs.getTimestamp("TIME_STAMP").getTime());
					mData.setReceiveJobNum(rs.getLong("RECEIVE_JOB_NUM"));
					mData.setPushJobNum(rs.getLong("PUSH_JOB_NUM"));
					mData.setExeSuccessNum(rs.getLong("EXE_SUCCESS_NUM"));
					mData.setExeFailedNum(rs.getLong("EXE_FAILED_NUM"));
					mData.setExeLaterNum(rs.getLong("EXE_LATER_NUM"));
					mData.setExeExceptionNum(rs.getLong("EXE_EXCEPTION_NUM"));
					mData.setFixExecutingJobNum(rs.getLong("FIX_EXECUTING_JOB_NUM"));
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
	public List<String> getJobTrackers() {
		SelectSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql();
		return sql.select().all().from().table(getTabName()).list(new ResultSetHandler<List<String>>() {
			@Override
			public List<String> handle(ResultSet rs) throws SQLException {
				List<String> list = new ArrayList<String>();
				while (rs.next()) {
					list.add(rs.getString("IDENTITY"));
				}
				return list;
			}
		});
	}

}
