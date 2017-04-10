package cn.uway.ucloude.uts.minitor.access.db;

import java.util.List;
import java.util.UUID;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.builder.InsertSql;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.minitor.access.domain.JobTrackerMDataPo;
import cn.uway.ucloude.uts.minitor.access.face.JobTrackerMAccess;

public class DbJobTrackerMAccess extends JdbcAbstractAccess implements JobTrackerMAccess {

	public DbJobTrackerMAccess(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}
	
	protected String getTabName() {
		return "UTS_MD_JOB_TRACKER";
	}

	@Override
	public void insert(List<JobTrackerMDataPo> jobTrackerMDataPos) {
		InsertSql insertSql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql();
		insertSql.insert(getTabName()).columns("ID", "NODE_GROUP", "NODE_TYPE", "IDENTITY", "CREATE_TIME", "TIME_STAMP",
				"RECEIVE_JOB_NUM", "PUSH_JOB_NUM", "EXE_SUCCESS_NUM", "EXE_FAILED_NUM", "EXE_LATER_NUM",
				"EXE_EXCEPTION_NUM", "FIX_EXECUTING_JOB_NUM");
		for (JobTrackerMDataPo po : jobTrackerMDataPos) {
			java.sql.Timestamp createTime = null;
			java.sql.Timestamp timeStamp = null;
			if (po.getGmtCreated() != null) {
				createTime = new java.sql.Timestamp(po.getGmtCreated());
			}
			if (po.getTimestamp() != null) {
				timeStamp = new java.sql.Timestamp(po.getTimestamp());
			}
			insertSql.values(UUID.randomUUID().toString(), po.getNodeGroup(), po.getNodeType().getValue(), po.getIdentity(), createTime,
					timeStamp, po.getReceiveJobNum(), po.getPushJobNum(), po.getExeSuccessNum(), po.getExeFailedNum(),
					po.getExeLaterNum(), po.getExeExceptionNum(), po.getFixExecutingJobNum());
		}
		insertSql.doBatchInsert();
	}

}
