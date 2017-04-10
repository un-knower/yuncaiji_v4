package cn.uway.ucloude.data.dataaccess.mysql.builder;



import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.DeleteSql;
import cn.uway.ucloude.data.dataaccess.builder.WhereSql;


/**
 * @author magic.s.g.xie
 */
public class MysqlDeleteSql extends  DeleteSql{

	

	

	public MysqlDeleteSql(SqlTemplate sqlTemplate) {
		super(sqlTemplate);
	}




	@Override
	public DeleteSql whereSql(WhereSql whereSql) {
		// TODO Auto-generated method stub
		sql.append(whereSql.getSQL());
		params.addAll(whereSql.params());
		return this;
	}











	@Override
	public DeleteSql table(String table) {
		// TODO Auto-generated method stub
		 sql.append(" `").append(table.trim()).append("` ");
	     return this;
	}








	



	@Override
	public DeleteSql andBetween(String column, Object start, Object end) {
		// TODO Auto-generated method stub
		 if (start == null && end == null) {
	            return this;
	        }

	        if (start != null && end != null) {
	            sql.append(" ADN (").append(column).append(" BETWEEN ? AND ? ").append(")");
	            params.add(start);
	            params.add(end);
	            return this;
	        }

	        if (start == null) {
	            sql.append(column).append(" <= ? ");
	            params.add(end);
	            return this;
	        }

	        sql.append(column).append(" >= ? ");
	        params.add(start);
	        return this;
	}




	@Override
	public DeleteSql orBetween(String column, Object start, Object end) {
		// TODO Auto-generated method stub
		if (start == null && end == null) {
            return this;
        }

        if (start != null && end != null) {
            sql.append(" OR (").append(column).append(" BETWEEN ? AND ? ").append(")");
            params.add(start);
            params.add(end);
            return this;
        }

        if (start == null) {
            sql.append(column).append(" <= ? ");
            params.add(end);
            return this;
        }

        sql.append(column).append(" >= ? ");
        params.add(start);
        return this;
	}
}
