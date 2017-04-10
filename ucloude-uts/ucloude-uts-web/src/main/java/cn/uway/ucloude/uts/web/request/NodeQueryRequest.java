package cn.uway.ucloude.uts.web.request;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.builder.SqlUtil;
import cn.uway.ucloude.data.dataaccess.model.LogicOptType;
import cn.uway.ucloude.query.QueryRequest;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.cluster.NodeType;

public class NodeQueryRequest extends QueryRequest {
	private String identity;
	private String ip;
	private String nodeGroup;
	private NodeType nodeType;
	private Boolean available;
	private Date startDate;
	private Date endDate;

	public String getWhereSql(List<Object> params) {
		List<String> list = new ArrayList<String>();
		SqlUtil.getWhere("IP", LogicOptType.IsEqualTo, this.ip, list, params);
		SqlUtil.getWhere("NODE_GROUP", LogicOptType.IsEqualTo, this.nodeGroup, list, params);
		SqlUtil.getWhere("IDENTITY", LogicOptType.IsEqualTo, this.identity, list, params);
		if (this.startDate != null) {
			SqlUtil.getWhere("CREATE_TIME", LogicOptType.IsGreaterThanOrEqualTo,
					new java.sql.Date(this.startDate.getTime()), list, params);
		}
		if (this.endDate != null) {
			SqlUtil.getWhere("CREATE_TIME", LogicOptType.IsLessThan, new java.sql.Date(this.endDate.getTime()), list,
					params);
		}
		SqlUtil.getWhere("NODE_TYPE", LogicOptType.IsEqualTo, this.nodeType.getValue(), list, params);
		SqlUtil.getWhere("AVAILABLE", LogicOptType.IsEqualTo, this.available, list, params);
		return StringUtil.join(list, " AND ");
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getNodeGroup() {
		return nodeGroup;
	}

	public void setNodeGroup(String nodeGroup) {
		this.nodeGroup = nodeGroup;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = NodeType.getNodeType(nodeType);
	}

	public Boolean getAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public NodeQueryRequest() {
		// 默认不分页
	}

}
