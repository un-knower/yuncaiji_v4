package cn.uway.ucloude.data.dataaccess.mysql.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.DropTableSql;

/**
 * @author magic.s.g.xie
 */
public class MysqlDropTableSql extends DropTableSql {

	public MysqlDropTableSql(SqlTemplate sqlTemplate) {
		super(sqlTemplate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public DropTableSql drop(String table) {
		// TODO Auto-generated method stub
		 sql.append("DROP TABLE IF EXISTS ").append(table);
	        return this;
	}

    

}
