package cn.uway.ucloude.uts.core.cluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonFormat;

import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.registry.NodeRegistryUtils;

public class Node {
	// 是否可用
    private boolean available = true;
    private String clusterName;
    private NodeType nodeType;
    private String ip;
    private Integer port = 0;
    private String hostName;
    private String group;
    private Long createTime;
    // 线程个数
    private Integer threads;
    // 唯一标识
    private String identity;
    // 命令端口
    private Integer httpCmdPort;

    // 自己关注的节点类型
    private List<NodeType> listenNodeTypes;

    private String fullString;

    private Job job;

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Integer getHttpCmdPort() {
        return httpCmdPort;
    }

    public void setHttpCmdPort(Integer httpCmdPort) {
        this.httpCmdPort = httpCmdPort;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean isAvailable) {
        this.available = isAvailable;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8") 
    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public List<NodeType> getListenNodeTypes() {
        return listenNodeTypes;
    }

    public void setListenNodeTypes(List<NodeType> listenNodeTypes) {
        this.listenNodeTypes = listenNodeTypes;
    }

    public void addListenNodeType(NodeType nodeType) {
        if (this.listenNodeTypes == null) {
            this.listenNodeTypes = new ArrayList<NodeType>();
        }
        this.listenNodeTypes.add(nodeType);
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return !(identity != null ? !identity.equals(node.identity) : node.identity != null);

    }

    @Override
    public int hashCode() {
        return identity != null ? identity.hashCode() : 0;
    }

    public String getAddress() {
        return ip + ":" + port;
    }

    public String toFullString() {
        if (fullString == null) {
            fullString = NodeRegistryUtils.getFullPath(this);
        }
        return fullString;
    }

    @Override
    public String toString() {
        return JsonConvert.serialize(this);
    }
}
