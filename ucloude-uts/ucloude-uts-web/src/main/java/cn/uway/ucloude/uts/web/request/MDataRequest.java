package cn.uway.ucloude.uts.web.request;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.builder.SqlUtil;
import cn.uway.ucloude.data.dataaccess.model.LogicOptType;
import cn.uway.ucloude.query.QueryRequest;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.cluster.NodeType;

public class MDataRequest extends QueryRequest {
	private NodeType nodeType;

	private String id;

	private String nodeGroup;

	private String identity;

	private Long startTime;

	private Long endTime;

	private JVMType jvmType;

	public String getWhereSql(List<Object> params) {
		List<String> list = new ArrayList<String>();
		SqlUtil.getWhere("ID", LogicOptType.IsEqualTo, this.id, list, params);
		SqlUtil.getWhere("NODE_GROUP", LogicOptType.IsEqualTo, this.nodeGroup, list, params);
		SqlUtil.getWhere("identity", LogicOptType.IsEqualTo, this.identity, list, params);
		if (this.startTime != null) {
			SqlUtil.getWhere("CREATE_TIME", LogicOptType.IsGreaterThanOrEqualTo, new java.sql.Timestamp(this.startTime), list,
					params);
		}
		if (this.endTime != null) {
			SqlUtil.getWhere("CREATE_TIME", LogicOptType.IsLessThan, new java.sql.Timestamp(this.endTime), list, params);
		}
		SqlUtil.getWhere("node_Type", LogicOptType.IsEqualTo, this.nodeType.getValue(), list, params);
		return StringUtil.join(list, " AND ");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = NodeType.getNodeType(nodeType);
	}

	public JVMType getJvmType() {
		return jvmType;
	}

	public void setJvmType(int jvmType) {
		this.jvmType = JVMType.getJVMType(jvmType);
	}
}
