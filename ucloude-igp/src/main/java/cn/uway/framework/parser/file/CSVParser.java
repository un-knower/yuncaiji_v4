package cn.uway.framework.parser.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.log.BadWriter;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.parser.file.templet.Field;
import cn.uway.framework.parser.file.templet.TempletParser;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.StringUtil;
import cn.uway.util.TimeUtil;

/**
 * csv解码器 只支持单个csv文件，不支持批量/压缩包
 * 
 * @author yuy 2013.12.26
 */
public class CSVParser extends FileParser{

	private static final ILogger LOGGER = LoggerManager.getLogger(CSVParser.class);

	// bad FileLogger
	protected static final ILogger badWriter = BadWriter.getInstance().getBadWriter();

	/** 内容分隔符 */
	protected String currSplitSign = ",";

	public CSVParser(){}

	public CSVParser(String tmpfilename){
		super(tmpfilename);
	}

	@Override
	public void parse(AccessOutObject accessOutObject) throws Exception{
		this.accessOutObject = accessOutObject;

		this.before();

		LOGGER.debug("开始解码:{}", accessOutObject.getRawAccessName());

		// 解析模板 获取当前文件对应的Templet
		parseTemplet();
		
		if(templet == null){
			LOGGER.debug("文件没有找到模板，不进行采集，直接返回:{}", accessOutObject.getRawAccessName());
			return;
		}

		if(templet.getEncoding() != null)
			reader = new BufferedReader(new InputStreamReader(inputStream, templet.getEncoding()), 16 * 1024);
		else
			reader = new BufferedReader(new InputStreamReader(inputStream), 16 * 1024);

		// 获取头
		readHead();

		// 字段定位
		setFieldLocalMap(head);

		LOGGER.debug("[{}]-获取头line={}", task.getId(), head);
		readLineNum++;
	}

	@Override
	public boolean hasNextRecord() throws Exception{
		if(null == reader){
			return false;
		}
		try{
			String line = reader.readLine();
			if(line == null)
				return false;
			// 读取完整一行
			currentLine = getFullLine(line);
			return true;
		}catch(IOException e){
			this.cause = "【CSV解码】IO读文件发生异常：" + e.getMessage();
			throw e;
		}
	}

	@Override
	public ParseOutRecord nextRecord() throws Exception{
		readLineNum++;
		ParseOutRecord record = new ParseOutRecord();
		String tmpLine = switchLineWithSplitSign(currSplitSign, currentLine, splitSign);
		String [] valList = StringUtil.split(tmpLine, splitSign);
		List<Field> fieldList = templet.getFieldList();
		// Map<String, String> map = new HashMap<String, String>();
		Map<String,String> map = this.createExportPropertyMap(templet.getDataType());
		for(Field field : fieldList){
			if(field == null){
				continue;
			}
			// 定位，即找出模板中的字段在原始文件中的位置
			Integer indexInLine = fieldLocalMap.get(field.getName());
			// 找不到，设置为空
			if(indexInLine == null){
				if(map.get(field.getIndex()) != null)
					continue;
				map.put(field.getIndex(), "");
				continue;
			}
			if(indexInLine >= valList.length)
				break;
			String value = valList[indexInLine];
			value = value.replace("\"", "");
			// 字段值处理
			if(!fieldValHandle(field, value, map)){
				invalideNum++;
				return null;
			}
			if("true".equals(field.getIsPassMS())){
				int i = value.indexOf(".");
				value = (i == -1 ? value : value.substring(0, i));
			}
			map.put(field.getIndex(), null != value ? value.trim() : value);
		}

		// 公共回填字段
		map.put("MMEID", String.valueOf(task.getExtraInfo().getOmcId()));
		map.put("COLLECTTIME", TimeUtil.getDateString(new Date()));
		handleTime(map);
		record.setType(templet.getDataType());
		record.setRecord(map);
		return record;
	}

	/**
	 * 读文件头
	 * 
	 * @throws Exception
	 */
	public void readHead() throws Exception{
		head = reader.readLine();
		if(head == null){
			throw new NullPointerException("head is null，解码退出");
		}
	}

	@Override
	public List<ParseOutRecord> getAllRecords(){
		return null;
	}

	@Override
	public void close(){
		// 标记解析结束时间
		this.endTime = new Date();

		LOGGER.debug("[{}]-CSV解析，处理{}行", new Object[]{task.getId(),readLineNum});
	}

	@Override
	public Date getDataTime(ParseOutRecord outRecord){
		return null;
	}

	/**
	 * 字段定位
	 */
	public void setFieldLocalMap(String head){
		String tmpHead = switchLineWithSplitSign(currSplitSign, head, splitSign);
		String [] fieldNames = StringUtil.split(tmpHead, splitSign);
		fieldLocalMap = new HashMap<String,Integer>();
		for(int n = 0; n < fieldNames.length; n++){
			String fieldName = fieldNames[n].toUpperCase().replace("\"", "");
			fieldLocalMap.put(fieldName, n);
		}
	}

	/**
	 * 解析模板 获取当前文件对应的Templet
	 * 
	 * @throws Exception
	 */
	public void parseTemplet() throws Exception{
		// 解析模板
		TempletParser csvTempletParser = new TempletParser();
		csvTempletParser.tempfilepath = templates;
		csvTempletParser.parseTemp();
		templetMap = csvTempletParser.getTemplets();
		// 获取当前文件对应的Templet
		getMyTemplet();
		if(templet == null){
			LOGGER.debug("没有找到对应的模板，跳过：" + rawName);
		}else{
			currSplitSign = templet.getSplitSign();
		}
	}

	/**
	 * 读取完整一行
	 * 
	 * @param line_
	 * @throws IOException
	 */
	public String getFullLine(String line_) throws IOException{
		// 不包含引号，不存在跨行
		if(line_.indexOf("\"") == -1){
			return line_;
		}
		String tmpLine = switchLineWithSplitSign(currSplitSign, line_, splitSign);
		String [] valList = StringUtil.split(tmpLine, splitSign);
		// 最后一个不是引号开始，即不跨行
		if(!valList[valList.length - 1].startsWith("\"")){
			return line_;
		}
		String line = null;
		line_ += (line = reader.readLine()) == null ? "" : line;
		while(line.indexOf("\"") == -1){
			line_ += ((line = reader.readLine()) == null ? "" : line);
		}
		return getFullLine(line_);
	}

	/**
	 * 获取当前文件对应的Templet
	 */
	public void getMyTemplet(){
		Iterator<String> it = templetMap.keySet().iterator();
		while(it.hasNext()){
			String file = it.next();
			// 匹配通配符*
			String wildCard = "*";
			if(file.indexOf(wildCard) > -1){
				if(FilenameUtils.wildcardMatch(FilenameUtils.getName(rawName), file)){

					templet = templetMap.get(file);
				}
			}else{
				templet = templetMap.get(file);
			}

		}
	}

	/**
	 * @param regex 正则表达式
	 * @param input 输入字符串
	 * @return
	 */
	public static boolean findValue(String regex, String input){
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		if(matcher.find()){
			String result = matcher.group();
			if(!(null == result || "".equals(result.trim()))){
				return true;
			}
		}
		return false;
	}

	/**
	 * 通配符匹配
	 * 
	 * @param src 解析模板中的文件名或classname
	 * @param dest 厂家原始文件，如果是压缩包，则会带路径
	 * @param wildCard 通配符
	 * @return
	 */
	public static boolean wildCardMatch(String src, String dest, String wildCard){
		boolean flag = false;
		String tmp = src.replace("*", "(.)*");
		if(dest.contains("/")){
			int lastIndex = dest.lastIndexOf("/");
			String tmpFileNameOrClassName = dest.substring(lastIndex + 1);

			boolean result = findValue(tmp, tmpFileNameOrClassName);
			if(result)
				return true;

		}else{
			boolean result = findValue(tmp, dest);
			if(result)
				return true;
		}
		return flag;
	}

	/**
	 * 解析文件名
	 */
	public void parseFileName(){
		String patternTime = StringUtil.getPattern(accessOutObject.getRawAccessName(),
				"\\d{4}[-]\\d{2}[-]\\d{2}[_]\\d{2}[-]\\d{2}");
		if(patternTime != null){
			this.currentDataTime = TimeUtil.getDateTime(patternTime, "yyyy-MM-dd_HH-mm");
			return;
		}
		patternTime = StringUtil.getPattern(accessOutObject.getRawAccessName(), "\\d{8}[_]\\d{4}[.]");
		if(patternTime != null){
			patternTime = patternTime.replace(".", "");
			this.currentDataTime = TimeUtil.getDateTime(patternTime, "yyyyMMdd_HHmm");
			return;
		}

		// 普天LTE
		patternTime = StringUtil.getPattern(accessOutObject.getRawAccessName(), "20\\d{10}[.]");
		if(patternTime != null){
			patternTime = patternTime.replace(".", "");
			this.currentDataTime = TimeUtil.getDateTime(patternTime, "yyyyMMddHHmm");
			return;
		}

		// 大唐LTE
		patternTime = StringUtil.getPattern(accessOutObject.getRawAccessName(), "[-]\\d{8}[-]\\d{4}");
		if(patternTime != null){
			patternTime = patternTime.replace("-", "");
			this.currentDataTime = TimeUtil.getDateTime(patternTime, "yyyyMMddHHmm");
			return;
		}

		// 山东LTE中兴参数
		patternTime = StringUtil.getPattern(accessOutObject.getRawAccessName(), "[_]\\d{14}");
		if(patternTime != null){
			patternTime = patternTime.replace("_", "");
			this.currentDataTime = TimeUtil.getDateTime(patternTime, "yyyyMMddHHmmss");
			return;
		}
		patternTime = StringUtil.getPattern(accessOutObject.getRawAccessName(), "\\d{12}");
		if(patternTime != null){
			this.currentDataTime = TimeUtil.getDateTime(patternTime, "yyyyMMddHHmm");
			return;
		}
		patternTime = StringUtil.getPattern(accessOutObject.getRawAccessName(), "\\d{10}");
		if(patternTime != null){
			this.currentDataTime = TimeUtil.getDateTime(patternTime, "yyyyMMddHH");
			return;
		}
		patternTime = StringUtil.getPattern(accessOutObject.getRawAccessName(), "[_]\\d{8}[.]");
		if(patternTime != null){
			patternTime = patternTime.replace("_", "").replace(".", "");
			this.currentDataTime = TimeUtil.getDateTime(patternTime, "yyyyMMdd");
			return;
		}
		patternTime = StringUtil.getPattern(accessOutObject.getRawAccessName(), "\\d{8}[.]\\d{4}");
		if(patternTime != null){
			this.currentDataTime = TimeUtil.getDateTime(patternTime, "yyyyMMdd.HHmm");
			return;
		}
		patternTime = StringUtil.getPattern(accessOutObject.getRawAccessName(),
				"\\d{4}[-]\\d{2}[-]\\d{2}[-]\\d{2}[-]\\d{2}");
		if(patternTime != null){
			this.currentDataTime = TimeUtil.getDateTime(patternTime, "yyyy-MM-dd-HH-mm");
			return;
		}
		if(this.currentDataTime == null){
			this.currentDataTime = task.getDataTime();
		}
		return;
	}
}