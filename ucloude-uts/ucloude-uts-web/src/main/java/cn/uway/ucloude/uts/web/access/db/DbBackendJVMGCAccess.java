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
import cn.uway.ucloude.uts.monitor.access.db.DbJVMGCAccess;
import cn.uway.ucloude.uts.monitor.access.domain.JVMGCDataPo;
import cn.uway.ucloude.uts.web.access.face.BackendJVMGCAccess;
import cn.uway.ucloude.uts.web.request.JvmDataRequest;
import cn.uway.ucloude.uts.web.request.MDataRequest;

class DbBackendJVMGCAccess extends DbJVMGCAccess implements BackendJVMGCAccess {

	public DbBackendJVMGCAccess(String connKey) {
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
	public List<JVMGCDataPo> queryAvg(MDataRequest request) {
		SelectSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql();
		List<Object> params = new ArrayList<Object>();
		sql.select().all().from().table(getTabName()).where(request.getWhereSql(params), params);
		return sql.list(new ResultSetHandler<List<JVMGCDataPo>>() {
			@Override
			public List<JVMGCDataPo> handle(ResultSet rs) throws SQLException {
				List<JVMGCDataPo> list = new ArrayList<JVMGCDataPo>();
				while (rs.next()) {
					JVMGCDataPo mData = new JVMGCDataPo();
					mData.setId(rs.getString("ID"));
					mData.setNodeGroup(rs.getString("NODE_GROUP"));
					mData.setNodeType(NodeType.getNodeType(rs.getInt("NODE_TYPE")));
					mData.setIdentity(rs.getString("IDENTITY"));
					mData.setGmtCreated(rs.getTimestamp("CREATE_TIME").getTime());
					mData.setTimestamp(rs.getTimestamp("TIME_STAMP").getTime());
					mData.setYoungGCCollectionCount(rs.getLong("YOUNG_GC_CLT_COUNT"));
					mData.setYoungGCCollectionTime(rs.getLong("YOUNG_GC_CLT_TIME"));
					mData.setFullGCCollectionCount(rs.getLong("FULL_GC_CLT_COUNT"));
					mData.setFullGCCollectionTime(rs.getLong("FULL_GC_CLT_TIME"));
					mData.setSpanYoungGCCollectionCount(rs.getLong("SPAN_YOUNG_GC_CLT_COUNT"));
					mData.setSpanYoungGCCollectionTime(rs.getLong("SPAN_YOUNG_GC_CLT_TIME"));
					mData.setSpanFullGCCollectionCount(rs.getLong("SPAN_FULL_GC_CLT_COUNT"));
					mData.setSpanFullGCCollectionTime(rs.getLong("SPAN_FULL_GC_CLT_TIME"));
					list.add(mData);
				}
				return list;
			}
		});
	}

}
