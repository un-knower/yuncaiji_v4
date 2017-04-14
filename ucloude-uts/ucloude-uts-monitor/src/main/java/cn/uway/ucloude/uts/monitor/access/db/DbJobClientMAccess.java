package cn.uway.ucloude.uts.monitor.access.db;

import java.util.List;
import java.util.UUID;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.builder.InsertSql;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.monitor.access.domain.JobClientMDataPo;
import cn.uway.ucloude.uts.monitor.access.face.JobClientMAccess;

public class DbJobClientMAccess extends JdbcAbstractAccess implements JobClientMAccess {

	public DbJobClientMAccess(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}

	protected String getTabName() {
		return "UTS_MD_JOB_CLIENT";
	}
	
	

	@Override
	public void insert(List<JobClientMDataPo> jobTrackerMDataPos) {
		InsertSql insertSql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql();
		insertSql.insert(getTabName()).columns("ID", "NODE_GROUP", "NODE_TYPE", "IDENTITY", "CREATE_TIME", "TIME_STAMP",
				"SUBMIT_SUCCESS_NUM", "SUBMIT_FAILED_NUM", "FAIL_STORE_NUM", "SUBMIT_FAIL_STORE_NUM",
				"HANDLE_FEEDBACK_NUM");
		for (JobClientMDataPo po : jobTrackerMDataPos) {
			java.sql.Timestamp createTime = null;
			java.sql.Timestamp timeStamp = null;
			if (po.getGmtCreated() != null) {
				createTime = new java.sql.Timestamp(po.getGmtCreated());
			}
			if (po.getTimestamp() != null) {
				timeStamp = new java.sql.Timestamp(po.getTimestamp());
			}
			insertSql.values(UUID.randomUUID().toString(), po.getNodeGroup(), po.getNodeType().getValue(), po.getIdentity(), createTime,
					timeStamp, po.getSubmitSuccessNum(), po.getSubmitFailedNum(), po.getFailStoreNum(),
					po.getSubmitFailStoreNum(), po.getHandleFeedbackNum());
		}
		insertSql.doBatchInsert();
	}
}
