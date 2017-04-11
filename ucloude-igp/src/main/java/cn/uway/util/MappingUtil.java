package cn.uway.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.UcloudePathUtil;

public class MappingUtil {
	
	private static ILogger LOGGER = LoggerManager.getLogger(MappingUtil.class);
	
	private static MappingUtil mapping = null;
	
	private MappingUtil()
	{
		
	}
	
	// 城市信息
	public static class CityInfo {

		public String cityId;

		public String chName;
		
		public String enName;

		@Override
		public String toString() {
			return "CityInfo [cityId=" + cityId + ", chName=" + chName + ", enName=" + enName
					+ "]";
		}
	};		
	
	private static Map<String,CityInfo> cityMap = null;
	
	public static synchronized MappingUtil instance()
	{
		if(null == mapping)
		{
			mapping = new MappingUtil();
			mapping.loadCity();
		}
		return mapping;
	}
	
	private void loadCity() {
		final String xmlCfgFile = UcloudePathUtil.makeIgpConfPath("city_mapping.xml");
		File file = new File(xmlCfgFile);
		if (!file.exists() || !file.isFile()) {
			LOGGER.debug("File not found. xml file = "
					+ xmlCfgFile);
			return;
		}

		Document document = null;
		SAXReader reader = new SAXReader();
		FileInputStream input = null;
		try {
			input = new FileInputStream(xmlCfgFile);
			document = reader.read(input);
		} catch (Exception e) {
			LOGGER.error("载入xml文件时发生异常", e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}

		cityMap = new HashMap<String, CityInfo>();
		List<Node> elList = (List<Node>) document.selectNodes("mapping/province/city");
		for(Node node: elList)
		{
			Element el = (Element)node; 
			String cityId = el.attributeValue("city_id");
			CityInfo city = new CityInfo();
			city.cityId = cityId;
			city.enName = el.attributeValue("city_en");
			city.chName = el.attributeValue("city_cn");		
			cityMap.put(cityId, city);
		}
	}
	
	public String getEnCityNameById(String id)
	{
		if(cityMap == null)
		{
			return "";
		}
		return cityMap.get(id) == null?"":cityMap.get(id).enName;
	}
	
	
}
