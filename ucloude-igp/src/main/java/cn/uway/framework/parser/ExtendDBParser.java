package cn.uway.framework.parser;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.accessor.JdbcAccessOutObject;
import cn.uway.framework.parser.database.DatabaseParseTempletParser;
import cn.uway.framework.parser.database.DatabaseParserTemplate;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.StringUtil;
import cn.uway.util.TimeUtil;

public class ExtendDBParser extends AdaptFileExportDBParser{

	private static final ILogger LOGGER = LoggerManager.getLogger(ExtendDBParser.class);
	
	
	@Override
	public void parse(AccessOutObject accessOutObject) throws Exception {
		this.startTime = new Date();
		this.task = accessOutObject.getTask();
		this.currentDataTime = this.task.getDataTime();
		this.parserTemplateList = DatabaseParseTempletParser.parseTemplates(templates);
		this.dateTime = getDateString(task.getDataTime());
		JdbcAccessOutObject outObject = (JdbcAccessOutObject)accessOutObject;
		connection = outObject.getConnection();
		parserDataSourceList = new LinkedList<ParserDataSource>();
		
		for (DatabaseParserTemplate tpl : parserTemplateList) {
			ParserDataSource pds = new ParserDataSource();
			
			pds.tpl_ = tpl;
			pds.sql_ = StringUtil.convertCollectPath(tpl.getSql(), new Date(this.task.getDataTime().getTime()-this.task.getDelayDataTimeDelay()));
			pds.sql_ = initCondition(pds.sql_);
			pds.statement_ = connection.prepareStatement(pds.sql_);
			LOGGER.debug(pds.sql_);
			pds.resultSet_ = pds.statement_.executeQuery();
			pds.metaData_ = pds.resultSet_.getMetaData();
			pds.columnNum_ = pds.metaData_.getColumnCount();
			pds.stroeMapSize_ = getInitialMapSize(pds.columnNum_);
			parserDataSourceList.add(pds);
		}
	}
	

	@Override
	public ParseOutRecord nextRecord() throws Exception {
		this.totalNum++;
		Map<String,String> data = new HashMap<String,String>(stroeMapSize);
		int i = 1;
		try {
			for(; i <= columnNum; i++){
				int columnType = metaData.getColumnType(i);
				// 时间类型
				if (columnType == 91 || columnType == 92 || columnType == 93) {
					Date date = resultSet.getDate(i);
					if(null == date)
					{
						continue;
					}
					data.put(metaData.getColumnName(i), TimeUtil.getDateString(date));
					continue;
				}
				data.put(metaData.getColumnName(i), replace(resultSet.getString(i)));
			}
			if(!data.isEmpty()){
				this.parseSucNum++;
			}
			// 增加OMCID COLLECTTIME STAMPTIME字段
			String omc = data.get("OMC");
			if(StringUtil.isEmpty(omc))
				data.put("OMC", String.valueOf(task.getExtraInfo().getOmcId()));
			data.put("COLLECTTIME", getDateString(new Date()));
			data.put("STAMPTIME", dateTime);
		} catch (ArrayIndexOutOfBoundsException e) {
			//添加驱动包里面索引越界异常捕获，将异常数据丢弃掉。
			LOGGER.warn("数据异常，现在是第{}条记录，第{}列，总列数{}，列名{}", new Object[]{this.totalNum, i, columnNum, metaData.getColumnName(i)});
			return null;
		}
		ParseOutRecord outRecord = new ParseOutRecord();
		outRecord.setType(this.parserTemplate.getDataType());
		outRecord.setRecord(data);
		return outRecord;
	}
	
	private String initCondition(String sql)
	{
		sql = sql.replaceAll("%%VENDOR", task.getExtraInfo().getVendor());
		sql = sql.replaceAll("%%CITY_ID", String.valueOf(task.getExtraInfo().getCityId()));
		return sql;
	}
}
