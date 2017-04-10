package cn.uway.ucloude.data.dataaccess.mysql.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.OrderByType;
import cn.uway.ucloude.data.dataaccess.builder.SelectSql;
import cn.uway.ucloude.data.dataaccess.exception.JdbcException;
import cn.uway.ucloude.utils.StringUtil;

/**
 * @author magic.s..g.xie
 */
public class MySqlSelectSql extends SelectSql {

	public MySqlSelectSql(SqlTemplate sqlTemplate) {
		super(sqlTemplate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SelectSql table(String table) {
		// TODO Auto-generated method stub
		sql.append("`").append(table).append("`");
		return this;
	}

	@Override
	public SelectSql tables(String... tables) {
		// TODO Auto-generated method stub
		String split = "";
		for (String table : tables) {
			sql.append(split);
			split = ",";
			sql.append(table.trim()).append(" ");
		}
		return this;
	}

	@Override
	public SelectSql page(int start, int size) {
		// TODO Auto-generated method stub
		sql.append(" LIMIT ").append(1 + (start - 1) * size).append(",").append(size);
		return this;
	}

	@Override
	public SelectSql columns(String... columns) {
		// TODO Auto-generated method stub
		if (columns == null || columns.length == 0) {
			throw new JdbcException("columns must have length");
		}

		String split = "";
		for (String column : columns) {
			sql.append(split);
			split = ",";
			sql.append(column.trim()).append(" ");
		}
		return this;
	}

	@Override
	public SelectSql orderBy(String column, OrderByType order) {
		// TODO Auto-generated method stub
		if (StringUtil.isEmpty(column) || order == null) {
			return this;
		}

		if (curOrderByColumnSize == 0) {
			sql.append(ORDER_BY);
		} else if (curOrderByColumnSize > 0) {
			sql.append(" , ");
		}
		sql.append(" ").append(column).append(" ").append(order);
		curOrderByColumnSize++;
		return this;
	}

	@Override
	public SelectSql column(String column, OrderByType order) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if (StringUtil.isEmpty(column) || order == null) {
			return this;
		}

		if (curOrderByColumnSize == 0) {
			sql.append(ORDER_BY);
		} else if (curOrderByColumnSize > 0) {
			sql.append(" , ");
		}
		sql.append(" ").append(column).append(" ").append(order);
		curOrderByColumnSize++;
		return this;
	}

}