package cn.uway.framework.warehouse;

import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.warehouse.exporter.ExporterArgs;

/**
 * WareHouse
 * 
 * @author chenrongqiang 2012-10-31
 */
public interface WareHouse {

	/**
	 * 申请仓库 判断是否还有仓库容量
	 * 
	 * @return 仓库ID
	 */
	boolean isWarehouseReady(Long taskId);

	/**
	 * 通知warehouse 当前任务已经申请一个仓库
	 * 
	 * @param taskId
	 */
	void applyNotice(Long taskId);

	/**
	 * 通知warehouse 当前任务已经关闭一个仓库
	 */
	void shutdownNotice(Long taskId);

	/**
	 * 申请仓库
	 * 
	 * @return 仓库ID
	 */
	long apply(ExporterArgs exporterArgs);

	/**
	 * 往仓库中写入数据
	 * 
	 * @param reposId
	 * @param outRecords
	 * @return
	 */
	int transport(long reposId, ParseOutRecord outRecords);

	/**
	 * 提交数据
	 * 
	 * @param reposId
	 */
	void commit(long reposId, boolean exceptionFlag);

	/**
	 * 数据回滚
	 * 
	 * @param reposId
	 */
	void rollBack(long reposId);

	/**
	 * 获取仓库数据处理报告
	 * 
	 * @param reposId
	 */
	WarehouseReport getReport(long reposId);

	/**
	 * 关闭指定仓库
	 * 
	 * @param reposId
	 */
	void close(long reposId);

	/**
	 * 停止仓库服务
	 */
	void shutdown();
}
