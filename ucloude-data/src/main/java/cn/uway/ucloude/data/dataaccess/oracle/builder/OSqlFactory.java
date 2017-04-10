package cn.uway.ucloude.data.dataaccess.oracle.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.DeleteSql;
import cn.uway.ucloude.data.dataaccess.builder.DropTableSql;
import cn.uway.ucloude.data.dataaccess.builder.InsertSql;
import cn.uway.ucloude.data.dataaccess.builder.SelectSql;
import cn.uway.ucloude.data.dataaccess.builder.SqlFactory;
import cn.uway.ucloude.data.dataaccess.builder.UpdateSql;
import cn.uway.ucloude.data.dataaccess.builder.WhereSql;


public class OSqlFactory extends SqlFactory {
	public OSqlFactory(SqlTemplate sqlTemplate) {
		super(sqlTemplate);
		// TODO Auto-generated constructor stub
	}
	@Override
	public DeleteSql getDeleteSql(){
		return new ODeleteSql(sqlTemplate);
	}
	@Override
	public DropTableSql getDropTableSql(){
		return new ODropTableSql(sqlTemplate);
	}
	@Override
	public InsertSql getInsertSql(){
		return new OInsertSql(sqlTemplate);
	}
	@Override
	public SelectSql getSelectSql(){
		return new OSelectSql(sqlTemplate);
	}
	
	@Override
	public UpdateSql getUpdateSql(){
		return new OUpdateSql(sqlTemplate);
	}
	@Override
	public WhereSql getWhereSql() {
		// TODO Auto-generated method stub
		return new OWhereSql();
	}
}
