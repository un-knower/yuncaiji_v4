package cn.uway.framework.warehouse.exporter;

import java.util.Set;
import java.util.concurrent.Callable;

import cn.uway.framework.cache.Cacher;

/**
 * Exporter 输出器 如:写文件、写数据库、北向接口等
 * 
 * @author chenrongqiang 2012-10-31
 */
public interface Exporter extends Callable<ExportFuture> {

	/**
	 * 输出数据块
	 * 
	 * @param blockData
	 */
	void export(BlockData blockData) throws Exception;

	/**
	 * 关闭输出器 释放资源 如DB关闭connection/statement/ResultSet ，文件输出器关闭文件流等 close方法正常情况不由外部调用 Exporter在判断数据输出结束后自己关闭释放资源。
	 */
	void close();

	/**
	 * 接口方法 Exporter实现当输出发生异常时，并且需要关闭输出器
	 */
	void endExportOnException();

	/**
	 * 设置输出数据类型
	 * 
	 * @param dataType
	 */
	void setDataType(int dataType);

	/**
	 * 获取Exporter的类型
	 * 
	 * @return
	 */
	int getType();

	/**
	 * 获取当前Exporter使用的缓存
	 * 
	 * @return Cacher
	 */
	Cacher getCacher();

	/**
	 * 获取输出器的输出模版ID
	 * 
	 * @return
	 */
	int getExportId();
	
	/**
	 * 构建输出字段属性集合表
	 * @param propertysSet 用于存放当前export使用的属性集合
	 */
	void buildExportPropertysList(Set<String> propertysSet);
	
	/**
	 * 终止处理
	 * @param breakCause 终止原因
	 */
	void breakProcess(String breakCause);
}
