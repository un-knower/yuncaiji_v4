package cn.uway.ucloude.data.dataaccess.oracle.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.DeleteSql;

public class ODeleteSql extends DeleteSql {

	public ODeleteSql(SqlTemplate sqlTemplate) {
		super(sqlTemplate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public DeleteSql table(String table) {
		// TODO Auto-generated method stub
		this.sql.append(table.trim());
		return this;
	}

	@Override
	public DeleteSql andBetween(String column, Object start, Object end) {
		// TODO Auto-generated method stub
		if (start == null && end == null) {
			return this;
		} else if (start != null && end != null) {
			this.sql.append(" ADN (").append(column).append(" BETWEEN ? AND ? ").append(")");
			this.params.add(start);
			this.params.add(end);
			return this;
		} else if (start == null) {
			this.sql.append(column).append(" <= ? ");
			this.params.add(end);
			return this;
		} else {
			this.sql.append(column).append(" >= ? ");
			this.params.add(start);
			return this;
		}
	}

	@Override
	public DeleteSql orBetween(String column, Object start, Object end) {
		// TODO Auto-generated method stub
		if (start == null && end == null) {
			return this;
		} else if (start != null && end != null) {
			this.sql.append(" OR (").append(column).append(" BETWEEN ? AND ? ").append(")");
			this.params.add(start);
			this.params.add(end);
			return this;
		} else if (start == null) {
			this.sql.append(column).append(" <= ? ");
			this.params.add(end);
			return this;
		} else {
			this.sql.append(column).append(" >= ? ");
			this.params.add(start);
			return this;
		}
	}

}
