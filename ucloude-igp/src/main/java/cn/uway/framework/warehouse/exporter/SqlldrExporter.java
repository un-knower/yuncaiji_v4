package cn.uway.framework.warehouse.exporter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
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
import cn.uway.framework.warehouse.exporter.template.DatabaseExporterBean;
import cn.uway.framework.warehouse.exporter.template.ExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.SqlldrColumnTemplateBean;
import cn.uway.framework.warehouse.exporter.template.SqlldrExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.FileUtil;
import cn.uway.util.SqlLdrWriter;
import cn.uway.util.SqlldrManager;
import cn.uway.util.SqlldrResult;
import cn.uway.util.SqlldrRunner;
import cn.uway.util.TimeUtil;

/**
 * SqlldrExporter
 * <p>采用sqlldr工具方式入库</p>
 * @author linp 2015-06-08
 * @version 1.0
 * @since 3.0
 */
public class SqlldrExporter extends AbstractExporter {

	private static final ILogger LOGGER = LoggerManager.getLogger(SqlldrExporter.class); // 日志
	
	protected static final int DEFULT_BATCH_FLASH_NUM = 1000;
	
	protected static final String SQLLDR_PATH = AppContext.getBean("sqlldrFileDir", String.class);
	
	private static final String SQLLDR_SAMEDATABASE_LIMITNUM_STR= AppContext.getBean("sqlldrSameDataBaseLimitNum", String.class);
	
	private static  int SQLLDR_SAMEDATABASE_LIMITNUM_INT=30;

	
	// 输出文件名不包含后缀 
	protected String exportFileName;

	// 输出字段列表
	protected List<SqlldrColumnTemplateBean> exportFields;

	// 输出器参数定义
	protected ExporterArgs exporterArgs;

	// 输出模版
	protected SqlldrExportTemplateBean exportTempletBean;
	
	protected SqlLdrWriter sqlLdrWriter;
	
	/**
	 * 状态表操作DAO
	 */
	protected StatusDAO statusDAO = AppContext.getBean("statusDAO", StatusDAO.class);
	
	/**
	 * 输出总时间
	 */
	protected double totalTime;
	
	static{
		try {
			if(SQLLDR_SAMEDATABASE_LIMITNUM_STR != null && SQLLDR_SAMEDATABASE_LIMITNUM_STR.indexOf("$") < 0) {
				SQLLDR_SAMEDATABASE_LIMITNUM_INT= Integer.valueOf(SQLLDR_SAMEDATABASE_LIMITNUM_STR);
			}
			LOGGER.debug("SQLDR最大并发数:{}", SQLLDR_SAMEDATABASE_LIMITNUM_INT);
		} catch (NumberFormatException e) {
			LOGGER.error("SQLLDR_SAMEDATABASE_LIMITNUM TYPE IS NOT NUMBER ",e);
		}
		
	}

	/**
	 * 根据输出模版初始化输出器
	 * 
	 * @param exportTempletBean
	 */
	public SqlldrExporter(SqlldrExportTemplateBean exportTempletBean, ExporterArgs exporterArgs) {
		super(exporterArgs, exportTempletBean.getId());
		this.exportId = exportTempletBean.getId();
		this.exportTempletBean = exportTempletBean;
		this.exporterArgs = exporterArgs;
		this.dest = exportTempletBean.getTable().getTableName();
		this.exportFields = exportTempletBean.getTable().getColumnsList();
		this.exportType = ExportTemplateBean.EXPORT_DB_SQLLDR;
		this.encode = exportTempletBean.getEncode();
		createCacher(AbstractCacher.MEMORY_CACHER);
		StringBuilder sbd = new StringBuilder();
		sbd.append(exportTempletBean.getTable().getTableName());
		sbd.append("_").append(TimeUtil.getDateString_yyyyMMddHHmmss(exporterArgs.getDataTime()));
		sbd.append("_").append(TimeUtil.getDateString_yyyyMMddHHmmssSSS(new Date()));
		this.exportFileName = sbd.toString();
		try {
			checkFile();
			initSqlldrFile();
		} catch (Exception e) {
			LOGGER.warn("MapBufferedFileExporter创建失败", e);
		}
	}
	
	/**
	 * task文件夹检查 避免因为文件夹不存在生成文件失败,同时做清理工作
	 * 
	 * @throws IOException
	 */
	protected void checkFile() throws IOException {
		File rootDir = new File(SQLLDR_PATH);
		String taskFileDirName = SQLLDR_PATH + File.separator + task.getId();
		File taskFileDir = new File(taskFileDirName);
		if (!rootDir.exists() && rootDir.mkdirs())
			LOGGER.debug("sqlldr文件根目录{}不存在,尝试创建目录成功!", SQLLDR_PATH);
		if (!taskFileDir.exists() && taskFileDir.mkdirs())
			LOGGER.debug("sqlldr任务id目录{}不存在,创建目录成功!", taskFileDirName);
		//sqlldr 该taskid的文件初始化，清空原来该任务的数据文件
		File[] tableFiles = taskFileDir.listFiles(); 
		String checkFileName = this.exportFileName.substring(0, this.exportFileName.lastIndexOf("_"));
		for(File tableFile : tableFiles){
			if(tableFile.getName().startsWith(checkFileName)){
				tableFile.delete();
			}
		}
	}

	/**
	 * 初始化文件流,传入的tablename，文件名为数据时间_tablename_处理时间
	 * 
	 * @param exportFileName
	 * @throws Exception 
	 * @throws IOException
	 */
	protected void initSqlldrFile() throws Exception {
		// 如果是需要生成zip格式 writer的初始化不一样
		String fileName = SQLLDR_PATH + File.separator + task.getId() + File.separator + exportFileName + ".txt"; // 构造文件名
		DatabaseExporterBean dbTargetBean = (DatabaseExporterBean) exportTempletBean.getExportTargetBean(); 
		int batchFlash = dbTargetBean.getBatchNum();
		if(batchFlash < 1 || batchFlash > 10000)
			batchFlash = DEFULT_BATCH_FLASH_NUM;
		
		sqlLdrWriter = new SqlLdrWriter(fileName, batchFlash, exportTempletBean.getSplit());
		sqlLdrWriter.setCharset(this.encode);
		sqlLdrWriter.setConnectionInfo(dbTargetBean.getConnectionInfo());
		sqlLdrWriter.setTableTemlateBean(exportTempletBean.getTable());
		sqlLdrWriter.open();
	}


	protected void writeFile(ParseOutRecord out) {
		this.current++;
		//this.breakPoint++;
		Map<String, String> cdlRecord = out.getRecord();
		String lineRecord = createLineMessage(cdlRecord);
		try {//
			sqlLdrWriter.write(lineRecord,false);
			//this.succ++;
		} catch (Exception e) {
			//this.fail++;
			LOGGER.error("SqlldrExporter写入数据文件失败", e);
		}
	}

	/**
	 * 创建一条输出记录
	 * 
	 * @param record
	 * @return
	 */
	protected String createLineMessage(Map<String, String> record) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < exportFields.size(); i++) {
			SqlldrColumnTemplateBean columm = exportFields.get(i);
			String value = record.get(columm.getPropertyName());
			if("true".equals(columm.getIsSpan()) && columm.getDefaultValue() != null) {
				stringBuffer.append(columm.getDefaultValue()).append(exportTempletBean.getSplit());
				continue;
			}
			if (value == null || value.equalsIgnoreCase("NaN")) {
				value = "";
			}
			
			value= value.replace(exportTempletBean.getSplit(), " ");
			stringBuffer.append(value).append(exportTempletBean.getSplit());
		}
		if(stringBuffer.length() > 0)
			return stringBuffer.toString().substring(0, stringBuffer.length()-1);
		else 
			return "";
	}

	/**
	 * 正常流程下关闭输出器 包含文件流释放和文件后缀名修改
	 * @throws Exception 
	 */
	public void close() {
		SqlldrResult sqlldrResult = null;
		try {
			sqlLdrWriter.commit();

			String abExportFilePath = SQLLDR_PATH + File.separator + task.getId() + File.separator + exportFileName;
			String txtPath = abExportFilePath + ".txt";
			if (this.current > 0) {
				LOGGER.debug("sqlldr文件数据生成完成,文件名：{},将执行sqlldr入库。", txtPath);
				DatabaseExporterBean dbTargetBean = (DatabaseExporterBean) exportTempletBean.getExportTargetBean(); 
				DatabaseConnectionInfo dbInfo = dbTargetBean.getConnectionInfo();
				long taskId = task.getId();
				String keyId = exportTempletBean.getTable().getTableName();
				//20150609暂时serviceName使用Description字段的值
				String serviceName = dbInfo.getDescription();
				String userName = dbInfo.getUserName();
				String password = dbInfo.getPassword();
				String cltPath = abExportFilePath + ".clt";
				String badPath = abExportFilePath + ".bad";
				String logPath = abExportFilePath + ".log";
				int dataCacheLine = dbTargetBean.getBatchNum();
				if(dataCacheLine < 1 || dataCacheLine > 20000){
					dataCacheLine = 5000;
				}
				SqlldrRunner sqlldrRunner = new SqlldrRunner(taskId, keyId, serviceName, userName, password, cltPath, badPath, logPath, 1, dataCacheLine);
				//暂时定每个数据库同时在实行的sqlldr个数
			
				sqlldrResult = SqlldrManager.getInstance().toRunSqlldr(SQLLDR_SAMEDATABASE_LIMITNUM_INT, sqlldrRunner);
				if(sqlldrResult != null){
					this.succ += sqlldrResult.getLoadSuccCount();
					this.fail += sqlldrResult.getLoadFailCount();
					
					//例如00:05:20.23   小数点后面的会被省略
					String[] times = sqlldrResult.getTotalTime().replaceAll(" ", "").split(":");
					totalTime = Integer.valueOf(times[0])*3600 + Integer.valueOf(times[1])*60 + Double.valueOf(times[2]);
				}
			} else {
				this.succ = this.current;
				this.fail = 0;
				totalTime = 0;
				
				String cltPath = abExportFilePath + ".clt";
				FileUtil.removeFile(txtPath);
				FileUtil.removeFile(cltPath);
			}
			
			if(!TaskWorkerFactory.isLogCltInsert(task.getWorkerType())){
				Status status = exporterArgs.getObjStatus().get(0);
				status.breakPointProcess(exportId, this.succ);
				status.updateBySynchronized(statusDAO, exporterArgs.getObjStatus().get(0).getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
	    } finally {
	    	if(isLogCltInsertFlag()){
	    		logCltInsert();
	    	}else{
	    		if(this.dbLoggerFlag){
	    			writeDbLog();
	    		}
	    	}
	    	LOGGER.debug("【入库时间统计】{}表入库耗时{}秒，入库成功{}条数据，sqlldr入库失败{}条数据，{}任务，原始文件：{}，CITY：{}，OMC：{}，BSC：{}，VENDOR：{}", new Object[]{exportTempletBean.getTable().getTableName(), totalTime,
					this.succ, this.fail, task.getId(), (this.exporterArgs != null ? entryNames.get(0) : ""), task.getExtraInfo().getCityId(),
					task.getExtraInfo().getOmcId(), task.getExtraInfo().getBscId(), task.getExtraInfo().getVendor()});
	    }
	}
	
	private void writeDbLog() {
		if (!TaskWorkerFactory.isLogCltInsert(task.getWorkerType())) {
			LogCdrInsert.getInstance().insert(task.getExtraInfo().getCityId(), task.getExtraInfo().getOmcId(), task.getExtraInfo().getBscId(), exportTempletBean.getTable().getTableName(),
					exporterArgs.getDataTime(), startTime, endTime, succ, fail, total, task.getExtraInfo().getVendor(), entryNames.get(0));
		}
	}

	
	/**
	 * 资源释放方法 关闭文件流
	 */
	public void release() {
		sqlLdrWriter.dispose();
	}

	/**
	 * 异常情况下只释放资源
	 */
	public void endExportOnException() {
		release();
	}

	/**
	 * 具体的输出方法 线程循环调用 指导数据输入完成
	 * @throws Exception 
	 */
	public void export(BlockData blockData) throws Exception {
		List<ParseOutRecord> outDatas = blockData.getData();
		for (ParseOutRecord outDate : outDatas) {
			this.writeFile(outDate);
		}
	}

	@Override
	public void buildExportPropertysList(Set<String> propertysSet) {
		if (propertysSet == null || exportFields == null)
			return;

		for (int i = 0; i < exportFields.size(); i++) {
			SqlldrColumnTemplateBean temp = exportFields.get(i);
			String propName = temp.getPropertyName();
			propertysSet.add(propName);
		}
	}
}
