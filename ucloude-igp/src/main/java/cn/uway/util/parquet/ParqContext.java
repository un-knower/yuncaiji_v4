package cn.uway.util.parquet;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.hadoop.conf.Configuration;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.hdfs.HdfsConfig;

/**
 * ParqWriter用到的信息(带超时关闭功能)
 * 
 * @author sunt
 *
 */
public class ParqContext {
	protected static ILogger LOG = LoggerManager.getLogger(ParqContext.class);

	// TODO 改为外部可配置
	// 新增配置项，如果配置项不存在就退出程序
	private static String cfgFile = "conf/impala/hdfs.cfg";
	protected static HdfsConfig hdfsCfg;
	protected static SchemaDS schemaDS;
	
	private static Timer timer;
	// 1分钟
	private static long checkPeriod = 1 * 60 * 1000;

	static {
		LOG.info("Ready to load the cfgFile:{}", cfgFile);
		try {
			hdfsCfg = new HdfsConfig(cfgFile);
			schemaDS = new SchemaDS(hdfsCfg.getSchemaFiles());
		} catch (Exception e) {
			LOG.error("The cfgFile loading failure,App will quit：{}", cfgFile);
			System.exit(-1);
		}
		LOG.info("The cfgFile is successfully loaded.");
		
		timer = new Timer("PW超时关闭");
		timer.schedule(new PWMonitor(), checkPeriod, checkPeriod);
	}

	/**
	 * 获取全局配置
	 * 
	 * @return
	 */
	public static Configuration getGlobalCfg() {
		return hdfsCfg.getGlobalCfg();
	}

	/**
	 * new一个新的配置实体，防止配置混淆
	 * 
	 * @return
	 */
	public static Configuration getNewCfg() {
		return hdfsCfg.getNewCfg();
	}

	/**
	 * 根据表名获取对应的schema信息
	 * 
	 * @param tblName 表名
	 * @return
	 */
	public static String getSchema(String tblName) {
		return schemaDS.getSchema(tblName);
	}
	
	/**
	 * 数据入库时，日期字符串的格式
	 * 
	 * @return
	 */
	public static String getDateFormat(){
		return hdfsCfg.getDateFormat();
	}
	
	/**
	 * Cacher名字
	 * 
	 * @return
	 */
	public static String getCacherName(){
		return hdfsCfg.getCacherName();
	}
	
	/**
	 * getPartition
	 * 
	 * @return
	 */
	public static String getPartition(){
		return hdfsCfg.getPartition();
	}
	
	/**
	 * 获取超时关闭配置
	 * 
	 * @return
	 */
	public static int getWaitForClose(){
		return hdfsCfg.getWaitForClose();
	}
}


/**
 * 定时检查是否需要关闭
 * 
 * @author sunt
 *
 */
class PWMonitor extends TimerTask {
	private final ILogger LOG = LoggerManager.getLogger(PWMonitor.class);

	@Override
	public void run() {
		try {
			LOG.debug("wKey.closeAll");
			PWPool.checkClose();
		} catch (Exception e) {
			LOG.error("wKey.closeAll err msg:{};cause:{}", e.getMessage(),
					e.getCause());
		}
	}
}
