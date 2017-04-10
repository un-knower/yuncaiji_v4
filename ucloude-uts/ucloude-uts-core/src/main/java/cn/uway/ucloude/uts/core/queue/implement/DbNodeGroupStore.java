package cn.uway.ucloude.uts.core.queue.implement;

import java.math.BigDecimal;
import java.util.List;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.data.dataaccess.builder.OrderByType;
import cn.uway.ucloude.data.dataaccess.builder.SelectSql;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.data.dataaccess.builder.WhereSql;
import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.queue.NodeGroupStore;
import cn.uway.ucloude.uts.core.queue.domain.NodeGroupGetReq;
import cn.uway.ucloude.uts.core.queue.domain.NodeGroupPo;

/**
 * 节点管理
 * 
 * @author Uway-M3
 *
 */
public class DbNodeGroupStore extends AbstractDbJobQueue implements NodeGroupStore {

	public DbNodeGroupStore(String connectionKey) {
		super(connectionKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addNodeGroup(NodeType nodeType, String name) {
		BigDecimal count = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql()
                .select()
                .columns("count(1)")
                .from()
                .table(getTableName())
                .where("node_type = ?", nodeType.getValue())
                .and("name = ?", name)
                .single();
        if (count.longValue() > 0) {
            //  already exist
            return;
        }
        SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql()
                .insert(getTableName())
                .columns("node_type", "name", "CREATE_TIME")
                .values(nodeType.getValue(), name, new java.sql.Timestamp(SystemClock.now()))
                .doInsert();
		
	}

	@Override
	public void removeNodeGroup(NodeType nodeType, String name) {
		SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql()
        .delete()
        .from()
        .table(getTableName())
        .where("node_type = ?", nodeType.getValue())
        .and("name = ?", name)
        .doDelete();
	}

	@Override
	public List<NodeGroupPo> getNodeGroup(NodeType nodeType) {
		 SelectSql sql =  SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql()
	                .select()
	                .all()
	                .from()
	                .table(getTableName());
	     if(nodeType != null && nodeType.getValue() > -1)
	    	 sql.where("node_type = ?", nodeType.getValue());
	         
		 return sql.list(RshHolder.NODE_GROUP_LIST_RSH);
	}

	@Override
	public Pagination<NodeGroupPo> getNodeGroup(NodeGroupGetReq request) {
		// TODO Auto-generated method stub
		Pagination<NodeGroupPo> response = new Pagination<NodeGroupPo>();
		WhereSql whereSql = SqlBuilderFactory
				.getSqlFactory(getSqlTemplate())
				.getWhereSql();
		if(request.getNodeType() != null && request.getNodeType().getValue() > -1)
			whereSql.andOnNotNull("node_type = ?", request.getNodeType() == null ? null : request.getNodeType().getValue());
		
		whereSql.andOnNotEmpty("name = ?", request.getNodeGroup());
		BigDecimal results = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql()
                .select()
                .columns("count(1)")
                .from()
                .table(getTableName())
                .whereSql(whereSql)
                .single();
        response.setTotal(results.longValue());
        if (response.getTotal() == 0) {
            return response;
        }

        List<NodeGroupPo> rows = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql()
                .select()
                .all()
                .from()
                .table(getTableName())
                .whereSql(
                        whereSql
                )
                .orderBy()
                .column("CREATE_TIME", OrderByType.DESC)
                .page(request.getPage(), request.getPageSize())
                .list(RshHolder.NODE_GROUP_LIST_RSH);

        response.setData(rows);

        return response;
	}

	@Override
	public List<NodeGroupPo> getNodeGroups(NodeGroupGetReq request) {
		WhereSql whereSql = SqlBuilderFactory
				.getSqlFactory(getSqlTemplate())
				.getWhereSql()
				.andOnNotNull("node_type = ?", request.getNodeType() == null ? null : request.getNodeType().getValue())
                .andOnNotEmpty("name = ?", request.getNodeGroup());
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql()
                .select()
                .all()
                .from()
                .table(getTableName())
                .whereSql(
                        whereSql
                )
                .orderBy()
                .column("CREATE_TIME", OrderByType.DESC).list(RshHolder.NODE_GROUP_LIST_RSH);
	}

	@Override
	protected String getTableName() {
		return "uts_group_node_info";
	}

	
}
