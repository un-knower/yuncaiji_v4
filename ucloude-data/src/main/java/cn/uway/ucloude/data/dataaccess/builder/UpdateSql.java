package cn.uway.ucloude.data.dataaccess.builder;



import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.exception.JdbcException;
import cn.uway.ucloude.data.dataaccess.utils.SQLFormatter;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * @author Magic.s.g.xie
 */
public abstract class UpdateSql {

    private static final ILogger LOGGER = LoggerManager.getLogger(UpdateSql.class);

    private SqlTemplate sqlTemplate;
    protected StringBuilder sql = new StringBuilder();
    protected List<Object> params = new LinkedList<Object>();

    public UpdateSql(SqlTemplate sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }

    public UpdateSql update() {
        sql.append("UPDATE ");
        return this;
    }

    public abstract UpdateSql table(String table);

    public abstract UpdateSql set(String column, Object value);

    public UpdateSql setOnNotNull(String column, Object value) {
        if (value == null) {
            return this;
        }
        return set(column, value);
    }

    public UpdateSql where() {
        sql.append(" WHERE ");
        return this;
    }

    public UpdateSql whereSql(WhereSql whereSql) {
        sql.append(whereSql.getSQL());
        params.addAll(whereSql.params());
        return this;
    }

    public UpdateSql where(String condition, Object value) {
        sql.append(" WHERE ").append(condition);
        params.add(value);
        return this;
    }

    public UpdateSql and(String condition, Object value) {
        sql.append(" AND ").append(condition);
        params.add(value);
        return this;
    }

    public UpdateSql or(String condition, Object value) {
        sql.append(" OR ").append(condition);
        params.add(value);
        return this;
    }

    public UpdateSql and(String condition) {
        sql.append(" AND ").append(condition);
        return this;
    }

    public UpdateSql or(String condition) {
        sql.append(" OR ").append(condition);
        return this;
    }

    public UpdateSql andOnNotNull(String condition, Object value) {
        if (value == null) {
            return this;
        }
        return and(condition, value);
    }

    public UpdateSql orOnNotNull(String condition, Object value) {
        if (value == null) {
            return this;
        }
        return or(condition, value);
    }

    public UpdateSql andOnNotEmpty(String condition, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        return and(condition, value);
    }

    public UpdateSql orOnNotEmpty(String condition, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        return or(condition, value);
    }

    public UpdateSql andBetween(String column, Object start, Object end) {

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

    public UpdateSql orBetween(String column, Object start, Object end) {

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

    public int doUpdate() {
        String finalSQL = getSQL();
        try {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(SQLFormatter.format(finalSQL));
            }

            return sqlTemplate.update(finalSQL, params.toArray());
        } catch (SQLException e) {
            throw new JdbcException("Update SQL Error:" + SQLFormatter.format(finalSQL), e);
        }
    }

    public String getSQL() {
        return sql.toString();
    }
}
