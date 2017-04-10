package cn.uway.usummary.export;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import cn.uway.usummary.cache.Cacher;
import cn.uway.usummary.entity.ExportFuture;

public interface Exporter  extends Callable<ExportFuture>{
	
	public void export(List<Map<String,String>> records) throws Exception;
	
	/**
	 * 获取当前Exporter使用的缓存
	 * 
	 * @return Cacher
	 */
	public Cacher getCacher();
	
	/**
	 * 终止处理
	 * @param breakCause 终止原因
	 */
	public void breakProcess(String breakCause);
	
	public void close();
}
