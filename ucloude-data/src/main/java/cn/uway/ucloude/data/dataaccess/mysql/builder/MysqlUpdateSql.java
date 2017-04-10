package cn.uway.ucloude.data.dataaccess.mysql.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.UpdateSql;

/**
 * @author Magic.s.g.xie
 */
public class MysqlUpdateSql extends UpdateSql {

	public MysqlUpdateSql(SqlTemplate sqlTemplate) {
		super(sqlTemplate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public UpdateSql table(String table) {
		// TODO Auto-generated method stub
		sql.append(" `").append(table).append("` ");
        return this;
	}

	@Override
	public UpdateSql set(String column, Object value) {
		// TODO Auto-generated method stub
		if (params.size() > 0) {
            sql.append(",");
        } else {
            sql.append(" SET ");
        }
        sql.append("`").append(column).append("`").append(" = ? ");
        params.add(value);
        return this;
	}

    
}
