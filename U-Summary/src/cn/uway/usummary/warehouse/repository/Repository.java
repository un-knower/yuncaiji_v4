package cn.uway.usummary.warehouse.repository;

import java.util.Map;

import cn.uway.usummary.entity.WarehouseReport;

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
	int transport(Map<String,String> record);

	/**
	 * 提交数据
	 * 
	 * @param reposId
	 */
	void commit(boolean exceptionFlag);

	/**
	 * 获取数据仓库报表
	 * 
	 * @return 数据仓库报表
	 */
	WarehouseReport getReport();
}
