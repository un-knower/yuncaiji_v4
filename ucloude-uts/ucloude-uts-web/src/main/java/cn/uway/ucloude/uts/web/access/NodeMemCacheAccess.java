package cn.uway.ucloude.uts.web.access;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.builder.SelectSql;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.data.dataaccess.builder.WhereSql;
import cn.uway.ucloude.data.dataaccess.mysql.builder.MysqlWhereSql;
import cn.uway.ucloude.data.dataaccess.utils.JdbcTypeUtils;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.utils.CharacterUtils;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.web.request.NodeQueryRequest;
import cn.uway.ucloude.data.dataaccess.builder.OrderByType;

public class NodeMemCacheAccess extends MemoryAccess {
	private static final ILogger LOGGER = LoggerManager.getLogger(NodeMemCacheAccess.class);

	public NodeMemCacheAccess() {
		super();
	}

	public void addNode(List<Node> nodes) {
		for (Node node : nodes) {
			try {
				NodeQueryRequest request = new NodeQueryRequest();
				request.setIdentity(node.getIdentity());
				List<Node> existNodes = search(request);
				if (CollectionUtil.isNotEmpty(existNodes)) {
					// 如果存在,那么先删除
					removeNode(existNodes);
				}

				SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql().insert("uts_node")
						.columns("identity", "available", "cluster_name", "node_type", "ip", "port", "node_group",
								"create_time", "threads", "host_name", "http_cmd_port")
						.values(node.getIdentity(), node.isAvailable() ? 1 : 0, node.getClusterName(),
								node.getNodeType().name(), node.getIp(), node.getPort(), node.getGroup(),
								node.getCreateTime(), node.getThreads(), node.getHostName(), node.getHttpCmdPort())
						.doInsert();
			} catch (Exception e) {
				LOGGER.error("Insert {} error!", node, e);
			}
		}
	}

	public void clear() {
		SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql().delete().from().table("uts_node").doDelete();
	}

	public void removeNode(List<Node> nodes) {
		for (Node node : nodes) {
			try {
				SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql().delete().from().table("uts_node")
						.where("identity = ?", node.getIdentity()).doDelete();
			} catch (Exception e) {
				LOGGER.error("Delete {} error!", node, e);
			}
		}
	}

	public Node getNodeByIdentity(String identity) {
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().all().from().table("uts_node")
				.where("identity = ?", identity).single(RshHandler.NODE_RSH);
	}

	public List<Node> getNodeByNodeType(NodeType nodeType) {
		NodeQueryRequest nodePaginationReq = new NodeQueryRequest();
		nodePaginationReq.setNodeType(nodeType.getValue());
		return search(nodePaginationReq);
	}

	public List<Node> search(NodeQueryRequest request) {
		SelectSql selectSql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().all().from()
				.table("uts_node").whereSql(buildWhereSql(request));
		if (StringUtil.isNotEmpty(request.getField())) {
			selectSql.orderBy().column(CharacterUtils.camelCase2Underscore(request.getField()),
					OrderByType.convert(request.getDirection()));
		}
		return selectSql.page(request.getPage(), request.getPageSize()).list(RshHandler.NODE_LIST_RSH);
	}

	private WhereSql buildWhereSql(NodeQueryRequest request) {
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getWhereSql()
				.andOnNotEmpty("identity = ?", request.getIdentity())
				.andOnNotEmpty("node_group = ?", request.getNodeGroup())
				.andOnNotNull("node_type = ?", request.getNodeType() == null ? null : request.getNodeType().name())
				.andOnNotEmpty("ip = ?", request.getIp()).andOnNotNull("available = ?", request.getAvailable())
				.andBetween("create_time", JdbcTypeUtils.toTimestamp(request.getStartDate()),
						JdbcTypeUtils.toTimestamp(request.getEndDate()));
	}

	public Pagination<Node> pageSelect(NodeQueryRequest request) {
		Pagination<Node> response = new Pagination<Node>();
		SelectSql sql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().columns("count(1)")
				.from().table("uts_node").whereSql(buildWhereSql(request));
		BigDecimal total = sql.single();
		response.setTotal(total.longValue());
		List<Node> nodes;
		if (total.longValue() > 0) {
			nodes = search(request);
		} else {
			nodes = new ArrayList<Node>();
		}
		response.setData(nodes);
		return response;
	}
}
