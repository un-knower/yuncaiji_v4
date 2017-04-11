package cn.uway.framework.parser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.accessor.JdbcAccessOutObject;
import cn.uway.framework.parser.database.DatabaseParseTempletParser;
import cn.uway.framework.parser.database.DatabaseParserTemplate;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.DbUtil;
import cn.uway.util.StringUtil;

/**
 * DbParser 数据库采集通用解析器 解析模版中只需要配置SQL语句。DbParser使用JDBC查询并且组装为对应的数据格式
 * 
 * @author chenrongqiang 2012-12-5
 */
public class DatabaseParser extends AbstractParser{

	/**
	 * 日志
	 */
	protected static final ILogger LOGGER = LoggerManager.getLogger(DatabaseParser.class);
	
	/**
	 * 数据库解析模版
	 */
	protected DatabaseParserTemplate parserTemplate;

	/**
	 * 数据库连接
	 */
	protected Connection connection;

	/**
	 * 结果集
	 */
	protected ResultSet resultSet;

	/**
	 * 采集表元信息
	 */
	protected ResultSetMetaData metaData;

	protected PreparedStatement statement;

	/**
	 * 采集字段数
	 */
	protected int columnNum;

	/**
	 * 初始化map大小 默认为16 根据采集字段数的多少取2的整数倍
	 */
	protected int stroeMapSize = 16;

	protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	protected String dateTime;

	/**
	 * Parser关闭方法 Connection在接入器中有关闭，此处不用关闭
	 */
	public void close(){
		this.endTime = new Date();
		DbUtil.close(resultSet, statement, null);
	}

	/**
	 * 一次解析出所有的数据 暂时不实现
	 */
	public List<ParseOutRecord> getAllRecords(){
		return null;
	}

	/**
	 * 获取真实数据时间 数据库解析一般是周期性任务 可以直接取任务中的数据时间 如果有特殊情况 子类重载该方法
	 */
	public Date getDataTime(ParseOutRecord outRecord){
		return this.task.getDataTime();
	}

	/**
	 * 判断是否还有更多的记录 DbParser直接使用resultSet.next()即可
	 */
	public boolean hasNextRecord() throws Exception{
		try{
			return resultSet.next();
		}catch(Exception e){
			// 发生错误的情况直接终止 JDBC一旦发生错误，则会一直报错
			throw new Exception("JDBC 错误 ", e);
		}
	}

	/**
	 * 获取下一条解析记录 直接从metaData对象读取数据源的列数、并且将所有的数据都以string的形式存储
	 */
	public ParseOutRecord nextRecord() throws Exception{
		this.totalNum++;
		Map<String,String> data = new HashMap<String,String>(stroeMapSize);
		int i = 1;
		try {
			for(; i <= columnNum; i++){
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
		outRecord.setRecord(data);
		return outRecord;
	}

	/**
	 * 替换:',','\n'等关键符号。
	 * 
	 * @param raw value
	 * @return String value after replace
	 * @Date 2013-8-6 下午1:40:26
	 * @author tianjing
	 */
	protected final String replace(String rawValue){
		if(rawValue == null)
			return rawValue;
		return rawValue.replace('\n', ' ').replace(',', ' ');
	}

	/**
	 * Parser初始化 <br>
	 * 1、完成模版解析及转换<br>
	 * 2、数据库的查询
	 */
	public void parse(AccessOutObject accessOutObject) throws Exception{
		this.startTime = new Date();
		this.task = accessOutObject.getTask();
		this.currentDataTime = this.task.getDataTime();
		this.parserTemplate = DatabaseParseTempletParser.parse(templates);
		JdbcAccessOutObject outObject = (JdbcAccessOutObject)accessOutObject;
		connection = outObject.getConnection();
		String sql = StringUtil.convertCollectPath(parserTemplate.getSql(), this.task.getDataTime());
		statement = connection.prepareStatement(sql);
		resultSet = statement.executeQuery();
		metaData = resultSet.getMetaData();
		this.columnNum = metaData.getColumnCount();
		this.stroeMapSize = getInitialMapSize(columnNum);
		this.dateTime = getDateString(task.getDataTime());
	}

	/**
	 * 获取初始化map的大小<br>
	 * 如果columnNum是2的整数倍 则返回columnNum*2,否则大于columnNum的最小的2的整数倍数字
	 * 
	 * @param columnNum
	 * @return 初始化map的大小
	 */
	protected int getInitialMapSize(int columnNum){
		if(columnNum < 16)
			return 16;
		// 数据库字段最多值能有1024个 即2的10次方
		for(int i = 10; i > 3; i--){
			int maxNum = 1 << i;
			if(maxNum <= columnNum){
				return maxNum << 1;
			}
		}
		return 16;
	}

	/**
	 * 将java.util.Date 转换为字符串类型
	 * 
	 * @param date
	 * @return 日期字符串
	 */
	protected String getDateString(Date date){
		if(date == null)
			return "";
		return this.dateFormat.format(date);
	}
}
