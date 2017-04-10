package cn.uway.ucloude.uts.web.access.domain;

import java.util.Date;

import cn.uway.ucloude.uts.core.cluster.NodeType;

public class NodeOnOfflineLog {
	  // 日志时间
    private Date logTime;
    // 取值 ONLINE(上线) OFFLINE(离线)
    private String event;
    /**
     * 下面属性来自
     * {@link cn.uway.ucloude.uts.core.cluster.Node}
     */
    private String clusterName;
    private String ip;
    private Integer port;
    private String hostName;
    private String group;
    private Date createTime;
    private Integer threads;
    private String identity;
    private NodeType nodeType;
    private Integer httpCmdPort;
	public Date getLogTime() {
		return logTime;
	}
	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
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
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
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
	public NodeType getNodeType() {
		return nodeType;
	}
	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}
	public Integer getHttpCmdPort() {
		return httpCmdPort;
	}
	public void setHttpCmdPort(Integer httpCmdPort) {
		this.httpCmdPort = httpCmdPort;
	}

}
