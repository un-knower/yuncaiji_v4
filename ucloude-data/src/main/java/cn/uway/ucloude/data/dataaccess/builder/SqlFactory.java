package cn.uway.ucloude.data.dataaccess.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;

public abstract class SqlFactory {
	protected final SqlTemplate sqlTemplate;
	public SqlFactory(SqlTemplate sqlTemplate){
		this.sqlTemplate = sqlTemplate;
	}
	
	public abstract DeleteSql getDeleteSql();
	public abstract DropTableSql getDropTableSql();
	public abstract InsertSql getInsertSql();
	public abstract SelectSql getSelectSql();
	public abstract UpdateSql getUpdateSql();
	
	public abstract WhereSql getWhereSql();
}
