package cn.uway.usummary.cache.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.cache.Cache;
import cn.uway.usummary.dao.USummaryDao;
import cn.uway.usummary.entity.USummaryConfInfo;

public class MapCacheImpl implements Cache{
	
	private static Logger LOG = LoggerFactory.getLogger(MapCacheImpl.class);
	
	private USummaryDao usummaryDao;
	
	private static Map<Long,USummaryConfInfo> cacheMap = null;
	
	public boolean loadConf() {
		Map<Long,USummaryConfInfo> cacheMapTmp = usummaryDao.loadConf();
		if(cacheMapTmp == null){
			LOG.debug("加载usummary_cfg_conf表数据失败!");
			return false;
		}
		if(cacheMap != null){
			cacheMap.clear();
			cacheMap.putAll(cacheMapTmp);
		}else{
			cacheMap = cacheMapTmp;
		}
		
		return true;
	}

	public USummaryConfInfo get(Long sqlNum) {
		// 缓存为空，重新加载缓存
		if(cacheMap == null){
			this.loadConf();
		}
		
		// 根据SQL编号获取信息
		if(cacheMap.get(sqlNum) == null){
			USummaryConfInfo conf = usummaryDao.queryConfById(sqlNum);
			if(conf == null){
				LOG.debug("usummary_cfg_conf表中没有sqlNum="+sqlNum+"的记录或没启用该记录!");
				return null;
			}
			cacheMap.put(conf.getSqlNum(), conf);
			return conf;
		}
		return cacheMap.get(sqlNum);
	}

	public USummaryDao getUsummaryDao() {
		return usummaryDao;
	}

	public void setUsummaryDao(USummaryDao usummaryDao) {
		this.usummaryDao = usummaryDao;
	}
	
}
