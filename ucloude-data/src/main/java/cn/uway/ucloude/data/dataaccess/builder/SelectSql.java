package cn.uway.ucloude.data.dataaccess.builder;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.uway.ucloude.data.dataaccess.ResultSetHandler;
import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.builder.OrderByType;
import cn.uway.ucloude.data.dataaccess.exception.JdbcException;
import cn.uway.ucloude.data.dataaccess.utils.SQLFormatter;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * @author magic.s..g.xie
 */
public abstract class SelectSql {

	private static final ILogger LOGGER = LoggerManager.getLogger(SelectSql.class);

	private SqlTemplate sqlTemplate;
	protected StringBuilder sql = new StringBuilder();
	protected List<Object> params = new LinkedList<Object>();
	protected int curOrderByColumnSize = 0;
	protected static final String ORDER_BY = " ORDER BY ";

	public SelectSql(SqlTemplate sqlTemplate) {
		this.sqlTemplate = sqlTemplate;
	}

	public SelectSql select() {
		sql.append(" SELECT ");
		return this;
	}

	public SelectSql all() {
		sql.append(" * ");
		return this;
	}

	public abstract SelectSql columns(String... columns);
	public SelectSql from() {
		sql.append(" FROM ");
		return this;
	}

	public abstract SelectSql  table(String table);

	public abstract SelectSql tables(String... tables);

	public SelectSql where() {
		sql.append(" WHERE ");
		return this;
	}

	public SelectSql whereSql(WhereSql whereSql) {
		sql.append(whereSql.getSQL());
		params.addAll(whereSql.params());
		return this;
	}

	public SelectSql where(String condition, Object value) {
		sql.append(" WHERE ").append(condition);
		params.add(value);
		return this;
	}

	/**
	 * @param condition
	 *            拼接的Sql
	 * @param values
	 * @return
	 */
	public SelectSql where(String condition, List<Object> values) {
		sql.append(" WHERE ").append(condition);
		params.addAll(values);
		return this;
	}

	public SelectSql and(String condition, Object value) {
		sql.append(" AND ").append(condition);
		params.add(value);
		return this;
	}

	public SelectSql and(String conditions, List<Object> values) {
		sql.append(" AND ").append(conditions);
		params.addAll(values);
		return this;
	}

	public SelectSql or(String condition, Object value) {
		sql.append(" OR ").append(condition);
		params.add(value);
		return this;
	}

	public SelectSql orderBy() {
		curOrderByColumnSize = 0;
		return this;
	}

	public abstract SelectSql orderBy(String column, OrderByType order);

	public SelectSql and(String condition) {
		sql.append(" AND ").append(condition);
		return this;
	}

	public SelectSql or(String condition) {
		sql.append(" OR ").append(condition);
		return this;
	}

	public SelectSql andOnNotNull(String condition, Object value) {
		if (value == null) {
			return this;
		}
		return and(condition, value);
	}

	public SelectSql orOnNotNull(String condition, Object value) {
		if (value == null) {
			return this;
		}
		return or(condition, value);
	}

	public SelectSql andOnNotEmpty(String condition, String value) {
		if (StringUtils.isEmpty(value)) {
			return this;
		}
		return and(condition, value);
	}

	public SelectSql orOnNotEmpty(String condition, String value) {
		if (StringUtils.isEmpty(value)) {
			return this;
		}
		return or(condition, value);
	}

	public SelectSql andBetween(String column, Object start, Object end) {

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
			sql.append(" ").append(column).append(" <= ? ");
			params.add(end);
			return this;
		}

		sql.append("").append(column).append(" >= ? ");
		params.add(start);
		return this;
	}

	public SelectSql orBetween(String column, Object start, Object end) {

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

	public abstract SelectSql page(int page, int size);

	public SelectSql groupBy(String... columns) {
		sql.append(" GROUP BY ");
		String split = "";
		for (String column : columns) {
			sql.append(split);
			split = ",";
			sql.append(column.trim()).append(" ");
		}
		return this;
	}

	public SelectSql having(String condition) {
		sql.append(" HAVING ").append(condition);
		return this;
	}

	public SelectSql innerJoin(String condition) {
		sql.append(" INNER JOIN ").append(condition);
		return this;
	}

	public SelectSql rightOuterJoin(String condition) {
		sql.append(" RIGHT OUTER JOIN ").append(condition);
		return this;
	}

	public SelectSql leftOuterJoin(String condition) {
		sql.append(" LEFT OUTER JOIN ").append(condition);
		return this;
	}

	public <T> List<T> list(ResultSetHandler<List<T>> handler) {
		try {
			return sqlTemplate.query(getSQL(), handler, params.toArray());
		} catch (Exception e) {
			throw new JdbcException("Select SQL Error:" + getSQL(), e);
		}
	}

	public <T> T single(ResultSetHandler<T> handler) {
		try {
			return sqlTemplate.query(getSQL(), handler, params.toArray());
		} catch (Exception e) {
			throw new JdbcException("Select SQL Error:" + getSQL(), e);
		}
	}

	public <T> T single() {
		String finalSQL = getSQL();
		try {

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(SQLFormatter.format(finalSQL));
			}

			return sqlTemplate.queryForValue(finalSQL, params.toArray());
		} catch (Exception e) {
			throw new JdbcException("Select SQL Error:" + SQLFormatter.format(finalSQL), e);
		}
	}
	
	public abstract SelectSql column(String colName, OrderByType orderByType);

	public String getSQL() {
		return sql.toString();
	}

	public List<Object> getParams() {
		return this.params;
	}
}