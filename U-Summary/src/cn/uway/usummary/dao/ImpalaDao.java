package cn.uway.usummary.dao;

import org.apache.commons.dbcp.BasicDataSource;

public class ImpalaDao {
	/**
	 * 数据库连接池
	 */
	private BasicDataSource datasource;

	public BasicDataSource getDatasource() {
		return datasource;
	}

	public void setDatasource(BasicDataSource datasource) {
		this.datasource = datasource;
	}
	
}
