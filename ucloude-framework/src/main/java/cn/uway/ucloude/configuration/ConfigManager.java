package cn.uway.ucloude.configuration;



import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

class ConfigManager {
	Map<String, Configuration>  _configs;
	private ConfigManager(String fileName) throws ConfigurationException{
		_configs = new HashMap<String, Configuration>();
		boolean isXml = fileName.endsWith("xml");
		if(_configs.containsKey(fileName))
		{
			_configs.remove(fileName);
		}
		
		_configs.put(fileName, getConfiguration(fileName,isXml));

		
	}
	
	private Configuration getConfiguration(String fileName,boolean isXml) throws ConfigurationException{
		if(isXml== true)
			return new XMLConfiguration( fileName);
		else
			return new PropertiesConfiguration(fileName);
	}
	
	public static ConfigManager create(String fileName) throws Exception{
		return new ConfigManager(fileName);
	}
	
	public Configuration getConfiguration(){
		
		CompositeConfiguration configuration= new CompositeConfiguration();
		for(Map.Entry<String, Configuration> item :_configs.entrySet()){
			configuration.addConfiguration(item.getValue());
		}
		return configuration;
	}
}
