package cn.uway.ucloude.uts.monitor.access.db;

import java.util.List;
import java.util.UUID;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.builder.InsertSql;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.monitor.access.domain.TaskTrackerMDataPo;
import cn.uway.ucloude.uts.monitor.access.face.TaskTrackerMAccess;

public class DbTaskTrackerMAccess extends JdbcAbstractAccess implements TaskTrackerMAccess {
	public DbTaskTrackerMAccess(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}

	protected String getTabName() {
		return "UTS_MD_TASK_TRACKER";
	}

	@Override
	public void insert(List<TaskTrackerMDataPo> pos) {
		InsertSql insertSql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql();
		insertSql.insert(getTabName()).columns("ID", "node_Group", "node_Type", "identity", "CREATE_TIME", "time_stamp",
				"exe_Success_Num", "exe_Failed_Num", "exe_Later_Num", "exe_Exception_Num", "total_Running_Time");
		for (TaskTrackerMDataPo po : pos) {
			java.sql.Timestamp createTime = null;
			java.sql.Timestamp timeStamp = null;
			if (po.getGmtCreated() != null) {
				createTime = new java.sql.Timestamp(po.getGmtCreated());
			}
			if (po.getTimestamp() != null) {
				timeStamp = new java.sql.Timestamp(po.getTimestamp());
			}
			insertSql.values(UUID.randomUUID().toString(), po.getNodeGroup(), po.getNodeType().getValue(), po.getIdentity(), createTime,
					timeStamp, po.getExeSuccessNum(), po.getExeFailedNum(), po.getExeLaterNum(),
					po.getExeExceptionNum(), po.getTotalRunningTime());
		}
		insertSql.doBatchInsert();
	}

}
