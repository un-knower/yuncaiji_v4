package cn.uway.ucloude.uts.monitor.access.db;

import java.util.List;
import java.util.UUID;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.builder.InsertSql;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.monitor.access.domain.JVMMemoryDataPo;
import cn.uway.ucloude.uts.monitor.access.face.JVMMemoryAccess;

public class DbJVMMemoryAccess extends JdbcAbstractAccess implements JVMMemoryAccess {

	public DbJVMMemoryAccess(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}
	protected String getTabName() {
		return "UTS_MD_JVM_MEMORY";
	}

	@Override
	public void insert(List<JVMMemoryDataPo> pos) {
		InsertSql insertSql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql();
		insertSql.insert(getTabName()).columns("ID", "NODE_GROUP", "NODE_TYPE", "IDENTITY", "CREATE_TIME", "TIME_STAMP",
				"HEAP_MEMORY_COMMITTED", "HEAP_MEMORY_INIT", "HEAP_MEMORY_MAX", "HEAP_MEMORY_USED",
				"NON_HEAP_MEMORY_COMMITTED", "NON_HEAP_MEMORY_INIT", "NON_HEAP_MEMORY_MAX", "NON_HEAP_MEMORY_USED",
				"PERM_GEN_COMMITTED", "PERM_GEN_INIT", "PERM_GEN_MAX", "PERM_GEN_USED", "OLD_GEN_COMMITTED",
				"OLD_GEN_INIT", "OLD_GEN_MAX", "OLD_GEN_USED", "EDEN_SPACE_COMMITTED", "EDEN_SPACE_INIT",
				"EDEN_SPACE_MAX", "EDEN_SPACE_USED", "SURVIVOR_COMMITTED", "SURVIVOR_INIT", "SURVIVOR_MAX",
				"SURVIVOR_USED");
		for (JVMMemoryDataPo po : pos) {
			java.sql.Timestamp createTime = null;
			java.sql.Timestamp timeStamp = null;
			if (po.getGmtCreated() != null) {
				createTime = new java.sql.Timestamp(po.getGmtCreated());
			}
			if (po.getTimestamp() != null) {
				timeStamp = new java.sql.Timestamp(po.getTimestamp());
			}
			insertSql.values(UUID.randomUUID().toString(), po.getNodeGroup(), po.getNodeType().getValue(), po.getIdentity(), createTime,
					timeStamp, po.getHeapMemoryCommitted(), po.getHeapMemoryInit(), po.getHeapMemoryMax(),
					po.getHeapMemoryUsed(), po.getNonHeapMemoryCommitted(), po.getNonHeapMemoryInit(),
					po.getNonHeapMemoryMax(), po.getNonHeapMemoryUsed(), po.getPermGenCommitted(), po.getPermGenInit(),
					po.getPermGenMax(), po.getPermGenUsed(), po.getOldGenCommitted(), po.getOldGenInit(),
					po.getOldGenMax(), po.getOldGenUsed(), po.getEdenSpaceCommitted(), po.getEdenSpaceInit(),
					po.getEdenSpaceMax(), po.getEdenSpaceUsed(), po.getSurvivorCommitted(), po.getEdenSpaceInit(),
					po.getSurvivorMax(), po.getSurvivorUsed());
		}
		insertSql.doBatchInsert();
	}

}
