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
 * 多语句模板解析类
 * 
 * @author tylerlee @ 2016年3月18日
 */
public class DatabaseParseMultiTempletParser {

	private static final ILogger logger = LoggerManager.getLogger(DatabaseParseMultiTempletParser.class); // 日志

	/**
	 * 解析数据库解析模版 目前一个模版只支持一个数据库对象采集
	 * 
	 * @param templetFileName
	 * @return DbParserTemplet
	 * @throws Exception
	 */
	public static List<DatabaseParserMultiTemplate> parse(String templetFileName) throws Exception {
		File templetFile = new File(templetFileName);
		if (!templetFile.exists()) {
			throw new IllegalArgumentException("模版路径配置错误。配置文件" + templetFileName + "未找到");
		}
		// logger.debug("开始解析数据库解析模板:" + templetFileName);
		FileInputStream fIns = new FileInputStream(templetFileName);
		Element rootEle = new SAXReader().read(fIns).getRootElement();
		List<DatabaseParserMultiTemplate> templatesList = new ArrayList<DatabaseParserMultiTemplate>();
		DatabaseParserMultiTemplate dbParserTemplet = null;
		List<?> elements = rootEle.elements("templet");
		for (Object element : elements) {
			Element ele = (Element) element;
			if (ele.attributeValue("isuse", "true").equals("false")) {
				continue;
			}
			dbParserTemplet = new DatabaseParserMultiTemplate();
			dbParserTemplet.setId(Long.parseLong(ele.attributeValue("id")));
			String dataTypeStr = ele.attributeValue("dataType");
			if (dataTypeStr != null) {
				dbParserTemplet.setDataType(Integer.parseInt(dataTypeStr));
			}
			String typeId = ele.attributeValue("label_type_id");
			if (typeId != null) {
				dbParserTemplet.setTypeId(Integer.parseInt(typeId));
			}
			String others = ele.attributeValue("others");
			if (others != null) {
				String lo = others.toLowerCase();
				dbParserTemplet.setOthers("on".equals(lo) || "true".equals(lo));
			}
			String isConf = ele.attributeValue("isConf");
			if (isConf != null) {
				String lo = isConf.toLowerCase();
				dbParserTemplet.setConf("on".equals(lo) || "true".equals(lo));
			}
			List<?> sqlEles = ele.elements("sql");
			List<String> sqlList = new ArrayList<String>();
			for (Object sqlE : sqlEles) {
				Element sqlEle = (Element) sqlE;
				String sql = sqlEle.getText();
				sqlList.add(sql.trim());
			}
			dbParserTemplet.setSqlList(sqlList);
			templatesList.add(dbParserTemplet);
		}
		logger.debug("解析数据库解析模板成功:" + templetFileName);
		if (templatesList.size() == 0) {
			throw new IllegalArgumentException("模版有误。配置文件" + templetFileName + "为空");
		}
		return templatesList;
	}
}
