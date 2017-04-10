package cn.uway.usummary.cache;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.context.AppContext;
import cn.uway.usummary.entity.USummaryConfInfo;

public class CacheManager {
	
	private static Logger LOG = LoggerFactory.getLogger(CacheManager.class);
	
	private  Cache cache = null;
	
	// 缓存刷新周期
	private  Integer periodMinutes = 60;
	
	public void loadCache(){
		LOG.debug("开始加载map缓存!");
		if(!cache.loadConf()){
			LOG.debug("加载map缓存失败，程序退出!");
			System.exit(0);
		}
		LOG.debug("加载map缓存完成!");
		new Timer().schedule(new TimerLoader(), periodMinutes.intValue()*60*1000);
	}
	
	public synchronized USummaryConfInfo get(Long sqlNum){
		return cache.get(sqlNum);
	}
	
	public synchronized void refreshCache(){
		LOG.debug("开始刷新map缓存!");
		cache.loadConf();
		LOG.debug("刷新map缓存成功!");
	}
	
	class TimerLoader extends TimerTask{

		@Override
		public void run() {
			refreshCache();
		}
		
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public Integer getPeriodMinutes() {
		return periodMinutes;
	}

	public void setPeriodMinutes(Integer periodMinutes) {
		this.periodMinutes = periodMinutes;
	}

	
}
