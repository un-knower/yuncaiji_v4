package cn.uway.ucloude.uts.web.access;
import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.SqlTemplateFactory;
import cn.uway.ucloude.data.dataaccess.builder.SqlBuilderFactory;
import cn.uway.ucloude.uts.core.ExtConfigKeys;

public abstract class MemoryAccess {
	private SqlTemplate sqlTemplate;

	protected SqlTemplate getSqlTemplate() {
		return sqlTemplate;
	}
	
	public MemoryAccess(){
		sqlTemplate = SqlTemplateFactory.create(ExtConfigKeys.CONNECTION_KEY);
		SqlBuilderFactory.getSqlFactory(sqlTemplate).getDeleteSql().delete().from()
        .table("uts_node")
        .doDelete();
	}

}
