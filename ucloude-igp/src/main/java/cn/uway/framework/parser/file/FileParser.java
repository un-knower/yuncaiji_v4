package cn.uway.framework.parser.file;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.accessor.StreamAccessOutObject;
import cn.uway.framework.log.BadWriter;
import cn.uway.framework.parser.AbstractParser;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.parser.file.templet.Field;
import cn.uway.framework.parser.file.templet.Templet;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.FileUtil;
import cn.uway.util.StringUtil;
import cn.uway.util.TimeUtil;

/**
 * 文件抽象解码器
 * 
 * @author yuy
 * @date 2014.4.28
 */
public abstract class FileParser extends AbstractParser {

	private static final ILogger LOGGER = LoggerManager.getLogger(FileParser.class);

	// bad FileLogger
	protected static final ILogger badWriter = BadWriter.getInstance().getBadWriter();

	public Templet templet = null;

	public InputStream inputStream;

	public BufferedReader reader = null;

	public BufferedReader br = null;

	public AccessOutObject accessOutObject;

	public long readLineNum = 0; // 记录总行数

	public String currentLine = null; // 要解析的当前行

	public String rawName = null; // 要解析的文件名

	public String head = null; // 文件内容的第一行

	public Map<String, Integer> fieldLocalMap; // 根据head对厂家字段进行定位（记录索引）

	public static String splitSign = "`~"; // 分隔符

	public Map<String, Templet> templetMap; // 模板map<String/*file*/,String/*Templet*/>

	public int index = 0;

	public FileParser() {
	}

	public FileParser(String tmpfilename) {
		templates = tmpfilename;
	}

	@Override
	public void before() {
		if (accessOutObject == null)
			throw new NullPointerException("AccessOutObject is null.");
		StreamAccessOutObject streamAccessOutObject = (StreamAccessOutObject) accessOutObject;
		task = streamAccessOutObject.getTask();
		if (task == null)
			throw new NullPointerException("Task is null.");
		inputStream = streamAccessOutObject.getOutObject();
		if (inputStream == null)
			throw new NullPointerException("接入对象无效，inputStream is null");
		rawName = streamAccessOutObject.getRawAccessName();// 流文件名

		// 标记解析开始时间
		this.startTime = new Date();

		this.parseFileName();
	}

	@Override
	public void parse(AccessOutObject accessOutObject) throws Exception {
		this.accessOutObject = accessOutObject;

		this.before();

		LOGGER.debug("开始解码:{}", accessOutObject.getRawAccessName());

		// currentDataTime 用于生成summary文件进行日期匹配
		this.currentDataTime = this.getCurrentDataTime();

		// 解析模板 获取当前文件对应的Templet
		parseTemplet();

		if (templet == null) {
			throw new NullPointerException("没有找到对应的模板，解码退出");
		}

		reader = new BufferedReader(new InputStreamReader(inputStream), 16 * 1024);
	}

	/**
	 * 添加数据时间
	 * 
	 * @param map
	 */
	protected void handleTime(Map<String, String> map) {
		if (this.getCurrentDataTime() != null)
			map.put("STAMPTIME", TimeUtil.getDateString(this.getCurrentDataTime()));
	}

	@Override
	public List<ParseOutRecord> getAllRecords() {
		return null;
	}

	@Override
	public void close() {
		// 标记解析结束时间
		this.endTime = new Date();
	}

	@Override
	public Date getDataTime(ParseOutRecord outRecord) {
		return null;
	}

	/**
	 * 解析模板 获取当前文件对应的Templet
	 * 
	 * @throws Exception
	 */
	public void parseTemplet() throws Exception {
		return;
	}

	/**
	 * 解析文件名
	 */
	public void parseFileName() {
		return;
	}

	/**
	 * @return currentDataTime 用于生成summary文件进行日期匹配
	 */
	public Date getCurrentDataTime() {
		return this.currentDataTime;
	}

	// 主动设置currentDataTime(只适用于带时区的文件名，如爱立信、阿朗)
	public void setCurrentDataTime(String fileName) {
		try {
			fileName = FileUtil.getFileName(fileName);
			String patternTime = StringUtil.getPattern(fileName, "\\d{8}[.]\\d{4}[+]\\d{2}");
			if (patternTime != null) {
				int addPlace = patternTime.indexOf("+");
				String currPatternTime = patternTime.substring(0, addPlace).replace(".", "_");
				// 不考虑时区
				Date dataTime = TimeUtil.getyyyyMMdd_HHmmDate(currPatternTime);
				// 考虑时区
				int timeZone = Integer.parseInt(patternTime.substring(addPlace + 1));
				int beijingTimeZone = 8;// 北京时区 东八区
				if (timeZone == beijingTimeZone)
					this.currentDataTime = dataTime;
				else {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(dataTime);
					calendar.add(Calendar.HOUR, beijingTimeZone - timeZone);
					this.currentDataTime = calendar.getTime();
				}
			}
		} catch (Exception e) {
			LOGGER.debug("解析文件名异常", e);
		}
	}

	/**
	 * 转换分隔符(,')
	 * 
	 * @param line
	 * @return String 转换后的字符串(去了引号)
	 */
	public static String switchLineWithSplitSign(String currSplitSign, String line, String splitSign) {
		if (currSplitSign == null)
			return null;
		char[] charArray = currSplitSign.toCharArray();
		String strValue = null;
		if (charArray.length == 1) {
			strValue = swicthLine_(charArray[0], line, splitSign);
		} else {
			strValue = line.replace(currSplitSign, splitSign);
		}
		if (strValue != null)
			strValue = strValue.replace("\"", "");
		return strValue;
	}

	/**
	 * 转换分隔符(,')
	 * 
	 * @param line
	 * @return String 转换后的字符串(去了引号)
	 */
	public static String switchLine(String line, String splitSign) {
		String strValue = swicthLine_(',', line, splitSign);
		if (strValue != null)
			strValue = strValue.replace("\"", "");
		return strValue;
	}

	/**
	 * 转换分隔符(,')
	 * 
	 * @param line
	 * @return String 转换后的字符串(没去引号)
	 */
	public static String swicthLine_(char currSplitSign, String line, String splitSign) {
		List<String> strList = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		boolean flag = false;// 双引号标记
		char tmpChar = '0';
		try {
			for (char s : line.toCharArray()) {
				if ((s == currSplitSign) && flag == false) {
					strList.add(sb.toString() + splitSign);
					sb = new StringBuffer();
					tmpChar = s;
					continue;
				}
				if (s == '\"') {
					if (flag == true) {
						flag = false;
					} else if (tmpChar == currSplitSign || sb.length() == 0) {
						flag = true;
					}
					sb.append(s);
					tmpChar = s;
					continue;
				}
				sb.append(s);
				tmpChar = s;
			}
		} catch (Exception e) {
			LOGGER.debug("switchLine出现异常", e);
		}
		if (sb.toString().length() > 0) {
			strList.add(sb.toString());
		}
		String strArray[] = strList.toArray(new String[strList.size()]);
		String strValue = "";
		for (String ss : strArray) {
			strValue += ss;
		}
		return strValue;
	}

	/**
	 * 字段值处理(拆分，支持多表达式)
	 * 
	 * @param field
	 * @param str
	 * @param map
	 * @param index
	 * @return detailed realize of fieldValHandle(Field field, String str, Map<String,String> map)
	 */
	public boolean fieldValHandle(Field field, String str, Map<String, String> map) {
		if (field == null)
			return true;
		if (StringUtil.isEmpty(str))
			return true;
		if(field.getName().equalsIgnoreCase("PMPDCPLATTIMEDLQCI")){
			System.out.println(field.getName());
		}
		if (!"true".equals(field.getIsSplit())) {
			// 爱立信性能特殊分拆处理（pmErabModAttQci等）
			if ("true".equals(field.getIsSpecialSplit())) {
				specialSplitHanlder(field, str, map);
				return true;
			}
			// 爱立信性能直接分拆处理（pmHoExeInAttQci）
			if ("true".equals(field.getIsDirectSplit())) {
				directSplitHanlder(field, str, map);
				return true;
			}
			return true;
		}
		//把模板里配置的节点的子属性个数和实际项值的个数中最少的那个数字作为循环次数，
		//依次使用项值给指定的属性赋值,2016年10月27日 diao
		if(!StringUtil.isEmpty(field.getIsDirectSplit()) && field.getIsDirectSplit().equalsIgnoreCase("true") &&
				!StringUtil.isEmpty(str)){
			directSplitHanlder(field, str, map);
			return true;
		}
		String regex = field.getRegex();
		return handleValue(field, str, map, regex);
	}

	protected boolean handleValue(Field field, String str, Map<String, String> map, String regex) {
		int regexsNum = 0;
		// 处理多个表达式的情况
		if (field.getHasOtherRegexs() != null && "yes".equals(field.getHasOtherRegexs())) {
			regexsNum = field.getRegexsNum();
			String regexsSplitSign = field.getRegexsSplitSign();
			String[] regexArray = StringUtil.split(regex, regexsSplitSign);
			if (index < regexsNum && index < regexArray.length) {
				regex = regexArray[index];
			} else {
				badWriter.debug("拆分失败，" + field.getName() + ":" + str);
				index = 0;
				return false;
			}
		}
		// 分拆字段列表
		List<Field> fieldList = field.getSubFieldList();
		// 表达式分拆
		String[] valList = StringUtil.split(regex, "?");
		// 不排序标志
		boolean isNotOrderFlag = field.isOrder() != null && "false".equals(field.isOrder());

		int start = 0;
		for (int n = 0; n < fieldList.size() && n < (valList.length - 1); n++) {
			Field f = fieldList.get(n);
			int end = 0;
			// 按照顺序取值
			if (!isNotOrderFlag) {
				start = start + valList[n].length();
				if (n + 1 == fieldList.size()) {
					end = str.length();
				} else {
					// 开始长度大于字符串长度
//					if (start + 1 >= str.length())
					if (start + 1 > str.length())
						return handleNotMatched(field, str, map, regexsNum);
					String s = str.substring(start);
					int next = n + 1;
					end = start + s.indexOf(valList[next]);
					// 到字符串末尾了
					if (end == start && next + 1 == valList.length)
						end = str.length();
				}
			}
			// 按照field.name匹配取值(适配华为)
			else {
				String name = f.getName();
				boolean isFound = false;
				String fromStr = null;
				String endStr = null;
				for (String A : valList) {
					int ind = A.indexOf(name);
					if (ind > -1) {
						isFound = true;
						fromStr = A;
						continue;
					}
					if (isFound) {
						endStr = A;
						break;
					}
				}
				// 在表达式中没有找到当前field.name，要换表达式
				if (!isFound) {
					return handleNotMatched(field, str, map, regexsNum);
				}
				start = str.indexOf(fromStr) + fromStr.length();
				end = str.indexOf(endStr);
				// str中匹配不上，要换表达式
				if (start == -1) {
					return handleNotMatched(field, str, map, regexsNum);
				}
			}
			// 为""，并且是到最后了，赋整个长度
			if (end == 0 && n == valList.length - 2) {
				end = str.length();
			}
			// 匹配不上表达式
			if (end == -1 || end < start) {
				return handleNotMatched(field, str, map, regexsNum);
			}
			try {
				// 拆分字段赋值
				map.put(f.getIndex(), str.substring(start, end));
			} catch (Exception e) {
				return handleNotMatched(field, str, map, regexsNum);
			}
			start = end;
		}
		index = 0;
		return true;
	}

	/**
	 * 处理匹配不上表达式的情况
	 * 
	 * @param field
	 * @param str
	 * @param map
	 * @param regexsNum
	 * @return
	 */
	private boolean handleNotMatched(Field field, String str, Map<String, String> map, int regexsNum) {
		if (regexsNum > 0) {
			index++;
			return fieldValHandle(field, str, map);
		} else {
			badWriter.debug("拆分失败，" + field.getName() + ":" + str);
			index = 0;
			return false;
		}
	}

	/**
	 * 特殊分拆处理器
	 * 
	 * @param field
	 * @param str
	 * @param map
	 */
	public void specialSplitHanlder(Field field, String str, Map<String, String> map) {
		if (StringUtil.isEmpty(str))
			return;
		String[] array = StringUtil.split(str, ",");
		map.put(field.getIndex() + "_0", array[0].trim());
		for (int n = 1; n < array.length; n++) {
			map.put(field.getIndex() + "_" + array[n].trim(), array[++n].trim());
		}
		return;
	}

	/**
	 * 直接分拆处理器
	 * 
	 * @param field
	 * @param str
	 * @param map
	 */
	public void directSplitHanlder(Field field, String str, Map<String, String> map) {
		if (StringUtil.isEmpty(str)) 
			return;
		String[] array = StringUtil.split(str, ",");
		//根据项目值个数动态分配值到属性 add diao 2016年10月27
		if(field.getSubFieldList()!=null && field.getSubFieldList().size()!=0 && 
				!StringUtil.isEmpty(field.getIsDirectSplit())){
			//反转数组 
			if(!StringUtil.isEmpty(field.isOrder()) && field.isOrder().equalsIgnoreCase("false")){
				String[] arr_temp=new String[array.length];
				for(int i=array.length-1, n=0;i>=0;i--,n++){
					arr_temp[n]=array[i];
				}
				array=arr_temp;
			}
			for(int i=1;i<=field.getSubFieldList().size() && i<=array.length;i++){
				map.put(field.getSubFieldList().get(i-1).getIndex(), array[i-1].trim());
			}
			return;
		}
		for (int n = 0; n < array.length; n++) {
			map.put(field.getIndex() + "_" + n, array[n].trim());
		}
		return;
	}
}
