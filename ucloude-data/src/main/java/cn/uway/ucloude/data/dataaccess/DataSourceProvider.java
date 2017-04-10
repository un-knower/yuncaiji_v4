package cn.uway.ucloude.data.dataaccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

import cn.uway.ucloude.configuration.UCloudeConfiguration;
import cn.uway.ucloude.data.dataaccess.dao.ConnectionDAO;
import cn.uway.ucloude.data.dataaccess.model.ProtocalType;
import cn.uway.ucloude.data.dataaccess.model.ProtocalView;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class DataSourceProvider {
	private static ILogger logger = LoggerManager.getLogger(DataSourceProvider.class);
	
	static Map<String, DataSource> maps ;
	
	static Map<String, String> providers;
	
	static {
		
	}
	

	public static void initialDataSource(String confPath){
		confPath = confPath + "/jdbc.properties";
		Properties configuration = getProperties(confPath);
		try {
			//configuration = ;
			if(configuration != null)
			{
				DataSource ds = getDataSource(configuration);
				initMap(ds);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("获取原始jdbc数据源出错",e);
		}
		
		
	}
	
	private static Properties getProperties(String cfgPath)
	{
		 Properties conf = new Properties();
	        File file = new File(cfgPath);
	        InputStream is = null;
	        try {
	            is = new FileInputStream(file);
	        } catch (FileNotFoundException e) {
	        	is = DataSourceProvider.class.getClassLoader().getResourceAsStream("conf/jdbc.properties");
	        }
	        try {
	            conf.load(is);
	        } catch (IOException e) {
	           
	        }
	        return conf;
	}
	
	private static void initMap(DataSource ds){
		maps = new HashMap<String, DataSource>();
		providers = new HashMap<String, String>();
		List<ProtocalView> views = ConnectionDAO.getInstance().getProtocalView(ds, ProtocalType.DB);
		if(views != null && !views.isEmpty()){
			for(ProtocalView view:views){
				if(!maps.containsKey(view.connKey)){
					maps.put(view.connKey, createDataSource(view));
					providers.put(view.connKey, view.getDriver());
				}
			}
		}
	}
	
//	/**
//	 * 创建数据源DataSourceProvider
//	 * @return
//	 */
//	public static DataSourceProvider createProvider(){
//		return new DataSourceProvider();
//	}
	
	/**
	 * 获取数据源
	 * @param key
	 * @return
	 */
	public static DataSource getDataSource(String key){
		if(maps.containsKey(key))
			return maps.get(key);
		return null;
	}
	
	public static String getDriver(String key){
		if(providers.containsKey(key))
			return providers.get(key);
		return null;
	}
	
	private static DataSource getDataSource(Properties configuration){
		
        ProtocalView view = new ProtocalView();
        view.setUrl(configuration.getProperty("jdbc.url"));
        view.setDriver(configuration.getProperty("jdbc.driverClassName"));
        view.setPassWord(configuration.getProperty("jdbc.password"));
        view.setUserName(configuration.getProperty("jdbc.username"));
        view.setMaxActive(15);
        view.setMaxIdle(15);
        view.setMaxWait(3);
        view.setValidateQuery("select * from dual ");
        return createDataSource(view);

	}
	
	private static DataSource createDataSource(ProtocalView view){
		BasicDataSource datasource = null;
		try{
			//LOGGER.debug("creating dbpool...");
			Properties properties = new Properties();
			properties.put("type", "javax.sql.DataSource");
			properties.put("driverClassName", view.getDriver());
			properties.put("url", view.getUrl());
			properties.put("username", view.getUserName());
			properties.put("password", view.getPassWord());
			properties.put("maxActive", view.getMaxIdle());
			properties.put("maxIdle", view.getMaxIdle());
			properties.put("maxWait", view.getMaxWait());
			properties.put("validationQuery", view.getValidateQuery());
			properties.put("testOnBorrow", "true");
			properties.put("testOnReturn", "true");
			properties.put("testWhileIdle", "true");
			datasource = (BasicDataSource)BasicDataSourceFactory.createDataSource(properties);
			//LOGGER.debug("DbPoolManager：创建数据库连接池，用户：{}", view.getUserName());
		}catch(Exception e){
			logger.error(String.format("创建数据库连接池,用户：{}", view.toString()), e);
			//LOGGER.error("DbPoolManager：创建数据源失败：", e);
		}
		return datasource;
	}
}
