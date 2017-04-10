package cn.uway.ucloude.data.dataaccess.oracle.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.DropTableSql;

public class ODropTableSql extends DropTableSql {

	public ODropTableSql(SqlTemplate sqlTemplate) {
		super(sqlTemplate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public DropTableSql drop(String table) {
		// TODO Auto-generated method stub
		this.sql.append("BEGIN EXECUTE IMMEDIATE 'DROP TABLE ").append(table)
				.append("'; EXCEPTION WHEN OTHERS THEN NULL;END;");
		return this;
	}

}
