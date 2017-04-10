package cn.uway.ucloude.uts.core.queue.implement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.uway.ucloude.data.dataaccess.ResultSetHandler;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.serialize.TypeReference;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.LogType;
import cn.uway.ucloude.uts.core.cluster.NodeType;
import cn.uway.ucloude.uts.core.domain.JobRunResult;
import cn.uway.ucloude.uts.core.domain.JobType;
import cn.uway.ucloude.uts.core.queue.domain.ExtParamsInfo;
import cn.uway.ucloude.uts.core.queue.domain.JobFeedbackPo;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.domain.NodeGroupPo;
import  cn.uway.ucloude.uts.core.domain.Level;

class RshHolder {
	public static final ResultSetHandler<JobPo> JOB_PO_RSH = new ResultSetHandler<JobPo>() {
        @Override
        public JobPo handle(ResultSet rs) throws SQLException {
            if (!rs.next()) {
                return null;
            }
            return getJobPo(rs);
        }
    };

    public static final ResultSetHandler<List<JobPo>> JOB_PO_LIST_RSH = new ResultSetHandler<List<JobPo>>() {
        @Override
        public List<JobPo> handle(ResultSet rs) throws SQLException {
            List<JobPo> jobPos = new ArrayList<JobPo>();
            while (rs.next()) {
                jobPos.add(getJobPo(rs));
            }
            return jobPos;
        }
    };
    
    public static ResultSetHandler<Map<String,String>> JOB_EXT_PARAMS = new ResultSetHandler<Map<String,String>>(){
    	 @Override
         public Map<String,String> handle(ResultSet rs) throws SQLException {
    		 Map<String,String> maps = new HashMap<String,String>() ;
             while (rs.next()) {
                 getExtParam(rs,maps);
             }
             return maps;
         }
    };
    
    private static void getExtParam(ResultSet rs,Map<String,String> map) throws SQLException{
    	map.put(rs.getString("key"), rs.getString("value"));
    }

    private static JobPo getJobPo(ResultSet rs) throws SQLException {
        JobPo jobPo = new JobPo();
        jobPo.setJobId(rs.getString("job_id"));
        jobPo.setPriority(rs.getInt("priority"));
        jobPo.setLastGenerateTriggerTime(rs.getTimestamp("last_generate_trigger_time") == null?null:rs.getTimestamp("last_generate_trigger_time").getTime());
        jobPo.setRetryTimes(rs.getInt("retry_times"));
        jobPo.setMaxRetryTimes(rs.getInt("max_retry_times"));
        jobPo.setRelyOnPrevCycle(rs.getBoolean("rely_on_prev_cycle"));
//        jobPo.setInternalExtParams(JsonConvert.deserialize(rs.getString("internal_ext_params"), new TypeReference<HashMap<String, String>>() {
//        }));
        jobPo.setTaskId(rs.getString("task_id"));
        jobPo.setRealTaskId(rs.getString("real_task_id"));
        jobPo.setGmtCreated(rs.getTimestamp("created_time").getTime());
        jobPo.setGmtModified(rs.getTimestamp("MODIFIED_TIME").getTime());
        jobPo.setSubmitNodeGroup(rs.getString("submit_node_group"));
        jobPo.setTaskTrackerNodeGroup(rs.getString("task_tracker_node_group"));
        //jobPo.setExtParams(JsonConvert.deserialize(rs.getString("ext_params"), new TypeReference<HashMap<String, String>>() {
        //}));
        jobPo.setJobType(JobType.getJobType(rs.getInt("job_type"))); ;
       
        jobPo.setIsRunning(rs.getBoolean("IS_RUNNING"));
        jobPo.setTaskTrackerIdentity(rs.getString("task_tracker_identity"));
        jobPo.setCronExpression(rs.getString("cron_expression"));
        jobPo.setNeedFeedback(rs.getBoolean("NEED_FEED_BACK"));
        jobPo.setTriggerTime(rs.getTimestamp("trigger_time") == null?null:rs.getTimestamp("trigger_time").getTime());
        jobPo.setRepeatCount(rs.getInt("repeat_count"));
        jobPo.setRepeatedCount(rs.getInt("repeated_count"));
        jobPo.setRepeatInterval(rs.getLong("repeat_interval"));
        return jobPo;
    }

    public static final ResultSetHandler<List<JobFeedbackPo>> JOB_FEED_BACK_LIST_RSH = new ResultSetHandler<List<JobFeedbackPo>>() {
        @Override
        public List<JobFeedbackPo> handle(ResultSet rs) throws SQLException {
            List<JobFeedbackPo> jobFeedbackPos = new ArrayList<JobFeedbackPo>();
            while (rs.next()) {
                JobFeedbackPo jobFeedbackPo = new JobFeedbackPo();
                jobFeedbackPo.setId(rs.getString("id"));
                jobFeedbackPo.setJobRunResult(JsonConvert.deserialize(rs.getString("job_result"), new TypeReference<JobRunResult>() {
                }));
                jobFeedbackPo.setNodeGroup(rs.getString("NODE_GROUP"));
                jobFeedbackPo.setGmtCreated(rs.getTimestamp("CREATED_TIME").getTime());
                jobFeedbackPos.add(jobFeedbackPo);
            }
            return jobFeedbackPos;
        }
    };

    public static final ResultSetHandler<List<NodeGroupPo>> NODE_GROUP_LIST_RSH = new ResultSetHandler<List<NodeGroupPo>>() {
        @Override
        public List<NodeGroupPo> handle(ResultSet rs) throws SQLException {
            List<NodeGroupPo> list = new ArrayList<NodeGroupPo>();
            while (rs.next()) {
                NodeGroupPo nodeGroupPo = new NodeGroupPo();
                nodeGroupPo.setNodeType(NodeType.getNodeType(rs.getInt("node_type")));
                nodeGroupPo.setName(rs.getString("name"));
                nodeGroupPo.setGmtCreated(rs.getTimestamp("CREATE_TIME").getTime());
                list.add(nodeGroupPo);
            }
            return list;
        }
    };
    
    public static final ResultSetHandler<List<ExtParamsInfo>> EXT_PARAM_LIST_RSH = new ResultSetHandler<List<ExtParamsInfo>>() {
		@Override
		public List<ExtParamsInfo> handle(ResultSet rs) throws SQLException {
			List<ExtParamsInfo> result = new ArrayList<ExtParamsInfo>();
			while (rs.next()) {
				ExtParamsInfo param = new ExtParamsInfo();
				param.setId(rs.getString("ID"));
				param.setTypeId(rs.getInt("TYPE_ID"));
				param.setKey(rs.getString("KEY"));
				param.setValue(rs.getString("VALUE"));
				result.add(param);
			}
			return result;
		}
	};

    public static final ResultSetHandler<List<JobLogPo>> JOB_LOGGER_LIST_RSH = new ResultSetHandler<List<JobLogPo>>() {
        @Override
        public List<JobLogPo> handle(ResultSet rs) throws SQLException {
            List<JobLogPo> result = new ArrayList<JobLogPo>();
            while (rs.next()) {
                JobLogPo jobLogPo = new JobLogPo();
                jobLogPo.setLogTime(rs.getTimestamp("log_time").getTime());
                jobLogPo.setGmtCreated(rs.getTimestamp("CREATED_TIME").getTime());
                jobLogPo.setLogType(LogType.getLogType(rs.getInt("log_type")));
                jobLogPo.setSuccess(rs.getBoolean("success"));
                jobLogPo.setMsg(rs.getString("msg"));
                jobLogPo.setTaskTrackerIdentity(rs.getString("task_tracker_identity"));
                jobLogPo.setLevel(Level.valueOf(rs.getString("level")));
                jobLogPo.setTaskId(rs.getString("task_id"));
                jobLogPo.setRealTaskId(rs.getString("real_task_id"));
                
                jobLogPo.setJobType(JobType.getJobType(rs.getInt("job_type")));
                jobLogPo.setJobId(rs.getString("job_id"));
                jobLogPo.setPriority(rs.getInt("priority"));
                jobLogPo.setSubmitNodeGroup(rs.getString("submit_node_group"));
                jobLogPo.setTaskTrackerNodeGroup(rs.getString("task_tracker_node_group"));
                jobLogPo.setExtParams(JsonConvert.deserialize(rs.getString("ext_params"), new TypeReference<Map<String, String>>() {
                }));
                jobLogPo.setInternalExtParams(JsonConvert.deserialize(rs.getString("internal_ext_params"), new TypeReference<HashMap<String, String>>() {
                }));
                jobLogPo.setNeedFeedback(rs.getBoolean("need_feedback"));
                jobLogPo.setCronExpression(rs.getString("cron_expression"));
                jobLogPo.setTriggerTime(rs.getTimestamp("trigger_time").getTime());
                jobLogPo.setRetryTimes(rs.getInt("retry_times"));
                jobLogPo.setMaxRetryTimes(rs.getInt("max_retry_times"));
                jobLogPo.setDepPreCycle(rs.getBoolean("rely_on_prev_cycle"));
                jobLogPo.setRepeatCount(rs.getInt("repeat_count"));
                jobLogPo.setRepeatedCount(rs.getInt("repeated_count"));
                jobLogPo.setRepeatInterval(rs.getLong("repeat_interval"));
                result.add(jobLogPo);
            }
            return result;
        }
    };
}
