package cn.uway.ucloude.data.dataaccess;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

public class SqlTemplateFactory {
	 private static final ConcurrentMap<DataSource, SqlTemplate> HOLDER = new ConcurrentHashMap<DataSource, SqlTemplate>();

    public static SqlTemplate create(String key) {
        //DataSourceProvider dataSourceProvider = DataSourceProvider.createProvider();
        DataSource dataSource = DataSourceProvider.getDataSource(key);

        SqlTemplate sqlTemplate = HOLDER.get(dataSource);
        if (sqlTemplate != null) {
            return sqlTemplate;
        }
        synchronized (HOLDER) {
            sqlTemplate = HOLDER.get(dataSource);
            if (sqlTemplate != null) {
                return sqlTemplate;
            }
            sqlTemplate = new SqlTemplateImpl(dataSource,DataSourceProvider.getDriver(key));
            HOLDER.putIfAbsent(dataSource, sqlTemplate);
            return sqlTemplate;
        }
    }
}
