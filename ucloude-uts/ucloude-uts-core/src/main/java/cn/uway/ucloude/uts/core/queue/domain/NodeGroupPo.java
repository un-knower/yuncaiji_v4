package cn.uway.ucloude.uts.core.queue.domain;

import cn.uway.ucloude.uts.core.cluster.NodeType;

public class NodeGroupPo {
	private NodeType nodeType;
    /**
     * 名称
     */
    private String name;
    /**
     * 创建时间
     */
    private Long gmtCreated;

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }
}
