package cn.uway.ucloude.cache.impl;

import org.springframework.beans.factory.InitializingBean;

import cn.uway.ucloude.cache.Cache;
import cn.uway.ucloude.cache.CacheManager;

public class UCacheManager implements InitializingBean, CacheManager {
	private Cache cache;

    /**
     * Specify the Cache instances to use for this CacheManager.
     */
    public void setCache(Cache cache) {
        this.cache = cache;
    }

 

    public Cache getCache() {
        return cache;
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        if(cache == null){
            cache = new ConcurrentMapCache();
        }
    }

}
