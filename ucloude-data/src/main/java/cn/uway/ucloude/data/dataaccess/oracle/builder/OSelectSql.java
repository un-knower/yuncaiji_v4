package cn.uway.ucloude.data.dataaccess.oracle.builder;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.OrderByType;
import cn.uway.ucloude.data.dataaccess.builder.SelectSql;
import cn.uway.ucloude.data.dataaccess.exception.JdbcException;
import cn.uway.ucloude.utils.StringUtil;

public class OSelectSql extends SelectSql {

	public OSelectSql(SqlTemplate sqlTemplate) {
		super(sqlTemplate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SelectSql columns(String... columns) {
		// TODO Auto-generated method stub
		if (columns != null && columns.length != 0) {
			String split = "";
			String[] arr$ = columns;
			int len$ = columns.length;

			for (int i$ = 0; i$ < len$; ++i$) {
				String column = arr$[i$];
				this.sql.append(split);
				split = ",";
				this.sql.append(column.trim()).append(" ");
			}

			return this;
		} else {
			throw new JdbcException("columns must have length");
		}
	}

	@Override
	public SelectSql table(String table) {
		// TODO Auto-generated method stub
		this.sql.append(table);
		return this;
	}

	@Override
	public SelectSql tables(String... tables) {
		// TODO Auto-generated method stub
		String split = "";
		String[] arr$ = tables;
		int len$ = tables.length;

		for (int i$ = 0; i$ < len$; ++i$) {
			String table = arr$[i$];
			this.sql.append(split);
			split = ",";
			this.sql.append(table.trim()).append(" ");
		}

		return this;
	}

	@Override
	public SelectSql page(int page, int size) {
		// TODO Auto-generated method stub
		String limitO = " select * from (select A.*,rownum rn from( {sql} )A where rownum < {end}) where rn >= {start}";
		limitO = limitO.replace("{sql}", this.sql).replace("{end}", String.valueOf(page * size + 1)).replace("{start}",
				String.valueOf((page - 1) * size + 1));
		this.sql = new StringBuilder(limitO);
		return this;
	}

	@Override
	public SelectSql orderBy(String column, OrderByType order) {
		// TODO Auto-generated method stub
		if (!StringUtil.isEmpty(column) && order != null) {
			if (this.curOrderByColumnSize == 0) {
				this.sql.append(" ORDER BY ");
			} else if (this.curOrderByColumnSize > 0) {
				this.sql.append(" , ");
			}

			this.sql.append(" ").append(column).append(" ").append(order);
			++this.curOrderByColumnSize;
			return this;
		} else {
			return this;
		}
	}

	@Override
	public SelectSql column(String column, OrderByType order) {
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
