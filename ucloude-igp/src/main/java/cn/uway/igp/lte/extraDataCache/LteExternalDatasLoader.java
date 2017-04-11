package cn.uway.igp.lte.extraDataCache;

import cn.uway.framework.external.ExternalLoader;
import cn.uway.igp.lte.extraDataCache.cache.Cache;
import cn.uway.igp.lte.extraDataCache.cache.CityInfoCache;
import cn.uway.igp.lte.extraDataCache.cache.LteCellCfgCache;
import cn.uway.igp.lte.extraDataCache.cache.LteNeiCellCfgDynamicCache;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class LteExternalDatasLoader implements ExternalLoader {

	private static final ILogger log = LoggerManager.getLogger(LteExternalDatasLoader.class);

	// public NeInfoCache neInfoCache;

	@Override
	public boolean loadExternalDatas() {
		log.debug("【开始初始化LTE外部数据……】");
		Cache.init();
		// 网元数据加载
		// neInfoCache.start();
		{
//			log.debug("【开始初始化LTE网元数据】");
			log.debug("【LTE网元数据将采用延时加载】");
			LteCellCfgCache.startLoad();
//			if (LteCellCfgCache.isEmpty())
//				ImportantILogger.getLogger().warn("【注意】未能加载到网元数据，如果此程序需要关联汇总，请立即关闭程序，并检查网元数据。");
//			else
//				log.debug("【LTE网元数据加载完成】");
		}
		
		{
			log.debug("【开始初始化LTE城市信息数据】");
			CityInfoCache.startLoad();
			if (CityInfoCache.isEmpty())
				log.warn("【注意】未能加载到城市信息，如果此程序采集LTE话单，请检查。");
			else
				log.debug("【LTE城市信息数据加载完成】");
		}
		
		{
//			log.debug("【开始初始化LTE邻区网元数据】");
			log.debug("【LTE邻区数据将采用延时加载】");
			LteNeiCellCfgDynamicCache.startLoad();
//			if (LteNeiCellCfgDynamicCache.isEmpty())
//				ImportantILogger.getLogger().warn("【注意】未能加载到邻区网元数据，如果此程序需要关联汇总，请立即关闭程序，并检查网元数据。");
//			else
//				log.debug("【LTE邻区网元数据加载完成】");
		}		
		
		log.debug("【LTE外部数据初始化完成。】");
		return true;
	}

	// /**
	// * @param neInfoCache
	// */
	// public void setNeInfoCache(NeInfoCache neInfoCache) {
	// this.neInfoCache = neInfoCache;
	// }

}
