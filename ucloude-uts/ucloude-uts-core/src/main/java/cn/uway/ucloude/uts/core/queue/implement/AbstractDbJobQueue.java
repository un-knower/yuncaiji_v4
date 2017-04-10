package cn.uway.ucloude.uts.core.queue.implement;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Select;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.ResultSetHandler;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.data.dataaccess.builder.UpdateSql;
import cn.uway.ucloude.data.dataaccess.builder.WhereSql;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.data.dataaccess.builder.InsertSql;
import cn.uway.ucloude.data.dataaccess.builder.OrderByType;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.utils.Assert;
import cn.uway.ucloude.uts.core.queue.JobQueue;
import cn.uway.ucloude.uts.core.queue.domain.ExtParamsInfo;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.domain.JobQueueReq;

public abstract class AbstractDbJobQueue extends JdbcAbstractAccess implements JobQueue {

	private static final ILogger LOGGER = LoggerManager.getLogger(AbstractDbJobQueue.class);

	public AbstractDbJobQueue(String connectionKey) {
		super(connectionKey);
	}

	protected boolean add(String tableName, JobPo jobPo) {
		boolean result = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql().insert(tableName)
				.columns("job_id", "job_type", "priority", "retry_times", "max_retry_times", "rely_on_prev_cycle",
						"task_id", "real_task_id", "CREATED_TIME", "MODIFIED_TIME", "submit_node_group",
						"task_tracker_node_group",
						// "ext_params",
						// "internal_ext_params",
						"IS_RUNNING", "task_tracker_identity", "NEED_FEED_BACK", "cron_expression", "trigger_time",
						"repeat_count", "repeated_count", "repeat_interval")
				.values(jobPo.getJobId(), jobPo.getJobType() == null ? null : jobPo.getJobType().getValue(),
						jobPo.getPriority(), jobPo.getRetryTimes(), jobPo.getMaxRetryTimes(),
						jobPo.getRelyOnPrevCycle(), jobPo.getTaskId(), jobPo.getRealTaskId(),
						new java.sql.Timestamp(jobPo.getGmtCreated()), new java.sql.Timestamp(jobPo.getGmtModified()),
						jobPo.getSubmitNodeGroup(), jobPo.getTaskTrackerNodeGroup(),
						// JSON.toJSONString(jobPo.getExtParams()),
						// JSON.toJSONString(jobPo.getInternalExtParams()),
						jobPo.isRunning(), jobPo.getTaskTrackerIdentity(), jobPo.isNeedFeedback(),
						jobPo.getCronExpression(),
						jobPo.getTriggerTime() == null ? null : new java.sql.Timestamp(jobPo.getTriggerTime()),
						jobPo.getRepeatCount(), jobPo.getRepeatedCount(), jobPo.getRepeatInterval())
				.doInsert() == 1;
		boolean returnValue = false;
		if (result == true) {
			BigDecimal count = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select()
					.columns("count(1)").from().table("uts_job_extparams").where("ID=?", jobPo.getJobId()).single();
			if (count.longValue() < 1) {
				addExtParams(jobPo.getJobId(), jobPo.getExtParams(), jobPo.getInternalExtParam());
			}

			returnValue = true;
		}
		return returnValue;
	}

	@Override
	public Pagination<JobPo> pageSelect(JobQueueReq request) {
		// TODO Auto-generated method stub
		Pagination<JobPo> response = new Pagination<JobPo>();

		WhereSql whereSql = buildWhereSql(request);

		BigDecimal results = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select()
				.columns("count(1)").from().table(getTableName()).whereSql(whereSql).single();
		response.setTotal(results.longValue());

		if (response.getTotal() > 0) {

			List<JobPo> jobPos = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().all().from()
					.table(getTableName()).whereSql(whereSql).orderBy()
					.column(request.getField(), OrderByType.convert(request.getDirection()))
					.page(request.getPage(), request.getPageSize()).list(RshHolder.JOB_PO_LIST_RSH);
			response.setData(jobPos);
		}
		return response;
	}

	public JobPo getJob(String jobId) {
		JobPo jobInfo = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().all().from()
				.table(getTableName()).where("job_id = ?", jobId).single(RshHolder.JOB_PO_RSH);
		if (jobInfo != null) {
			getExtParams(jobInfo);
		}
		return jobInfo;
	}

	@Override
	public boolean selectiveUpdateByJobId(JobQueueReq request) {
		Assert.hasLength(request.getJobId(), "Only allow update by jobId");

		UpdateSql sql = buildUpdateSqlPrefix(request);

		boolean result = sql.where("job_id=?", request.getJobId()).doUpdate() == 1;

		return result;
	}

	private void addExtParams(String jobID, Map<String, String> extParams, Map<String, String> internalParams) {

		boolean result = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql().delete().from()
				.table("uts_job_extparams").where("ID=?", jobID).doDelete() > 0;
		InsertSql insertSql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql()
				.insert("uts_job_extparams").columns("ID", "TYPE_ID", "KEY", "VALUE");
		if (extParams != null && extParams.size() > 0)
			initInsertExtParamsValue(jobID, 0, extParams, insertSql);
		if (internalParams != null && internalParams.size() > 0)
			initInsertExtParamsValue(jobID, 1, internalParams, insertSql);
		if (insertSql.getParams() != null && insertSql.getParams().size() > 0)
			insertSql.doBatchInsert();

	}

	private void initInsertExtParamsValue(String jobID, int typeID, Map<String, String> param, InsertSql insertSql) {
		for (Map.Entry<String, String> entry : param.entrySet()) {
			insertSql.values(jobID, typeID, entry.getKey(), entry.getValue());
		}
	}

	@Override
	public boolean selectiveUpdateByTaskId(JobQueueReq request) {
		Assert.hasLength(request.getRealTaskId(), "Only allow update by realTaskId and taskTrackerNodeGroup");
		Assert.hasLength(request.getTaskTrackerNodeGroup(), "Only allow update by realTaskId and taskTrackerNodeGroup");

		UpdateSql sql = buildUpdateSqlPrefix(request);
		return sql.where("real_task_id = ?", request.getRealTaskId())
				.and("task_tracker_node_group = ?", request.getTaskTrackerNodeGroup()).doUpdate() == 1;
	}

	protected abstract String getTableName();

	private UpdateSql buildUpdateSqlPrefix(JobQueueReq request) {
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getUpdateSql().update().table(getTableName())
				.setOnNotNull("cron_expression", request.getCronExpression())
				.setOnNotNull("need_feed_back", request.getNeedFeedback())
				// .setOnNotNull("ext_params",
				// JsonConvert.serialize(request.getExtParams()))
				.setOnNotNull("trigger_time", request.getTriggerTime()==null?null: new java.sql.Timestamp(request.getTriggerTime().getTime()))
				.setOnNotNull("priority", request.getPriority())
				.setOnNotNull("max_retry_times", request.getMaxRetryTimes())
				.setOnNotNull("rely_on_prev_cycle",
						request.getRelyOnPrevCycle() == null ? true : request.getRelyOnPrevCycle())
				.setOnNotNull("submit_node_group", request.getSubmitNodeGroup())
				.setOnNotNull("task_tracker_node_group", request.getTaskTrackerNodeGroup())
				.setOnNotNull("repeat_count", request.getRepeatCount())
				.setOnNotNull("repeat_interval", request.getRepeatInterval())
				.setOnNotNull("MODIFIED_TIME", new java.sql.Timestamp(SystemClock.now()));
	}

	private WhereSql buildWhereSql(JobQueueReq request) {
		WhereSql whereSql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getWhereSql()
				.andOnNotEmpty("job_id = ?", request.getJobId()).andOnNotEmpty("task_id = ?", request.getTaskId())
				.andOnNotEmpty("real_task_id = ?", request.getRealTaskId())
				.andOnNotEmpty("task_tracker_node_group = ?", request.getTaskTrackerNodeGroup());
		if (request.getJobType() != null)
			whereSql.andOnNotEmpty("job_type = ?",
					request.getJobType() == null ? null : request.getJobType().getValue());
		whereSql.andOnNotEmpty("submit_node_group = ?", request.getSubmitNodeGroup()).andOnNotNull("need_feed_back = ?",
				request.getNeedFeedback());
		if (request.getStartGmtCreated() != null && request.getEndGmtCreated() != null)
			whereSql.andBetween("CREATED_TIME", new java.sql.Timestamp(request.getStartGmtCreated().getTime()),
					new java.sql.Timestamp(request.getEndGmtCreated().getTime()));
		if (request.getStartGmtModified() != null && request.getEndGmtModified() != null)
			whereSql.andBetween("MODIFIED_TIME", new java.sql.Timestamp(request.getStartGmtModified().getTime()),
					new java.sql.Timestamp(request.getEndGmtModified().getTime()));
		return whereSql;
	}

	private JobPo getExtParams(JobPo jobPo) {
		List<ExtParamsInfo> params = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select().all()
				.from().table("uts_job_extparams").where("ID=?", jobPo.getJobId()).list(RshHolder.EXT_PARAM_LIST_RSH);
		for (ExtParamsInfo param : params) {
			if (param.getTypeId() == 1) {
				jobPo.setInternalExtParam(param.getKey(), param.getValue());
			} else {
				jobPo.setExtParams(param.getKey(), param.getValue());
			}
		}
		return jobPo;
	}
}
