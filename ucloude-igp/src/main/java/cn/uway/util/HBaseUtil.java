package cn.uway.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.util.Bytes;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.UcloudePathUtil;

/**
 * hbase for version 1.1.1
 * 
 * @author tylerlee @ 2016年8月17日
 */
public class HBaseUtil {

	public static final String ZK_QUORUM = "hbase.zookeeper.quorum";

	public static final String ZK_CLIENTPORT = "hbase.zookeeper.property.clientPort";

	private Configuration conf;

	private Connection connection;

	private static final ILogger LOGGER = LoggerManager.getLogger(HBaseUtil.class);

	/**
	 * @param hbaseConf
	 *            hbase访问配置文件
	 */
	public HBaseUtil(String hbaseConf) {
		conf = HBaseConfiguration.create();
		conf.addResource(new Path(UcloudePathUtil.makeIgpConfPath(hbaseConf)));
	}

	/**
	 * @param zkQuorum
	 *            hbase.zookeeper.quorum
	 * @param zkClientPort
	 *            hbase.zookeeper.property.clientPort
	 */
	public HBaseUtil(String zkQuorum, String zkClientPort) {
		conf = HBaseConfiguration.create();
		conf.set(ZK_QUORUM, zkQuorum);
		conf.set(ZK_CLIENTPORT, zkClientPort);
	}

	public void connectServer() throws IOException {
		LOGGER.debug("正在尝试连接HBase结数库. quorum=[{}], port={}", conf.get(ZK_QUORUM), conf.get(ZK_CLIENTPORT));
		// Connection 的创建是个重量级的工作，线程安全，是操作hbase的入口
		connection = ConnectionFactory.createConnection(conf);
		LOGGER.debug("HBase数据库连接成功.");
	}

	/**
	 * 判断表是否存在
	 */
	public boolean tableExist(String table_name) throws IOException {
		TableName tn = TableName.valueOf(table_name);
		Admin admin = connection.getAdmin();
		boolean e = admin.tableExists(tn);
		admin.close();
		return e;
	}

	/**
	 * 创建一个表
	 * 
	 * @param table_name
	 *            表名称
	 * @param compressType
	 *            压缩算法
	 * @param family_names
	 *            列族名称集合
	 * @throws IOException
	 */
	public boolean createTable(String tableName, Algorithm compressType, String... familyNames) throws IOException {
		// 获取TableName
		TableName tn = TableName.valueOf(tableName);
		Admin admin = connection.getAdmin();
		if (admin.tableExists(tn))
			return false;
		// table 描述
		HTableDescriptor hTable = new HTableDescriptor(tn);
		for (String fn : familyNames) {
			// column 描述，添加列簇
			HColumnDescriptor family = new HColumnDescriptor(fn);
			if (compressType != null) {
				family.setCompressionType(compressType);
			}
			hTable.addFamily(family);
		}
		admin.createTable(hTable);
		admin.close();
		return true;
	}

	/**
	 * 创建一个表
	 * 
	 * @param table_name
	 *            表名称
	 * @param family_names
	 *            列族名称集合
	 * @throws IOException
	 */
	public boolean createTable(String tableName, String... familyNames) throws IOException {
		return createTable(tableName, null, familyNames);
	}

	/**
	 * 根据表名获取数据表
	 * 
	 * @param tableName
	 *            表名
	 * @param cacheBuffSize
	 *            表入库缓存尺寸
	 * @return
	 * @throws IOException
	 */
	public Table getTable(String tableName, int cacheBuffSize) throws IOException {
		TableName tn = TableName.valueOf(tableName);
		if (connection == null)
			return null;

		Table table = connection.getTable(tn);
		if (table == null)
			return null;

		table.setWriteBufferSize(cacheBuffSize);
		return table;
	}

	/**
	 * 删除表
	 * 
	 * @param tableName
	 *            表名
	 * @return
	 * @throws IOException
	 */
	public boolean dropTable(String tableName) throws IOException {
		TableName tn = TableName.valueOf(tableName);
		Admin admin = connection.getAdmin();
		boolean del = false;
		if (admin.tableExists(tn)) {
			// 在删除一张表前，要使其失效
			admin.disableTable(tn);
			admin.deleteTable(tn);
			del = true;
		}
		admin.close();
		return del;
	}

	/**
	 * 增加一条记录
	 * 
	 * @param tableName
	 *            表名
	 * @param rowKey
	 *            行键
	 * @param family
	 *            列族名称
	 * @param values
	 *            列名、列值
	 * @return
	 * @throws IOException
	 */
	public boolean insertSingleRow(String tableName, String rowKey, String family, Map<String, String> values) throws IOException {
		if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(family) || values == null || values.size() < 0)
			throw new NullPointerException("table name,family,values exist null or '',please recheck it.");

		// List<Put> putList = new ArrayList<Put>();
		// 一个PUT代表一行，构造函数传入的是RowKey
		Put put = null;
		if (StringUtils.isEmpty(rowKey)) {
			put = new Put(Bytes.toBytes(System.currentTimeMillis()));
		} else {
			put = new Put(Bytes.toBytes(rowKey));
		}
		for (Map.Entry<String, String> item : values.entrySet()) {
			String value = item.getValue();
			if (StringUtils.isEmpty(value))
				continue;
			String qualifier = item.getKey();
			put.addColumn(Bytes.toBytes(family), StringUtils.isEmpty(qualifier) ? null : Bytes.toBytes(qualifier), Bytes.toBytes(value));
		}
		if (put.isEmpty()) {
			return false;
		}
		TableName tn = TableName.valueOf(tableName);
		Table table = connection.getTable(tn);
		table.put(put);
		table.close();
		return true;
	}

	/**
	 * 添加多行数据
	 * 
	 * @param tableName
	 *            表名
	 * @param family
	 *            列族名称
	 * @param values
	 *            行键、列名、列值
	 * @throws IOException
	 */
	public boolean insertManyRow(String tableName, String family, Map<String, Map<String, String>> values) throws IOException {
		if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(family) || values == null || values.size() < 0)
			throw new NullPointerException("table name,family,values exist null or '',please recheck it.");

		List<Put> putList = new ArrayList<Put>();
		// 一个PUT代表一行，构造函数传入的是RowKey
		Set<Entry<String, Map<String, String>>> entrySet = values.entrySet();
		for (Entry<String, Map<String, String>> item : entrySet) {
			Map<String, String> columnValue = item.getValue();
			if (columnValue == null)
				continue;

			Put put = null;
			if (StringUtils.isEmpty(item.getKey())) {
				put = new Put(Bytes.toBytes(System.currentTimeMillis()));
			} else {
				put = new Put(Bytes.toBytes(item.getKey()));
			}
			Set<Entry<String, String>> cvSet = columnValue.entrySet();
			for (Entry<String, String> cv : cvSet) {
				String value = cv.getValue();
				if (StringUtils.isEmpty(value))
					continue;
				String qualifier = cv.getKey();
				put.addColumn(Bytes.toBytes(family), StringUtils.isEmpty(qualifier) ? null : Bytes.toBytes(qualifier), Bytes.toBytes(value));
			}
			if (!put.isEmpty()) {
				putList.add(put);
			}
		}
		if (putList.isEmpty()) {
			return false;
		}
		TableName tn = TableName.valueOf(tableName);
		Table table = connection.getTable(tn);
		table.put(putList);
		table.close();
		return true;
	}

	/**
	 * 删除指定行数据（没有返回值，有就删除，没有就忽略）
	 * 
	 * @param rowKey
	 *            行键
	 * @throws IOException
	 */
	public void deleteRow(String tableName, String rowKey) throws IOException {
		// 表名对象
		TableName tn = TableName.valueOf(tableName);
		// 表对象
		Table table = connection.getTable(tn);
		Delete delete = new Delete(Bytes.toBytes(rowKey));
		// 没有返回值，有就删除该条数据；没有就忽略，也不会抛出异常
		table.delete(delete);
		table.close();
	}

	/**
	 * 删除指定名称的列簇(没有返回值，只要family存在就已经删除成功了)
	 * 
	 * @param tableName
	 *            表名
	 * @param family
	 *            列族名
	 * @throws IOException
	 */
	public void deleteFamily(String tableName, String family) throws IOException {
		Admin admin = connection.getAdmin();
		TableName tn = TableName.valueOf(tableName);
		// 没有返回值，只要family存在就已经删除成功了；如果family不存在就会抛出异常
		admin.deleteColumn(tn, Bytes.toBytes(family));
		admin.close();
	}

	/**
	 * 获取所有数据
	 * 
	 * @param tableName
	 *            表名
	 * @return Map<String|rowkey, Map<String|cellName, String|cellValue>>
	 */
	public Map<String, Map<String, String>> queryAll(String tableName) throws IOException {
		return queryRange(tableName, null, null);
	}

	/**
	 * 获取所有数据
	 * 
	 * @param tableName
	 *            表名
	 * @param startRow
	 *            开始行
	 * @param stopRow
	 *            结束行
	 * @return Map<String|rowkey, Map<String|cellName, String|cellValue>>
	 * @throws IOException
	 */
	public Map<String, Map<String, String>> queryRange(String tableName, String startRow, String stopRow) throws IOException {
		Table table = connection.getTable(TableName.valueOf(tableName));
		/**
		 * 查询所有记录,每个cell代表一个列簇中的一个区域， 例如：有一个列簇为 test_1<br/>
		 * 1.如果存储数据时没有存储列修饰符，则cell代表整个列簇的内容，查询出的就是该行下整个列簇的内容 <br/>
		 * 2.如果存储数据时有存储列修饰符，则每个列簇下的列修饰符各有一个cell
		 */
		Scan scan = null;
		if (startRow != null) {
			if (stopRow != null)
				scan = new Scan(Bytes.toBytes(startRow), Bytes.toBytes(stopRow));
			else
				scan = new Scan(Bytes.toBytes(startRow));
		} else {
			scan = new Scan();
		}
		ResultScanner resultScaner = table.getScanner(scan);
		Map<String, Map<String, String>> resultMap = new HashMap<String, Map<String, String>>();
		/**
		 * 循环读取按行区分： 读取结果为： <br/>
		 * 该表RowKey为：1445320222118 列簇为：test_1 值为：这是第一行第一列的数据 列簇为：test_2 值为：这是第一行第二列的数据 列簇为：test_3 值为：这是第一行第三列的数据<br/>
		 * 该表RowKey为：1445320222120 列簇为：test_1 值为：这是第二行第一列的数据 列簇为：test_2 值为：这是第二行第二列的数据 列簇为：test_3 值为：这是第二行第三列的数据
		 */
		for (Result rs : resultScaner) {
			// String str = Bytes.toString(rs.getValue(Bytes.toBytes("info"), Bytes.toBytes("money")));
			Map<String, String> cellMap = new HashMap<String, String>();
			// 这边循环是按cell进行循环
			for (Cell cell : rs.rawCells()) {
				cellMap.put(Bytes.toString(CellUtil.cloneQualifier(cell)), Bytes.toString(CellUtil.cloneValue(cell)));
			}
			// System.out.println("为：" + new String(rs.getRow()));
			// 该表每行记录的RowKey
			resultMap.put(Bytes.toString(rs.getRow()), cellMap);
		}
		table.close();
		return resultMap;
	}

	/**
	 * 根据RowKey查询单行
	 * 
	 * @param tableName
	 *            表名
	 * @param rowKey
	 *            行键
	 * @return
	 * @throws IOException
	 */
	public Map<String, String> queryByRowKey(String tableName, String rowKey) throws IOException {
		Table table = connection.getTable(TableName.valueOf(tableName));
		Result rs = table.get(new Get(Bytes.toBytes(rowKey)));
		Map<String, String> cellMap = new HashMap<String, String>();
		for (Cell cell : rs.rawCells()) {
			// 疑问：同个行，一个列簇里具有多列的查询？
			cellMap.put(Bytes.toString(CellUtil.cloneQualifier(cell)), Bytes.toString(CellUtil.cloneValue(cell)));
		}
		table.close();
		return cellMap;
	}

	/**
	 * 获取HBase连接(初始化connection是个重量级的工作，线程安全，是操作hbase的入口)
	 * 
	 * @return Connection
	 */
	public Connection getConnection() {
		return connection;
	}

	public Configuration getConf() {
		return conf;
	}

	public void close() {
		try {
			if (connection != null)
				connection.close();
		} catch (IOException e) {
			LOGGER.error("Hbase关闭错误", e);
		}
	}
}
