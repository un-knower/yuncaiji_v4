package cn.uway.usummary.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class CustomerPropertyConfigurer extends PropertyPlaceholderConfigurer{
	
	private static Map<String,String> properties = new HashMap<String,String>();
	
	protected void processProperties(
			ConfigurableListableBeanFactory beanFactoryToProcess,
			Properties props) throws BeansException {
		for(Entry<Object,Object> entry:props.entrySet()){
			String stringKey = String.valueOf(entry.getKey());
			String stringValue = String.valueOf(entry.getValue());
			properties.put(stringKey, stringValue);
		}
		super.processProperties(beanFactoryToProcess, props);
	}
	
	public static Map<String, String> getProperties() {
		return properties;
	}
	
	public static String getProperty(String key){
		return properties.get(key);
	}
}


