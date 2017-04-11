package cn.uway.framework.parser.database;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 数据库接口的解析模板的解码器
 * 
 * @author yuy @ 6 May, 2014
 */
public class DBParseTempletParser {

	private static final ILogger logger = LoggerManager.getLogger(DBParseTempletParser.class); // 日志

	/**
	 * 解析数据库解析模版 目前一个模版只支持一个数据库对象采集
	 * 
	 * @param templetFileName
	 * @return DbParserTemplet
	 * @throws Exception
	 */
	public static List<DatabaseParserTemplate> parse(String templetFileName) throws Exception {
		File templetFile = new File(templetFileName);
		if (!templetFile.exists()) {
			throw new IllegalArgumentException("模版路径配置错误。配置文件" + templetFileName + "未找到");
		}
//		logger.debug("开始解析数据库解析模板:" + templetFileName);
		FileInputStream fIns = new FileInputStream(templetFileName);
		Element rootEle = new SAXReader().read(fIns).getRootElement();
		List<DatabaseParserTemplate> templatesList = new ArrayList<DatabaseParserTemplate>();
		DatabaseParserTemplate dbParserTemplet = null;
		List<?> elements = rootEle.elements("templet");
		for (Object element : elements) {
			Element ele = (Element) element;
			dbParserTemplet = new DatabaseParserTemplate();
			dbParserTemplet.setId(Long.parseLong(ele.attributeValue("id")));
			String dataTypeStr = ele.attributeValue("dataType");
			if (dataTypeStr != null) {
				dbParserTemplet.setDataType(Integer.parseInt(dataTypeStr));
			}
			String typeId = ele.attributeValue("label_type_id");
			if (typeId != null) {
				dbParserTemplet.setTypeId(Integer.parseInt(typeId));
			}
			
			String prefixSql = ele.attributeValue("prefixSql");
			if (prefixSql != null) {
				dbParserTemplet.setPrefixSql(prefixSql);
			}
			
			Element sqlElement = ele.element("sql");
			String sql = sqlElement.getText();
			dbParserTemplet.setSql(sql.trim());
			templatesList.add(dbParserTemplet);
		}
		logger.debug("解析数据库解析模板成功:" + templetFileName);
		if (templatesList.size() == 0) {
			throw new IllegalArgumentException("模版有误。配置文件" + templetFileName + "为空");
		}
		return templatesList;
	}

}
