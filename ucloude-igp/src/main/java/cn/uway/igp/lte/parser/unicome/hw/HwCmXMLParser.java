package cn.uway.igp.lte.parser.unicome.hw;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.parser.file.FileParser;
import cn.uway.framework.parser.file.templet.CSVCfcTempletParser;
import cn.uway.framework.parser.file.templet.Field;
import cn.uway.framework.parser.file.templet.TempletParser;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.FileUtil;
import cn.uway.util.StringUtil;
import cn.uway.util.TimeUtil;

/**
 * 联通华为参数解析器
 * 
 * @author tianjing @ 2014-5-10
 */
public class HwCmXMLParser extends FileParser {

	private static ILogger LOGGER = LoggerManager.getLogger(HwCmXMLParser.class);
	
	// 填充基站级别唯一标识<NODEBNAME,USERLABEL>
	protected static Map<String,String> uls = new HashMap<String, String>();
	protected static final String NODEB_MO="UMTSNODEB";
	protected static final String NODEB_NAME="NODEBNAME";
	protected Boolean isNodeB = false;

	protected static final String MO_TAG = "<MO className=\"";

	protected static final String ATTR_TAG = "<attr name=\"";

	/**
	 * lastClassName
	 */
	public String lastClassName = "";

	/**
	 * className
	 */
	public String className = "";
	
	// 源enodeb id（根据BTS3900CELL中的数据回填）
	public String s_enodebid = null;
	// 源cell id　（根据BTS3900CELL中的数据回填）
	public String s_cellid = null;
	// 源小区标识类
	protected static final String SRC_ENBCELL_CLASSNAME = "BTS3900CELL";
	
	// 运营商标识 0：联通 2：电信
	protected static final String operatorsMar = AppContext.getBean("operatorsMark", String.class);
	
	protected boolean operatorsMarFlag = false;

	/**
	 * 存放一条数据
	 */
	public Map<String, String> recordDataMap = null;

	public HwCmXMLParser(String tmpfilename) {
		super(tmpfilename);
		// 根据运营商标识过滤数据,只有0 2时，按照运行商标识过滤，其余的不做过滤
		operatorsMarFlag = operatorsMar.equals("0")?true:operatorsMar.equals("2");
	}

	@Override
	public void parse(AccessOutObject accessOutObject) throws Exception {
		this.accessOutObject = accessOutObject;
		this.before();
		LOGGER.debug("开始解码:{}", accessOutObject.getRawAccessName());
		// 解析模板 获取当前文件对应的templet
		parseTemplet();
		// 转换为缓冲流读取
		this.reader = new BufferedReader(new InputStreamReader(inputStream, "GBK"), 16 * 1024);
	}

	@Override
	public boolean hasNextRecord() throws Exception {
		int index = -1;
		String lineStr = "";
		try {
			while ((lineStr = this.reader.readLine()) != null) {
				if ((index = lineStr.indexOf(MO_TAG)) > -1) {
					this.lastClassName = this.className;
					this.className = lineStr.substring(index + MO_TAG.length(), lineStr.indexOf("\" "));
					if ((this.lastClassName != null && this.lastClassName.length()>0) && findMyTemplet(this.lastClassName)) {
						// 根据运营商标识过滤数据,只有0 2时，按照运行商标识过滤，其余的不做过滤
						if(operatorsMarFlag && this.recordDataMap.get("CNOPERATORID") != null
								&& !operatorsMar.equals(this.recordDataMap.get("CNOPERATORID")))
						{
							this.recordDataMap = null;
							continue;
						}
						isNodeB = this.lastClassName.endsWith(NODEB_MO);
						return true;
					} else {
						this.recordDataMap = null;
					}
				} else if ((index = lineStr.indexOf(ATTR_TAG)) > -1) {
					if (this.recordDataMap == null)
						this.recordDataMap = new HashMap<String, String>();
					String attrName = lineStr.substring(index + ATTR_TAG.length(), lineStr.indexOf("\">")).toUpperCase();
					String attrVal = lineStr.substring(lineStr.indexOf("\">") + 2, lineStr.indexOf("</attr>"));
					this.recordDataMap.put(attrName,attrVal);
				}
			}
			// 最后一条数据
			if (this.recordDataMap != null && this.recordDataMap.size() > 0) {
				this.lastClassName = this.className;
				this.className = null;
				if (findMyTemplet(this.lastClassName)) {
					isNodeB = this.lastClassName.endsWith(NODEB_MO);
					return true;
				} else {
					this.recordDataMap = null;
				}
			}
		} catch (Exception e) {
			this.cause = lineStr + ":【华为参数xml解码】IO读文件发生异常：" + e.getMessage();
			LOGGER.debug(cause);
			throw e;
		}
		return false;
	}

	@Override
	public ParseOutRecord nextRecord() throws Exception {
		readLineNum++;
		ParseOutRecord record = new ParseOutRecord();
		List<Field> fieldList = this.templet.getFieldList();
		Map<String, String> recordData = this.createExportPropertyMap(this.templet.getDataType());
		for (Field field : fieldList) {
			if (field == null) {
				continue;
			}
			String value = this.recordDataMap.get(field.getName().toUpperCase());
			// 找不到，设置为空
			if (value == null) {
				continue;
			}
			if ("DAYLIGHTSAVEINFO".equals(field.getName())) {
				continue;
			}
			recordData.put(field.getIndex(), value);
			// 是否拆封字段
			if ("true".equals(field.getIsSplit())) {
				// FDN 拆封
				if ("FDN".equalsIgnoreCase(field.getName())) {
					if (!this.splitFDNStr(value, field, recordData)) {
						LOGGER.error("节点：{}，fdn：{}，拆封失败。", new Object[]{this.className, value});
						this.invalideNum++;
						this.recordDataMap = null;
						return null;
					}
				} else if ("NAME".equalsIgnoreCase(field.getName())) {
					// NAME 拆封
					if (!this.splitNameStr(value, field, recordData)) {
						LOGGER.error("节点：{}，name：{}，拆封失败。", new Object[]{this.className, value});
						this.invalideNum++;
						this.recordDataMap = null;
						return null;
					}
				}
			}
		}
		// 公共回填字段
		recordData.put("MMEID", String.valueOf(task.getExtraInfo().getOmcId()));
		recordData.put("COLLECTTIME", TimeUtil.getDateString(new Date()));
		// 把任务表中的BSC_ID字段添加到内存中 @author Niow 2014-6-16
		recordData.put("BSCID", String.valueOf(task.getExtraInfo().getBscId()));
		handleTime(recordData);
		
		if (SRC_ENBCELL_CLASSNAME.equalsIgnoreCase(templet.getDataName())) {
			this.s_enodebid = recordData.get("NEID");
			this.s_cellid = recordData.get("CELLID");
		} else {
			recordData.put("S_ENODEBID", this.s_enodebid);
			recordData.put("S_CELLID", this.s_cellid);
		}
		
		if(isNodeB){
			String nodeBName = recordData.get(NODEB_NAME);
			String ul = uls.get(nodeBName);
			if(null == ul){
				ul = CacheManager.getUserLabel(task, job, nodeBName);
				uls.put(nodeBName, ul);
			}
			recordData.put("USERLABEL",ul);
		}
		record.setType(templet.getDataType());
		record.setRecord(recordData);
		this.recordDataMap = null;
		return record;
	}

	/**
	 * FDN 拆封
	 * 
	 * @param value
	 * @param field
	 * @param recordData
	 */
	public final boolean splitFDNStr(String value, Field field, Map<String, String> recordData) {
		// "NE=2808,eNodeBBBP=0_3_0" or "NE=2808,eNodeBBbuFan=0_16_0" or ...
		try {
			String[] values = StringUtil.split(value, ",");
			// 分拆字段列表
			List<Field> fieldList = field.getSubFieldList();
			for (Field subField : fieldList) {
				for (int i = 0; i < values.length; i++) {
					// 特殊处理，"NE=421,NE=351"
					if ("NE2".equals(subField.getName()) && i > 0) {
						int index = values[i].indexOf("=");
						if (index < 1) {
							continue;
						}
						String name = values[i].substring(0, index);
						if (subField.getName().indexOf(name) < 0) {
							continue;
						}
						String val = values[i].substring(index + 1, values[i].length());
						recordData.put(subField.getIndex(), val);
						break;
					}
					int index = values[i].indexOf("=");
					if (index < 1) {
						continue;
					}
					String name = values[i].substring(0, index);
					if (!name.equalsIgnoreCase(subField.getName())) {
						continue;
					}
					String val = values[i].substring(index + 1, values[i].length());
					recordData.put(subField.getIndex(), val);
					break;
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			return false;
		}
		return true;
	}

	/**
	 * NAME 拆封
	 * 
	 * @param value
	 * @param field
	 * @param recordData
	 */
	public final boolean splitNameStr(String value, Field field, Map<String, String> recordData) {
		// "单板类型=BBP, 柜号=0, 框号=0, 槽号=3" or "eNodeB名称=FBJ000003, 本地小区标识=1, 小区名称=FBJ000003A1, 小区双工模式=CELL_FDD" or ...
		try {
			String[] values = StringUtil.split(value, ", ");
			// 分拆字段列表
			List<Field> fieldList = field.getSubFieldList();
			for (Field subField : fieldList) {
				for (int i = 0; i < values.length; i++) {
					int index = values[i].indexOf("=");
					if (index < 1) {
						continue;
					}
					String name = values[i].substring(0, index);
					if (!name.equalsIgnoreCase(subField.getName())) {
						continue;
					}
					String val = values[i].substring(index + 1, values[i].length());
					recordData.put(subField.getIndex(), val);
					break;
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			return false;
		}
		return true;
	}

	/**
	 * 找到当前对应的Templet
	 */
	public final boolean findMyTemplet(String className) {
		this.templet = templetMap.get(className);
		if (templet == null) {
//			LOGGER.debug("没有找到对应的模板，跳过，classname:{}", className);
			return false;
		}
		return true;
	}

	/**
	 * 解析文件名
	 * 
	 * @throws Exception
	 */
	public void parseFileName() {
		try {
			String fileName = FileUtil.getFileName(this.rawName);
			String patternTime = StringUtil.getPattern(fileName, "20\\d{6}");
			this.currentDataTime = getDateTime(patternTime, "yyyyMMdd");
		} catch (Exception e) {
			LOGGER.debug("解析文件名异常", e);
		}
	}

	// 将时间转换成format格式的Date
	public final Date getDateTime(String date, String format) {
		if (date == null) {
			return null;
		}
		if (format == null) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		try {
			DateFormat df = new SimpleDateFormat(format);
			return df.parse(date);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void close() {
		// 标记解析结束时间
		this.endTime = new Date();
		LOGGER.debug("[{}]-华为参数XML解析，处理{}条记录", new Object[]{task.getId(), readLineNum});
	}

	/**
	 * 解析模板 获取当前文件对应的Templet
	 * 
	 * @throws Exception
	 */
	public void parseTemplet() throws Exception {
		// 解析模板
		TempletParser csvTempletParser = new CSVCfcTempletParser();
		csvTempletParser.tempfilepath = templates;
		csvTempletParser.parseTemp();
		templetMap = csvTempletParser.getTemplets();
	}
}
