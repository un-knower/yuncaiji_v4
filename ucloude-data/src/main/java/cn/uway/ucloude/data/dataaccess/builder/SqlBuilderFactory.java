package cn.uway.ucloude.data.dataaccess.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.dao.ConnectionDAO;
import cn.uway.ucloude.data.dataaccess.mysql.builder.MysqlSqlFactory;
import cn.uway.ucloude.data.dataaccess.oracle.builder.OSqlFactory;

public class SqlBuilderFactory {
	
	
	
	public static SqlFactory getSqlFactory(SqlTemplate sqlTemplate) {
		if(sqlTemplate.getProvider() == ConnectionDAO.mapDBTypeDriver.get("ORACLE")){
			return new OSqlFactory(sqlTemplate);
		}
		else if(sqlTemplate.getProvider() == ConnectionDAO.mapDBTypeDriver.get("MYSQL")){
			return new MysqlSqlFactory(sqlTemplate);
		}else 
			return null;
	}

}
