package cn.uway.ucloude.data.dataaccess;


public abstract class JdbcAbstractAccess {
	private SqlTemplate sqlTemplate;

    public JdbcAbstractAccess(String connKey) {
        this.sqlTemplate = SqlTemplateFactory.create(connKey);
    }

    public SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }
}
