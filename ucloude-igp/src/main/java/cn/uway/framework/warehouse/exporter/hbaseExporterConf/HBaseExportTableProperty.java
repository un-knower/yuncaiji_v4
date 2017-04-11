package cn.uway.framework.warehouse.exporter.hbaseExporterConf;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.util.Bytes;

import cn.uway.framework.warehouse.exporter.AbsHBaseKeyBuilder;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 这个是HBase 0.94的支持包位置 //import
 * org.apache.hadoop.hbase.io.hfile.Compression.Algorithm;
 */

public class HBaseExportTableProperty {

	/**
	 * 日志
	 */
	protected static final ILogger LOGGER = LoggerManager
			.getLogger(HBaseExportTableProperty.class);

	public enum HbaseExportKeyType {
		e_string, e_byte, e_short, e_int, e_long, e_date,
	}

	public static class HBaseTabPrimaryKeyProperty {

		// 键值名称
		public String keyName;

		// 键值类型
		public HbaseExportKeyType type;

		// 数据格式
		public String format;

		// 精度
		public String precision;

		// 是否分表主键字段
		public boolean isSplitKeyFlag;

		// 分表Key输出属性, format这货不是线程安全的，所以定义了ThreadLocal对象。
		public ThreadLocal<Format> splitTabKeyFormat;

		// 标准日期输入格式，从record中取出解析
		public ThreadLocal<SimpleDateFormat> dateFormat;

		// private static final ThreadLocal<SimpleDateFormat> dateFormat = new
		// ThreadLocal<SimpleDateFormat>() {
		//
		// protected SimpleDateFormat initialValue() {
		// return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// }
		// };

		// 这个接口，目前写得很简单，主要是为了适应目前C网要求，如要通用，还有很多需要扩展；
		public byte[] transToKeyBytes(String keyValue) {
			if (keyValue == null)
				return null;

			if (type != null) {
				try {
					switch (type) {
						case e_date : {
							if (dateFormat == null) {
								return null;
							}

							Date date = dateFormat.get().parse(keyValue);
							// 精确到秒
							if (precision != null
									&& precision.equalsIgnoreCase("ss")) {
								return Bytes
										.toBytes((int) (date.getTime() / 1000));
							}

							return Bytes.toBytes((long) date.getTime());
						}
						case e_string : {
							if (precision != null && precision.length() > 0) {
								int len = Integer.parseInt(precision);
								byte[] bu = Bytes.toBytes(keyValue);
								byte[] buff = new byte[len];
								System.arraycopy(bu, 0, buff, 0,
										Math.min(bu.length, len));
								return buff;
							}
							return Bytes.toBytes(keyValue);
						}
						case e_byte : {
							Byte n = Byte.parseByte(keyValue);
							return Bytes.toBytes(n);
						}
						case e_short : {
							Short n = Short.parseShort(keyValue);
							return Bytes.toBytes(n);
						}
						case e_int : {
							Integer n = Integer.parseInt(keyValue);
							return Bytes.toBytes(n);
						}
						case e_long : {
							Long n = Long.parseLong(keyValue);
							return Bytes.toBytes(n);
						}
						default :
							break;
					}
				} catch (Exception e) {
					LOGGER.warn("HBaseTabPrimaryKeyProperty::transToKeyBytes() has error occured. keyValue={} cause={}. igp will ignore current record.", new Object[]{keyValue, e.getMessage()});
					return null;
				}
			}

			return Bytes.toBytes(keyValue);
		}

		public String transToSplitTabKeyValue(String keyValue) {
			if (keyValue == null)
				return null;

			if (splitTabKeyFormat == null)
				return keyValue;

			if (type != null) {
				try {
					switch (type) {
						case e_date : {
							Date date = dateFormat.get().parse(keyValue);
							return ((SimpleDateFormat) splitTabKeyFormat.get())
									.format(date);
						}
						case e_string : {
							return keyValue;
						}
						case e_byte : {
							Byte n = Byte.parseByte(keyValue);
							return n.toString();
						}
						case e_short : {
							Short n = Short.parseShort(keyValue);
							return n.toString();
						}
						case e_int : {
							Integer n = Integer.parseInt(keyValue);
							return n.toString();
						}
						case e_long : {
							Long n = Long.parseLong(keyValue);
							return n.toString();
						}
						default :
							break;
					}
				} catch (Exception e) {
					LOGGER.warn("HBaseTabPrimaryKeyProperty::transToSplitTabKeyValue() has error occured. keyValue={} cause={}. igp will ignore current record.", new Object[]{keyValue, e.getMessage()});
					return null;
				}
			}

			return keyValue;
		}
	}
	
	public static class HBaseExportField {
		public String fieldName;
		public String propertyName;
		public byte[] columnFamilyFieldBytes;
		
		public HBaseExportField(String fieldName, String propertyName) {
			this.fieldName = fieldName;
			this.propertyName = propertyName;
			this.columnFamilyFieldBytes = Bytes.toBytes(fieldName);
		}
	}
	
	public static class HBaseSubTablePropery {

		// 输出表名称
		public String exportTableName;

		// 表索引号
		public int tabIndex;

		// 分表字段
		public HBaseTabPrimaryKeyProperty splitTabKeyProp;

		// 主键
		public List<HBaseTabPrimaryKeyProperty> primaryKeys;
		
		// 需要输出的字段
		public List<HBaseExportField> exportFields;
	}

	// 表主名称和Expoter模板对应的表名一致
	private String tableName;

	// 分区数量
	private byte partitionNum;

	// 压缩算法
	private Algorithm compressionAlgorithm;

	// 主键生成器
	private AbsHBaseKeyBuilder keyBuilder;

	// 主表属性
	private HBaseSubTablePropery mainTablePropery;

	// 索引表属性
	private List<HBaseSubTablePropery> indexTableProperty;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public byte getPartitionNum() {
		return partitionNum;
	}

	public void setPartitionNum(byte partitionNum) {
		this.partitionNum = partitionNum;
	}

	public Algorithm getCompressionAlgorithm() {
		return compressionAlgorithm;
	}

	public void setCompressionAlgorithm(Algorithm compressionAlgorithm) {
		this.compressionAlgorithm = compressionAlgorithm;
	}

	public AbsHBaseKeyBuilder getKeyBuilder() {
		return keyBuilder;
	}

	public void setKeyBuilder(AbsHBaseKeyBuilder keyBuilder) {
		this.keyBuilder = keyBuilder;
	}

	public HBaseSubTablePropery getMainTablePropery() {
		return mainTablePropery;
	}

	public void setMainTablePropery(HBaseSubTablePropery mainTablePropery) {
		this.mainTablePropery = mainTablePropery;
	}

	public List<HBaseSubTablePropery> getIndexTableProperty() {
		return indexTableProperty;
	}

	public void setIndexTableProperty(
			List<HBaseSubTablePropery> indexTableProperty) {
		this.indexTableProperty = indexTableProperty;
	}
}
