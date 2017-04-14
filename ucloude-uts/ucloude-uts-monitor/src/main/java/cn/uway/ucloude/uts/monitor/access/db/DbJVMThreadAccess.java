package cn.uway.ucloude.uts.monitor.access.db;

import java.util.List;
import java.util.UUID;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.builder.InsertSql;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.monitor.access.domain.JVMThreadDataPo;
import cn.uway.ucloude.uts.monitor.access.face.JVMThreadAccess;

public class DbJVMThreadAccess extends JdbcAbstractAccess implements JVMThreadAccess {
	
	public DbJVMThreadAccess(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}
	
	protected String getTabName() {
		return "UTS_MD_JVM_THREAD";
	}

	@Override
	public void insert(List<JVMThreadDataPo> pos) {
		InsertSql insertSql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql();
		insertSql.insert(getTabName()).columns("ID", "NODE_GROUP", "NODE_TYPE", "IDENTITY", "CREATE_TIME", "TIME_STAMP",
				"DAEMON_THREAD_COUNT", "THREAD_COUNT", "TOTAL_STARTED_THREAD_COUNT", "DEAD_LOCKED_THREAD_COUNT",
				"PROCESS_CPU_TIME_RATE");
		for (JVMThreadDataPo po : pos) {
			java.sql.Timestamp createTime = null;
			java.sql.Timestamp timeStamp = null;
			if (po.getGmtCreated() != null) {
				createTime = new java.sql.Timestamp(po.getGmtCreated());
			}
			if (po.getTimestamp() != null) {
				timeStamp = new java.sql.Timestamp(po.getTimestamp());
			}
			insertSql.values(UUID.randomUUID().toString(), po.getNodeGroup(), po.getNodeType().getValue(), po.getIdentity(), createTime,
					timeStamp, po.getDaemonThreadCount(), po.getThreadCount(), po.getTotalStartedThreadCount(),
					po.getDeadLockedThreadCount(), po.getProcessCpuTimeRate());
		}
		insertSql.doBatchInsert();
	}

}
