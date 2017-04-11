package cn.uway.framework.warehouse.exporter.template;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cn.uway.framework.warehouse.repository.StrategyConstant;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.FileUtil;
import cn.uway.util.StringUtil;

/**
 * ExportTemplate 可以在程序初始化阶段完成export_config.xml解析<br>
 * 
 * @author chenrongqiang 2012-11-11
 */
public class ExportTemplate {

	static ILogger log = LoggerManager.getLogger(ExportTemplate.class);

//	public static void main(String[] args) throws Exception {
//		ExportTemplate.parseExportTemplate("E:\\company\\svn\\igp\\trunk\\igp_v3\\app_runner\\template\\export\\lte\\liantong_lte_hw_pm_xml_export_sqlldr.xml");
//	}
	/**
	 * Parse export template
	 * 
	 * @param templateFileName
	 *            The fullname of export template (include path)
	 * @return List<ExportTemplateBean> The list of export templates
	 * @throws Exception
	 */
	public static ExportTemplatePojo parseExportTemplate(String templateFileName) throws Exception {
		if (!FileUtil.exists(templateFileName)) {
			throw new Exception("输出模板：" + templateFileName + "未找到");
		}
		ExportTemplatePojo pojo = new ExportTemplatePojo();
		List<ExportTemplateBean> exportTemplates = new ArrayList<ExportTemplateBean>();
		FileInputStream inputStream = new FileInputStream(templateFileName);
		
		//TODO add by tyler for hadoop begin
		/*if (!HDFSFileHelper.getInstance().existsInHDFS(templateFileName)) {
			throw new Exception("输出模板：" + templateFileName + "未找到");
		}
		ExportTemplatePojo pojo = new ExportTemplatePojo();
		List<ExportTemplateBean> exportTemplates = new ArrayList<ExportTemplateBean>();
		InputStream inputStream = HDFSFileHelper.getInstance().getInputStream(templateFileName);*/
		//add by tyler for hadoop end
		
		Element rootEle = new SAXReader().read(inputStream).getRootElement();

		// 输出策略
		String strategy = rootEle.attributeValue("exportStrategy");
		if (strategy == null || "".equals(strategy.trim())) {
			// 默认策略
			pojo.setExportStrategy(StrategyConstant.STRATEGY_ONETOMULTI);
		} else {
			pojo.setExportStrategy(strategy.trim());
		}
		// 是否包含汇总
		String isSummary = rootEle.attributeValue("isSummary");
		if (isSummary != null && "true".equalsIgnoreCase(isSummary.trim())) {
			pojo.setSummary(true);
		} else {
			pojo.setSummary(false);
		}
		// 数据汇总类型
		String summaryDataType = rootEle.attributeValue("summaryDataType");

		// 是否入到主库（config.ini中配置的log_clt_insert所在库）
		String isToMainDB_P = rootEle.attributeValue("isToMainDB");
		
		String split_P = rootEle.attributeValue("split");
		
		String encode_P = rootEle.attributeValue("encode");

		// 加载 export 节点,构建出 ExportTemplate 对象列表
		@SuppressWarnings("unchecked")
		List<Element> exports = rootEle.elements("export");
		for (Element exportEle : exports) {
			// 输出模版ID
			if (exportEle.attributeValue("id") == null)
				throw new Exception("输出模板<export>节点无id属性，模板：" + templateFileName);
			int exportId = Integer.parseInt(exportEle.attributeValue("id"));
			String type = exportEle.attributeValue("type");
			int dataType = -100;
			int intType = Integer.parseInt(type.trim());
			String data_Type = exportEle.attributeValue("dataType");
			// 数据类型 区分一个数据采集中可能存在多种数据问题
			if (data_Type != null)
				dataType = Integer.parseInt(data_Type);
			String encode = exportEle.attributeValue("encode");
			String exportTarget = exportEle.attributeValue("exportTarget");
			Integer exportTargetId = 0;
			if (!isDBCfgExport(intType)) {
				if (exportTarget == null) {
					if (intType != ExportTemplateBean.EXPORT_EMPTY) {
						log.warn("文件输出模版定义targetId为空!将忽略此输出");
						continue;
					}
				} else if (exportTarget != null) {
					exportTargetId = Integer.parseInt(exportTarget.trim());
				}
			}
			// 是否入到主库（config.ini中配置的log_clt_insert所在库），目前只针对数据库
			String isToMainDB = exportEle.attributeValue("isToMainDB");
			// 数据库输出和文件输出的配置加载分别来源于数据库和export_config.xml需要分别加载
			// 并且数据库输出的开关通过IGP_CFG_DB_EXPORT表is_used来控制
			if (isDBExoprt(intType)) {
				//sqlldr方式入库
				if (Integer.toString(ExportTemplateBean.EXPORT_DB_SQLLDR).equals(type)) {
					// 数据库输出的目的地配置在Job线程实际启动时加载 需要根据任务配置城市和解决方案ID来决定
					SqlldrExportTemplateBean exportTemplate = new SqlldrExportTemplateBean();
					exportTemplate.setId(exportId);
					exportTemplate.setType(intType);
					exportTemplate.setDataType(dataType);
					exportTemplate.setEncode(StringUtil.isEmpty(encode) ? encode_P : encode);
					exportTemplate.setSplit(StringUtil.isEmpty(exportEle.attributeValue("split")) ? split_P : exportEle.attributeValue("split"));
					exportTemplate.setIsToMainDB(StringUtil.isEmpty(isToMainDB) ? isToMainDB_P : isToMainDB);
					// Parse table from export template
					Element tableElement = exportEle.element("table");
					SqlldrTableTemplateBean sqlldrTableTemplate = new SqlldrTableTemplateBean();
					sqlldrTableTemplate.setTableName(tableElement.attributeValue("value"));
					Element columnss = exportEle.element("columns");
					List<?> columns = columnss.elements("column");
					if (columns == null || columns.size() == 0)
						continue;
					for (Object cObj : columns) {
						if (cObj == null || !(cObj instanceof Element))
							continue;
						Element columnEle = (Element) cObj;
						String colName = columnEle.attributeValue("name");
						String property = columnEle.attributeValue("property");
						SqlldrColumnTemplateBean colTemplate = new SqlldrColumnTemplateBean(colName, property);
						String ctype = columnEle.attributeValue("type");
						String defautValue = columnEle.attributeValue("defaultValue");
						String isSpan = columnEle.attributeValue("isSpan");
						String format = columnEle.attributeValue("format");
						if(ctype != null){ 
							colTemplate.setType(ctype);
						}
						if(format != null){
							colTemplate.setFormat(format);
						}
						if (isSpan != null) {
							colTemplate.setIsSpan(isSpan);
						}
						if (defautValue != null)
							colTemplate.setDefaultValue(defautValue);
						
						if (sqlldrTableTemplate.getColumns().get(colName) == null) {
							sqlldrTableTemplate.getColumns().put(colName, colTemplate);
							sqlldrTableTemplate.getColumnsList().add(colTemplate);
						} else {
							// log.warn("输出模板<column name='" + colName + "'重复，模板：" + templateFileName + "，type = " + type);
						}
					}
					exportTemplate.setTable(sqlldrTableTemplate);
					exportTemplates.add(exportTemplate);
					continue;
				}
				
				// parq方式入库
				// FIXME需要重构，sqlldr、db，太乱了 
				if (Integer.toString(ExportTemplateBean.PARQUET_EXPORTER).equals(type) 
						|| Integer.toString(ExportTemplateBean.CONFIGURE_PARQUET_EXPORTER).equals(type)) {
					String partitionType = exportEle.attributeValue("partitionType");
					int ptint = (null != partitionType ? Integer.valueOf(partitionType) : 4);
					
					String ctType = exportEle.attributeValue("ctType");
					
					// 数据库输出的目的地配置在Job线程实际启动时加载 需要根据任务配置城市和解决方案ID来决定
					ParqExportTemplateBean exportTemplate = new ParqExportTemplateBean();
					exportTemplate.setId(exportId);
					exportTemplate.setType(intType);
					exportTemplate.setDataType(dataType);
					exportTemplate.setPartitionType(ptint);
					exportTemplate.setCtType(ctType==null?0:Integer.parseInt(ctType));
					exportTemplate.setEncode(StringUtil.isEmpty(encode) ? encode_P : encode);
					exportTemplate.setSplit(StringUtil.isEmpty(exportEle.attributeValue("split")) ? split_P : exportEle.attributeValue("split"));
					exportTemplate.setIsToMainDB(StringUtil.isEmpty(isToMainDB) ? isToMainDB_P : isToMainDB);
					// Parse table from export template
					Element tableElement = exportEle.element("table");
					ParqTableTemplateBean tableTemplate = new ParqTableTemplateBean();
					tableTemplate.setTableName(tableElement.attributeValue("value"));
					Element columnss = exportEle.element("columns");
					List<?> columns = columnss.elements("column");
					if (columns == null || columns.size() == 0)
						continue;
					for (Object cObj : columns) {
						if (cObj == null || !(cObj instanceof Element))
							continue;
						Element columnEle = (Element) cObj;
						String colName = columnEle.attributeValue("name");
						String property = columnEle.attributeValue("property");
						ColumnTemplateBean colTemplate = new ColumnTemplateBean(colName, property);
						String defautValue = columnEle.attributeValue("defaultValue");
						String isSpan = columnEle.attributeValue("isSpan");
						String format = columnEle.attributeValue("format");
						if(format != null){
							colTemplate.setFormat(format);
						}
						if (isSpan != null) {
							colTemplate.setIsSpan(isSpan);
						}
						if (defautValue != null)
							colTemplate.setDefaultValue(defautValue);
						
						if (tableTemplate.getColumns().get(colName) == null) {
							tableTemplate.getColumns().put(colName, colTemplate);
							tableTemplate.getColumnsList().add(colTemplate);
						} else {
							// log.warn("输出模板<column name='" + colName + "'重复，模板：" + templateFileName + "，type = " + type);
						}
					}
					exportTemplate.setTable(tableTemplate);
					exportTemplates.add(exportTemplate);
					continue;
				}
				
				// 数据库输出的目的地配置在Job线程实际启动时加载 需要根据任务配置城市和解决方案ID来决定
				DbExportTemplateBean exportTemplate = new DbExportTemplateBean();
				exportTemplate.setSummaryDataType(summaryDataType);
				exportTemplate.setId(exportId);
				exportTemplate.setType(intType);
				exportTemplate.setDataType(dataType);
				exportTemplate.setTargetId(exportTargetId);
				exportTemplate.setEncode(encode);
				exportTemplate.setIsToMainDB(StringUtil.isEmpty(isToMainDB) ? isToMainDB_P : isToMainDB);
				// Parse table from export template
				Element tableElement = exportEle.element("table");
				DbTableTemplateBean dbTableTemplate = new DbTableTemplateBean();
				dbTableTemplate.setTableName(tableElement.attributeValue("value"));
				dbTableTemplate.setStorageType(tableElement.attributeValue("storageType"));
				Element columnss = exportEle.element("columns");
				List<?> columns = columnss.elements("column");
				if (columns == null || columns.size() == 0)
					continue;
				for (Object cObj : columns) {
					if (cObj == null || !(cObj instanceof Element))
						continue;
					Element columnEle = (Element) cObj;
					String colName = columnEle.attributeValue("name");
					String property = columnEle.attributeValue("property");
					String defautValue = columnEle.attributeValue("defaultValue");
					String isSpan = columnEle.attributeValue("isSpan");
					ColumnTemplateBean colTemplate = new ColumnTemplateBean(colName, property);
					Attribute attribute = columnEle.attribute("format");
					String sequence = columnEle.attributeValue("sequence");
					if (attribute != null)
						colTemplate.setFormat(attribute.getValue());
					if (isSpan != null) {
						colTemplate.setIsSpan(isSpan);
					}
					if (null != sequence){
						colTemplate.setSequence(sequence);
					}
					if (defautValue != null)
						colTemplate.setDefaultValue(defautValue);
					if (dbTableTemplate.getColumns().get(colName) == null) {
						dbTableTemplate.getColumns().put(colName, colTemplate);
						dbTableTemplate.getColumnsList().add(colTemplate);
					} else {
						// log.warn("输出模板<column name='" + colName + "'重复，模板：" + templateFileName + "，type = " + type);
					}
				}
				Element sqlElement = exportEle.element("sql");
				if (sqlElement != null) {
					dbTableTemplate.sql = sqlElement.getTextTrim();
				}
				exportTemplate.setTable(dbTableTemplate);
				exportTemplates.add(exportTemplate);
				continue;
			}
			
			// 远程文件模板解析
			if (type.equals(Integer.toString(ExportTemplateBean.REMOTE_FILE_EXPORTER))
					|| type.equals(Integer.toString(ExportTemplateBean.EXTEND_REMOTE_FILE_EXPORTER))
					|| type.equals(Integer.toString(ExportTemplateBean.EXPORT_EMPTY))) {
				RemoteFileExportTemplateBean exportTemplate = new RemoteFileExportTemplateBean();
				exportTemplate.setId(exportId);
				exportTemplate.setType(Integer.valueOf(type));
				exportTemplate.setDataType(dataType);
				exportTemplate.setTargetId(exportTargetId);
				exportTemplate.setEncode(encode);
				exportTemplate.setIsToMainRemote(StringUtil.isEmpty(isToMainDB) ? isToMainDB_P : isToMainDB);
				exportTemplate.setOn(true);
				
				// 同时兼容输入数据库的模板和输出到本地的模板
				Element elementFields = exportEle.element("fields");
				if (elementFields == null) {
					elementFields = exportEle.element("columns");
				}
				if (elementFields == null)
					continue;
				
				List<?> fields = elementFields.elements("field");
				if (fields == null || fields.size() < 1) {
					fields = elementFields.elements("column");
				}
				if (fields == null || fields.size() == 0)
					continue;

				for (Object fObj : fields) {
					if (fObj == null || !(fObj instanceof Element))
						continue;
					Element fieldEle = (Element) fObj;
					FieldTemplateBean fieldTemplate = new FieldTemplateBean();
					
					String propertyName = fieldEle.attributeValue("property");
					String columnName = fieldEle.attributeValue("name");
					if (columnName == null) {
						columnName = propertyName;
					}
					
					fieldTemplate.setColumnName(columnName);
					fieldTemplate.setPropertyName(propertyName);

					Attribute attribute = fieldEle.attribute("format");
					if (attribute != null)
						fieldTemplate.setFormat(attribute.getValue());
					exportTemplate.addFieldTemplate(fieldTemplate);
				}
				exportTemplates.add(exportTemplate);
				continue;
			}
			
			// kafka 输出
			if (type.equals(Integer.toString(ExportTemplateBean.KAFKA_EXPORTER))) {
				/**
				 * 文件方式是否打开输出开关通过on来配置
				 */
				String openFlag = exportEle.attributeValue("on");
				if ("false".equalsIgnoreCase(openFlag))
					continue;
				
				ExporterBean bean = ExportTargetTempletContext.getInstance().getFileExportTargetBean(exportTargetId, true);
				if (bean == null) {
					log.info("export target id:{} 对应的输出配置在export_config.xml中为空或已关闭.", exportTargetId);
					continue;
				}
				
				FileExportTemplateBean exportTemplate = new FileExportTemplateBean();
				exportTemplate.setId(exportId);
				exportTemplate.setType(ExportTemplateBean.KAFKA_EXPORTER);
				exportTemplate.setDataType(dataType);
				exportTemplate.setTargetId(exportTargetId);
				exportTemplate.setEncode(encode);
				exportTemplate.setExportTargetBean(bean);
				Element elementFields = exportEle.element("fields");
				List<?> fields = elementFields.elements("field");
				if (fields == null || fields.size() == 0)
					continue;

				for (Object fObj : fields) {
					if (fObj == null || !(fObj instanceof Element))
						continue;
					Element fieldEle = (Element) fObj;
					FieldTemplateBean fieldTemplate = new FieldTemplateBean();
					
					String propertyName = fieldEle.attributeValue("property");
					String columnName = fieldEle.attributeValue("name");
					if (columnName == null) {
						columnName = propertyName;
					}
					
					fieldTemplate.setColumnName(columnName);
					fieldTemplate.setPropertyName(propertyName);

					Attribute attribute = fieldEle.attribute("format");
					if (attribute != null)
						fieldTemplate.setFormat(attribute.getValue());
					exportTemplate.addFieldTemplate(fieldTemplate);
				}
				exportTemplates.add(exportTemplate);
				continue;
			}
			
			if (type.equals(Integer.toString(ExportTemplateBean.LOACL_FILE_EXPORTER))) {
				/**
				 * 文件方式是否打开输出开关通过on来配置
				 */
				String openFlag = exportEle.attributeValue("on");
				if ("false".equalsIgnoreCase(openFlag))
					continue;
				
				ExporterBean bean = ExportTargetTempletContext.getInstance().getFileExportTargetBean(exportTargetId, true);
				if (bean == null) {
					log.info("export target id:{} 对应的输出配置在export_config.xml中为空或已关闭.", exportTargetId);
					continue;
				}
				
				FileExportTemplateBean exportTemplate = new FileExportTemplateBean();
				exportTemplate.setId(exportId);
				exportTemplate.setType(ExportTemplateBean.LOACL_FILE_EXPORTER);
				exportTemplate.setDataType(dataType);
				exportTemplate.setTargetId(exportTargetId);
				exportTemplate.setEncode(encode);
				exportTemplate.setExportTargetBean(bean);
				Element elementFields = exportEle.element("fields");
				List<?> fields = elementFields.elements("field");
				if (fields == null || fields.size() == 0)
					continue;

				for (Object fObj : fields) {
					if (fObj == null || !(fObj instanceof Element))
						continue;
					Element fieldEle = (Element) fObj;
					FieldTemplateBean fieldTemplate = new FieldTemplateBean();
					
					String propertyName = fieldEle.attributeValue("property");
					String columnName = fieldEle.attributeValue("name");
					if (columnName == null) {
						columnName = propertyName;
					}
					
					fieldTemplate.setColumnName(columnName);
					fieldTemplate.setPropertyName(propertyName);

					Attribute attribute = fieldEle.attribute("format");
					if (attribute != null)
						fieldTemplate.setFormat(attribute.getValue());
					exportTemplate.addFieldTemplate(fieldTemplate);
				}
				exportTemplates.add(exportTemplate);
				continue;
			}
			
			// 输出方式：infobright文件，且归类数据
			if (type.equals(Integer.toString(ExportTemplateBean.EXPORT_INFOBRIGHT))) {
				/**
				 * 文件方式是否打开输出开关通过on来配置
				 */
				String openFlag = exportEle.attributeValue("on");
				if ("false".equalsIgnoreCase(openFlag))
					continue;

				// 数据分类规则配置解析
				Element ruleElement = exportEle.element("sortedDataRule");
				if (ruleElement == null) {
					throw new Exception("输出模板无<splitData>节点，模板：" + templateFileName + "，type = " + type);
				}
				String timeKey = ruleElement.attributeValue("timeKey");
				if (timeKey == null || "".equals(timeKey.trim())) {
					throw new Exception("输出模板<splitData>节点timeKye属性为空，模板：" + templateFileName + "，type = " + type);
				}
				String dimension = ruleElement.attributeValue("dimension");
				if (dimension == null || "".equals(dimension.trim())) {
					throw new Exception("输出模板<splitData>节点dimension属性为空，模板：" + templateFileName + "，type = " + type);
				}
				String beginTime = ruleElement.attributeValue("beginTime");
				if (beginTime == null || "".equals(beginTime.trim())) {
					throw new Exception("输出模板<splitData>节点beginTime属性为空，模板：" + templateFileName + "，type = " + type);
				}
				String endTime = ruleElement.attributeValue("endTime");
				if (endTime == null || "".equals(endTime.trim())) {
					throw new Exception("输出模板<splitData>节点endTime属性为空，模板：" + templateFileName + "，type = " + type);
				}
				String middleTime = ruleElement.attributeValue("middleTime");
				if (middleTime == null || "".equals(middleTime.trim())) {
					throw new Exception("输出模板<splitData>节点middleTime属性为空，模板：" + templateFileName + "，type = " + type);
				}
				SortedDataRule sortedDataRule = new SortedDataRule();
				sortedDataRule.setTimeKey(timeKey);
				sortedDataRule.setDimension(dimension.toUpperCase());
				sortedDataRule.setBeginTime(beginTime);
				sortedDataRule.setEndTime(endTime);
				sortedDataRule.setMiddleTime(middleTime);

				InfoBrightFileExportTemplateBean exportTemplate = new InfoBrightFileExportTemplateBean();
				exportTemplate.setId(exportId);
				exportTemplate.setType(ExportTemplateBean.EXPORT_INFOBRIGHT);
				exportTemplate.setDataType(dataType);
				exportTemplate.setTargetId(exportTargetId);
				exportTemplate.setEncode(encode);
				exportTemplate.setExportTargetBean(ExportTargetTempletContext.getInstance().getFileExportTargetBean(exportTargetId));
				exportTemplate.setSplitDataFormatBean(sortedDataRule);
				Element elementFields = exportEle.element("fields");
				List<?> fields = elementFields.elements("field");
				if (fields == null || fields.size() == 0)
					continue;

				for (Object fObj : fields) {
					if (fObj == null || !(fObj instanceof Element))
						continue;
					Element fieldEle = (Element) fObj;
					FieldTemplateBean fieldTemplate = new FieldTemplateBean();
					fieldTemplate.setPropertyName(fieldEle.attributeValue("property"));

					Attribute attribute = fieldEle.attribute("format");
					if (attribute != null)
						fieldTemplate.setPropertyName(attribute.getValue());
					exportTemplate.addFieldTemplate(fieldTemplate);
				}
				exportTemplates.add(exportTemplate);
				continue;
			}
		}
		pojo.setExportTemplates(exportTemplates);
		log.debug("输出模版解析成功:模版路径=" + templateFileName);
		return pojo;
	}

	public static boolean isDBExoprt(int type) {
		return type == ExportTemplateBean.JDBC_EXPORTER || type == ExportTemplateBean.EXPORT_DB_MYSQL
				|| type == ExportTemplateBean.EXPORT_DB_GREENPLUM || type == ExportTemplateBean.EXPORT_DB_SUMMARY
				|| type == ExportTemplateBean.FAULT_TOLERANT_POOLED_JDBC_EXPORTER
				|| type == ExportTemplateBean.EXPORT_DB_SQLLDR
				|| type == ExportTemplateBean.PARQUET_EXPORTER
				|| type == ExportTemplateBean.JDBC_TRUNCATE_AND_EXPORTER
				|| type == ExportTemplateBean.JDBC_BAKUP_AND_EXPORTER 
				|| type == ExportTemplateBean.CONFIGURE_PARQUET_EXPORTER;
	}
	
	public static boolean isDBCfgExport(int type) {
		return type == ExportTemplateBean.REMOTE_FILE_EXPORTER 
				|| type == ExportTemplateBean.EXTEND_REMOTE_FILE_EXPORTER
				|| isDBExoprt(type);
	}
}
