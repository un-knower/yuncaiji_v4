package cn.uway.ucloude.data.dataaccess.mysql.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.*;

public class MysqlSqlFactory extends SqlFactory {
	public MysqlSqlFactory(SqlTemplate sqlTemplate) {
		super(sqlTemplate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public DeleteSql getDeleteSql(){
		return new MysqlDeleteSql(sqlTemplate);
	}
	
	@Override
	public DropTableSql getDropTableSql(){
		return new MysqlDropTableSql(sqlTemplate);
	}
	
	@Override
	public InsertSql getInsertSql(){
		return new MysqlInsertSql(sqlTemplate);
	}
	
	@Override
	public SelectSql getSelectSql(){
		return new MySqlSelectSql(sqlTemplate);
	}
	
	@Override
	public UpdateSql getUpdateSql(){
		return new MysqlUpdateSql(sqlTemplate);
	}

	@Override
	public WhereSql getWhereSql() {
		// TODO Auto-generated method stub
		return new MysqlWhereSql();
	}
}
