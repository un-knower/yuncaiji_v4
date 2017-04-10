package cn.uway.ucloude.uts.biz.logger.db;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.ResultSetHandler;
import cn.uway.ucloude.data.dataaccess.builder.InsertSql;
import cn.uway.ucloude.data.dataaccess.builder.OrderByType;
import cn.uway.ucloude.data.dataaccess.builder.SelectSql;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.data.dataaccess.builder.WhereSql;

import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.serialize.TypeReference;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.biz.logger.JobLogger;
import cn.uway.ucloude.uts.biz.logger.domain.*;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.domain.JobType;

public class DbJobLogger extends JdbcAbstractAccess implements JobLogger {

	public DbJobLogger(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void log(JobLogPo jobLogPo) {
		// TODO Auto-generated method stub
		if (jobLogPo == null) {
            return;
        }
        InsertSql insertSql = buildInsertSql();

        setInsertSqlValues(insertSql, jobLogPo).doInsert();
	}

	@Override
	public void log(List<JobLogPo> jobLogPos) {
		// TODO Auto-generated method stub
		if (CollectionUtil.isEmpty(jobLogPos)) {
            return;
        }

        InsertSql insertSql = buildInsertSql();

        for (JobLogPo jobLogPo : jobLogPos) {
            setInsertSqlValues(insertSql, jobLogPo);
        }
        insertSql.doBatchInsert();
	}

	@Override
	public Pagination<JobLogPo> search(JobLoggerRequest request) {
		// TODO Auto-generated method stub
		Pagination<JobLogPo> response = new Pagination<JobLogPo>();

        BigDecimal results = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql()
                .select()
                .columns("count(1)")
                .from()
                .table("mod_job_biz_logger")
                .whereSql(buildWhereSql(request))
                .single();
        response.setTotal(results.intValue());
        if (response.getTotal()==0) {
            return response;
        }
        // 查询 rows
        List<JobLogPo> rows = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql()
                .select()
                .all()
                .from()
                .table("mod_job_biz_logger")
                .whereSql(buildWhereSql(request))
                .orderBy()
                .column("log_time", OrderByType.DESC)
                .page(request.getPage(), request.getPageSize())
                .list(JOB_LOGGER_LIST_RSH);
        response.setData(rows);

        return response;
	}
	
	private WhereSql buildWhereSql(JobLoggerRequest request) {
	        WhereSql sql= SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getWhereSql();
	        if(StringUtil.isNotEmpty(request.getTaskId()))
	                sql.andOnNotEmpty("task_id = ?", request.getTaskId());
	        if(StringUtil.isNotEmpty(request.getRealTaskId()))
	                sql.andOnNotEmpty("real_task_id = ?", request.getRealTaskId());
	        if(StringUtil.isNotEmpty(request.getTaskTrackerNodeGroup()))
	                sql.andOnNotEmpty("task_tracker_node_group = ?", request.getTaskTrackerNodeGroup());
	        if(request.getStartLogTime() != null && request.getEndLogTime() != null)
	                sql.andBetween("log_time", new java.sql.Timestamp(request.getStartLogTime()), new java.sql.Timestamp(request.getEndLogTime()));
	                return sql;
	}
	
	private InsertSql buildInsertSql() {
        return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql()
                .insert("mod_job_biz_logger")
                .columns("LOG_TIME",
                        "GMT_CREATED",
                        "LOG_TYPE",
                        "SUCCESS",
                        "MSG",
                        "TASK_TRACKER_IDENTITY",
                        "LOG_LEVEL",
                        "TASK_ID",
                        "REAL_TASK_ID",
                        "JOB_ID",
                        "JOB_TYPE",
                        "PRIORITY",
                        "SUBMIT_NODE_GROUP",
                        "TASK_TRACKER_NODE_GROUP",
                        "EXT_PARAMS",
                        "INTERNAL_EXT_PARAMS",
                        "NEED_FEEDBACK",
                        "CRON_EXPRESSION",
                        "TRIGGER_TIME",
                        "RETRY_TIMES",
                        "MAX_RETRY_TIMES",
                        "RELY_ON_PREV_CYCLE",
                        "REPEAT_COUNT",
                        "REPEATED_COUNT",
                        "REPEAT_INTERVAL"
                );
    }

	
	private InsertSql setInsertSqlValues(InsertSql insertSql, JobLogPo jobLogPo){
		java.sql.Timestamp tiggerTime =null;
		if(jobLogPo.getTriggerTime()!=null){
			tiggerTime = new java.sql.Timestamp(jobLogPo.getTriggerTime());
		}
		insertSql.values(new java.sql.Timestamp(jobLogPo.getLogTime()),
				new java.sql.Timestamp(jobLogPo.getGmtCreated()),
                jobLogPo.getLogType().getValue(),
                jobLogPo.isSuccess(),
                jobLogPo.getMsg(),
                jobLogPo.getTaskTrackerIdentity(),
                jobLogPo.getLevel().ordinal(),
                jobLogPo.getTaskId(),
                jobLogPo.getRealTaskId(),
                jobLogPo.getJobId(),
                jobLogPo.getJobType() == null ? null : jobLogPo.getJobType().getValue(),
                jobLogPo.getPriority(),
                jobLogPo.getSubmitNodeGroup(),
                jobLogPo.getTaskTrackerNodeGroup(),
                jobLogPo.getExtParams()!=null&&jobLogPo.getExtParams().size()>0? JsonConvert.serialize(jobLogPo.getExtParams()):null,
                jobLogPo.getInternalExtParams()!=null&&jobLogPo.getInternalExtParams().size()>0? JsonConvert.serialize(jobLogPo.getInternalExtParams()):null,
                jobLogPo.isNeedFeedback(),
                jobLogPo.getCronExpression(),
                tiggerTime,
                jobLogPo.getRetryTimes(),
                jobLogPo.getMaxRetryTimes(),
                jobLogPo.getDepPreCycle(),
                jobLogPo.getRepeatCount(),
                jobLogPo.getRepeatedCount(),
                jobLogPo.getRepeatInterval());
		return insertSql;
	}
	
	private static final ResultSetHandler<List<JobLogPo>> JOB_LOGGER_LIST_RSH = new ResultSetHandler<List<JobLogPo>>() {
        @Override
        public List<JobLogPo> handle(ResultSet rs) throws SQLException {
            List<JobLogPo> result = new ArrayList<JobLogPo>();
            while (rs.next()) {
                JobLogPo jobLogPo = new JobLogPo();
                jobLogPo.setLogTime(rs.getTimestamp("LOG_TIME").getTime());
                jobLogPo.setGmtCreated(rs.getTimestamp("GMT_CREATED").getTime());
                jobLogPo.setLogType(LogType.getLogType(rs.getInt("LOG_TYPE")));
                jobLogPo.setSuccess(rs.getBoolean("SUCCESS"));
                jobLogPo.setMsg(rs.getString("MSG"));
                jobLogPo.setTaskTrackerIdentity(rs.getString("TASK_TRACKER_IDENTITY"));
                jobLogPo.setLevel(Level.values()[rs.getInt("LOG_LEVEL")]);
                jobLogPo.setTaskId(rs.getString("TASK_ID"));
                jobLogPo.setRealTaskId(rs.getString("REAL_TASK_ID"));
                int jobType = rs.getInt("JOB_TYPE");
                if (jobType>0) {
                    jobLogPo.setJobType(JobType.getJobType(jobType));
                }
                jobLogPo.setJobId(rs.getString("JOB_ID"));
                jobLogPo.setPriority(rs.getInt("PRIORITY"));
                jobLogPo.setSubmitNodeGroup(rs.getString("SUBMIT_NODE_GROUP"));
                jobLogPo.setTaskTrackerNodeGroup(rs.getString("TASK_TRACKER_NODE_GROUP"));
                jobLogPo.setExtParams(JsonConvert.deserialize(rs.getString("EXT_PARAMS"), new TypeReference<Map<String, String>>() {
                }));
                jobLogPo.setInternalExtParams(JsonConvert.deserialize(rs.getString("INTERNAL_EXT_PARAMS"), new TypeReference<HashMap<String, String>>() {
                }));
                jobLogPo.setNeedFeedback(rs.getBoolean("NEED_FEEDBACK"));
                jobLogPo.setCronExpression(rs.getString("CRON_EXPRESSION"));
                jobLogPo.setTriggerTime(rs.getTimestamp("TRIGGER_TIME")==null?null: rs.getTimestamp("TRIGGER_TIME").getTime());
                jobLogPo.setRetryTimes(rs.getInt("RETRY_TIMES"));
                jobLogPo.setMaxRetryTimes(rs.getInt("MAX_RETRY_TIMES"));
                jobLogPo.setDepPreCycle(rs.getBoolean("RELY_ON_PREV_CYCLE"));
                jobLogPo.setRepeatCount(rs.getInt("REPEAT_COUNT"));
                jobLogPo.setRepeatedCount(rs.getInt("REPEATED_COUNT"));
                jobLogPo.setRepeatInterval(rs.getLong("REPEAT_INTERVAL"));
                result.add(jobLogPo);
            }
            return result;
        }
    };

}
