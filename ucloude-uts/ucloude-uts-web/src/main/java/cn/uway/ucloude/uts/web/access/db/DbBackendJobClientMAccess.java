package cn.uway.ucloude.uts.web.access.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.ResultSetHandler;
import cn.uway.ucloude.data.dataaccess.builder.*;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.monitor.access.db.DbJobClientMAccess;
import cn.uway.ucloude.uts.monitor.access.domain.JobClientMDataPo;
import cn.uway.ucloude.uts.web.access.face.BackendJobClientMAccess;
import cn.uway.ucloude.uts.web.admin.vo.NodeInfo;
import cn.uway.ucloude.uts.web.request.MDataRequest;

class DbBackendJobClientMAccess extends DbJobClientMAccess implements BackendJobClientMAccess {

	public DbBackendJobClientMAccess(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void delete(MDataRequest request) {
		DeleteSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql();
		List<Object> params = new ArrayList<Object>();
		sql.delete().from().table(getTabName()).where(request.getWhereSql(params), params).doDelete();
	}

	@Override
	public List<JobClientMDataPo> querySum(MDataRequest request) {
		SelectSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql();
		List<Object> params = new ArrayList<Object>();
		sql.select().all().from().table(getTabName()).where(request.getWhereSql(params), params);
		return sql.list(new ResultSetHandler<List<JobClientMDataPo>>() {
			@Override
			public List<JobClientMDataPo> handle(ResultSet rs) throws SQLException {
				List<JobClientMDataPo> list = new ArrayList<JobClientMDataPo>();
				while (rs.next()) {
					JobClientMDataPo mData = new JobClientMDataPo();
					mData.setId(rs.getString("ID"));
					mData.setNodeGroup(rs.getString("NODE_GROUP"));
					mData.setNodeType(NodeType.getNodeType(rs.getInt("NODE_TYPE")));
					mData.setIdentity(rs.getString("IDENTITY"));
					mData.setGmtCreated(rs.getTimestamp("CREATE_TIME").getTime());
					mData.setTimestamp(rs.getTimestamp("TIME_STAMP").getTime());
					mData.setSubmitSuccessNum(rs.getLong("SUBMIT_SUCCESS_NUM"));
					mData.setSubmitFailedNum(rs.getLong("SUBMIT_FAILED_NUM"));
					mData.setFailStoreNum(rs.getLong("FAIL_STORE_NUM"));
					mData.setSubmitFailStoreNum(rs.getLong("SUBMIT_FAIL_STORE_NUM"));
					mData.setHandleFeedbackNum(rs.getLong("HANDLE_FEEDBACK_NUM"));
					list.add(mData);
				}
				return list;
			}
		});
	}

	@Override
	public List<NodeInfo> getJobClients() {
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
