package cn.uway.ucloude.uts.minitor.access.domain;

import cn.uway.ucloude.uts.core.cluster.NodeType;

/**
 * @author uway
 */
public abstract class MDataPo {

	/**
	 * 自增，唯一主键
	 */
    private String id;
    /**
     * 创建时间
     */
    private Long gmtCreated;
    /**
     * 记录时间(监控数据时间点)
     */
    private Long timestamp;

    private NodeType nodeType;
    /**
     * NodeGroup
     */
    private String nodeGroup;
    /**
     * TaskTracker 节点标识
     */
    private String identity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public NodeType getNodeType() {
        return this.nodeType;
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

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }
}
