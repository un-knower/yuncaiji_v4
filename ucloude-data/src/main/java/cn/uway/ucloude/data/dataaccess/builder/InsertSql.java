package cn.uway.ucloude.data.dataaccess.builder;



import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.exception.DupEntryException;
import cn.uway.ucloude.data.dataaccess.exception.JdbcException;
import cn.uway.ucloude.data.dataaccess.exception.TableNotExistException;
import cn.uway.ucloude.data.dataaccess.utils.SQLFormatter;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * @author magic.s.g.xie
 */
public abstract class InsertSql {

    private static final ILogger LOGGER = LoggerManager.getLogger(InsertSql.class);

    private SqlTemplate sqlTemplate;
    protected StringBuilder sql = new StringBuilder();
    protected List<Object[]> params = new LinkedList<Object[]>();
    public List<Object[]> getParams() {
		return params;
	}

	protected int columnsSize = 0;

    public InsertSql(SqlTemplate sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }

    public abstract InsertSql insert(String table);

    public abstract InsertSql insertIgnore(String table);

    public abstract InsertSql columns(String... columns);
    
    public InsertSql values(Object... values) {
        if (values == null || values.length != columnsSize) {
            throw new JdbcException("values.length must eq columns.length");
        }
        params.add(values);
        return this;
    }
    
    

    public int doInsert() {

        if (params.size() == 0) {
            throw new JdbcException("No values");
        }
        if (params.size() > 1) {
            throw new JdbcException("values.length gt 1, please use doBatchInsert");
        }

        String execSql = sql.toString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(execSql);
        }
        try {
            return sqlTemplate.insert(execSql, params.get(0));
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DupEntryException("Insert SQL Error:" + execSql, e);
            } else if (e.getMessage().contains("doesn't exist Query:")) {
                throw new TableNotExistException("Insert SQL Error:" + execSql, e);
            }
            throw new JdbcException("Insert SQL Error:" + execSql, e);
        } catch (Exception e) {
            throw new JdbcException("Insert SQL Error:" + execSql, e);
        }
    }

    public int[] doBatchInsert() {

        if (params.size() == 0) {
            throw new JdbcException("No values");
        }

        String finalSQL = sql.toString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SQLFormatter.format(finalSQL));
        }

        try {
            Object[][] objects = new Object[params.size()][columnsSize];
            for (int i = 0; i < params.size(); i++) {
                objects[i] = params.get(i);
            }
            return sqlTemplate.batchInsert(finalSQL, objects);
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DupEntryException("Insert SQL Error:" + SQLFormatter.format(finalSQL), e);
            } else if (e.getMessage().contains("doesn't exist Query:")) {
                throw new TableNotExistException("Insert SQL Error:" + SQLFormatter.format(finalSQL), e);
            }
            throw new JdbcException("Insert SQL Error:" + SQLFormatter.format(finalSQL), e);
        } catch (Exception e) {
            throw new JdbcException("Insert SQL Error:" + SQLFormatter.format(finalSQL), e);
        }
    }

    public String getSQL() {
        return sql.toString();
    }

}
