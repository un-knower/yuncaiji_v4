package cn.uway.ucloude.configuration.test;



import java.util.Iterator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;

public class ConfigManagerTest {
	@Test
	public void testGetConfiguration(){
//		ConfigManager cm = ConfigManager.create("elib.xml");
//		Configuration cfg = cm.getConfiguration();
//		String value = cfg.;
//		System.out.println(cfg);
		try {
			XMLConfiguration cfg = new XMLConfiguration("./elib.xml");
			Iterator<String> iterators = cfg.getKeys();
			while(iterators.hasNext()){
				System.out.println(iterators.next());
			}
			for(String key :cfg.getStringArray("configSections.section[@name]"))
			{
				System.out.println("ConfigSections:"+key);
			}
			//String value = cfg.getKeys();
			EnvironmentConfiguration config =new EnvironmentConfiguration();
			System.out.println(config.getString("JAVA_HOME"));
			//System.out.println(config.getString("MEMERY"));
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
