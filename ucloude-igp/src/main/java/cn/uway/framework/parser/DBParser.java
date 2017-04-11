package cn.uway.framework.parser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.accessor.JdbcAccessOutObject;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.parser.database.DBParseTempletParser;
import cn.uway.framework.parser.database.DatabaseParserTemplate;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.ReTask;
import cn.uway.framework.task.Task;
import cn.uway.framework.task.dao.TaskDAO;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.DbUtil;
import cn.uway.util.StringUtil;
import cn.uway.util.TimeUtil;

/**
 * DBParser 数据库接口解码器
 * 
 * @author yuy 2014-5-6
 */
public class DBParser extends DatabaseParser {

	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(DBParser.class);

	/**
	 * 数据库解析模版
	 */
	protected List<DatabaseParserTemplate> parserTemplatesList;

	/**
	 * 模板List的索引
	 */
	protected int templetIndex = 0;

	/**
	 * 模板id
	 */
	protected long templetId;

	/**
	 * 模板ids
	 */
	protected StringBuilder templetIds;

	/**
	 * 数据类型，同模板中dataType
	 */
	protected Integer dataType;

	/**
	 * 单个表的记录数
	 */
	protected int singleTable_recordsNum = 0;

	/**
	 * 采集失败原因
	 */
	protected String cause;

	/**
	 * 分隔符
	 */
	protected static final String SPLITSIGN = ",";

	/**
	 * Parser初始化 <br>
	 * 1、完成模版解析及转换<br>
	 * 2、数据库的查询
	 */
	public void parse(AccessOutObject accessOutObject) throws Exception {
		this.startTime = new Date();
		this.task = accessOutObject.getTask();
		this.currentDataTime = this.task.getDataTime();

		this.parserTemplatesList = DBParseTempletParser.parse(templates);

		// 补采任务处理
		if (task instanceof ReTask)
			reTaskHandle(accessOutObject);

		this.dateTime = getDateString(currentDataTime);
		JdbcAccessOutObject outObject = (JdbcAccessOutObject) accessOutObject;
		connection = outObject.getConnection();
	}

	/**
	 * 判断是否还有更多的记录 DbParser直接使用resultSet.next()即可
	 */
	public boolean hasNextRecord() throws Exception {
		try {
			// 第一次开始查询
			if (templetIndex == 0 && singleTable_recordsNum == 0) {
				if (!getNextQuery())
					return false;
			}
			if (hasNext())
				return true;
			// 当没有数据，加入补采标记
			if (singleTable_recordsNum == 0)
				rememberTempletIds();
			// 获取查询实例
			if (!getNextQuery())
				return false;
			return hasNextRecord();
		} catch (Exception e) {
			// 发生错误的情况直接终止 JDBC一旦发生错误，则会一直报错
			throw new Exception("JDBC 错误 ", e);
		}
	}

	/**
	 * 获取下一条解析记录 直接从metaData对象读取数据源的列数、并且将所有的数据都以string的形式存储
	 */
	public ParseOutRecord nextRecord() throws Exception {
		this.totalNum++;
		this.singleTable_recordsNum++;
		// Map<String,String> data = new HashMap<String,String>(stroeMapSize);
		Map<String, String> data = this.createExportPropertyMap(dataType);
		for (int i = 1; i <= columnNum; i++) {
			int columnType = metaData.getColumnType(i);
			// 时间类型
			if (columnType == 91 || columnType == 92 || columnType == 93) {
				Date date = resultSet.getDate(i);
				data.put(metaData.getColumnName(i), TimeUtil.getDateString(date));
				continue;
			}
			// TO_DO 支持其他类型
			data.put(metaData.getColumnName(i), replace(resultSet.getString(i)));
		}
		if (!data.isEmpty()) {
			this.parseSucNum++;
		}
		// 增加OMCID COLLECTTIME STAMPTIME字段
		data.put("MMEID", String.valueOf(task.getExtraInfo().getOmcId()));
		data.put("COLLECTTIME", getDateString(new Date()));
		data.put("STAMPTIME", dateTime);
		ParseOutRecord outRecord = new ParseOutRecord();
		outRecord.setRecord(data);
		outRecord.setType(dataType);
		return outRecord;
	}

	/**
	 * Parser关闭方法 Connection在接入器中有关闭，此处不用关闭
	 */
	public void close() {
		this.endTime = new Date();
		DbUtil.close(resultSet, statement, null);
		// 补采处理
		regatherHandle(task);
	}

	/**
	 * 获取查询实例
	 * 
	 * @return true or false
	 * @throws SQLException
	 */
	public boolean getNextQuery() throws SQLException {
		if (templetIndex >= parserTemplatesList.size())
			return false;
		// 验证表是否存在，不存在，则跳过
		while (!isTableExists(parserTemplatesList.get(templetIndex).getSql())) {
			templetIndex++;
			if (templetIndex >= parserTemplatesList.size())
				return false;
		}
		// 初始化查询
		initQuery();
		return true;
	}

	/**
	 * 补采任务处理，主要处理要补采的内容，即重组parserTemplatesList
	 * 
	 * @param accessOutObject
	 */
	public void reTaskHandle(AccessOutObject accessOutObject) {
		ReTask reTask = (ReTask) task;
		this.currentDataTime = reTask.getRegather_datetime();
		String path = reTask.getRegatherPath();
		// 只补采需要的。如果两者相等，默认全补
		if (path != null && !path.equals(accessOutObject.getRawAccessName())) {
			String[] templetIds = StringUtil.split(path, SPLITSIGN);
			List<DatabaseParserTemplate> list = new ArrayList<DatabaseParserTemplate>();
			Map<Long, DatabaseParserTemplate> map = swithToHashMap();
			for (String templetId : templetIds) {
				long id = Long.parseLong(templetId);
				list.add(map.get(id));
			}
			this.parserTemplatesList = list;
			map = null;
		}
	}

	/**
	 * 把list转换成map，便于处理
	 * 
	 * @return Map<Long, DatabaseParserTemplate>
	 */
	public Map<Long, DatabaseParserTemplate> swithToHashMap() {
		Map<Long, DatabaseParserTemplate> map = new HashMap<Long, DatabaseParserTemplate>(parserTemplatesList.size());
		for (DatabaseParserTemplate template : parserTemplatesList) {
			map.put(template.getId(), template);
		}
		return map;
	}

	/**
	 * 加入补采，先判断是否满足补采条件，更新/插入补采记录
	 */
	public void regatherHandle(Task task) {
		if (task == null || !(task instanceof PeriodTask))
			return;
		PeriodTask periodTask = (PeriodTask) task;
		// 最大补采次数等于-1时，不需要补采
		if (periodTask.getMaxGatherTime() == -1)
			return;
		boolean isNeedReCollect = templetIds != null && templetIds.length() > 0;
		TaskDAO taskDao = AppContext.getBean("taskDAO", TaskDAO.class);
		// 补采任务
		if (periodTask instanceof ReTask) {
			ReTask reTask = (ReTask) periodTask;
			reTask.setTimes(reTask.getTimes() + 1);
			// 补采成功
			if (!isNeedReCollect) {
				reTask.setStatus(ReTask.SUCCESS_COLLECT_STATUS);
				reTask.setSuccessDate(new Date());
				// 更新补采次数，状态和成功时间
				taskDao.updateRTaskRecords(reTask);
				if (reTask.getTimes() == reTask.getMaxGatherTime())
					LOGGER.debug("补采任务{},任务名称{},补采时间点{},已经达到最大采集次数{}次，下次不再采集",
							new Object[]{reTask.getId(), reTask.getName(), TimeUtil.getDateString(reTask.getRegather_datetime()), reTask.getTimes()});
				return;
			}
			// 补采失败（有一个表补采失败，都算作失败）
			reTask.setRegatherPath(getRegatherPath(templetIds.toString()));
			// 更新补采次数和路径（templetIds）
			taskDao.updateRTaskRecords(reTask);
			return;
		}

		// 不需要补采
		if (!isNeedReCollect)
			return;

		// 第一次补采，插入新的补采记录
		String paths = getRegatherPath(templetIds.toString());
		if (cause == null)
			cause = "从厂家库中select出来的记录数为0";
		LOGGER.debug("本次需要补采的templetId(解析模板Id)：{}，补采原因：{}", new Object[]{paths, cause});
		taskDao.insertIntoRTaskRecords(periodTask, paths, cause);
	}

	/**
	 * @return regatherPath
	 */
	public static String getRegatherPath(String templetIds) {
		if (templetIds.endsWith(SPLITSIGN))
			return templetIds.substring(0, templetIds.length() - 1);
		return templetIds;
	}

	/**
	 * 记录templetsId
	 */
	public void rememberTempletIds() {
		if (templetIds == null)
			templetIds = new StringBuilder();
		templetIds.append(templetId).append(SPLITSIGN);
	}

	/**
	 * @return true or false : dose have next record
	 * @throws SQLException
	 */
	public boolean hasNext() throws SQLException {
		return resultSet.next();
	}

	/**
	 * 初始化查询
	 * 
	 * @return true or false,means success is true,fail is false
	 * @throws SQLException
	 */
	public void initQuery() throws SQLException {
		// 如果不为空，先释放资源，便于垃圾回收
		DbUtil.close(resultSet, statement, null);
		DatabaseParserTemplate template = parserTemplatesList.get(templetIndex);
		String sql = StringUtil.convertCollectPath(template.getSql(), currentDataTime);
		statement = connection.prepareStatement(sql);
		resultSet = statement.executeQuery();
		metaData = resultSet.getMetaData();
		this.columnNum = metaData.getColumnCount();
		this.templetIndex++;
		this.singleTable_recordsNum = 0;
		this.templetId = template.getId();
		// 设置dataType
		setDataType(template.getDataType());
	}

	/**
	 * 验证表是否存在
	 * 
	 * @param sql
	 * @return true or false
	 * @throws SQLException
	 */
	public boolean isTableExists(String sql) throws SQLException {
		sql = sql.toUpperCase();
		int wherePos = sql.indexOf("WHERE");
		String tableNames = sql.substring(sql.indexOf("FROM") + 4, wherePos > -1 ? wherePos : sql.length()).trim();
		String[] tableNamesArray = StringUtil.split(tableNames, ",");
		for (String tableName : tableNamesArray) {
			tableName = tableName.trim();
			int index = 0;
			if ((index = tableName.indexOf(" ")) > -1)
				tableName = tableName.substring(0, index).trim();
			if (!DbUtil.tableExists(connection, tableName, task.getId())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 设置当前dataType
	 * 
	 * @param dataType
	 */
	public void setDataType(Integer dataType) {
		if (dataType == null || dataType == 0) {
			this.dataType = ParseOutRecord.DEFAULT_DATA_TYPE;
			return;
		}
		this.dataType = dataType;
	}

	/**
	 * @return templetIds
	 */
	public StringBuilder getTempletIds() {
		return templetIds;
	}

	/**
	 * @param templetIds
	 */
	public void setTempletIds(StringBuilder templetIds) {
		this.templetIds = templetIds;
	}

	/**
	 * @return cause
	 */
	public String getCause() {
		return cause;
	}

	/**
	 * @param cause
	 */
	public void setCause(String cause) {
		this.cause = cause;
	}

}
