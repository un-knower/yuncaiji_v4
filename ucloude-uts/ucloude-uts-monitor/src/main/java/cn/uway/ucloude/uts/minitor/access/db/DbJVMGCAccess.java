package cn.uway.ucloude.uts.minitor.access.db;

import java.util.List;
import java.util.UUID;

import cn.uway.ucloude.data.dataaccess.JdbcAbstractAccess;
import cn.uway.ucloude.data.dataaccess.builder.InsertSql;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.minitor.access.domain.JVMGCDataPo;
import cn.uway.ucloude.uts.minitor.access.face.JVMGCAccess;

public class DbJVMGCAccess extends JdbcAbstractAccess implements JVMGCAccess {

	public DbJVMGCAccess(String connKey) {
		super(connKey);
		// TODO Auto-generated constructor stub
	}
	
	protected String getTabName() {
		return "UTS_MD_JVM_GC";
	}

	@Override
	public void insert(List<JVMGCDataPo> pos) {
		InsertSql insertSql = SqlBuilderFactory.getSqlFactory(getSqlTemplate()).getInsertSql();
		insertSql.insert(getTabName()).columns("ID", "node_Group", "node_Type", "identity", "CREATE_TIME", "time_stamp",
				"young_GC_CLT_Count", "young_GC_CLT_Time", "full_GC_CLT_Count", "full_GC_CLT_Time",
				"span_Young_GC_CLT_Count", "span_Young_GC_CLT_Time", "span_Full_GC_CLT_Count", "span_Full_GC_CLT_Time");
		for (JVMGCDataPo po : pos) {
			java.sql.Timestamp createTime = null;
			java.sql.Timestamp timeStamp = null;
			if (po.getGmtCreated() != null) {
				createTime = new java.sql.Timestamp(po.getGmtCreated());
			}
			if (po.getTimestamp() != null) {
				timeStamp = new java.sql.Timestamp(po.getTimestamp());
			}
			insertSql.values(UUID.randomUUID().toString(), po.getNodeGroup(), po.getNodeType().getValue(), po.getIdentity(), createTime,
					timeStamp, po.getYoungGCCollectionCount(), po.getFullGCCollectionTime(),
					po.getFullGCCollectionCount(), po.getFullGCCollectionTime(), po.getSpanYoungGCCollectionCount(),
					po.getSpanYoungGCCollectionTime(), po.getSpanFullGCCollectionCount(),
					po.getSpanFullGCCollectionTime());
		}
		insertSql.doBatchInsert();
	}

}
