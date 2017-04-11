package cn.uway.framework.parser.database;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class DatabaseParseTempletParser {

	private static final ILogger logger = LoggerManager.getLogger(DatabaseParseTempletParser.class); // 日志

	/**
	 * 解析数据库解析模版 目前一个模版只支持一个数据库对象采集
	 * 
	 * @param templetFileName
	 * @return DbParserTemplet
	 * @throws Exception
	 */
	public static DatabaseParserTemplate parse(String templetFileName) throws Exception {
		File templetFile = new File(templetFileName);
		if (!templetFile.exists()) {
			throw new IllegalArgumentException("模版路径配置错误。配置文件" + templetFileName + "未找到");
		}
		logger.debug("开始解析数据库解析模板:" + templetFileName);
		FileInputStream fIns = new FileInputStream(templetFileName);
		Element rootEle = new SAXReader().read(fIns).getRootElement();
		Element templet = rootEle.element("templet");
		DatabaseParserTemplate dbParserTemplet = new DatabaseParserTemplate();
		dbParserTemplet.setId(Long.parseLong(templet.attributeValue("id")));
		String dataTypeStr = templet.attributeValue("dataType");
		if (dataTypeStr != null) {
			dbParserTemplet.setDataType(Integer.parseInt(dataTypeStr));
		}
		String busTypeStr = templet.attributeValue("busType");
		if (busTypeStr != null) {
			dbParserTemplet.setBusType(busTypeStr);
		}
		Element sqlElement = templet.element("sql");
		String sql = sqlElement.getText();
		dbParserTemplet.setSql(sql);
		logger.debug("解析数据库解析模板结束:" + templetFileName);
		return dbParserTemplet;
	}

	public static List<DatabaseParserTemplate> parseTemplates(String templetFileName) throws Exception {
		List<DatabaseParserTemplate> templates = new LinkedList<DatabaseParserTemplate>();
		
		File templetFile = new File(templetFileName);
		if (!templetFile.exists()) {
			throw new IllegalArgumentException("模版路径配置错误。配置文件" + templetFileName + "未找到");
		}
		logger.debug("开始解析数据库解析模板:" + templetFileName);
		FileInputStream fIns = new FileInputStream(templetFileName);
		Element rootEle = new SAXReader().read(fIns).getRootElement();
		
		List<Node> elTemplates = rootEle.selectNodes("templet");
		if (elTemplates == null) {
			throw new IllegalArgumentException("模版中找不到任何template结点。配置文件" + templetFileName + "未找到");
		}
		
		for (Node tmpl : elTemplates) {
			Element templet = (Element)tmpl;
			//Element templet = rootEle.element("templet");
			DatabaseParserTemplate dbParserTemplet = new DatabaseParserTemplate();
			dbParserTemplet.setId(Long.parseLong(templet.attributeValue("id")));
			String dataTypeStr = templet.attributeValue("dataType");
			if (dataTypeStr != null) {
				dbParserTemplet.setDataType(Integer.parseInt(dataTypeStr));
			}
			String busTypeStr = templet.attributeValue("busType");
			if (busTypeStr != null) {
				dbParserTemplet.setBusType(busTypeStr);
			}
			Element sqlElement = templet.element("sql");
			String sql = sqlElement.getText();
			dbParserTemplet.setSql(sql);
			templates.add(dbParserTemplet);
		}
		
		logger.debug("解析数据库解析模板结束:" + templetFileName);
		
		return templates;
	}
}
