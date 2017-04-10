package cn.uway.ucloude.data.dataaccess.builder;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.exception.JdbcException;
import cn.uway.ucloude.data.dataaccess.utils.SQLFormatter;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.StringUtil;



public abstract class DeleteSql {
	
	private static final ILogger LOGGER = LoggerManager.getLogger(DeleteSql.class);

	private SqlTemplate sqlTemplate;
	protected StringBuilder sql = new StringBuilder();
	protected List<Object> params = new LinkedList<Object>();

	public DeleteSql(SqlTemplate sqlTemplate) {
		this.sqlTemplate = sqlTemplate;
	}

	
	public DeleteSql delete() {
		sql.append(" DELETE ");
		return this;
	}
	 
	public DeleteSql all(){
		   sql.append(" * ");
	        return this;
	}
	 
	public DeleteSql from(){
		sql.append(" FROM ");
        return this;
	}
	 
	public abstract DeleteSql table(String table);
	 
	public DeleteSql where() {
        sql.append(" WHERE ");
        return this;
    }
	
	public DeleteSql whereSql(WhereSql whereSql){
        sql.append(whereSql.getSQL());
        params.addAll(whereSql.params());
        return this;
    }
	public DeleteSql where(String condition, Object value){
        sql.append(" WHERE " +condition);
        params.add(value);
        return this;
    }
	 
	public DeleteSql where(String conditions, List<Object> values){
        sql.append(" WHERE " +conditions);
        params.addAll(values);
        return this;
    }
	 
	public DeleteSql and(String condition, Object value) {
        sql.append(" AND ").append(condition);
        params.add(value);
        return this;
    }
	public DeleteSql or(String condition, Object value) {
        sql.append(" OR ").append(condition);
        params.add(value);
        return this;
    }
	public DeleteSql and(String condition) {
        sql.append(" AND ").append(condition);
        return this;
    }
	 
	public DeleteSql or(String condition){
        sql.append(" OR ").append(condition);
        return this;
    }
	
	public DeleteSql andOnNotNull(String condition, Object value){
        if (value == null) {
            return this;
        }
        return and(condition, value);
    }
	
	public DeleteSql orOnNotNull(String condition, Object value){
        if (value == null) {
            return this;
        }
        return or(condition, value);
    }
	public DeleteSql andOnNotEmpty(String condition, String value) {
        if (StringUtil.isEmpty(value)) {
            return this;
        }
        return and(condition, value);
    }
	 
	public DeleteSql orOnNotEmpty(String condition, String value) {
        if (StringUtil.isEmpty(value)) {
            return this;
        }
        return or(condition, value);
    }

	public abstract DeleteSql andBetween(String column, Object start, Object end);
	 
	public abstract DeleteSql orBetween(String column, Object start, Object end);
	 
	public int doDelete() {
		String finalSQL = getSQL();
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(SQLFormatter.format(finalSQL));
			}
			return sqlTemplate.delete(finalSQL, params.toArray());
		} catch (SQLException e) {
			throw new JdbcException("Delete SQL Error:" + SQLFormatter.format(finalSQL), e);
		}
	}

	public String getSQL() {
		return sql.toString();
	}
}
