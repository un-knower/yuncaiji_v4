package cn.uway.ucloude.uts.core.domain;

import cn.uway.ucloude.query.QueryRequest;
import cn.uway.ucloude.uts.core.cluster.NodeType;

public class NodeGroupReq extends QueryRequest {
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
