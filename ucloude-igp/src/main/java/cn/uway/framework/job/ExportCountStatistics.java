package cn.uway.framework.job;

import java.util.Map;

/**
 * 用于获取入库情况统计信息。
 * 
 * @author chensijiang 2014-9-16
 */
public interface ExportCountStatistics {

	
	
	/**
	 * 获取入库情况统计信息。KEY为表为，Integer为入库数量。应返回拷贝，而不是引用。
	 * 
	 * @return 入库情况统计信息。
	 */
	public Map<String, Integer> getExportCountStatics();

	/**
	 * 重置入库情况统计信息。
	 */
	public void resetExportCountStatics();
}
