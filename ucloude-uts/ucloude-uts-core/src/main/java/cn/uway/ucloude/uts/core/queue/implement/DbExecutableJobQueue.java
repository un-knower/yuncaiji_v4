package cn.uway.ucloude.uts.core.queue.implement;

import java.util.Date;
import java.util.List;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.data.dataaccess.exception.TableNotExistException;
import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.queue.ExecutableJobQueue;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.domain.JobQueueReq;

/**
 * 基于DB的等待队列实现
 * 
 * @author Uway-M3
 *
 */
public class DbExecutableJobQueue extends AbstractDbJobQueue implements ExecutableJobQueue {

	public DbExecutableJobQueue(String connectionKey) {
		super(connectionKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean createQueue(String taskTrackerNodeGroup) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean removeQueue(String taskTrackerNodeGroup) {
		// TODO Auto-generated method stub
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql().delete().from().table(getTableName()).where("task_tracker_node_group=?",taskTrackerNodeGroup).doDelete()>0;
	}

	@Override
	public boolean add(JobPo jobPo) {
		// TODO Auto-generated method stub
		try {
            jobPo.setGmtModified(SystemClock.now());
            return super.add(getTableName(), jobPo);
        } catch (TableNotExistException e) {
            // 表不存在
            createQueue(jobPo.getTaskTrackerNodeGroup());
            add(jobPo);
        }
        return true;
	}

	@Override
	public boolean remove(String taskTrackerNodeGroup, String jobId) {
		// TODO Auto-generated method stub
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql().delete()
                .from()
                .table(getTableName())
                .where("job_id = ?", jobId)
                .doDelete() == 1;
	}

	@Override
	public long countJob(String realTaskId, String taskTrackerNodeGroup) {
		// TODO Auto-generated method stub
		return  SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select()
                .columns("COUNT(1)")
                .from()
                .table(getTableName())
                .where("real_task_id = ?", realTaskId)
                .and("task_tracker_node_group = ?", taskTrackerNodeGroup)
                .single();
	}

	@Override
	public boolean removeBatch(String realTaskId, String taskTrackerNodeGroup) {
		// TODO Auto-generated method stub
		return  SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getDeleteSql().delete()
                .from()
                .table(getTableName())
                .where("real_task_id = ?", realTaskId)
                .and("task_tracker_node_group = ?", taskTrackerNodeGroup)
                .doDelete()> 0;
	}

	@Override
	public void resume(String jobId, String taskTrackerNodeGroup) {
		// TODO Auto-generated method stub
		SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getUpdateSql().update()
        .table(getTableName())
        .set("is_running", false)
        .set("task_tracker_identity", null)
        .set("MODIFIED_TIME", new java.sql.Timestamp( SystemClock.now()))
        .where("job_id=?", jobId)
        .and("task_tracker_node_group = ?", taskTrackerNodeGroup)
        .doUpdate();
	}

	@Override
	public List<JobPo> getDeadJob(String taskTrackerNodeGroup, long deadline) {
		// TODO Auto-generated method stub
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select()
                .all()
                .from()
                .table(getTableName())
                .where("is_running = ?", true)
                .and("MODIFIED_TIME < ?", new java.sql.Timestamp(deadline))
                .and("task_tracker_node_group = ?", taskTrackerNodeGroup)
                .list(RshHolder.JOB_PO_LIST_RSH);
	}

	@Override
	public JobPo getJob(String taskTrackerNodeGroup, String taskId) {
		// TODO Auto-generated method stub
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select()
                .all()
                .from()
                .table(getTableName())
                .where("task_id = ?", taskId)
                .and("task_tracker_node_group = ?", taskTrackerNodeGroup)
                .single(RshHolder.JOB_PO_RSH);
	}

	@Override
	protected String getTableName() {
		// TODO Auto-generated method stub
		return "uts_Excutable_job";
	}

	
}
