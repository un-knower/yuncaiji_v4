package cn.uway.ucloude.uts.core.queue.implement;

import java.util.Date;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.queue.RepeatJobQueue;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.domain.JobQueueReq;

/**
 * 重复执行的任务队列
 * 
 * @author Uway-M3
 *
 */
public class DbRepeatJobQueue extends AbstractDbSchedulerJobQueue  implements RepeatJobQueue {

	public DbRepeatJobQueue(String connectionKey) {
		super(connectionKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean add(JobPo jobPo) {
		// TODO Auto-generated method stub
		return super.add(getTableName(), jobPo);
	}

	@Override
	public boolean remove(String jobId) {
		// TODO Auto-generated method stub
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate())
				.getDeleteSql()
				.delete()
                .from()
                .table(getTableName())
                .where("job_id = ?", jobId)
                .doDelete() == 1;
	}

	@Override
	public JobPo getJob(String taskTrackerNodeGroup, String taskId) {
		// TODO Auto-generated method stub
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate())
				.getSelectSql()
				.select()
                .all()
                .from()
                .table(getTableName())
                .where("task_id = ?", taskId)
                .and("task_tracker_node_group = ?", taskTrackerNodeGroup)
                .single(RshHolder.JOB_PO_RSH);
	}

	@Override
	public int incRepeatedCount(String jobId) {
		// TODO Auto-generated method stub
		while (true) {
            JobPo jobPo = getJob(jobId);
            if (jobPo == null) {
                return -1;
            }
            if (SqlBuilderFactory.getSqlFactory(getSqlTemplate())
            		.getUpdateSql()
                    .update()
                    .table(getTableName())
                    .set("repeated_count", jobPo.getRepeatedCount() + 1)
                    .where("job_id = ?", jobId)
                    .and("repeated_count = ?", jobPo.getRepeatedCount())
                    .doUpdate() == 1) {
                return jobPo.getRepeatedCount() + 1;
            }
        }
	}

	@Override
	protected String getTableName() {
		// TODO Auto-generated method stub
		return "uts_repeat_job";
	}

	

	
}
