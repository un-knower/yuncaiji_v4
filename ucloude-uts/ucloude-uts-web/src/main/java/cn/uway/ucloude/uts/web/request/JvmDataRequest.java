package cn.uway.ucloude.uts.web.request;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.builder.SqlUtil;
import cn.uway.ucloude.data.dataaccess.model.LogicOptType;
import cn.uway.ucloude.utils.StringUtil;

public class JvmDataRequest {
	private String identity;

	private Long startTime;

	private Long endTime;

	public String getWhereSql(List<Object> params) {
		List<String> list = new ArrayList<String>();
		SqlUtil.getWhere("identity", LogicOptType.IsEqualTo, this.identity, list, params);
		if (this.startTime != null) {
			SqlUtil.getWhere("CREATE_TIME", LogicOptType.IsGreaterThanOrEqualTo, new Date(this.startTime), list,
					params);
		}
		if (this.endTime != null) {
			SqlUtil.getWhere("CREATE_TIME", LogicOptType.IsLessThan, new Date(this.endTime), list, params);
		}
		return StringUtil.join(list, " AND ");
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
}
