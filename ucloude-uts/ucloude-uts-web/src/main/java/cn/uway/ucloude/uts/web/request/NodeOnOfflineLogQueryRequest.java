package cn.uway.ucloude.uts.web.request;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.builder.SqlUtil;
import cn.uway.ucloude.data.dataaccess.model.LogicOptType;
import cn.uway.ucloude.query.QueryRequest;
import cn.uway.ucloude.utils.StringUtil;

public class NodeOnOfflineLogQueryRequest extends QueryRequest {
	private Date startLogTime;

	private Date endLogTime;

	private String group;

	private String identity;

	private String event;

	public String getWhereSql(List<Object> params) {
		List<String> list = new ArrayList<String>();
		SqlUtil.getWhere("NODE_GROUP", LogicOptType.IsEqualTo, this.group, list, params);
		SqlUtil.getWhere("IDENTITY", LogicOptType.IsEqualTo, this.identity, list, params);
		SqlUtil.getWhere("EVENT", LogicOptType.IsEqualTo, this.event, list, params);
		if (this.startLogTime != null) {
			SqlUtil.getWhere("CREATE_TIME", LogicOptType.IsGreaterThanOrEqualTo,
					new java.sql.Timestamp(this.startLogTime.getTime()), list, params);
		}
		if (this.endLogTime != null) {
			SqlUtil.getWhere("CREATE_TIME", LogicOptType.IsLessThan, new java.sql.Timestamp(this.endLogTime.getTime()), list,
					params);
		}
		return StringUtil.join(list, " AND ");
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Date getStartLogTime() {
		return startLogTime;
	}

	public void setStartLogTime(Date startLogTime) {
		this.startLogTime = startLogTime;
	}

	public Date getEndLogTime() {
		return endLogTime;
	}

	public void setEndLogTime(Date endLogTime) {
		this.endLogTime = endLogTime;
	}
}
