package cn.uway.framework.external;

import java.util.Map;

public interface SwitchManager {

	/**
	 * 打开指定的配置
	 * @return
	 */
	public Boolean open();
	
	/**
	 * 使用前需要先判断是否缓存完毕
	 * @return
	 */
	public Boolean isReady();
	
	public String getCacheValue(String cscheName,String key);
	
	public Map<String, String> getCacheMap(String cscheName,String key);
	
	public Map<String, String> getCacheCityIdCityNameMap(String cscheName,String key);
}
