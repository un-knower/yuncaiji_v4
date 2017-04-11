package cn.uway.framework.warehouse.exporter;

import java.io.OutputStream;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.uway.framework.cache.AbstractCacher;
import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.framework.log.BadWriter;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.warehouse.exporter.template.ColumnTemplateBean;
import cn.uway.framework.warehouse.exporter.template.DatabaseExporterBean;
import cn.uway.framework.warehouse.exporter.template.DbExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.DbUtil;
import cn.uway.util.TimeUtil;

/**
 * 不记录状态表的JDBC入库，用于IGP1中迁移过来的Oracle入库，不涉及状态表的操作，并且主要特点是支持入库报错后的恢复。 批量insert操作，调用
 * {@link PreparedStatement#executeBatch()}方法出错后，调用对应连接的
 * {@link Connection#commit()}方法，然后再调用{@link PreparedStatement#getUpdateCount()}
 * 方法，可得到入库成功的条数，这个数字加1，表示出错的那一条insert语句，然后再从下一条开始入库，重复这一过程，以便将所有可入库的数据入库。
 * 
 * @author chensijiang 2014-9-24
 */
public class NoStatusJdbcBatchExport extends AbstractExporter{

	/**
	 * 日志
	 */
	protected static final ILogger LOG = LoggerManager.getLogger(NoStatusJdbcBatchExport.class);

	/**
	 * 错误记录日志
	 */
	protected static final ILogger badWriter = BadWriter.getInstance().getBadWriter();

	/**
	 * 默认批次条数
	 */
	protected static final int DEFAULT_BATCH_NUM = 100;

	/**
	 * 每多少条向数据库提交一次。
	 */
	protected int batchNum;

	protected int batchNumBackup;

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

	protected List<ParseOutRecord> cachedLastBatchRecords;

	/**
	 * 输出总时间
	 */
	protected long totalTime;

	protected SQLException lastSQLException;

	protected DatabaseConnectionInfo connectionInfo;

	/** 一个表入库时的最大出错次数，达到此次数后放弃入库，避免无限的重试。 */
	private static final int MAX_ERROR_COUNT_OF_ONE_TABLE = Integer.parseInt(System.getProperty("20", "20"));

	private int errorCountOfOneTable;

	boolean isMySqlDB = false;

	/**
	 * 构造函数
	 * 
	 * @param dbExportTempletBean 数据库输出实体定义
	 * @param exporterArgs 输出参数
	 */
	public NoStatusJdbcBatchExport(DbExportTemplateBean dbExportTempletBean, ExporterArgs exporterArgs){
		super(exporterArgs, dbExportTempletBean.getId());
		this.exportId = dbExportTempletBean.getId();
		this.dbExportTempletBean = dbExportTempletBean;
		this.dest = dbExportTempletBean.getTable().getTableName();
		this.exportType = 1;
		this.columns = dbExportTempletBean.getTable().getColumnsList();
		createInsertSql();
		initJdbc(exporterArgs);
		this.cachedLastBatchRecords = new ArrayList<>();
		LOG.debug("Oracle输出器初始化完成:目的地=" + dbExportTempletBean.getTable().getTableName() + ",输出开始断点=" + this.breakPoint);
	}

	/**
	 * 创建数据库链接
	 */
	protected void initJdbc(ExporterArgs exporterArgs){
		try{
			createCacher(AbstractCacher.BLOCK_CACHER);
			DatabaseExporterBean dbTargetBean = (DatabaseExporterBean)dbExportTempletBean.getExportTargetBean();
			connectionInfo = dbTargetBean.getConnectionInfo();
			Class.forName(connectionInfo.getDriver());
			Connection con = DriverManager.getConnection(connectionInfo.getUrl(), connectionInfo.getUserName(),
					connectionInfo.getPassword());
			this.batchNum = dbTargetBean.getBatchNum();
			if(connectionInfo.getDriver().indexOf("mysql") > -1){
				isMySqlDB = true;
			}
			// 如果批量入库条数非法[目前限制最大10000]
			if(batchNum <= 0 || batchNum > 10000)
				batchNum = DEFAULT_BATCH_NUM;
			// batchNum = 10;
			LOG.debug("每次批量入库条数为：{}，exportId：{}", new Object[]{batchNum,dbExportTempletBean.getId()});
			this.con = con;
			this.statement = con.prepareStatement(this.sql);
		}catch(Exception e){
			exporterInitErrorFlag = true;
			// -1表示初始化失败
			this.errorCode = -1;
			this.cause = e.getMessage();
			LOG.error("数据入库时，数据库连接创建失败。", e);
		}
	}

	// 这里入库用的PreparedStatement，即“绑定变量”方式的SQL语句，如果是普通的Statement，
	// 那么对于数据库，每条记录都是一条全新的SQL语句，会非常慢，并且数据库吃不消。
	// 提交方式是批量的，即在本地缓存一定数量的记录，再提交，这样会快很多。
	// 如果采用每条都executeUpdate的方式，那么每条记录都有一次网络IO，数据库也提交一次，
	// 这样非常的慢，并且网络和数据库的负荷也极大。
	// 但批量提交的缺点是一批记录中如果有一条出错，那么这整批记录都会入库失败。
	public void export(ParseOutRecord record, boolean last) throws Exception{
		long start = System.currentTimeMillis();
		this.current++;
		int colNum = this.columns.size();
		int inIndex = 0;
		Map<String,String> map = record.getRecord();
		for(int i = 0; i < colNum; i++){
			ColumnTemplateBean column = this.columns.get(i);
			String prop = column.getPropertyName();
			String value = map.get(prop);
			if(column.getIsSpan() != null && column.getIsSpan().equalsIgnoreCase("true")
					&& column.getDefaultValue() != null){
				// 如果字段的默认值作为sql拼接，则跳过对这个字段的赋值
				continue;
			}
			if(value != null && value.equalsIgnoreCase("nan"))
				value = null;
			if(value == null && column.getDefaultValue() != null){
				statement.setString(++ inIndex, column.getDefaultValue());
				continue;
			}
			if(column.getFormat() == null){
				statement.setString(++ inIndex, value);
				continue;
			}
			if(column.getFormat().equals("blob")){// 处理大字段blob
				statement.setBlob(++ inIndex, handleBlob(value));
				continue;
			}else{
				// 处理时间字符串中包含T，如'1980-01-06T00:00:19'
				if(value != null && value.indexOf("T") > -1)
					value = value.replace("T", " ");
			}

			if(column.getFormat().equals("Timestamp")){// 处理Timestamp
				statement.setTimestamp(++ inIndex, new Timestamp(TimeUtil.getDate(value).getTime()));
				continue;
			}
			statement.setString(++ inIndex, value);
		}
		statement.addBatch();
		if(!last)
			this.cachedLastBatchRecords.add(record);
		this.currNum++;
		if(this.currNum >= batchNum && !last){
			statement.executeBatch();
			if(!last){
				recoverCount = 0;
				if(batchNumBackup > 0)
					batchNum = batchNumBackup;
			}
			succ += this.currNum;
			if(!last)
				this.cachedLastBatchRecords.clear();
			this.currNum = 0;
			DbUtil.close(null, statement, null);
			statement = con.prepareStatement(this.sql);
		}
		long end = System.currentTimeMillis();
		totalTime += (end - start);
	}

	private void commitLastBatch(){
		try{
			currNum = 0;
			current = 0;
			batchNum = this.cachedLastBatchRecords.size();
			this.__exportLast(this.cachedLastBatchRecords, 0, this.cachedLastBatchRecords.size() - 1);
		}catch(Exception ex){
			LOG.warn("提交最后一批INSERT时出错。", ex);
		}finally{
			this.cachedLastBatchRecords.clear();
		}
	}

	@Override
	public void close(){
		long start = System.currentTimeMillis();
		commitLastBatch();
		DbUtil.close(null, statement, con);
		long end = System.currentTimeMillis();
		totalTime += (end - start);
		LOG.debug("【入库时间统计】{}表入库耗时{}秒，入库成功{}条数据，no入库失败{}条数据，{}任务，原始文件：{}，CITY：{}，OMC：{}，BSC：{}，VENDOR：{}", new Object[]{this.table,
				totalTime / 1000.00,this.succ,this.fail,task.getId(),(this.exporterArgs != null ? entryNames.get(0) : ""),
				task.getExtraInfo().getCityId(),task.getExtraInfo().getOmcId(),task.getExtraInfo().getBscId(),
				task.getExtraInfo().getVendor()});

	}

	@Override
	public void endExportOnException(){
		close();
	}

	@Override
	public void export(BlockData blockData) throws Exception{
		List<ParseOutRecord> outData = blockData.getData();
		if(outData == null || outData.isEmpty())
			return;

		try{
			this.__export(outData, 0, outData.size() - 1);
		}catch(Exception ex){
			LOG.error("入库出错，表：{}", this.table);
			if(ex instanceof SQLException){
				SQLException sqle = (SQLException)ex;
				if(isFatalException(sqle)){
					LOG.warn("出现了不可恢复的入库异常，入库中止。", sqle);
					this.lastSQLException = sqle;
					return;
				}
			}
			throw ex;
		}
	}

	private void __exportLast(List<ParseOutRecord> datas, int startIndex, int maxIndex) throws BatchUpdateException,
			Exception{
		try{
			//重复提交内容，注释掉
			//			for (int i = startIndex; i <= maxIndex; i++) {
			//				ParseOutRecord outRecord = datas.get(i);
			//				export(outRecord, true);
			//			}
			statement.executeBatch();
			succ += (maxIndex - startIndex + 1);
		}catch(BatchUpdateException ex){
			SQLException sqle = (SQLException)ex;
			if(isFatalException(sqle)){
				LOG.warn("出现了不可恢复的入库异常，入库中止。", sqle);
				this.lastSQLException = sqle;
				return;
			}
			int count = this.handleBatchUpdateException(ex);
			DbUtil.close(null, statement, con);
			con = DriverManager.getConnection(connectionInfo.getUrl(), connectionInfo.getUserName(),
					connectionInfo.getPassword());
			statement = con.prepareStatement(sql);
			if(count != -1){
				succ += count;
				// +1，从下一条开始，不加1的话，实际上就是从导致出错的那条开始入库了。
				int nextIndex = startIndex + count + 1;
				if(nextIndex > maxIndex){
					// 已入完。
					return;
				}
				errorCountOfOneTable++;
				if(errorCountOfOneTable >= MAX_ERROR_COUNT_OF_ONE_TABLE){
					LOG.warn("{}的入库出错次数已达到{}次，停止入库。", new Object[]{table,MAX_ERROR_COUNT_OF_ONE_TABLE});
					this.lastSQLException = sqle;
					return;
				}
				LOG.warn("{} - 批量入库时出错，错误是：{}，本次批量入库是从第{}条开始的，出错时已入库{}条，第{}条数据导致出错，现在从第{}条开始继续尝试入库。", new Object[]{
						table,ex.getMessage().trim(),startIndex + 1,count,count + 1,nextIndex + 1});

				this.__exportLast(datas, nextIndex, maxIndex);
			}else{
				throw ex;
			}
		}
	}

	int recoverCount = 0;

	private void __export(List<ParseOutRecord> datas, int startIndex, int maxIndex) throws BatchUpdateException,
			Exception{
		for(int i = startIndex; i <= maxIndex; i++){
			ParseOutRecord outRecord = datas.get(i);
			try{
				export(outRecord, false);
			}catch(BatchUpdateException ex){
				SQLException sqle = (SQLException)ex;
				if(isFatalException(sqle)){
					LOG.warn("出现了不可恢复的入库异常，入库中止。", sqle);
					this.lastSQLException = sqle;
					return;
				}
				int count = this.handleBatchUpdateException(ex);
				DbUtil.close(null, statement, con);
				con = DriverManager.getConnection(connectionInfo.getUrl(), connectionInfo.getUserName(),
						connectionInfo.getPassword());
				statement = con.prepareStatement(sql);
				if(count != -1){
					succ += count;
					recoverCount += count;
					// +1，从下一条开始，不加1的话，实际上就是从导致出错的那条开始入库了。
					int nextIndex = startIndex + count + 1;
					if(nextIndex > maxIndex){
						// 已入完。
						return;
					}
					errorCountOfOneTable++;
					if(errorCountOfOneTable >= MAX_ERROR_COUNT_OF_ONE_TABLE){
						LOG.warn("{}的入库出错次数已达到{}次，停止入库。", new Object[]{table,MAX_ERROR_COUNT_OF_ONE_TABLE});
						this.lastSQLException = sqle;
						return;
					}
					LOG.warn("{} - 批量入库时出错，错误是：{}，本次批量入库是从第{}条开始的，出错时已入库{}条，第{}条数据导致出错，现在从第{}条开始继续尝试入库。", new Object[]{
							table,ex.getMessage().trim(),startIndex + 1,count,count + 1,nextIndex + 1});

					// badWriter日志输出
					badWriterOutput(outRecord, ex);

					// 批量入库的门限值设为剩下所要入库的所有数据的条数，即出错的那一条的下一条开始，到最后。
					// 如果不这样的话，初始化时指定的批量入库条数，可能太大，导致不提交。
					if(batchNumBackup == 0)
						batchNumBackup = batchNum;
					batchNum = (maxIndex - recoverCount);
					current = 0;
					currNum = 0;
					this.__export(datas, nextIndex, maxIndex);
				}else{
					throw ex;
				}
			}
		}
	}

	/**
	 * 初始化输出SQL
	 * 
	 * @return
	 */
	protected void createInsertSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ").append(dbExportTempletBean.getTable().getTableName()).append(" (");
		this.table = dbExportTempletBean.getTable().getTableName();

		StringBuilder sbValue = new StringBuilder();
		int colNum = this.columns.size();
		for(int i = 0; i < colNum; i++){
			ColumnTemplateBean column = this.columns.get(i);

			sb.append(column.getColumnName());
			if(i < colNum - 1)
				sb.append(",");

			String fmt = column.getFormat();
			String isSpan = column.getIsSpan();
			String defaultValue = column.getDefaultValue();
			if(isSpan != null && isSpan.equalsIgnoreCase("true") && defaultValue != null){
				sbValue.append(defaultValue);
			}else if(fmt != null && !fmt.trim().isEmpty() && !fmt.equals("blob"))
				sbValue.append("to_date(?,'").append(fmt).append("')");
			else
				sbValue.append("?");

			if(i < colNum - 1)
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
	protected String buildQuerySql(){
		StringBuilder sb = new StringBuilder();
		sb.append("select ");

		int colNum = this.columns.size();
		for(int i = 0; i < colNum; i++){
			ColumnTemplateBean column = this.columns.get(i);

			sb.append(column.getColumnName());
			if(i < colNum - 1)
				sb.append(",");
		}
		sb.append(" from ").append(dbExportTempletBean.getTable().getTableName());
		sb.append(" where rownum < 1");

		return sb.toString();
	}

	/**
	 * badWriter日志输出
	 * 
	 * @param outRecord
	 * @param ex
	 */
	public void badWriterOutput(ParseOutRecord outRecord, BatchUpdateException ex){
		StringBuilder errorSqlCols = new StringBuilder("insert into " + table + "(");
		StringBuilder errorSqlVals = new StringBuilder(" values(");
		int colNum = this.columns.size();
		Map<String,String> map = outRecord.getRecord();
		for(int j = 0; j < colNum; j++){
			ColumnTemplateBean column = this.columns.get(j);
			String prop = column.getPropertyName();
			String value = map.get(prop);
			errorSqlCols.append(column.getColumnName());
			String fmt = column.getFormat();
			String isSpan = column.getIsSpan();
			String defaultValue = column.getDefaultValue();
			if(isSpan != null && isSpan.equalsIgnoreCase("true") && defaultValue != null){
				errorSqlVals.append("'").append(defaultValue).append("'");
			}else if(fmt != null && !fmt.trim().isEmpty() && !fmt.equals("blob")){
				if(isMySqlDB){
					errorSqlVals.append("'").append(value).append("'");
				}else{
					errorSqlVals.append("to_date('" + value + "','").append(fmt).append("')");
				}
			}else
				errorSqlVals.append("'").append(value).append("'");

			if(j < colNum - 1){
				errorSqlCols.append(",");
				errorSqlVals.append(",");
			}
		}
		errorSqlCols.append(")");
		errorSqlVals.append(")");
		badWriter.debug("入库失败，原因：{}", ex.getMessage().trim());
		badWriter.debug("sql：{}", errorSqlCols.toString() + errorSqlVals.toString());
	}

	/**
	 * 处理blob字段
	 * 
	 * @param value
	 * @return
	 */
	protected Blob handleBlob(String value){
		Blob blob = null;
		try{
			blob = this.con.createBlob();
			OutputStream out = blob.setBinaryStream(1);
			out.write(value.getBytes());
			out.flush();
			out.close();
		}catch(Exception e){
			LOG.error("blob字段处理异常", e);
		}
		return blob;
	}

	@Override
	public void buildExportPropertysList(Set<String> propertysSet){
		int colNum = this.columns.size();
		for(int i = 0; i < colNum; i++){
			ColumnTemplateBean column = this.columns.get(i);
			String prop = column.getPropertyName();
			propertysSet.add(prop);
		}
	}

	public SQLException getLastSQLException(){
		return this.lastSQLException;
	}

	/** 表或视图不存在。 */
	private static final String FATAL_TABLE_NOT_EXISTS = "ORA-00942";

	/** 索引'.'或这类索引的分区处于不可用状态。 */
	private static final String FATAL_TABLE_IDX_NOT_AVALIBLE = "ORA-01502";

	/** 插入的分区关键字未映射到任何分区。 */
	private static final String FATAL_TABLE_NOT_PAR = "ORA-14400";

	/** 标识符无效。 */
	private static final String FATAL_TABLE_COL_NOT_EXISTS = "ORA-00904";

	private static final String [] FATAL_ERRORS = {FATAL_TABLE_NOT_EXISTS,FATAL_TABLE_IDX_NOT_AVALIBLE,
			FATAL_TABLE_NOT_PAR,FATAL_TABLE_COL_NOT_EXISTS};

	/**
	 * 处理批量insert时的异常，计算接下来是否可以继续入库，如果可以，返回此已提交入库成功的条数。
	 * 
	 * @param ex 批量更新异常。
	 * @return 此已提交入库成功的条数。如果值等于-1，表示不可继续入库。
	 */
	private int handleBatchUpdateException(BatchUpdateException ex){
		try{
			con.commit();
		}catch(SQLException e){
			LOG.warn("提交INSERT时出错。", e);
			return -1;
		}
		int insertedCount;
		try{
			insertedCount = this.statement.getUpdateCount();
		}catch(SQLException e){
			LOG.warn("获取INSERT条数时出错。", e);
			return -1;
		}
		return insertedCount;
	}

	// 是否是不可恢复的异常，比如表不存在、列不存在这种，没有必要再继续下去了。
	private static final boolean isFatalException(SQLException ex){
		String msg = ex.getMessage().toUpperCase();
		for(String err : FATAL_ERRORS){
			if(msg.contains(err))
				return true;
		}
		return false;
	}

}
