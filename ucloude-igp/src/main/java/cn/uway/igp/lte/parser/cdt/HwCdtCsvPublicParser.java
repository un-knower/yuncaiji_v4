package cn.uway.igp.lte.parser.cdt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.accessor.StreamAccessOutObject;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.context.Vendor;
import cn.uway.framework.log.BadWriter;
import cn.uway.framework.orientation.Type.ONEWAYDELAY_CELL;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.parser.file.FileParser;
import cn.uway.framework.parser.file.templet.CSVCfcTempletParser;
import cn.uway.framework.parser.file.templet.Field;
import cn.uway.framework.parser.file.templet.Templet;
import cn.uway.framework.parser.file.templet.TempletParser;
import cn.uway.framework.task.worker.TaskWorkTerminateException;
import cn.uway.igp.lte.service.AbsImsiQuerySession.ImsiRequestResult;
import cn.uway.igp.lte.service.ImsiQueryHelper;
import cn.uway.igp.lte.service.LteCoreCommonDataManager;
import cn.uway.igp.lte.util.AssociateCellUtil;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.StringUtil;

public class HwCdtCsvPublicParser extends FileParser implements ImsiQueryHelper.ASynchronizedParser {

	private static ILogger LOGGER = LoggerManager.getLogger(HwCdtCsvPublicParser.class);

	private static final ILogger badWriter = BadWriter.getInstance().getBadWriter();

	public StreamAccessOutObject streamOut;

	protected int nMinOrientNeiCellsNumber = 0;
	
	public String rawFilePath = null; // 要解析的文件
	
	// 方位角范围，都是120
	//private static final int ANGLERANG = 120;

	public String rawFileName;

	public BufferedReader reader = null;

	public String[] cdrHeadInfo = null;
	public String[] cdrHeadFieldIndex = null;

	public HashMap<String, String> cdrFieldNames = null;

	public String lineRecord = null;

	public long readLineNum = 0; // 记录总行数

	// 保留小数点后5位
	public DecimalFormat df = new DecimalFormat("0.00000");
	public DecimalFormat d2f = new DecimalFormat("0.00");
	public DecimalFormat d6f = new DecimalFormat("0.000000");
	
	protected ImsiQueryHelper imsiQueryHelper;
	
	protected Long curr_enb_id;
	
	protected String[] lineValues = new String[2048];
	
	protected int debugLevel;
	
	/**
	 * 原始文件粒度
	 */
	protected final static long RAWFILE_UNIT_TIMESEC = 5*60*1000l;
	
	/**
	 * 数据时间和文件时间的有效区间
	 */
	private static final long timeValidRange = 24 * 60 * 60 * 1000L;
		
	/**
	 * 华为数据格式(20160320215032)，SimpleDateFormat不要定义成静态变量的,not synchronized.
	 * Date formats are not synchronized. It is recommended to create separate format instances for each thread. If multiple threads access a format concurrently, it must be synchronized externally.
	 */
	private final SimpleDateFormat cdrTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private final SimpleDateFormat dateToStringFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final SimpleDateFormat cdrFileTimeFormat = new SimpleDateFormat("yyyyMMddHHmm");
	
	private static final String[] timeFields = {"CDR_STARTTIME","CDR_ENDTIME","AC_RRCSTARTTIME","AC_RRCENDTIME","ERAB_CREATETIME1",
			"ERAB_CREATETIME2","ERAB_CREATETIME3","ERAB_CREATETIME4","ERAB_CREATETIME5","ERAB_CREATETIME6","ERAB_CREATETIME7",
			"ERAB_CREATETIME8","ERAB_DEACTIVATETIME1","ERAB_DEACTIVATETIME2","ERAB_DEACTIVATETIME3","ERAB_DEACTIVATETIME4",
			"ERAB_DEACTIVATETIME5","ERAB_DEACTIVATETIME6","ERAB_DEACTIVATETIME7","ERAB_DEACTIVATETIME8","HO_FIRST_STARTTIME",
			"HO_FIRST_EVENTTIME","HO_FIRST_ENDTIME","HO_LAST_STARTTIME","HO_LAST_EVENTTIME","HO_LAST_ENDTIME","RLS_CALLRELEASETIME","RLS_REDIRECTION_TIME"};
	
	//需要转换成10进制的16进制字段
	private static final String[] covertNumFields = {"CDR_MMEGROUPID","CDR_MMECODE","HO_FIRST_SRCCELLID","HO_FIRST_TGTCELLID","HO_LAST_TGTCELLID","RLS_CELLID","HO_LAST_SRCCELLID",
		"HO_HISTORYCELLECGI_1","HO_HISTORYCELLECGI_2","HO_HISTORYCELLECGI_3","HO_HISTORYCELLECGI_4","HO_HISTORYCELLECGI_5","HO_HISTORYCELLECGI_6","HO_HISTORYCELLECGI_7","HO_HISTORYCELLECGI_8"};
	
	private static final String[] hoHistoryCellEcgi = {"HO_HISTORYCELLECGI_1","HO_HISTORYCELLECGI_2","HO_HISTORYCELLECGI_3","HO_HISTORYCELLECGI_4","HO_HISTORYCELLECGI_5","HO_HISTORYCELLECGI_6","HO_HISTORYCELLECGI_7","HO_HISTORYCELLECGI_8"};
	
	private static final String[] covertD2FFields = {"AC_CELLRBPAYLOAD","AC_CELLPDCCHPAYLOAD","RLS_CELLRBPAYLOAD","RLS_CELLPDCCHPAYLOAD",
			"ERAB_UL_PACKET_DROPRATE1","ERAB_UL_PACKET_DROPRATE2","ERAB_UL_PACKET_DROPRATE3","ERAB_UL_PACKET_DROPRATE4","ERAB_UL_PACKET_DROPRATE5",
			"ERAB_UL_PACKET_DROPRATE6","ERAB_UL_PACKET_DROPRATE7","ERAB_UL_PACKET_DROPRATE8","ERAB_DL_PACKET_LOSSRATE1","ERAB_DL_PACKET_LOSSRATE2",
			"ERAB_DL_PACKET_LOSSRATE3","ERAB_DL_PACKET_LOSSRATE4","ERAB_DL_PACKET_LOSSRATE5","ERAB_DL_PACKET_LOSSRATE6","ERAB_DL_PACKET_LOSSRATE7",
			"ERAB_DL_PACKET_LOSSRATE8"};
	
	private static final String[]   acMrLtescplrdlqci= {"AC_MR_LTESCPLRDLQCI1","AC_MR_LTESCPLRDLQCI2","AC_MR_LTESCPLRDLQCI3","AC_MR_LTESCPLRDLQCI4",
		"AC_MR_LTESCPLRDLQCI5","AC_MR_LTESCPLRDLQCI6","AC_MR_LTESCPLRDLQCI7","AC_MR_LTESCPLRDLQCI8","AC_MR_LTESCPLRDLQCI9",
		"AC_MR_LTESCPLRULQCI1","AC_MR_LTESCPLRULQCI2","AC_MR_LTESCPLRULQCI3","AC_MR_LTESCPLRULQCI4",
		"AC_MR_LTESCPLRULQCI5","AC_MR_LTESCPLRULQCI6","AC_MR_LTESCPLRULQCI7","AC_MR_LTESCPLRULQCI8",
		"AC_MR_LTESCPLRULQCI9"};
	
	/**
	 * 是否是解析第一个文件(压缩包中的第一个文件)
	 */
	private boolean isFirstFileToParse = true;
	
	public HwCdtCsvPublicParser(){
		String sMinOrientNeiCellsNumber = AppContext.getBean("minOrientNeiCellsNumber", String.class);
		if (sMinOrientNeiCellsNumber != null && org.apache.commons.lang.math.NumberUtils.isNumber(sMinOrientNeiCellsNumber)) {
			nMinOrientNeiCellsNumber = Integer.parseInt(sMinOrientNeiCellsNumber);
		}
		
		String sdebugLevel = AppContext.getBean("debugLevel", String.class);
		if (sdebugLevel != null && org.apache.commons.lang.math.NumberUtils.isNumber(sdebugLevel)) {
			this.debugLevel  = Integer.parseInt(sMinOrientNeiCellsNumber);
		}
	}
	
	@Override
	public void parse(AccessOutObject accessOutObject) throws Exception {
		if (accessOutObject == null)
			throw new IOException("接入对象无效，null.");
		this.streamOut = (StreamAccessOutObject) accessOutObject;
		this.task = this.streamOut.getTask();
		this.rawFilePath = this.streamOut.getRawAccessName();
		this.startTime = new Timestamp(System.currentTimeMillis()); // 开始解析的时间。
		this.currentDataTime = parseFileName(rawFilePath);
		LOGGER.debug("开始解码:{}", this.rawFilePath);
		// 解析模板 获取当前文件对应的templet
		this.parseTemplet();
		imsiQueryHelper = ImsiQueryHelper.getHelperInstance(this.task.getId());
		Date fileTime = parseFileName(this.rawFilePath);
		if (fileTime != null) {
			ImsiRequestResult result = imsiQueryHelper.isCacheReady(fileTime.getTime() + RAWFILE_UNIT_TIMESEC);
			if (result == null) {
				throw new TaskWorkTerminateException("查询IMSI服务器出错");
			}
			
			if (result.value != ImsiRequestResult.RESPONSE_CACHE_IS_READY) {
				String errMsg = ImsiRequestResult.getResponseValueDesc(result.value);
				LOGGER.debug("{} cdr时间:{}，核心网{}cache最大生成时间{}.", new Object[]{
						errMsg, 
						getDateString(fileTime),
						result.getRequestServerInfo(), 
						getDateString(new Date(result.maxServerTimeInCache) )});
		
				throw new TaskWorkTerminateException("核心网话单未达到关联时间条件或服务器出错, 将在下一个周期继续采集.");
			}
		}
		this.reader = new BufferedReader(new InputStreamReader(this.streamOut.getOutObject(), "GBK"), 16 * 1024);	

		// 解析文件头
		this.parseCdrHead(this.reader.readLine());
		imsiQueryHelper.asynParse(this, isFirstFileToParse);
		if (isFirstFileToParse)
			isFirstFileToParse = false;
	}
	
	@Override
	public void asynExtractAllRecords() throws Exception {
		if (this.cdrFieldNames == null || this.reader == null || this.currentDataTime == null) {
			return ;
		}
		
		while (!imsiQueryHelper.isCanceParseFlagSet() && getLineData()) {
			extractNextReord();
		}
	}
	
	private ParseOutRecord extractNextReord() throws TimeoutException, ParseException {
		// 拆封字段
		int nCsvNum = split(this.lineRecord, ',', lineValues);
		if (nCsvNum < 1)
			return null;
		
		Map<String, String> recordData = this.createExportPropertyMap(ParseOutRecord.DEFAULT_DATA_TYPE);
		for (int i = 0; i < cdrHeadInfo.length && i<nCsvNum; i++) {
			if(lineValues[i] == null || lineValues[i].equals("N/A")){
				continue;
			}
			
			//cdrFieldNames  解析模板中的field
			String fieldIndex = this.cdrHeadFieldIndex[i];
			if (fieldIndex == null)
				continue;
			recordData.put(fieldIndex, lineValues[i]);
		}
		
		try {
			conversionProcess(recordData);
		} catch (Exception e) {
			LOGGER.warn("字段处理异常。原因：", e);
		}
		
		// NE_CELL_L表关联获取网元信息。关联字段：VENDOR - E_NODE_ID - CONNECTION_CELL_ID。
		boolean isreturnNull = AssociateCellUtil.associateCellInfo(task,recordData,nMinOrientNeiCellsNumber,Vendor.VENDOR_HW);
		//接入与释放都关联不上city_id，在切换与最后切换时再关联一次
		if(isreturnNull && StringUtils.isEmpty(recordData.get("CITY_ID"))){
			AssociateCellUtil.associateCityId(recordData,Vendor.VENDOR_HW);
		}
		if(!isreturnNull){
			badWriter.warn("关联不上网元。");
			return null;
		}

		//IMSI MSISDN关联
		String cdrStartTime = recordData.get("CDR_STARTTIME");
		String mmeueS1apid = recordData.get("CDR_MMEUES1APID");
		String stmsi = recordData.get("CDR_STMSI");
		
		String mmegi = recordData.get("CDR_MMEGROUPID");
		String mmec = recordData.get("CDR_MMECODE");
		
		// 从S_TMSI中切出MTmsi，Mmec: 1 bytes， MTmsi: 4 bytes
		String mtmsi = null;
		if (stmsi != null && stmsi.length() > 0) {
			//数值类型 16进制  10进制
			if(stmsi.toLowerCase().startsWith("0x")){
				mtmsi = Long.valueOf(stmsi.substring(2),16).toString();
			}else{
				mtmsi = Long.valueOf(stmsi,10).toString();
			}
		}
		
		//2016-05-11 13:39:47
		Date cdrTime = null;
		if (!StringUtils.isEmpty(cdrStartTime) && cdrStartTime.length()>=19)
		{
			cdrTime = dateToStringFormat.parse(cdrStartTime);
		}
		
		// 判断下记录的有效性
		if (cdrTime == null || Math.abs((currentDataTime.getTime() - cdrTime.getTime())) > timeValidRange) {
//			badWriter.warn("非法文件时间记录.CDR_STARTTIME{}, record:{}", cdrStartTime, this.lineRecord);
			return null;
		}
		
		// 厂家标识
		recordData.put("VENDOR", "ZY0808");

		ParseOutRecord record = new ParseOutRecord();
		record.setType(ParseOutRecord.DEFAULT_DATA_TYPE);
		record.setRecord(recordData);
		
		if (	imsiQueryHelper != null 
				&& cdrTime != null
				&& (mtmsi != null || mmeueS1apid != null)
				&& mmegi != null && mmec != null) {
			
			imsiQueryHelper.submitMatchRecord(record, cdrTime.getTime()/* + zoneTimeOffset*/, 
					LteCoreCommonDataManager.converToLong(mmeueS1apid), 
					LteCoreCommonDataManager.converToLong(mtmsi), 
					LteCoreCommonDataManager.converToInteger(mmegi),
					LteCoreCommonDataManager.converToInteger(mmec));
		}
		
		return record;
	}

	@Override
	public boolean hasNextRecord() throws Exception {
		if (imsiQueryHelper == null)
			return false;
		
		return imsiQueryHelper.hasMatchedRecord();
	}

	/**
	 * 读取一行记录
	 * 
	 * @return
	 */
	private final boolean getLineData() {
		if (this.reader == null) {
			return false;
		}
		try {
			while ((this.lineRecord = this.reader.readLine()) != null) {
				lineRecord = lineRecord.trim();
				if (lineRecord.length() < 1) {
					continue;
				}
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("文件解析异常。文件：{}，异常信息：{}", this.rawFilePath, e.getMessage());
			return false;
		}
		return false;
	}
	
	/**
	 * 字符串拆分
	 * 
	 * @param srcTxt
	 * @param splitChar
	 * @param maxArrayLength
	 * @return 拆分后的数组
	 */
	public static int split(String srcTxt, char splitChar, String[] values) {
		if (srcTxt == null || srcTxt.length()<1)
			return 0;
		
		int i=0;
		int start=0;
		int index = srcTxt.indexOf(splitChar);
		if (index < 0) {
			values[i++] = srcTxt;
			return i;
		}
		
		while (index != -1) {
			if (index > start)
				values[i++] = srcTxt.substring(start, index);
			else
				values[i++] = null;
			
			start = index + 1;
			index = srcTxt.indexOf(splitChar, start);
		}
		values[i++] = srcTxt.substring(start);
		
		return i;
	}

	@Override
	public ParseOutRecord nextRecord() throws Exception {
		ParseOutRecord record = imsiQueryHelper.getMatchedRecord();
		
		readLineNum++;
		return record;
	}

	/**
	 * 数据处理
	 * 
	 * @param recordData
	 */
	private void conversionProcess(Map<String, String> recordData) throws Exception {
		String acMrLtescplrdlqci1 = recordData.get("AC_MR_LTESCPLRDLQCI1");
		if( acMrLtescplrdlqci1 != null 
				&& acMrLtescplrdlqci1.equals("0.00")){
			recordData.put("AC_MR_LTESCPLRDLQCI1", "0");
		}
		
		for(String field:timeFields) {
			processCdrTime(recordData,field);
		}
		
		for(String field:covertNumFields){
			String tempField = recordData.get(field);
			if(StringUtil.isNotEmpty(tempField)){
				if(tempField.toLowerCase().startsWith("0x")){
					recordData.put(field, String.valueOf(Long.parseLong(tempField.substring(2),16)));
				}else{
					recordData.put(field, String.valueOf(Long.parseLong(tempField,16)));
				} 
			}
		}
		conversionNE(recordData,"HO_FIRST_SRCENBID","HO_FIRST_SRCCELLID");
		conversionNE(recordData,"HO_FIRST_TGTENBID","HO_FIRST_TGTCELLID");
		conversionNE(recordData,"HO_LAST_SRCENBID","HO_LAST_SRCCELLID");
		conversionNE(recordData,"HO_LAST_TGTENBID","HO_LAST_TGTCELLID");
		conversionNE(recordData,"RLS_ENBID","RLS_CELLID");
		
		convertDouble6Field(recordData, "RLS_LONGITUDE");
		convertDouble6Field(recordData, "RLS_LATITUDE");
		
		//AC_TAI  RLS_TAI 十六进制转10进制
		conversionTai(recordData);
		
		for(String field:covertD2FFields) {
			convertDouble2Field(recordData, field);
		}
		
		for(String field : acMrLtescplrdlqci){
			convertDouble2Field(recordData, field);
		}
		
		//呼叫与切换部分字段换算
		AssociateCellUtil.conversion_MR_LteScPHR(recordData);
		AssociateCellUtil.conversionFieldPlus140(recordData);
		AssociateCellUtil.conversionFieldPlus20Addhalf(recordData);
		
		for(String field : hoHistoryCellEcgi){
			String temp = recordData.get(field);
			if(StringUtils.isNotEmpty(temp) && temp.length() > 0){
				Long val = Long.parseLong(temp);
				Long a = (val & 0xFFFFFFFL);
				recordData.put(field, String.valueOf(a));
			}
		}
	}
	
	/**
	 * 格式化字段值(6位精度)
	 * @param recordData
	 * @param fieldName
	 */
	private void convertDouble6Field(Map<String, String> recordData, String fieldName) {
		String value = recordData.get(fieldName);
		if (value != null && value.length() > 0) {
			recordData.put(fieldName, d6f.format(Double.parseDouble(value.trim())));
		}
	}

	/**
	 * 格式化字段值(2位精度)
	 * @param recordData
	 * @param fieldName
	 */
	private void convertDouble2Field(Map<String, String> recordData, String fieldName) {
		String value = recordData.get(fieldName);
		 if (value != null && value.length() > 0) {
			recordData.put(fieldName, d2f.format(Double.parseDouble(value.trim())));
		}
	}
	
	private void conversionNE(Map<String, String> recordData,String enbId,String cellId)
	{
		// 转换成16进制，前5位为enodebid，后2位为cellid；
		String cell_Id = recordData.get(cellId);
		if(StringUtil.isNotEmpty(cell_Id))
		{
			Long nObjectID = Long.parseLong(cell_Id);
			Long nEnbid = ((nObjectID >> 8) & 0xFFFFF);
			Long nCellid = (nObjectID & 0xFF);
			recordData.put(enbId, nEnbid == null?"":nEnbid.toString());
			recordData.put(cellId, nCellid == null?"":nCellid.toString());
		}
	}
	
	/**
	 * 解析话单头信息
	 * 
	 * @throws Exception
	 */
	public final boolean parseCdrHead(String headStr) {
		if (headStr == null || "".equals(headStr.trim())) {
			LOGGER.warn("文件消息头解析异常，文件：{}、消息头信息：", new Object[]{this.rawFilePath, headStr});
			return false;
		}
		this.cdrHeadInfo = StringUtil.split(headStr.toUpperCase(), ",");
		this.cdrHeadFieldIndex = null;
		if (this.cdrHeadInfo != null) {
			this.cdrHeadFieldIndex = new String[this.cdrHeadInfo.length];
			int i = 0;
			for (String headName : this.cdrHeadInfo) {
				this.cdrHeadFieldIndex[i++] = this.cdrFieldNames.get(headName);
			}
		}
		
		return true;
	}

	/**
	 * 从文件名获取时间
	 * 	package: FDD-LTE_CDR_ALCATEL_OMCR_266512_20160302000000.csv.gz
	 * 	item:	FDD-LTE_CDR_ALCATEL_OMCR_266512_20160302000000.csv
	 * @param entryFileName
	 * @return
	 */
	public Date parseFileName(String entryFileName) {
		String patternTime = entryFileName.substring(entryFileName.lastIndexOf("-")+1, entryFileName.lastIndexOf("."));
		Date date = null;
		if (patternTime != null) {
			try {
				date = cdrFileTimeFormat.parse(patternTime);
				return date;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return date;
	}
	
	/**
	 * 转换cdr的时间点
	 * @param recordData
	 * @param fieldName
	 */
	public void processCdrTime(Map<String, String> recordData, String fieldName) {
		String value = recordData.get(fieldName);
		if (value == null || "".equals(value.trim()))
			return;
		try {
			// 时间格式为20160510152451.9或20160510152451
            if(value.length() >= 14)
            {
            	value = value.substring(0, 14);
            	Date currentDataTime = cdrTimeFormat.parse(value);
            	recordData.put(fieldName, dateToStringFormat.format(currentDataTime));
            }
			
		} catch (Exception e) {
			LOGGER.error("解析文件时间错误. fileName={} ,timeStr={}", new Object[]{fieldName, value}, e);
		}
	}

	@Override
	public void close() {
		if (this.imsiQueryHelper != null) {
			this.imsiQueryHelper.endParse();
		}
		
		// 标记解析结束时间
		this.endTime = new Date();
		LOGGER.debug("[{}]-华为cdr解析，处理{}条记录", new Object[]{task.getId(), readLineNum});
	}

	/**
	 * 解析模板 获取当前文件对应的Templet
	 * 
	 * @throws Exception
	 */
	public final void parseTemplet() throws Exception {
		// 解析模板
		TempletParser csvTempletParser = new CSVCfcTempletParser();
		csvTempletParser.tempfilepath = templates;
		csvTempletParser.parseTemp();
		Templet templet = csvTempletParser.getTemplets().get("-100");
		this.cdrFieldNames = new HashMap<>();
		for (Field field : templet.fieldList) {
			if (field == null) {
				continue;
			}
			this.cdrFieldNames.put(field.getName(), field.getIndex());
		}
	}
	
	//导频强度由强到若 大到小
	public static class CellStrengthComparator implements Comparator<ONEWAYDELAY_CELL> {
		public static CellStrengthComparator comparatorInstance = new CellStrengthComparator();
		
		@Override
		public int compare(ONEWAYDELAY_CELL o1, ONEWAYDELAY_CELL o2) {
	        BigDecimal firstWeight = new BigDecimal(o1.strength);
	        BigDecimal secondWeight = new BigDecimal(o2.strength);
	        return secondWeight.compareTo(firstWeight);
		}
	}	
	
	/**
	 * 对AC.TAI   RLS.TAI第三个值进行转换，16进制转10进制,值如460|11|0x4734
	 * @param recordData
	 */
	private void conversionTai(Map<String, String> recordData){
		String acTai = recordData.get("AC_TAI");
		String rlsTai = recordData.get("RLS_TAI");
		if(null != acTai){
			String ac[] = acTai.split("\\|");
			if(null != ac && ac.length == 3 && ac[2].toLowerCase().startsWith("0x")){
				Long tac = Long.parseLong(ac[2].substring(2),16);
				acTai = ac[0]+"|"+ac[1]+"|"+tac;
				recordData.put("AC_TAI", acTai);
			}
		}
		if(null != rlsTai){
			String rls[] = rlsTai.split("\\|");
			if(null != rls &&  rls.length == 3 && rls[2].toLowerCase().startsWith("0x")){
				Long rlstac = Long.parseLong(rls[2].substring(2),16);
				rlsTai = rls[0]+"|"+rls[1]+"|"+rlstac;
				recordData.put("RLS_TAI", rlsTai);
			}
		}	
	}

	@Override
	public List<ParseOutRecord> getAllRecords() {
		return null;
	}

	@Override
	public Date getDataTime(ParseOutRecord outRecord) {
		return this.currentDataTime;
	}
	
	public String getDateString(Date date) {
		if (date == null)
			return null;
		
		return dateToStringFormat.format(date);
	}

}

