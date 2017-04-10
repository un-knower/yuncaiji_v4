package cn.uway.ucloude.uts.core.queue.implement;

import java.util.List;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.core.queue.SchedulerJobQueue;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;

public abstract class AbstractDbSchedulerJobQueue  extends AbstractDbJobQueue implements SchedulerJobQueue {

	public AbstractDbSchedulerJobQueue(String connectionKey) {
		super(connectionKey);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean updateLastGenerateTriggerTime(String jobId, Long lastGenerateTriggerTime) {
		// TODO Auto-generated method stub
		return   SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getUpdateSql().update()
                .table(getTableName())
                .set("last_generate_trigger_time", new java.sql.Timestamp( lastGenerateTriggerTime))
                .set("MODIFIED_TIME",  new java.sql.Timestamp( SystemClock.now()))
                .where("job_id = ? ", jobId)
                .doUpdate() == 1;
	}

	@Override
	public List<JobPo> getNeedGenerateJobPos(Long checkTime, int topSize) {
		// TODO Auto-generated method stub
		return SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getSelectSql().select()
                .all()
                .from()
                .table(getTableName())
                .where("rely_on_prev_cycle = ?", false)
                .and("last_generate_trigger_time <= ?", new java.sql.Timestamp(checkTime))
                .page(0, topSize)
                .list(RshHolder.JOB_PO_LIST_RSH);
	}

	protected abstract String getTableName();

    

}
