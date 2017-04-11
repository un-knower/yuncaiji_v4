package cn.uway.util.hdfs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * HDFS 配置信息类
 * 
 * @author sunt
 *
 */
public class HdfsConfig {
	private static ILogger LOG = LoggerManager.getLogger(HdfsConfig.class);
	// 配置项通用分隔符
	private final static String SPERATOR = ",";

	public final static String KEY_HADOOP_USER_NAME = "HADOOP_USER_NAME";
	public final static String HDFS_USER_NAME = "hdfsUserName";
	public final static String SCHEMA_FILE = "schemaFiles";
	public final static String LOG_CON_FFILE = "logConfFile";
	public final static String DATE_FORMAT = "dateFormat";
	public final static String WAIT_FOR_CLOSE = "waitForClose";
	public final static String CACHER_NAME = "cacherName";
	public final static String PARTITION = "partition";

	private static Pattern pattern = Pattern.compile("(\\d+)M");

	private Configuration globalCfg = new Configuration();

	public HdfsConfig(String cfgFileName) throws FileNotFoundException, IOException {
		init(cfgFileName);
	}

	public void init(String cfgFileName) throws FileNotFoundException, IOException {
		globalCfg.addResource(new Path("conf/core-site.xml"));
		globalCfg.addResource(new Path("conf/hdfs-site.xml"));
		Properties properties = new Properties();
		properties.load(new FileInputStream(cfgFileName));
//		String logConfFile = properties.getProperty(LOG_CON_FFILE);
//		if(null != logConfFile){
//			PropertyConfigurator.configure(logConfFile);
//		}
		for (Entry<Object, Object> entry : properties.entrySet()) {
			String val = entry.getValue().toString();
			Matcher m = pattern.matcher(val);
			if (m.find()) {
				long tmp = Integer.parseInt(m.group(1)) * 1024 * 1024;
				globalCfg.setLong(entry.getKey().toString(), tmp);
			} else {
				globalCfg.set(entry.getKey().toString(), val);
			}
		}
		System.setProperty(KEY_HADOOP_USER_NAME, globalCfg.get(HDFS_USER_NAME));
		LOG.info("{}加载完成", cfgFileName);
	}

	/**
	 * 获取hdfs配置，暂时只包括配置文件里的配置
	 * 
	 * @return
	 */
	public Configuration getNewCfg() {
		return new Configuration(globalCfg);
	}
	
	/**
	 * 获取默认配置
	 * 
	 * @return
	 */
	public Configuration getGlobalCfg() {
		return globalCfg;
	}
	
	/**
	 * 获取日期字符串
	 * 
	 * @return
	 */
	public String getDateFormat(){
		return globalCfg.get(DATE_FORMAT);
	}
	
	/**
	 * 获取超时关闭配置
	 * 
	 * @return
	 */
	public int getWaitForClose(){
		return globalCfg.getInt(WAIT_FOR_CLOSE,10);
	}
	/**
	 * 获取cacher名字
	 * 
	 * @return
	 */
	public String getCacherName(){
		return globalCfg.get(CACHER_NAME,"");
	}
	/**
	 * 获取分区粒度
	 * 
	 * @return
	 */
	public String getPartition(){
		return globalCfg.get(PARTITION,"none");
	}

	public String[] getSchemaFiles() {
		String str = globalCfg.get(SCHEMA_FILE);
		return str.split(SPERATOR);
	}

}
