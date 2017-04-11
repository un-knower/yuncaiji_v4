package cn.uway.framework.warehouse.exporter.hbaseExporterConf;

import java.io.FileInputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cn.uway.framework.warehouse.exporter.AbsHBaseKeyBuilder;
import cn.uway.framework.warehouse.exporter.hbaseExporterConf.HBaseExportTableProperty.HBaseExportField;
import cn.uway.util.FileUtil;
/**
 * 这个是HBase 0.94的支持包位置
 * //import org.apache.hadoop.hbase.io.hfile.Compression.Algorithm; 
 */

public class HBaseExportDBConf {
	public static class AttachParamThreadLocal<T> extends ThreadLocal<T> {
		private Object param;
		public AttachParamThreadLocal(Object param) {
			this.param = param;
		}
		
		public Object getParam() {
			return param;
		}
		
		public void setParam(Object param) {
			this.param = param;
		}
	}
	
	
	/**
	 * Map<表名, 输出表信息>
	 */
	private Map<String, HBaseExportTableProperty> mapTableConfigs = new HashMap<String, HBaseExportTableProperty>();
	/**
	 * Map<表名-id, 输出表信息>
	 */
	private Map<String, HBaseExportTableProperty> mapTableConfigs2 = new HashMap<String, HBaseExportTableProperty>();

	/**
	 * 配置模板文件名
	 */
	private String confFileName;
	
	public HBaseExportDBConf(String confFileName) throws Exception {
		this.confFileName = confFileName;
		parseExportTemplate(confFileName);
	}
	
	/**
	 * 解析HBASE输出配置模板
	 * @param templateFileName 模板文件名
	 * @throws Exception
	 */
	public void parseExportTemplate(String templateFileName) throws Exception {
		if (!FileUtil.exists(templateFileName)) {
			throw new Exception("HBASE入库配置模板：" + templateFileName + "未找到.");
		}
		FileInputStream inputStream = new FileInputStream(templateFileName);
		
		//TODO add by tyler for hadoop begin
		/*if (!HDFSFileHelper.getInstance().existsInHDFS(templateFileName)) {
			throw new Exception("HBASE入库配置模板：" + templateFileName + "未找到.");
		}
		InputStream inputStream = HDFSFileHelper.getInstance().getInputStream(templateFileName);*/
		//add by tyler for hadoop end
		
		Element rootEle = new SAXReader().read(inputStream).getRootElement();
		
		List<Element> exports = rootEle.elements("table");
		for (Element exportTable : exports) {
			HBaseExportTableProperty hbaseExportTable = new HBaseExportTableProperty();
			String id = exportTable.attributeValue("id");
			String tableName = exportTable.attributeValue("name");
			hbaseExportTable.setTableName(tableName);
			byte partitionNum = Byte.parseByte(exportTable.attributeValue("partitionNum"));
			hbaseExportTable.setPartitionNum(partitionNum);
			Algorithm compressionAlgorithm = null;
			if ("SNAPPY".equalsIgnoreCase(exportTable.attributeValue("compressionAlgorithm"))) {
				compressionAlgorithm = Algorithm.SNAPPY;
				hbaseExportTable.setCompressionAlgorithm(compressionAlgorithm);
			}
								
			AbsHBaseKeyBuilder builder = null;
			String builderName = exportTable.attributeValue("keyBuilder");
			if (builderName != null) {
				builder = (AbsHBaseKeyBuilder)Class.forName(builderName).newInstance();
				hbaseExportTable.setKeyBuilder(builder);
			}
			
			Element elMainTable = exportTable.element("mainTable");
			HBaseExportTableProperty.HBaseSubTablePropery mainTablePropery = new HBaseExportTableProperty.HBaseSubTablePropery();
			parseSubTableConfig(elMainTable, tableName, mainTablePropery);
			hbaseExportTable.setMainTablePropery(mainTablePropery);
			
			List<Element> elIndexTables = exportTable.elements("indexTable");
			List<HBaseExportTableProperty.HBaseSubTablePropery> IndexTablePropertys = new LinkedList<HBaseExportTableProperty.HBaseSubTablePropery>();
			for (Element elIndexTable : elIndexTables) {
				HBaseExportTableProperty.HBaseSubTablePropery indexTableProperty = new HBaseExportTableProperty.HBaseSubTablePropery();
				parseSubTableConfig(elIndexTable, tableName, indexTableProperty);
				IndexTablePropertys.add(indexTableProperty);
			}
			hbaseExportTable.setIndexTableProperty(IndexTablePropertys);
			
			mapTableConfigs.put(tableName, hbaseExportTable);
			String  tableTmp=new String(tableName);
			if(StringUtils.isEmpty(id))
				tableTmp=tableTmp+"-"+id;
			else 
				id="";
			mapTableConfigs2.put(tableTmp, hbaseExportTable);
			
		}
	}
	
	/**
	 * 解析输出表主键定义
	 * @param el 解析节点
	 * @param mainTableName 主表名称 
	 * @param subTabConf 子表配置项(OUT)
	 * @throws Exception
	 */
	private void parseSubTableConfig(Element el, String mainTableName, HBaseExportTableProperty.HBaseSubTablePropery subTabConf) throws Exception{
		subTabConf.tabIndex = Integer.parseInt(el.attributeValue("index"));
		subTabConf.exportTableName = el.attributeValue("name");
		if (subTabConf.exportTableName == null || subTabConf.exportTableName.trim().length()<1) {
			subTabConf.exportTableName = mainTableName;
		} else {
			subTabConf.exportTableName = mainTableName + "_" + subTabConf.exportTableName;
		}
		
		subTabConf.primaryKeys = new LinkedList<HBaseExportTableProperty.HBaseTabPrimaryKeyProperty>();
		List<Element> elPrimaryKeys = el.elements("primaryKey");
		List<Element> elFields = el.elements("field");
		
		for (Element elPrimaryKey : elPrimaryKeys) {
			HBaseExportTableProperty.HBaseTabPrimaryKeyProperty  primaryKeyProp = new HBaseExportTableProperty.HBaseTabPrimaryKeyProperty();
			
			primaryKeyProp.keyName = elPrimaryKey.attributeValue("name");
			String type = elPrimaryKey.attributeValue("type");
			if (type != null) {
				if (type.equalsIgnoreCase("DATE")) 
					primaryKeyProp.type = HBaseExportTableProperty.HbaseExportKeyType.e_date;
				else if (type.equalsIgnoreCase("INT"))
					primaryKeyProp.type = HBaseExportTableProperty.HbaseExportKeyType.e_int;
				else if (type.equalsIgnoreCase("INTEGER"))
					primaryKeyProp.type = HBaseExportTableProperty.HbaseExportKeyType.e_int;
				else if (type.equalsIgnoreCase("SHORT"))
					primaryKeyProp.type = HBaseExportTableProperty.HbaseExportKeyType.e_short;
				else if (type.equalsIgnoreCase("LONG"))
					primaryKeyProp.type = HBaseExportTableProperty.HbaseExportKeyType.e_long;
				else if (type.equalsIgnoreCase("BYTE"))
					primaryKeyProp.type = HBaseExportTableProperty.HbaseExportKeyType.e_byte;
				else if (type.equalsIgnoreCase("STRING")) 
					primaryKeyProp.type = HBaseExportTableProperty.HbaseExportKeyType.e_string;			
			}
			String dataFormat = elPrimaryKey.attributeValue("format");
			if (dataFormat != null) {
				primaryKeyProp.format = dataFormat;
				if (primaryKeyProp.type != null && primaryKeyProp.type.equals(HBaseExportTableProperty.HbaseExportKeyType.e_date)) {
					primaryKeyProp.dateFormat = new AttachParamThreadLocal<SimpleDateFormat>(dataFormat) {
						protected SimpleDateFormat initialValue() {
							return new SimpleDateFormat((String)this.getParam());
						}
					};
				}
			}
			
			// 设置默认时间解析格式
			if (primaryKeyProp.dateFormat == null) {
				primaryKeyProp.dateFormat = new ThreadLocal<SimpleDateFormat>() {
					protected SimpleDateFormat initialValue() {
						return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					}
				};
			}
			
			primaryKeyProp.precision = elPrimaryKey.attributeValue("precision");
			
			if ("true".equalsIgnoreCase(elPrimaryKey.attributeValue("splitTabKeyFlag"))) {
				primaryKeyProp.isSplitKeyFlag = true;
				if (primaryKeyProp.type != null ) {
					if (primaryKeyProp.type == HBaseExportTableProperty.HbaseExportKeyType.e_date) { 
						String format = elPrimaryKey.attributeValue("splitTabKeyFormat");
						if (format != null && format.length()>0) {
							primaryKeyProp.splitTabKeyFormat = new AttachParamThreadLocal<Format>(format) {
								protected Format initialValue() {
									return new SimpleDateFormat((String)this.getParam());
								}
							};
						}
					} else {
						primaryKeyProp.splitTabKeyFormat = null; 
					}
				}
				
				subTabConf.splitTabKeyProp = primaryKeyProp;
			}
			
			subTabConf.primaryKeys.add(primaryKeyProp);
		}
		
		// 解析输出附加字段
		if (elFields != null) {
			subTabConf.exportFields = new LinkedList<HBaseExportField>();
			for (Element elField : elFields) {
				String fieldName = elField.attributeValue("name");
				String propertyName = elField.attributeValue("property");
				
				subTabConf.exportFields.add(new HBaseExportField(fieldName, propertyName));
			}
		}
	}
	
	public HBaseExportTableProperty getExportTableConf(String tableName) {
		return mapTableConfigs.get(tableName);
	}
	public HBaseExportTableProperty getExportTableForIdConf(String tableNameAndId) {
		return mapTableConfigs2.get(tableNameAndId);
	}
	public String getConfFileName() {
		return confFileName;
	}

	public void setConfFileName(String confFileName) {
		this.confFileName = confFileName;
	}

//	public static void main(String[] args) throws Exception {
//		HBaseExportDBConf hConf = new HBaseExportDBConf("/home/shig/project/Java/igp_v3/app_runner/template/export/cdma/hbase/cdma_export_Table_hbase_conf.xml");
//		
//		System.out.println("ok");
//	}
}
