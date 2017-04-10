package cn.uway.ucloude.data.dataaccess.builder;



import cn.uway.ucloude.data.dataaccess.SqlTemplate;
import cn.uway.ucloude.data.dataaccess.exception.JdbcException;
import cn.uway.ucloude.data.dataaccess.utils.SQLFormatter;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * @author magic.s.g.xie
 */
public abstract class DropTableSql {

    private static final ILogger LOGGER = LoggerManager.getLogger(DropTableSql.class);

    private SqlTemplate sqlTemplate;
    protected StringBuilder sql = new StringBuilder();

    public DropTableSql(SqlTemplate sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }

    public abstract DropTableSql drop(String table);

    public boolean doDrop() {

        String finalSQL = sql.toString();

        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(SQLFormatter.format(finalSQL));
            }

            sqlTemplate.update(sql.toString());
        } catch (Exception e) {
            throw new JdbcException("Drop Table Error:" + SQLFormatter.format(finalSQL), e);
        }
        return true;
    }

}
