package cn.uway.ucloude.uts.web.access.db;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.ResultSetHandler;
import cn.uway.ucloude.data.dataaccess.SqlTemplateFactory;
import cn.uway.ucloude.data.dataaccess.builder.DeleteSql;
import cn.uway.ucloude.data.dataaccess.builder.InsertSql;
import cn.uway.ucloude.data.dataaccess.builder.SelectSql;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.web.access.domain.NodeOnOfflineLog;
import cn.uway.ucloude.uts.web.access.face.BackendNodeOnOfflineLogAccess;
import cn.uway.ucloude.uts.web.request.NodeOnOfflineLogQueryRequest;

class DbBackendNodeOnOfflineLogAccess extends JdbcAbstractAccess implements BackendNodeOnOfflineLogAccess {

	public DbBackendNodeOnOfflineLogAccess(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
		SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql().delete().from()
        .table(getTabName())
        .doDelete();
	}

	protected String getTabName() {
		return "UTS_NODE_ONOFFLINE_LOG";
	}

	@Override
	public void insert(List<NodeOnOfflineLog> nodeOnOfflineLogs) {
		InsertSql insertSql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql();
		insertSql.insert(getTabName()).columns("LOG_TIME", "EVENT", "CLUSTER_NAME", "IP", "PORT", "HOST_NAME",
				"NODE_GROUP", "CREATE_TIME", "THREADS", "IDENTITY", "NODE_TYPE", "HTTP_CMD_PORT");
		for (NodeOnOfflineLog po : nodeOnOfflineLogs) {
			java.sql.Timestamp createTime = null;
			java.sql.Timestamp logTime = null;
			if (po.getCreateTime() != null) {
				createTime = new java.sql.Timestamp(po.getCreateTime().getTime());
			}
			if (po.getLogTime() != null) {
				logTime = new java.sql.Timestamp(po.getLogTime().getTime());
			}
			insertSql.values(logTime, po.getEvent(), po.getClusterName(), po.getIp(), po.getPort(), po.getHostName(),
					po.getGroup(), createTime, po.getThreads(), po.getIdentity(),
					po.getNodeType() == null ? null : po.getNodeType().getValue(), po.getHttpCmdPort());
		}
		insertSql.doBatchInsert();
	}

	@Override
	public List<NodeOnOfflineLog> select(NodeOnOfflineLogQueryRequest request) {
		SelectSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql();
		List<Object> params = new ArrayList<Object>();
		return sql.select().all().from().table(getTabName()).where(request.getWhereSql(params), params)
				.list(new ResultSetHandler<List<NodeOnOfflineLog>>() {
					@Override
					public List<NodeOnOfflineLog> handle(ResultSet rs) throws SQLException {
						List<NodeOnOfflineLog> list = new ArrayList<NodeOnOfflineLog>();
						while (rs.next()) {
							NodeOnOfflineLog node = new NodeOnOfflineLog();
							node.setLogTime(new java.util.Date(rs.getTimestamp("LOG_TIME").getTime()));
							node.setEvent(rs.getString("EVENT"));
							node.setClusterName(rs.getString("CLUSTER_NAME"));
							node.setIp(rs.getString("IP"));
							node.setPort(rs.getInt("PORT"));
							node.setHostName(rs.getString("HOST_NAME"));
							node.setGroup(rs.getString("NODE_GROUP"));
							node.setCreateTime(new Date(rs.getTimestamp("CREATE_TIME").getTime()));
							node.setThreads(rs.getInt("THREADS"));
							node.setIdentity(rs.getString("IDENTITY"));
							Integer nodeType = rs.getInt("NODE_TYPE");
							if (nodeType != null) {
								node.setNodeType(NodeType.values()[nodeType]);
							}
							node.setHttpCmdPort(rs.getInt("HTTP_CMD_PORT"));
							list.add(node);
						}
						return list;
					}
				});
	}

	@Override
	public Long count(NodeOnOfflineLogQueryRequest request) {
		SelectSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql();
		List<Object> params = new ArrayList<Object>();
		BigDecimal count = sql.select().columns("COUNT(1) NUM").from().table(getTabName())
				.where(request.getWhereSql(params), params).single();
		return count.longValue();
	}

	@Override
	public void delete(NodeOnOfflineLogQueryRequest request) {
		DeleteSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql();
		List<Object> params = new ArrayList<Object>();
		sql.delete().from().table(getTabName()).where(request.getWhereSql(params), params).doDelete();
	}

}
