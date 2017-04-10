package cn.uway.ucloude.uts.core.queue.implement;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.builder.OrderByType;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.queue.JobFeedbackQueue;
import cn.uway.ucloude.uts.core.queue.domain.JobFeedbackPo;

/**
 * 失败反馈队列
 * 
 * @author Uway-M3
 *
 */
public class DbJobFeedbackQueue extends JdbcAbstractAccess implements JobFeedbackQueue {

	public DbJobFeedbackQueue(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean createQueue(String jobClientNodeGroup) {
		//JobFeedbackPoDao jobDao = new JobFeedbackPoDao(ExtConfigKeys.CONNECTION_KEY, getTableName(jobClientNodeGroup));
		return true;
	}

	@Override
	public boolean removeQueue(String jobClientNodeGroup) {
		//JobFeedbackPoDao jobDao = new JobFeedbackPoDao(ExtConfigKeys.CONNECTION_KEY, getTableName(jobClientNodeGroup));
		return  SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql()
				.delete()
				.from()
				.table(getTableName())
				.where("NODE_GROUP=?",jobClientNodeGroup)
				.doDelete()> 0;
	}

	@Override
	public boolean add(List<JobFeedbackPo> jobFeedbackPos) {
		 if (CollectionUtil.isEmpty(jobFeedbackPos)) {
	            return true;
	        }
	        // insert ignore duplicate record
	        for (JobFeedbackPo jobFeedbackPo : jobFeedbackPos) {
	            String jobClientNodeGroup = jobFeedbackPo.getJobRunResult().getJobMeta().getJob().getSubmitNodeGroup();
	            jobFeedbackPo.setId(UUID.randomUUID().toString());
	            SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql()
	                    .insertIgnore(getTableName())
	                    .columns("ID","GMT_CREATED", "job_result","NODE_GROUP")
	                    .values(jobFeedbackPo.getId(),jobFeedbackPo.getGmtCreated(), JsonConvert.serialize(jobFeedbackPo.getJobRunResult()),jobFeedbackPo.getNodeGroup())
	                    .doInsert();
	        }
	        return true;
	}

	@Override
	public boolean remove(String jobClientNodeGroup, String id) {
		
		return   SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql().delete()
		        .from()
		        .table(getTableName()).where("id = ?", id).and("NODE_GROUP=?",jobClientNodeGroup).doDelete() > 0;
	}

	@Override
	public long getCount(String jobClientNodeGroup) {
		BigDecimal count=  SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql()
                .select()
                .columns("count(1)")
                .from()
                .table(getTableName())
                .where("NODE_GROUP=?",jobClientNodeGroup)
                .single();
		return count.longValue();
	}

	@Override
	public List<JobFeedbackPo> fetchTop(String jobClientNodeGroup, int top) {
		
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql()
				.select()
                .all()
                .from()
                .table(getTableName())
                .where("NODE_GROUP=?",jobClientNodeGroup)
                .orderBy()
                .column("GMT_CREATED", OrderByType.ASC)
                .page(0, top)
                .list(RshHolder.JOB_FEED_BACK_LIST_RSH);
	}

	/**
	 * 获取表名，每个taskTracker一个表
	 * 
	 * @param taskTrackerNodeGroup
	 * @return
	 */
	private String getTableName() {
		// return "uts_fjq_" + jobClientNodeGroup;
		return "uts_Job_Feed_back";
	}

}
