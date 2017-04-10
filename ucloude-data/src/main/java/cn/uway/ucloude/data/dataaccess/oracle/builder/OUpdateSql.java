package cn.uway.ucloude.data.dataaccess.oracle.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.UpdateSql;

public class OUpdateSql extends UpdateSql {

	public OUpdateSql(SqlTemplate sqlTemplate) {
		super(sqlTemplate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public UpdateSql table(String table) {
		// TODO Auto-generated method stub
		this.sql.append(table);
        return this;
	}

	@Override
	public UpdateSql set(String column, Object value) {
		// TODO Auto-generated method stub
		 if(this.params.size() > 0) {
	            this.sql.append(",");
	        } else {
	            this.sql.append(" SET ");
	        }

	        this.sql.append(column).append(" = ? ");
	        this.params.add(value);
	        return this;
	}

}
