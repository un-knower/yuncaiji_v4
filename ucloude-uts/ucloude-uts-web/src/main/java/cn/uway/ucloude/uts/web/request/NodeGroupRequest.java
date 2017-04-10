package cn.uway.ucloude.uts.web.request;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.builder.SqlUtil;
import cn.uway.ucloude.data.dataaccess.model.LogicOptType;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.cluster.NodeType;

public class NodeGroupRequest {
	private NodeType nodeType;

	private String nodeGroup;

	public String getWhereSql(List<Object> params) {
		List<String> list = new ArrayList<String>();
		SqlUtil.getWhere("NODE_GROUP", LogicOptType.IsEqualTo, this.nodeGroup, list, params);
		SqlUtil.getWhere("node_Type", LogicOptType.IsEqualTo, this.nodeType, list, params);
		return StringUtil.join(list, " AND ");
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = NodeType.getNodeType(nodeType);
	}

	public String getNodeGroup() {
		return nodeGroup;
	}

	public void setNodeGroup(String nodeGroup) {
		this.nodeGroup = nodeGroup;
	}
}
