package cn.uway.ucloude.uts.core.queue.implement;

import java.util.List;
import java.util.Map;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.SqlTemplateFactory;
import cn.uway.ucloude.data.dataaccess.builder.OrderByType;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.ec.IEventCenter;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.core.queue.AbstractPreLoader;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;

public class DbPreLoader extends AbstractPreLoader {
	private static final ILogger LOGGER = LoggerManager.getLogger(DbPreLoader.class);
	private SqlTemplate sqlTemplate;
	protected SqlTemplate getSqlTemplate() {
		return sqlTemplate;
	}

	public DbPreLoader(String connKey, int loadSize, double factor, long interval, String identity,
			IEventCenter eventCenter) {
		super(connKey, loadSize, factor, interval, identity, eventCenter);
		this.sqlTemplate = SqlTemplateFactory.create(connKey);
	}
	
	protected String getTableName(){
		return "uts_Excutable_job";
	}

	//private static final ILogger LOGGER = LoggerManager.getLogger(DbPreLoader.class);

	/* 获取等待执行队列中的任务
	 * @see cn.uway.ucloude.uts.core.queue.AbstractPreLoader#getJob(java.lang.String, java.lang.String)
	 */
	@Override
	protected JobPo getJob(String taskTrackerNodeGroup, String jobId) {
		JobPo jobPo = SqlBuilderFactory.getSqlFactory(getSqlTemplate())
				.getSelectSql()
				.select()
	         .all()
	         .from()
	         .table(getTableName())
	         .where("job_id = ?", jobId)
	         .and("task_tracker_node_group = ?", taskTrackerNodeGroup)
	         .single(RshHolder.JOB_PO_RSH);
		jobPo.setExtParams(getExtParam(jobId,0));
		jobPo.setInternalExtParam(getExtParam(jobId,1));
		return jobPo;
	}
	
	private Map<String,String> getExtParam(String jobId, int type){
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate())
		.getSelectSql()
		.select()
	     .all()
	     .from()
	     .table("uts_job_extparams")
	     .where("id = ?", jobId)
	     .and("type_ID=?", type)
	     .single(RshHolder.JOB_EXT_PARAMS);
	}

	/* 设置任务为运行中 并更新时间
	 * @see cn.uway.ucloude.uts.core.queue.AbstractPreLoader#lockJob(java.lang.String, java.lang.String, java.lang.String, java.lang.Long, java.lang.Long)
	 */
	@Override
	protected boolean lockJob(String taskTrackerNodeGroup, String jobId, String taskTrackerIdentity, Long triggerTime,
			Long gmtModified) {
		  try {
	            return SqlBuilderFactory.getSqlFactory(getSqlTemplate())
	            		.getUpdateSql()
	                    .update()
	                    .table(getTableName())
	                    .set("is_running", true)
	                    .set("task_tracker_identity", taskTrackerIdentity)
	                    .set("CREATED_TIME", new java.sql.Timestamp(SystemClock.now()))
	                    .where("job_id = ?", jobId)
	                    .and("is_running = ?", false)
	                    .and("trigger_time = ?", new java.sql.Timestamp(triggerTime))
	                    .and("task_tracker_node_group=?",taskTrackerNodeGroup)
	                    .and("MODIFIED_TIME = ?", new java.sql.Timestamp(gmtModified))
	                    .doUpdate() == 1;
	        } catch (Exception e) {
	            LOGGER.error("Error when lock job:" + e.getMessage(), e);
	            return false;
	        }
	}

	/* 获取任务前N条
	 * @see cn.uway.ucloude.uts.core.queue.AbstractPreLoader#load(java.lang.String, int)
	 */
	@Override
	protected List<JobPo> load(String loadTaskTrackerNodeGroup, int loadSize) {
		 try {
	            List<JobPo> jobPos= SqlBuilderFactory.getSqlFactory(getSqlTemplate())
	            		.getSelectSql()
	                    .select()
	                    .all()
	                    .from()
	                    .table(getTableName())
	                    .where("is_running = ?", false)
	                    .and("trigger_time< ?", new java.sql.Timestamp(SystemClock.now()))
	                    .and("task_tracker_node_group=?",loadTaskTrackerNodeGroup)
	                    .orderBy()
	                    .column("priority", OrderByType.ASC)
	                    .column("trigger_time", OrderByType.ASC)
	                    .column("CREATED_TIME", OrderByType.ASC)
	                    .page(1, loadSize)
	                    .list(RshHolder.JOB_PO_LIST_RSH);
	            for(JobPo item:jobPos){
	            	item.setExtParams(getExtParam(item.getJobId(),0));
	            	item.setInternalExtParam(getExtParam(item.getJobId(),1));
	            }
	            return jobPos;
	        } catch (Exception e) {
	            LOGGER.error("Error when load job:" + e.getMessage(), e);
	            return null;
	        }
	}
}
