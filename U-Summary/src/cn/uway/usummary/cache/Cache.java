package cn.uway.usummary.cache;

import cn.uway.usummary.entity.USummaryConfInfo;

public interface Cache {
	
	/**
	 * 加载配置表到缓存中
	 */
	public boolean loadConf();
	
	/**
	 * 从缓存中获取数据
	 * @param sqlNum
	 * @return
	 */
	public USummaryConfInfo get(Long sqlNum);
}
