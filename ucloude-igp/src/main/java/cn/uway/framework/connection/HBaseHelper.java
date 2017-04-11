package cn.uway.framework.connection;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.UcloudePathUtil;
/**
 * 这个是HBase 0.94的支持包位置
 * //import org.apache.hadoop.hbase.io.hfile.Compression.Algorithm; 
 */


@SuppressWarnings("deprecation")
public class HBaseHelper {
	private static Map<String, HBaseHelper> helpers = new ConcurrentHashMap<String, HBaseHelper>();
	private static final ILogger LOGGER = LoggerManager.getLogger(HBaseHelper.class);
	
	protected Configuration hbaseConf;
	protected HConnection connection;
	protected String propertyFileName;
	
	/**
	 * 根据配置文件名，获取HBaseHelper对象
	 * @param propFileName
	 * @return
	 * @throws IOException 
	 */
	public static synchronized HBaseHelper getHelper(String propFileName) throws IOException {
		HBaseHelper helper = helpers.get(propFileName);
		if (helper == null) {
			helper = new HBaseHelper(propFileName);
			helpers.put(propFileName, helper);
		}
		
		if (!helper.isConnectionAvaliable()) {
			helper.connectServer(propFileName);
		}
		
		return helper;
	}
	
	/**
	 * 关闭HBaseHelper;
	 * @param helper
	 */
	public static synchronized void closeHelper(HBaseHelper helper) {
		if (helper == null) {
			return;
		}
		
		helpers.remove(helper.getPropertyFileName());
		helper.close();
	}
	
	/**
	 * @param propFileName 配置文件名
	 */
	public HBaseHelper(String propFileName) {
		this.propertyFileName = propFileName;
	}
	
	/**
	 * 连接HBASE服务器
	 * @param propFileName 连接配置文件名
	 * @return
	 */
	public void connectServer(String propFileName) throws IOException {
		hbaseConf = HBaseConfiguration.create();
		hbaseConf.addResource(new Path(UcloudePathUtil.makeIgpConfPath(propFileName)));
		//hbaseConf.set("hbase.zookeeper.quorum","192.168.15.128");  
		//hbaseConf.set("hbase.zookeeper.property.clientPort", "2181");  
		
		LOGGER.debug("正在尝试连接HBASE结数库. quorum=[{}], port={}", hbaseConf.get("hbase.zookeeper.quorum"), hbaseConf.get("hbase.zookeeper.property.clientPort"));
		connection = HConnectionManager.createConnection(hbaseConf);
		LOGGER.debug("HBASE数据库连接成功");
	}
	
	/**
	 * 创建数据表
	 * @param tableName		表名
	 * @param columnFamilys	列名链表
	 * @throws Exception
	 */
    public synchronized void creatTable(String tableName, List<String> columnFamilys, Algorithm compressType)  
            throws Exception {  
    	LOGGER.debug("正在初始化HBaseAdmin实例... ");
        HBaseAdmin admin = new HBaseAdmin(hbaseConf);
        LOGGER.debug("检测表名是否存在... 表名:{}", tableName);
        if (admin.tableExists(tableName)) {  
        	LOGGER.debug("HBase数据表已存在. 表名:{}", tableName) ;
        } else {  
        	LOGGER.debug("HBase开始创建表，表名:{} 列族压缩方式：{}, 列族：{}", new Object[]{tableName, (compressType==null?"none":compressType.toString()), columnFamilys});
			HTableDescriptor tableDesc = new HTableDescriptor(tableName);  
            for (String columnFamily : columnFamilys) {
            	HColumnDescriptor columnDesc = new HColumnDescriptor(columnFamily);
            	if (compressType != null) {
            		columnDesc.setCompressionType(compressType);
            	}
            	tableDesc.addFamily(columnDesc);	
            }  
            admin.createTable(tableDesc);  
            LOGGER.debug("HBase数据表创建成功. 表名:{}", tableName);
        }
        admin.close();
    }  

	/** 删除表
	 * @param tableName	表名
	 * @throws MasterNotRunningException
	 * @throws ZooKeeperConnectionException
	 * @throws IOException
	 */
	public synchronized void dropTable(String tableName) throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
		HBaseAdmin admin = new HBaseAdmin(hbaseConf);
		if (admin.tableExists(tableName)) {
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
			LOGGER.debug("HBase数据表删除成功.表名:{}", tableName) ;
		} else {
			LOGGER.debug("HBase数据表删除失败, 数据表不存在. 表名:{}", tableName) ;
		}
		
		admin.close();
	}
	
	/**
	 * 根据表名获取数据表
	 * @param tableName		表名
	 * @param cacheBuffSize	表入库缓存尺寸
	 * @return
	 * @throws IOException
	 */
	public HTableInterface getTable(String tableName, int cacheBuffSize) throws IOException {
		if (connection == null)
			return null;
		
		HTableInterface table = connection.getTable(tableName);
        
		// 设置缓存，不自动刷新
		table.setAutoFlush(false);
        table.setWriteBufferSize(cacheBuffSize);
        
        return table;
	}
	
	/**
	 * 连接是否有效
	 * @return true:有效; false:无效
	 */
	public boolean isConnectionAvaliable() {
		if (this.connection == null)
			return false;
		
		return !connection.isClosed();
	}
	
	/**
	 * 关闭HBase连接
	 */
	public void close() {
		try {
			this.connection.close();
		} catch (IOException e) {
			LOGGER.warn("关闭HBase连接发生异常", e);
		}
	}

	
	public Configuration getHbaseConf() {
		return hbaseConf;
	}

	
	public void setHbaseConf(Configuration hbaseConf) {
		this.hbaseConf = hbaseConf;
	}

	
	public HConnection getConnection() {
		return connection;
	}

	
	public void setConnection(HConnection connection) {
		this.connection = connection;
	}

	
	public String getPropertyFileName() {
		return propertyFileName;
	}

	
	public void setPropertyFileName(String propertyFileName) {
		this.propertyFileName = propertyFileName;
	}
}
