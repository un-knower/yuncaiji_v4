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
import cn.uway.ucloude.uts.minitor.access.db.DbJVMMemoryAccess;
import cn.uway.ucloude.uts.minitor.access.domain.JVMMemoryDataPo;
import cn.uway.ucloude.uts.web.access.face.BackendJVMMemoryAccess;
import cn.uway.ucloude.uts.web.request.JvmDataRequest;
import cn.uway.ucloude.uts.web.request.MDataRequest;

class DbBackendJVMMemoryAccess extends DbJVMMemoryAccess implements BackendJVMMemoryAccess {

	public DbBackendJVMMemoryAccess(String connKey) {
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
	public List<JVMMemoryDataPo> queryAvg(MDataRequest request) {
		SelectSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql();
		List<Object> params = new ArrayList<Object>();
		sql.select().all().from().table(getTabName()).where(request.getWhereSql(params), params);
		return sql.list(new ResultSetHandler<List<JVMMemoryDataPo>>() {
			@Override
			public List<JVMMemoryDataPo> handle(ResultSet rs) throws SQLException {
				List<JVMMemoryDataPo> list = new ArrayList<JVMMemoryDataPo>();
				while (rs.next()) {
					JVMMemoryDataPo mData = new JVMMemoryDataPo();
					mData.setId(rs.getString("ID"));
					mData.setNodeGroup(rs.getString("NODE_GROUP"));
					mData.setNodeType(NodeType.getNodeType(rs.getInt("NODE_TYPE")));
					mData.setIdentity(rs.getString("IDENTITY"));
					mData.setGmtCreated(rs.getTimestamp("CREATE_TIME").getTime());
					mData.setTimestamp(rs.getTimestamp("TIME_STAMP").getTime());
					mData.setHeapMemoryCommitted(rs.getLong("HEAP_MEMORY_COMMITTED"));
					mData.setHeapMemoryInit(rs.getLong("HEAP_MEMORY_INIT"));
					mData.setHeapMemoryMax(rs.getLong("HEAP_MEMORY_MAX"));
					mData.setHeapMemoryUsed(rs.getLong("HEAP_MEMORY_USED"));
					mData.setNonHeapMemoryCommitted(rs.getLong("NON_HEAP_MEMORY_COMMITTED"));
					mData.setNonHeapMemoryInit(rs.getLong("NON_HEAP_MEMORY_INIT"));
					mData.setNonHeapMemoryMax(rs.getLong("NON_HEAP_MEMORY_MAX"));
					mData.setNonHeapMemoryUsed(rs.getLong("NON_HEAP_MEMORY_USED"));
					mData.setPermGenCommitted(rs.getLong("PERM_GEN_COMMITTED"));
					mData.setPermGenInit(rs.getLong("PERM_GEN_INIT"));
					mData.setPermGenMax(rs.getLong("PERM_GEN_MAX"));
					mData.setPermGenUsed(rs.getLong("PERM_GEN_USED"));
					mData.setOldGenCommitted(rs.getLong("OLD_GEN_COMMITTED"));
					mData.setOldGenInit(rs.getLong("OLD_GEN_INIT"));
					mData.setOldGenMax(rs.getLong("OLD_GEN_MAX"));
					mData.setOldGenUsed(rs.getLong("OLD_GEN_USED"));
					mData.setEdenSpaceCommitted(rs.getLong("EDEN_SPACE_COMMITTED"));
					mData.setEdenSpaceInit(rs.getLong("EDEN_SPACE_INIT"));
					mData.setEdenSpaceMax(rs.getLong("EDEN_SPACE_MAX"));
					mData.setEdenSpaceUsed(rs.getLong("EDEN_SPACE_USED"));
					mData.setSurvivorCommitted(rs.getLong("SURVIVOR_COMMITTED"));
					mData.setSurvivorInit(rs.getLong("SURVIVOR_INIT"));
					mData.setSurvivorMax(rs.getLong("SURVIVOR_MAX"));
					mData.setSurvivorUsed(rs.getLong("SURVIVOR_USED"));
					list.add(mData);
				}
				return list;
			}
		});
	}

}
