package cn.uway.ucloude.uts.core.queue.domain;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import cn.uway.ucloude.data.dataaccess.builder.SqlUtil;
import cn.uway.ucloude.data.dataaccess.model.LogicOptType;
import cn.uway.ucloude.query.QueryRequest;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.domain.JobType;

public class JobQueueReq extends QueryRequest {

	public String getWhereSql(List<Object> params) {
		List<String> list = new ArrayList<String>();
		if(StringUtil.isNotEmpty(this.jobId))
			SqlUtil.getWhere("JOB_ID", LogicOptType.IsEqualTo, this.jobId, list, params);
		if(this.jobType != null)
			SqlUtil.getWhere("JOB_TYPE", LogicOptType.IsEqualTo, this.jobType, list, params);
		if(StringUtil.isNotEmpty(this.taskId))
			SqlUtil.getWhere("TASK_ID", LogicOptType.IsEqualTo, this.taskId, list, params);
		if(StringUtil.isNotEmpty(this.realTaskId))
			SqlUtil.getWhere("REAL_TASK_ID", LogicOptType.IsEqualTo, this.realTaskId, list, params);
		if(StringUtil.isNotEmpty(this.submitNodeGroup))
			SqlUtil.getWhere("SUBMIT_NODE_GROUP", LogicOptType.IsEqualTo, this.submitNodeGroup, list, params);
		if(StringUtil.isNotEmpty(this.taskTrackerIdentity))
			SqlUtil.getWhere("TASK_TRACKER_IDENTITY", LogicOptType.IsEqualTo, this.taskTrackerIdentity, list, params);
		if(StringUtil.isNotEmpty(this.taskTrackerNodeGroup))
			SqlUtil.getWhere("TASK_TRACKER_NODE_GROUP", LogicOptType.IsEqualTo, this.taskTrackerNodeGroup, list, params);
		if(this.startGmtCreated != null)
			SqlUtil.getWhere("CREATED_TIME", LogicOptType.IsGreaterThanOrEqualTo, new java.sql.Timestamp(this.startGmtCreated.getTime()), list, params);
		if(this.endGmtCreated != null)
			SqlUtil.getWhere("CREATED_TIME", LogicOptType.IsLessThan, new java.sql.Timestamp(this.endGmtCreated.getTime()), list, params);
		if(this.startGmtModified != null)
			SqlUtil.getWhere("MODIFIED_TIME", LogicOptType.IsGreaterThanOrEqualTo, new java.sql.Timestamp(this.startGmtModified.getTime()), list, params);
		if(this.endGmtModified != null)
			SqlUtil.getWhere("MODIFIED_TIME", LogicOptType.IsLessThan, new java.sql.Timestamp(this.endGmtModified.getTime()), list, params);
		if(this.endTriggerTime != null)
			SqlUtil.getWhere("TRIGGER_TIME", LogicOptType.IsLessThan, new java.sql.Timestamp(this.endTriggerTime.getTime()), list, params);
		if(this.q_needFeedback != null)
			SqlUtil.getWhere("NEED_FEED_BACK", LogicOptType.IsEqualTo, this.q_needFeedback, list, params);
		if(this.isRuning != null)
		SqlUtil.getWhere("IS_RUNNING", LogicOptType.IsEqualTo, this.isRuning, list, params);
		return StringUtil.join(list, " AND ");
	}

	public List<String> getSetFields(List<Object> params) {
		List<String> fields = new ArrayList<String>();
		SqlUtil.getSetFields("CRON_EXPRESSION", this.cronExpression, fields, params);
		SqlUtil.getSetFields("NEED_FEED_BACK", this.needFeedback, fields, params);
		SqlUtil.getSetFields("TRIGGER_TIME", this.triggerTime, fields, params);
		SqlUtil.getSetFields("PRIORITY", this.priority, fields, params);
		SqlUtil.getSetFields("MAX_RETRY_TIMES", this.maxRetryTimes, fields, params);
		SqlUtil.getSetFields("REPEAT_COUNT", this.repeatCount, fields, params);
		SqlUtil.getSetFields("REPEATED_COUNT", this.repeatedCount, fields, params);
		SqlUtil.getSetFields("REPEAT_INTERVAL", this.repeatInterval, fields, params);
		SqlUtil.getSetFields("RELY_ON_PREV_CYCLE", this.relyOnPrevCycle, fields, params);
		SqlUtil.getSetFields("MODIFIED_TIME", this.getModifiedTime(), fields, params);
		SqlUtil.getSetFields("LAST_GENERATE_TRIGGER_TIME", this.getLastGenerateTriggerTime(), fields, params);
		SqlUtil.getSetFields("IS_RUNNING", this.isRuning(), fields, params);
		return fields;
	}

	/**
	 * 清空更新的字段
	 */
	public void clearUpdateValue() {
		this.cronExpression = null;
		this.needFeedback = null;
		this.extParams = null;
		this.triggerTime = null;
		this.priority = null;
		this.maxRetryTimes = null;
		this.repeatCount = null;
		this.repeatedCount = null;
		this.repeatInterval = null;
		this.relyOnPrevCycle = null;
		this.modifiedTime = null;
		this.lastGenerateTriggerTime = null;
	}

	/**
	 * 清空选择的字段
	 */
	public void clearSelectValue() {
		this.jobId = null;
		this.jobType = null;
		this.taskId = null;
		this.realTaskId = null;
		this.submitNodeGroup = null;
		this.taskTrackerNodeGroup = null;
		this.setTaskTrackerIdentity(null);

	}

	// ------------ 下面是查询条件值 ---------------
	private String jobId;
	private JobType jobType;
	private String taskId;
	private String realTaskId;

	private String submitNodeGroup;

	private String taskTrackerIdentity;
	private String taskTrackerNodeGroup;

	private Date startGmtCreated;
	private Date endGmtCreated;
	private Date startGmtModified;
	private Date endGmtModified;
	private Date endTriggerTime;
	private Boolean q_needFeedback;

	// ------------ 下面是能update的值 -------------------

	private String cronExpression;

	private Boolean needFeedback;

	private Map<String, String> extParams;

	private Date triggerTime;

	private Integer priority;

	private Integer maxRetryTimes;

	private Integer repeatCount;

	private Integer repeatedCount;

	private Long repeatInterval;

	private Boolean relyOnPrevCycle;

	private Long modifiedTime;

	private Long lastGenerateTriggerTime;

	private Boolean isRuning;

	public JobType getJobType() {
		return jobType;
	}

	public void setJobType(int jobType) {
		this.jobType = JobType.getJobType(jobType);
	}
	
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getSubmitNodeGroup() {
		return submitNodeGroup;
	}

	public void setSubmitNodeGroup(String submitNodeGroup) {
		this.submitNodeGroup = submitNodeGroup;
	}

	public String getTaskTrackerNodeGroup() {
		return taskTrackerNodeGroup;
	}

	public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
		this.taskTrackerNodeGroup = taskTrackerNodeGroup;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public Boolean getNeedFeedback() {
		return needFeedback;
	}

	public void setNeedFeedback(Boolean needFeedback) {
		this.needFeedback = needFeedback;
	}

	public Map<String, String> getExtParams() {
		return extParams;
	}

	public void setExtParams(Map<String, String> extParams) {
		this.extParams = extParams;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Date getStartGmtCreated() {
		return startGmtCreated;
	}

	public void setStartGmtCreated(Date startGmtCreated) {
		this.startGmtCreated = startGmtCreated;
	}

	public Date getEndGmtCreated() {
		return endGmtCreated;
	}

	public void setEndGmtCreated(Date endGmtCreated) {
		this.endGmtCreated = endGmtCreated;
	}

	public Date getStartGmtModified() {
		return startGmtModified;
	}

	public void setStartGmtModified(Date startGmtModified) {
		this.startGmtModified = startGmtModified;
	}

	public Date getEndGmtModified() {
		return endGmtModified;
	}

	public void setEndGmtModified(Date endGmtModified) {
		this.endGmtModified = endGmtModified;
	}

	public Date getTriggerTime() {
		return triggerTime;
	}

	public void setTriggerTime(Date triggerTime) {
		this.triggerTime = triggerTime;
	}

	public Integer getMaxRetryTimes() {
		return maxRetryTimes;
	}

	public void setMaxRetryTimes(Integer maxRetryTimes) {
		this.maxRetryTimes = maxRetryTimes;
	}

	public Integer getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(Integer repeatCount) {
		this.repeatCount = repeatCount;
	}

	public Long getRepeatInterval() {
		return repeatInterval;
	}

	public void setRepeatInterval(Long repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

	public Boolean getRelyOnPrevCycle() {
		return relyOnPrevCycle;
	}

	public void setRelyOnPrevCycle(Boolean relyOnPrevCycle) {
		this.relyOnPrevCycle = relyOnPrevCycle;
	}

	public String getRealTaskId() {
		return realTaskId;
	}

	public void setRealTaskId(String realTaskId) {
		this.realTaskId = realTaskId;
	}

	public Long getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(Long modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	public Long getLastGenerateTriggerTime() {
		return lastGenerateTriggerTime;
	}

	public void setLastGenerateTriggerTime(Long lastGenerateTriggerTime) {
		this.lastGenerateTriggerTime = lastGenerateTriggerTime;
	}

	public Date getEndTriggerTime() {
		return endTriggerTime;
	}

	public void setEndTriggerTime(Date endTriggerTime) {
		this.endTriggerTime = endTriggerTime;
	}

	public String getTaskTrackerIdentity() {
		return taskTrackerIdentity;
	}

	public void setTaskTrackerIdentity(String taskTrackerIdentity) {
		this.taskTrackerIdentity = taskTrackerIdentity;
	}

	public Integer getRepeatedCount() {
		return repeatedCount;
	}

	public void setRepeatedCount(Integer repeatedCount) {
		this.repeatedCount = repeatedCount;
	}

	public Boolean isRuning() {
		return isRuning;
	}

	public void setRuning(boolean isRuning) {
		this.isRuning = isRuning;
	}

	public Boolean getQ_needFeedback() {
		return q_needFeedback;
	}

	public void setQ_needFeedback(Boolean q_needFeedback) {
		this.q_needFeedback = q_needFeedback;
	}
}
