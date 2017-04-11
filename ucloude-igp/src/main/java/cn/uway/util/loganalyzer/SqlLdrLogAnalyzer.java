package cn.uway.util.loganalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.FileUtil;
import cn.uway.util.SqlldrResult;
import cn.uway.util.Util;

/**
 * oracle Sql loader 日志分析工具
 * 
 * @author liuwx
 */
public class SqlLdrLogAnalyzer {
	private static Map<String, String> configMap = null;

	private static Map<String, String> configMapEn = null;
	// 存储用户自定义规则列表
	private static List<String> uerDefineRuleList = new ArrayList<String>();
	// 配置模板文件名
	// private static String configPath = "SqlLdrLogAnalyseTemplet.xml";
	// 通配符模板
	private static String templetMatchFile = "SqlLdrLogAnalyseTemplet*.xml";
	// 英文模板后缀标识
	 private static final String TEMPLET_MATCH_FILE_EN = "_en.xml";
	// 配置信息目录
	private static String TempletFileDir = "." + File.separator + "conf";
//	 private static String TempletFileDir=
//	 "E:\\company\\svn\\igp\\trunk\\igp_v3\\app_runner\\conf";
	// // 配置信息路径
	// private static String TempletFilePath = TempletFileDir + File.separator
	// + configPath;

	private static final ILogger LOGGER = LoggerManager.getLogger(SqlLdrLogAnalyzer.class); // 日志
	
	private static class SqlLdrLogAnalyzerHolder {
		private static SqlLdrLogAnalyzer instance = new SqlLdrLogAnalyzer();
	}

	// 私有化构造方法
	private SqlLdrLogAnalyzer() {
		initAnalyzerTemplet();
	}

	public static SqlLdrLogAnalyzer getInstance() {
		return SqlLdrLogAnalyzerHolder.instance;
	}

	/**
	 * 初始化加载中英文日志分析模板至类缓存
	 * 
	 * @throws LogAnalyzerException
	 */
	private void initAnalyzerTemplet() {
		List<String> list = FileUtil.getFileNames(TempletFileDir,
				templetMatchFile);
		try {
			if (list == null || list.size() == 0) {
				throw new LogAnalyzerException("sqlldr日志解析模板不存在");
			}
			for (String filePath : list) {
				if (filePath.toLowerCase().endsWith(TEMPLET_MATCH_FILE_EN)) {
					configMapEn = loadTemplet(filePath);
				} else {
					configMap = loadTemplet(filePath);
				}
			}
		} catch (LogAnalyzerException e) {
			e.printStackTrace();
		}
	}

	private Map<String, String> loadTemplet(String templet)
			throws LogAnalyzerException {

		Map<String, String> tmpMap = new HashMap<String, String>();

		String isOracleLog;// 是否是oracle日志文件
		String tableName;// 表名
		String loadSuccCount;// 载入成功行数
		String data;// 数据错误行数没有加载
		String when;// when子句失败行数没有加载
		String nullField;// 字段为空行数
		String skip;// 跳过的逻辑记录总数
		String read;// 读取的逻辑记录总数
		String refuse;// 拒绝的逻辑记录总数
		String abandon;// 废弃的逻辑记录总数
		String startTime;// 开始运行时间
		String endTime;// 运行结束时间
		String totalTime;// 执行时间
		String rule;// 用户自定义规则

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new LogAnalyzerException(
					"解析SqlLdrLogAnalyseTemplet.xml配置模板发生异常");
		}
		Document doc = null;
		File file1 = new File(templet);
		try {
			doc = builder.parse(file1);
		} catch (Exception e) {
			throw new LogAnalyzerException(
					"解析SqlLdrLogAnalyseTemplet.xml配置模板发生异常", e);
		}
		/* 获取动用信息 ,system-rule节点信息 */
		NodeList systemRuleNodeList = doc.getElementsByTagName("system-rule");
		if (systemRuleNodeList.getLength() >= 1) {
			if (doc.getElementsByTagName("is-oracle-log").item(0)
					.getFirstChild() == null) {
				isOracleLog = "";
			} else {
				isOracleLog = doc.getElementsByTagName("is-oracle-log").item(0)
						.getFirstChild().getNodeValue();
			}
			tmpMap.put("isOracleLog", unescapeJava(isOracleLog));
			if (doc.getElementsByTagName("table-name").item(0).getFirstChild() == null) {
				tableName = "";
			} else {
				tableName = doc.getElementsByTagName("table-name").item(0)
						.getFirstChild().getNodeValue();
			}
			tmpMap.put("tableName", unescapeJava(tableName));
			if (doc.getElementsByTagName("load-succ-count").item(0)
					.getFirstChild() == null) {
				loadSuccCount = "";
			} else {
				loadSuccCount = doc.getElementsByTagName("load-succ-count")
						.item(0).getFirstChild().getNodeValue();
			}
			tmpMap.put("loadSuccCount", unescapeJava(loadSuccCount));
			if (doc.getElementsByTagName("data").item(0).getFirstChild() == null) {
				data = "";
			} else {
				data = doc.getElementsByTagName("data").item(0).getFirstChild()
						.getNodeValue();
			}
			tmpMap.put("data", unescapeJava(data));
			if (doc.getElementsByTagName("when").item(0).getFirstChild() == null) {
				when = "";
			} else {
				when = doc.getElementsByTagName("when").item(0).getFirstChild()
						.getNodeValue();
			}
			tmpMap.put("when", unescapeJava(when));
			if (doc.getElementsByTagName("null-field").item(0).getFirstChild() == null) {
				nullField = "";
			} else {
				nullField = doc.getElementsByTagName("null-field").item(0)
						.getFirstChild().getNodeValue();
			}
			tmpMap.put("nullField", unescapeJava(nullField));
			if (doc.getElementsByTagName("skip").item(0).getFirstChild() == null) {
				skip = "";
			} else {
				skip = doc.getElementsByTagName("skip").item(0).getFirstChild()
						.getNodeValue();
			}
			tmpMap.put("skip", unescapeJava(skip));
			if (doc.getElementsByTagName("read").item(0).getFirstChild() == null) {
				read = "";
			} else {
				read = doc.getElementsByTagName("read").item(0).getFirstChild()
						.getNodeValue();
			}
			tmpMap.put("read", unescapeJava(read));
			if (doc.getElementsByTagName("refuse").item(0).getFirstChild() == null) {
				refuse = "";
			} else {
				refuse = doc.getElementsByTagName("refuse").item(0)
						.getFirstChild().getNodeValue();
			}
			tmpMap.put("refuse", unescapeJava(refuse));
			if (doc.getElementsByTagName("abandon").item(0).getFirstChild() == null) {
				abandon = "";
			} else {
				abandon = doc.getElementsByTagName("abandon").item(0)
						.getFirstChild().getNodeValue();
			}
			tmpMap.put("abandon", unescapeJava(abandon));
			if (doc.getElementsByTagName("start-time").item(0).getFirstChild() == null) {
				startTime = "";
			} else {
				startTime = doc.getElementsByTagName("start-time").item(0)
						.getFirstChild().getNodeValue();
			}
			tmpMap.put("startTime", unescapeJava(startTime));
			if (doc.getElementsByTagName("end-time").item(0).getFirstChild() == null) {
				endTime = "";
			} else {
				endTime = doc.getElementsByTagName("end-time").item(0)
						.getFirstChild().getNodeValue();
			}
			tmpMap.put("endTime", unescapeJava(endTime));
			if (doc.getElementsByTagName("total-time").item(0).getFirstChild() == null) {
				totalTime = "";
			} else {
				totalTime = doc.getElementsByTagName("total-time").item(0)
						.getFirstChild().getNodeValue();
			}
			tmpMap.put("totalTime", unescapeJava(totalTime));
		}
		/* 获取动用信息 ,user define rule节点信息 */
		NodeList userRuleNodeList = doc
				.getElementsByTagName("user-define-rule");
		if (userRuleNodeList.getLength() >= 1) {
			for (int i = 0; i < userRuleNodeList.getLength(); i++) {
				Node userRule = userRuleNodeList.item(i);
				NodeList nodelist = userRule.getChildNodes();
				for (int j = 0; j < nodelist.getLength(); j++) {
					Node nodeRule = nodelist.item(j);
					if (nodeRule.getNodeType() == Node.ELEMENT_NODE
							&& nodeRule.getNodeName().toLowerCase()
									.equals("rule")) {
						rule = getNodeValue(nodeRule);
						uerDefineRuleList.add(unescapeJava(rule));
					}
				}
			}
		}
		return tmpMap;
	}

	/**
	 * 分析日志文件
	 */
	public SqlldrResult analysis(String fileName) throws LogAnalyzerException {
		try {
			return analysis(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			throw new LogAnalyzerException("文件未找到", e);
		}
	}

	/*
	 * 分析日志文件
	 */
	public SqlldrResult analysis(InputStream in) throws LogAnalyzerException {
		BufferedReader br = null;
		InputStreamReader isr = null;
		Map<String, String> tempMap = null;
		SqlldrResult sqlldrResult = new SqlldrResult();
		sqlldrResult.setRuleList(new ArrayList<String>());
		try {
			isr = new InputStreamReader(in, "GBK");
			br = new BufferedReader(isr);

			/* 找模板 */
			String head = br.readLine();
			while (head == null || "".equals(head)) {
				head = br.readLine();
			}
			// 判断oracle中/英文版本
			boolean isChinese = isChineseVersion(head);
			if (isChinese && configMap == null) {
				throw new LogAnalyzerException("没有sqlldr中文日志解析模板");
			} else if ((!isChinese) && configMapEn == null) {
				throw new LogAnalyzerException("没有sqlldr英文日志解析模板");
			} else {
				tempMap = isChinese ? configMap : configMapEn;
			}
			// 最后验证
			if (isChinese(tempMap.get("tableName")) != isChinese) {
				throw new LogAnalyzerException(
						"sqlldr日志解析模板与oracle客户端的版本不匹配(中英文不匹配)");
			}

			// 判断是否是oracle 日志文件
			boolean isOracleLog = isOracleLog(head, tempMap);
			if (!isOracleLog) {
				// sysbase处理方式 暂时没有处理
				return new SqlldrResult();
			} else {
				// oracle 处理方式
				String lineData = null;
				try {
					while ((lineData = br.readLine()) != null) {
						lineData = lineData.trim();
						getSqlldrAnalyseResult(lineData, sqlldrResult, tempMap);
					}
				} catch (Exception e) {
					IOUtils.closeQuietly(br);
					IOUtils.closeQuietly(isr);
					IOUtils.closeQuietly(in);
					throw new LogAnalyzerException("分析log文件出现异常", e);
				}
			}
		} catch (Exception e) {
			LOGGER.error("分析SQLLDR日志时异常", e);
		} finally {
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(isr);
			IOUtils.closeQuietly(in);
		}
		return sqlldrResult;
	}

//	/*
//	 * 销毁
//	 */
//	@Override
//	public void destory() {
//		configMap = null;
//		uerDefineRuleList = null;
//	}

	public static String unescapeJava(String unicodeValue) {
		return unicodeValue;
		// return StringEscapeUtils.unescapeJava(unicodeValue);
	}

	public void init() throws LogAnalyzerException {
	}

	// 获取当前节点的值
	private static String getNodeValue(Node CurrentNode) {
		String strValue = "";
		NodeList nodelist = CurrentNode.getChildNodes();
		if (nodelist != null) {
			for (int i = 0; i < nodelist.getLength(); i++) {
				Node tempnode = nodelist.item(i);
				if (tempnode.getNodeType() == Node.TEXT_NODE) {
					strValue = tempnode.getNodeValue();
				}
			}
		}
		return strValue;
	}

	// 判断是否是oracle日志文件
	private boolean isOracleLog(String head, Map<String,String> map) {
		return head.contains(map.get("isOracleLog"));
	}

	// 判断是否是oracle日志文件
	private boolean isChineseVersion(String head) {
		return isChinese(head);
	}

	private void getSqlldrAnalyseResult(String line, SqlldrResult sqlldrResult, Map<String,String> map) {
		boolean b = false;
		for (int i = 0; i < uerDefineRuleList.size(); i++) {
			String regRule = uerDefineRuleList.get(i);
			if (regRule == null
					|| (regRule != null && "".equals(regRule.trim()))) {
				continue;
			}
			String result = regexQueryGroup(line, uerDefineRuleList.get(i), 0,
					sqlldrResult.getRuleList());
			if (Util.isNotNull(result)) {
				b = true;
			}
		}
		if (b)
			return;

		for (Entry<String, String> en : map.entrySet()) {
			String key = en.getKey();
			if ("isOracleLog".equalsIgnoreCase(key))
				continue;
			String value = en.getValue();
			String result = null;
			try {
				result = regexQueryGroup(line, value, 1,
						sqlldrResult.getRuleList());
			} catch (Exception e) {
				LOGGER.error("正则表达示出现异常," + value + "原因:{}", e);
			}
			if (Util.isNotNull(result)) {
				// cmap.remove(key);
				if ("tableName".equals(key)) {
					result = result.replace("'", "");
					result = result.replaceAll("\"", "");
					if (result.indexOf(".") > -1)
						result = result.substring(result.indexOf(".") + 1);
					sqlldrResult.setTableName(result);
				} else if ("loadSuccCount".equals(key)) {
					sqlldrResult.setLoadSuccCount(stringToInt(result));
				} else if ("data".equals(key)) {
					sqlldrResult.setData(stringToInt(result));
				} else if ("when".equals(key)) {
					sqlldrResult.setWhen(stringToInt(result));
				} else if ("nullField".equals(key)) {
					sqlldrResult.setNullField(stringToInt(result));
				} else if ("skip".equals(key)) {
					sqlldrResult.setSkip(stringToInt(result));
				} else if ("read".equals(key)) {
					sqlldrResult.setRead(stringToInt(result));
				} else if ("refuse".equals(key)) {
					sqlldrResult.setRefuse(stringToInt(result));
				} else if ("abandon".equals(key)) {
					sqlldrResult.setAbandon(stringToInt(result));
				} else if ("startTime".equals(key)) {
					sqlldrResult.setStartTime(result);
				} else if ("endTime".equals(key)) {
					sqlldrResult.setEndTime(result);
				} else if ("totalTime".equals(key)) {
					sqlldrResult.setTotalTime(result);
				}
			}
		}
	}

	// 通过正则表达式查找
	private String regexQueryGroup(String str, String regEx, int group,
			List<String> matchRuleList) {
		String resultValue = "";
		if (regEx == null || (regEx != null && "".equals(regEx.trim()))) {
			return resultValue;
		}
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		boolean result = m.find();// 查找是否有匹配的结果
		if (result) {
			resultValue = m.group(group);// 找出匹配的结果
			if (group == 0) {
				if (!matchRuleList.contains(resultValue)) {
					matchRuleList.add(resultValue);
				}
			}
		}
		return resultValue;
	}

	private int stringToInt(String str) {
		if (str == null || (str != null && str.trim().equals(""))) {
			return 0;
		}
		str = str.trim();
		return Integer.valueOf(str);
	}

	// 根据Unicode编码完美的判断中文汉字和符号
	private boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}

	// 完整的判断中文汉字和符号
	public boolean isChinese(String strName) {
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
		try {
			InputStream in = new FileInputStream(
					"E:\\company\\1434423646677.log");
			SqlldrResult sr = SqlLdrLogAnalyzer.getInstance().analysis(in);
			IOUtils.closeQuietly(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		// Pattern p = Pattern.compile("经过时间为:\\s+(.*\\d)");
//		// Matcher m = p.matcher("经过时间为: 00: 00: 00.36");
//		// System.out.println(m.find());// 查找是否有匹配的结果
//
	}
}
