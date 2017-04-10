package cn.uway.ucloude.uts.core.queue.domain;

import java.util.ArrayList;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.builder.SqlUtil;
import cn.uway.ucloude.data.dataaccess.model.LogicOptType;
import cn.uway.ucloude.query.QueryRequest;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.cluster.NodeType;

/**
 * 
 * @author uway
 *
 */
public class NodeGroupGetReq extends QueryRequest {

	public String getWhereSql(List<Object> params) {
		List<String> list = new ArrayList<String>();
		if(this.nodeType != NodeType.All && this.nodeType != null){
			SqlUtil.getWhere("NODE_TYPE", LogicOptType.IsEqualTo, nodeType.getValue(), list, params);
		}
		SqlUtil.getWhere("NAME", LogicOptType.IsEqualTo, nodeGroup, list, params);
		return StringUtil.join(list, " AND ");
	}
	private NodeType nodeType;

	private String nodeGroup;

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	public String getNodeGroup() {
		return nodeGroup;
	}

	public void setNodeGroup(String nodeGroup) {
		this.nodeGroup = nodeGroup;
	}
}
