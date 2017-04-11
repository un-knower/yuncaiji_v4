package cn.uway.framework.warehouse.exporter;

import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.uway.framework.cache.AbstractCacher;
import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.job.LogCdrInsert;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.status.Status;
import cn.uway.framework.status.dao.StatusDAO;
import cn.uway.framework.task.worker.TaskWorkerFactory;
import cn.uway.framework.warehouse.exporter.template.ColumnTemplateBean;
import cn.uway.framework.warehouse.exporter.template.DatabaseExporterBean;
import cn.uway.framework.warehouse.exporter.template.DbExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.DbTableTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.DbUtil;
import cn.uway.util.TimeUtil;

/**
 * 使用JDBC批量提交的数据库输出器<
 * 
 * @author chenrongqiang 2012-11-8
 * @version 1.0
 * @since 3.0
 */
public class JdbcBatchExporter extends AbstractExporter {

	/**
	 * 日志
	 */
	protected static final ILogger LOGGER = LoggerManager.getLogger(JdbcBatchExporter.class);

	/**
	 * 默认批次条数
	 */
	protected static final int DEFAULT_BATCH_NUM = 1000;

	/**
	 * 每多少条向数据库提交一次。
	 */
	protected int batchNum;

	/**
	 * 记录当前已缓存了多少条。
	 */
	protected int currNum;

	/**
	 * 数据库输出模版
	 */
	protected DbExportTemplateBean dbExportTempletBean;

	/**
	 * 输出字段列表
	 */
	protected List<ColumnTemplateBean> columns;

	/**
	 * 数据库链接 在DBExporter初始化的时候创建 并且在异常或者数据分发完成后关闭
	 */
	protected Connection con;

	protected PreparedStatement statement;

	/**
	 * 执行SQL语句
	 */
	protected String sql;

	/**
	 * 数据库输出类型
	 */
	protected String storageType;

	/**
	 * 输出表名
	 */
	protected String table;

	/**
	 * 状态表操作DAO
	 */
	protected StatusDAO statusDAO = AppContext.getBean("statusDAO", StatusDAO.class);

	/**
	 * 日志表写入开关
	 */
	protected boolean dbLoggerFlag = AppContext.getBean("dbLoggerFlag", Boolean.class);

	/**
	 * 输出总时间
	 */
	protected long totalTime;

	protected SQLException lastSQLException;

	/** Map<列索引，列类型> 对于入库一个表， 有多条记录出错时， 只查询一次表结构获取数据类型， 没有必要每次 **/
	protected Map<Integer, Integer> columnMappingMap = new HashMap<Integer, Integer>();

	/**
	 * 构造函数
	 * 
	 * @param dbExportTempletBean
	 *            数据库输出实体定义
	 * @param exporterArgs
	 *            输出参数
	 */
	public JdbcBatchExporter(DbExportTemplateBean dbExportTempletBean, ExporterArgs exporterArgs) {
		super(exporterArgs, dbExportTempletBean.getId());
		this.exportId = dbExportTempletBean.getId();
		setBreakPoint();
		this.dbExportTempletBean = dbExportTempletBean;
		this.dest = dbExportTempletBean.getTable().getTableName();
		this.exportType = 1;
		this.storageType = dbExportTempletBean.getTable().storageType;
		this.columns = dbExportTempletBean.getTable().getColumnsList();

		if (DbTableTemplateBean.STORAGE_UPDATE_TYPE.equals(dbExportTempletBean.getTable().storageType))
			this.sql = dbExportTempletBean.getTable().sql;
		else
			createInsertSql();
		/** jdk1.6中的switch不支持String型，故改为上面处理方式 */
		// switch (dbExportTempletBean.getTable().storageType) {
		// case DbTableTemplateBean.STORAGE_UPDATE_TYPE :
		// this.sql = dbExportTempletBean.getTable().sql;
		// break;
		// default :
		// createInsertSql();
		// break;
		// }
		initJdbc(exporterArgs);
		LOGGER.debug("Oracle输出器初始化完成:目的地=" + dbExportTempletBean.getTable().getTableName() + ",输出开始断点=" + this.breakPoint);
	}

	/**
	 * 创建数据库链接
	 */
	protected void initJdbc(ExporterArgs exporterArgs) {
		try {
			createCacher(AbstractCacher.BLOCK_CACHER);
			DatabaseExporterBean dbTargetBean = (DatabaseExporterBean) dbExportTempletBean.getExportTargetBean();
			DatabaseConnectionInfo connectionInfo = dbTargetBean.getConnectionInfo();
			Class.forName(connectionInfo.getDriver());
			Connection con = DriverManager.getConnection(connectionInfo.getUrl(), connectionInfo.getUserName(), connectionInfo.getPassword());
			this.batchNum = dbTargetBean.getBatchNum();
			// 如果批量入库条数非法[目前限制最大10000]
			if (batchNum <= 0 || batchNum > 10000)
				batchNum = DEFAULT_BATCH_NUM;
			LOGGER.debug("每次批量入库条数为：{}，exportId：{}", new Object[]{batchNum, dbExportTempletBean.getId()});
			this.con = con;
			this.statement = con.prepareStatement(this.sql);
		} catch (Exception e) {
			exporterInitErrorFlag = true;
			// -1表示初始化失败
			this.errorCode = -1;
			this.cause = e.getMessage();
			LOGGER.error("数据入库时，数据库连接创建失败。", e);
		}
	}

	// 这里入库用的PreparedStatement，即“绑定变量”方式的SQL语句，如果是普通的Statement，
	// 那么对于数据库，每条记录都是一条全新的SQL语句，会非常慢，并且数据库吃不消。
	// 提交方式是批量的，即在本地缓存一定数量的记录，再提交，这样会快很多。
	// 如果采用每条都executeUpdate的方式，那么每条记录都有一次网络IO，数据库也提交一次，
	// 这样非常的慢，并且网络和数据库的负荷也极大。
	// 但批量提交的缺点是一批记录中如果有一条出错，那么这整批记录都会入库失败。
	public void export(Map<String, String> record) throws Exception {
		long start = System.currentTimeMillis();
		this.current++;
		if (this.current <= this.breakPoint) {
			this.fail++;
			return;
		}
		try {
			if (DbTableTemplateBean.STORAGE_UPDATE_TYPE.equals(this.storageType)) {
				Set<String> keySet = this.dbExportTempletBean.getTable().getColumns().keySet();
				for (String indexStr : keySet) {
					ColumnTemplateBean column = this.dbExportTempletBean.getTable().getColumns().get(indexStr);
					String prop = column.getPropertyName();
					String value = record.get(prop);
					if ((value != null && value.equalsIgnoreCase("nan")) || "".equalsIgnoreCase(value))
						value = null;
					// UPDATE暂不支持拼接SQL
					// if(column.getIsSpan() != null && column.getIsSpan().equalsIgnoreCase("true")
					// && column.getDefaultValue() != null){
					// //如果字段的默认值作为sql拼接，则跳过对这个字段的赋值
					// continue;
					// }
					if (column.getFormat() == null) {
						statement.setString(Integer.valueOf(indexStr), value);
						continue;
					}
					if (column.getFormat().equals("blob")) {// 处理大字段blob
						statement.setBlob(Integer.valueOf(indexStr), handleBlob(value));
						continue;
					}
					if (column.getFormat().equals("Timestamp")) {// 处理Timestamp
						statement.setTimestamp(Integer.valueOf(indexStr), new Timestamp(TimeUtil.getDate(value).getTime()));
						continue;
					}
					statement.setString(Integer.valueOf(indexStr), value);
				}
			} else {
				int colNum = this.columns.size();
				int inIndex = 0;
				for (int i = 0; i < colNum; i++) {
					ColumnTemplateBean column = this.columns.get(i);
					// 如果用序列，则跳过
					if(null != column.getSequence()){
						continue;
					}
					String prop = column.getPropertyName();
					String value = record.get(prop);
					if (column.getIsSpan() != null && column.getIsSpan().equalsIgnoreCase("true") && column.getDefaultValue() != null) {
						// 如果字段的默认值作为sql拼接，则跳过对这个字段的赋值
						continue;
					}
					if ((value != null && value.equalsIgnoreCase("nan")) || "".equalsIgnoreCase(value))
						value = null;
					if (value == null && column.getDefaultValue() != null) {
						statement.setString(++inIndex, column.getDefaultValue());
						continue;
					}
					if (column.getFormat() == null) {
						statement.setString(++inIndex, value);
						continue;
					}
					if (column.getFormat().equals("blob")) {// 处理大字段blob
						statement.setBlob(++inIndex, handleBlob(value));
						continue;
					} else {
						// 处理时间字符串中包含T，如'1980-01-06T00:00:19'
						if (value != null && value.indexOf("T") > -1)
							value = value.replace("T", " ");
					}

					if (column.getFormat().equals("Timestamp")  ) {// 处理Timestamp
						if (value != null)
							statement.setTimestamp(++inIndex, new Timestamp(TimeUtil.getDate(value).getTime()));
						else
							statement.setTimestamp(++inIndex, null);
						continue;
					}
					if("null".equalsIgnoreCase(value))
						value = null;
					
					statement.setString(++inIndex, value);
				}
			}
			statement.addBatch();
			this.currNum++;
			if (this.currNum >= batchNum) {
				this.currNum = 0;
				statement.executeBatch();
				DbUtil.close(null, statement, null);
				statement = con.prepareStatement(this.sql);
				this.breakPoint += batchNum;
				this.succ += batchNum;
				if (!TaskWorkerFactory.isLogCltInsert(task.getWorkerType())) {
					Status status = exporterArgs.getObjStatus().get(0);
					status.breakPointProcess(exportId, breakPoint);
					// this.statusDAO.update(objStatus, exporterArgs.getObjStatus().get(0).getId());
					status.updateBySynchronized(statusDAO, exporterArgs.getObjStatus().get(0).getId());
				}
			}
			long end = System.currentTimeMillis();
			totalTime += (end - start);
		} catch (Exception e) {
			if (e instanceof SQLException) {
				this.lastSQLException = (SQLException) e;
			}
			String errSql = buildErrRecordSql(record);
			// 主键冲突
			if (e instanceof SQLException && e.getMessage().indexOf("ORA-00001") >= 0) {
				LOGGER.error("主键冲突：{}", errSql);
				// ++this.fail;
				// --this.succ;
				// return;
			} else {
				LOGGER.error(errSql);
			}
			this.cause = e.getMessage();
			LOGGER.error("数据库输出发生异常,入库线程停止,当前断点=" + breakPoint + ",文件：" + entryNames.get(0) + "，任务：" + task.getId(), e);
			DbUtil.close(null, statement, con);

			throw new Exception("写入数据库失败：" + table, e);
		}
	}

	/**
	 * 组装错误SQL BUFFER;
	 * 
	 * @param record
	 *            记录集
	 * @return sql入库语句
	 */
	protected String buildErrRecordSql(Map<String, String> record) {
		PreparedStatement queryStatement = null;
		ResultSet rs = null;
		try {
			StringBuilder errSqlBuff = new StringBuilder();
			StringBuilder errMsg = null;
			int preQuesTokenPos = 0;
			int currQuesTokenPos = preQuesTokenPos;

			// 当出现错误时， 需要获取表结构的数据类型, 只需要获取一次即可, 方便输出日志记录， 解决问题： 针对主键冲突以及字段类型错误， 照成数据库io比较大
			if (columnMappingMap.size() == 0) {
				String querySql = buildQuerySql();
				queryStatement = con.prepareStatement(querySql);
				rs = queryStatement.executeQuery();
				ResultSetMetaData metaData = rs.getMetaData();
				int cCount = metaData.getColumnCount();
				// 模板里面的列索引 和表结构的列索引建立对应关系
				for (int i = 0; i < cCount; i++) {
					int columnType = metaData.getColumnType(i + 1);
					columnMappingMap.put(i, columnType);
				}
			}

			int colNum = this.columns.size();
			for (int i = 0; i < colNum; i++) {
				currQuesTokenPos = this.sql.indexOf("?", preQuesTokenPos);
				if (currQuesTokenPos < 0)
					break;

				errSqlBuff.append(this.sql.substring(preQuesTokenPos, currQuesTokenPos));
				// 偏移一个"?"号的位置
				preQuesTokenPos = currQuesTokenPos + 1;

				ColumnTemplateBean column = this.columns.get(i);
				String prop = column.getPropertyName();
				String value = record.get(prop);
				if (value != null && value.equalsIgnoreCase("nan"))
					value = null;

				if (value == null || "".equals(value)) {
					errSqlBuff.append("null");
				} else {
					int columnType = columnMappingMap.get(i);

					if (java.sql.Types.CHAR == columnType || java.sql.Types.NCHAR == columnType || java.sql.Types.VARCHAR == columnType
							|| java.sql.Types.NVARCHAR == columnType || java.sql.Types.DATE == columnType || java.sql.Types.TIMESTAMP == columnType) {
						errSqlBuff.append("'").append(value).append("'");
					} else {
						Character invalidChar = null;
						value = value.trim();

						if (java.sql.Types.BIGINT == columnType || java.sql.Types.DOUBLE == columnType || java.sql.Types.FLOAT == columnType
								|| java.sql.Types.DECIMAL == columnType || java.sql.Types.INTEGER == columnType
								|| java.sql.Types.NUMERIC == columnType) {
							for (int charIndex = 0; charIndex < value.length(); ++charIndex) {
								char c = value.charAt(charIndex);

								if (!((c == '-' && charIndex == 0) || ((c == '.' || c == 'E' || c == 'e') && charIndex > 0) || (c >= '0' && c <= '9'))) {
									invalidChar = c;
									break;
								}
							}
						}

						if (invalidChar != null) {
							if (errMsg == null) {
								errMsg = new StringBuilder();
								errMsg.append("TABLE:").append(this.dest).append(" 值类型与数据库类型不一致，数据库为数字类型，但值中包含非数字字符.\n");
							}

							errMsg.append("column: ").append(column.getColumnName()).append(" value:").append(value).append(" invalid char: [")
									.append(invalidChar.toString()).append("] \n");
						}

						errSqlBuff.append(value);
					}
				}
			}

			if (preQuesTokenPos < this.sql.length())
				errSqlBuff.append(this.sql.substring(preQuesTokenPos, this.sql.length()));

			if (errMsg != null)
				return errMsg.toString();

			return errSqlBuff.toString();
		} catch (SQLException e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append("\n");
			sb.append("sql语句：").append(this.sql).append("\n 数据行：");
			int colNum = this.columns.size();
			for (int i = 0; i < colNum; i++) {
				ColumnTemplateBean column = this.columns.get(i);
				String prop = column.getPropertyName();
				String value = record.get(prop);

				if (value != null && value.equalsIgnoreCase("nan"))
					value = null;
				sb.append((value != null ? "'" + value + "'" : "null")).append(",");
			}

			return sb.toString();
		} finally {
			DbUtil.close(rs, queryStatement, null);
		}

	}

	@Override
	public void close() {
		long start = System.currentTimeMillis();
		try {
			// 可能还有未提交的内容，最后要提交一下。
			/**
			 * <pre>
			 *  dissable:shig date:2014-5-22 
			 *  explain:statement.isClosed()调用会因为找不到方法，报错，
			 *  现改为，close过后，将statement置为null，通过检查statement是否为null，来替代调用isClosed()方法
			 * </pre>
			 */
			if (statement != null /* && !statement.isClosed() */&& this.total > 0)
				statement.executeBatch();
			this.succ += this.currNum;
			this.breakPoint += this.currNum;
			if (!TaskWorkerFactory.isLogCltInsert(task.getWorkerType())) {
				Status status = exporterArgs.getObjStatus().get(0);
				status.breakPointProcess(exportId, breakPoint);
				// this.statusDAO.update(status, exporterArgs.getObjStatus().get(0).getId());
				status.updateBySynchronized(statusDAO, exporterArgs.getObjStatus().get(0).getId());
			}
		} catch (Exception e) {
			this.fail += this.currNum;
			try {
				// 未关闭自动事务时，批量入库遇到无法插入的语句时，之前成功的还是会入库，通过getUpdateCount()可获取成功条数。
				this.succ += this.statement.getUpdateCount();
			} catch (SQLException ex) {
			}
			LOGGER.error(this.table + "  , endExport导入数据库失败", e);
		} finally {
			DbUtil.close(null, statement, con);
			statement = null;
			endTime = new Date();

			// 优先根据config.ini中的system.logcltinsert.flag进行判断，如果开启（1）， 添加日志入库操作 log_clt_insert ,数据库汇总需要依赖此表,
			// 话单入 log_cdr_insert, 则需要将 system.logcltinsert.flag=0 关闭.
			if (isLogCltInsertFlag()) {
				logCltInsert();
			} else {
				if (this.dbLoggerFlag) {
					writeDbLog();
				}
			}

			long end = System.currentTimeMillis();
			totalTime += (end - start);
			LOGGER.debug("【入库时间统计】{}表入库耗时{}秒，入库成功{}条数据，jdbc入库失败{}条数据，{}任务，原始文件：{}，CITY：{}，OMC：{}，BSC：{}，VENDOR：{}", new Object[]{this.table,
					totalTime / 1000.00, this.succ, this.fail, task.getId(), (this.exporterArgs != null ? entryNames.get(0) : ""),
					task.getExtraInfo().getCityId(), task.getExtraInfo().getOmcId(), task.getExtraInfo().getBscId(), task.getExtraInfo().getVendor()});
		afterClose();
		}
	}
	
	// 满足子类扩展需求：入库后再做一点额外业务
	protected void afterClose(){}

	private void writeDbLog() {
		if (!TaskWorkerFactory.isLogCltInsert(task.getWorkerType())) {
			LogCdrInsert.getInstance().insert(task.getExtraInfo().getCityId(), task.getExtraInfo().getOmcId(), task.getExtraInfo().getBscId(), table,
					exporterArgs.getDataTime(), startTime, endTime, succ, fail, total, task.getExtraInfo().getVendor(), entryNames.get(0));
		}
	}

	@Override
	public void endExportOnException() {
		close();
	}

	@Override
	public void export(BlockData blockData) throws Exception {
		List<ParseOutRecord> outData = blockData.getData();
		if (outData == null || outData.isEmpty())
			return;
		for (ParseOutRecord outRecord : outData) {
			Map<String, String> record = outRecord.getRecord();
			export(record);
		}
	}

	/**
	 * 初始化输出SQL
	 * 
	 * @return
	 */
	protected void createInsertSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ").append(dbExportTempletBean.getTable().getTableName()).append(" (");
		this.table = dbExportTempletBean.getTable().getTableName();

		DatabaseExporterBean dbTargetBean = (DatabaseExporterBean) dbExportTempletBean.getExportTargetBean();
		DatabaseConnectionInfo connectionInfo = dbTargetBean.getConnectionInfo();
		String jdbcDriver = connectionInfo.getDriver();

		boolean isMySqlDB = false;
		if (jdbcDriver.equalsIgnoreCase("com.mysql.jdbc.Driver")) {
			isMySqlDB = true;
		}

		StringBuilder sbValue = new StringBuilder();
		int colNum = this.columns.size();
		for (int i = 0; i < colNum; i++) {
			ColumnTemplateBean column = this.columns.get(i);

			sb.append(column.getColumnName());
			if (i < colNum - 1)
				sb.append(",");

			String fmt = column.getFormat();
			if (fmt != null && !fmt.trim().isEmpty() && !fmt.equals("blob")) {
				if (isMySqlDB) {
					sbValue.append("?");
					// sbValue.append("'").append(fmt).append("'");
				} else {
					sbValue.append("to_date(?,'").append(fmt).append("')");
				}
			} else if(null != column.getSequence()){
				sbValue.append(column.getSequence()+".nextval");
			}else{
				sbValue.append("?");
			}

			if (i < colNum - 1)
				sbValue.append(",");
		}
		sb.append(") values (").append(sbValue.toString()).append(")");
		this.sql = sb.toString();
	}

	/**
	 * 初始化输出SQL
	 * 
	 * @return
	 */
	protected String buildQuerySql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");

		int colNum = this.columns.size();
		for (int i = 0; i < colNum; i++) {
			ColumnTemplateBean column = this.columns.get(i);

			sb.append(column.getColumnName());
			if (i < colNum - 1)
				sb.append(",");
		}
		sb.append(" from ").append(dbExportTempletBean.getTable().getTableName());
		// sb.append(" where rownum < 1");
		sb.append(" where 1=0");

		return sb.toString();
	}

	/**
	 * 处理blob字段
	 * 
	 * @param value
	 * @return
	 */
	protected Blob handleBlob(String value) {
		Blob blob = null;
		try {
			blob = this.con.createBlob();
			OutputStream out = blob.setBinaryStream(1);
			out.write(value.getBytes());
			out.flush();
			out.close();
		} catch (Exception e) {
			LOGGER.error("blob字段处理异常", e);
		}
		return blob;
	}

	@Override
	public void buildExportPropertysList(Set<String> propertysSet) {
		int colNum = this.columns.size();
		for (int i = 0; i < colNum; i++) {
			ColumnTemplateBean column = this.columns.get(i);
			String prop = column.getPropertyName();
			propertysSet.add(prop);
		}
	}

	public SQLException getLastSQLException() {
		return this.lastSQLException;
	}

}
