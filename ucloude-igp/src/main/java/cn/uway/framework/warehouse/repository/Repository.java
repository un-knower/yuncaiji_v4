package cn.uway.framework.warehouse.repository;

import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.warehouse.WarehouseReport;

/**
 * @author chenrongqiang
 */
public interface Repository {

	/**
	 * 获取仓库
	 * 
	 * @return
	 */
	long getReposId();

	/**
	 * 往仓库中写入数据
	 * 
	 * @param outRecords
	 * @return
	 */
	int transport(ParseOutRecord[] outRecords);

	/**
	 * 往仓库中写入数据
	 * 
	 * @param outRecords
	 * @return
	 */
	int transport(ParseOutRecord outRecords);

	/**
	 * 提交数据
	 * 
	 * @param reposId
	 */
	void commit(boolean exceptionFlag);

	/**
	 * 数据回滚
	 * 
	 * @param reposId
	 */
	void rollBack();

	/**
	 * 获取数据仓库报表
	 * 
	 * @return 数据仓库报表
	 */
	WarehouseReport getReport();
}
