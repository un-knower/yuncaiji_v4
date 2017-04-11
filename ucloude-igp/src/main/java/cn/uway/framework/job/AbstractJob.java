package cn.uway.framework.job;

import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.accessor.Accessor;
import cn.uway.framework.accessor.AccessorReport;
import cn.uway.framework.accessor.LucDoAccessor;
import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.connection.DatabaseConnectionInfo;
import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.log.DBLogger;
import cn.uway.framework.parser.DBParser;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.parser.ParseOutRecordArrayMap;
import cn.uway.framework.parser.Parser;
import cn.uway.framework.parser.ParserReport;
import cn.uway.framework.solution.GatherSolution;
import cn.uway.framework.status.Status;
import cn.uway.framework.status.dao.StatusDAO;
import cn.uway.framework.task.DelayTask;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.ReTask;
import cn.uway.framework.task.Task;
import cn.uway.framework.task.worker.AbstractTaskWorker;
import cn.uway.framework.task.worker.TaskWorkTerminateException;
import cn.uway.framework.warehouse.GenericWareHouse;
import cn.uway.framework.warehouse.WarehouseReport;
import cn.uway.framework.warehouse.exporter.AbstractExporter;
import cn.uway.framework.warehouse.exporter.ExporterArgs;
import cn.uway.framework.warehouse.exporter.ExporterSummaryArgs;
import cn.uway.framework.warehouse.exporter.MapBufferedFileExporter;
import cn.uway.framework.warehouse.exporter.template.DatabaseExporterBean;
import cn.uway.framework.warehouse.exporter.template.DbExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.ExportTargetTempletContext;
import cn.uway.framework.warehouse.exporter.template.ExportTemplate;
import cn.uway.framework.warehouse.exporter.template.ExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.ExportTemplatePojo;
import cn.uway.framework.warehouse.exporter.template.ExporterBean;
import cn.uway.framework.warehouse.exporter.template.FileExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.ParqExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.RemoteFileExportTemplateBean;
import cn.uway.framework.warehouse.exporter.template.SqlldrExportTemplateBean;
import cn.uway.framework.warehouse.repository.BufferedMultiExportRepository;
import cn.uway.framework.warehouse.repository.Repository;
import cn.uway.framework.warehouse.repository.SyncDirectExportRepository;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.ArrayMapKeyIndex;
import cn.uway.util.SqlldrManager;
import cn.uway.util.StringUtil;

/**
 * 抽象作业类
 * 
 * @author chenrongqiang @ 2014-3-31
 */
public abstract class AbstractJob implements Job {

	/**
	 * 日志
	 */
	protected static final ILogger LOGGER = LoggerManager.getLogger(AbstractJob.class);
	
	/**
	 * 记录抽取正常模式，即对每一个文件均调用一次parse->hasnext->nextrecord过程(默认)
	 */
	public final static int EXTRACT_RECORD_NORMAL = 0;
	
	/**
	 * 记录抽取在调用parse完所有分组文件后，再进行(仅限taskworker.work_type=4，GroupFilesJob中处理)
	 */
	public final static int EXTRACT_RECORD_AFTER_PARSED_ALL_GROUPFILES= 1;
	
	
	/**
	 * 作业运行参数
	 */
	protected JobParam jobParam;

	/**
	 * 状态表操作DAO 用户记录和更新断点信息
	 */
	protected StatusDAO statusDAO = AppContext.getBean("statusDAO", StatusDAO.class);

	protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	protected GatherSolution solution;

	protected Accessor accessor;

	protected Parser parser;

	// 是否补汇任务(默认否)
	protected boolean repairJob;

	protected Repository repository;

	protected GenericWareHouse wareHouse = GenericWareHouse.getInstance();

	protected Task task;

	protected long totalTime = 0;

	protected long count = 0;

	protected int invalideNum = 0;

	/** 正确的任务id，考虑补采任务 */
	protected long rightTaskId;

	/** 当前采集时间，考虑补采任务 */
	protected Date currentDateTime;
	
	/**
	 * 文件规则表达式 
	 */
	protected String fileRuleExpression;
	
	
	/**
	 * 文件压缩包子文件匹配表达式
	 */
	protected String packSubFileMatchExpression;

	/**
	 * 输出参数
	 */
	protected ExporterArgs exporterArgs;

	/**
	 * 输出模版
	 */
	protected List<ExportTemplateBean> exportTemplateBeans;

	/**
	 * 本次采集的对象名称,如果是文件方式 则只保存文件名，不保存文件路径
	 */
	protected List<String> entryNames = new LinkedList<String>();

	/**
	 * 采集对象对应在状态表中的记录
	 */
	protected List<Status> statusList = new LinkedList<Status>();

	/**
	 * 输出模板bean
	 */
	protected ExportTemplatePojo exportTemplatePojo;

	protected static final String SPLIT = ";";
	
	protected int extractRecordMode;
	
	/**
	 * 构造方法
	 * 
	 * @param jobParam
	 *            作业运行参数{@link JobParam}
	 */
	public AbstractJob(JobParam jobParam) {
		super();
		this.extractRecordMode = EXTRACT_RECORD_NORMAL;
		this.jobParam = jobParam;
		this.task = jobParam.getTask();
		this.solution = jobParam.getSolution();
		this.accessor = solution.getAccessor();
		this.accessor.setTask(task);
		this.accessor.setConnectionInfo(jobParam.getConnInfo());
		
		List<GatherPathEntry> gatherObjects = task.getGatherPathDescriptor().getPaths();
		if (gatherObjects.size()>0) {
			packSubFileMatchExpression = gatherObjects.get(0).getPackSubFileSuffixFilter();
			fileRuleExpression = gatherObjects.get(0).getFileRuleExpression();
		}
		
		this.parser = solution.getParser();
		this.parser.setCurrentJob(this);
		this.parser.init(jobParam);

		this.rightTaskId = task.getId();
		this.currentDateTime = task.getDataTime();
		if (task instanceof ReTask) {
			ReTask reTask = (ReTask) task;
			this.rightTaskId = reTask.getrTaskId();
			this.currentDateTime = reTask.getRegather_datetime();
		}
	}
	
	public Accessor getAccessor() {
		return accessor;
	}

	/**
	 * 添加数据库输出方式的目的地配置<br>
	 * 主要解决spring配置中输出exportDefinition单例问题 每个job线程运行时会加载任务数据库输出配置<br>
	 * 一个输出模版中一种输出数据只能配置一个输出目的地<br>
	 * 数据库的输出配置已经迁移至IGP_CFG_DB_EXPORT表中[根据任务分组、输出模板ID、 数据类型动态决定]
	 * 
	 * @param solution
	 * @param task
	 */
	protected void createExportTemplate() {
		exportTemplatePojo = solution.getExportDefinition().getExportTemplatePojo();
		List<ExportTemplateBean> exportTempletBeans = exportTemplatePojo.getExportTemplates();
		exportTemplateBeans = new ArrayList<ExportTemplateBean>(exportTempletBeans.size());
		boolean sqlldrTypeExit = false;
		// 只需要对数据库方式进行处理
		for (ExportTemplateBean templetBean : exportTempletBeans) {
			int dataType = templetBean.getType();
			// 不需要加载汇总db输出对象(汇总输出在汇总运算后直接调用，不在此处调用)
			if (dataType == ExportTemplateBean.EXPORT_DB_SUMMARY)
				continue;
			// 非数据库输出方式 直接加入到新的模版中即可
			if (!ExportTemplate.isDBCfgExport(dataType)) {
				exportTemplateBeans.add(templetBean);
				continue;
			}
			//如果是sqlldr输出方式
			if(dataType == ExportTemplateBean.EXPORT_DB_SQLLDR){
				sqlldrTypeExit = true;
				SqlldrExportTemplateBean oldTempletBean = (SqlldrExportTemplateBean) templetBean;
				// 克隆一个新的数据库输出对象模版
				SqlldrExportTemplateBean sqlldrTempletBean = new SqlldrExportTemplateBean(oldTempletBean);
				ExporterBean exportBean = ExportTargetTempletContext.getInstance().getSqlldrExportTargetBean(task, sqlldrTempletBean);
				// 数据库输出的开关使用IGP_CFG_DB_EXPORT表中IS_USED配置
				if (exportBean == null) {
					LOGGER.warn("task_id={},dataType={},exportTemplateId={} 未找到数据库输出配置或者已关闭数据库输出", new Object[]{task.getId(),
							sqlldrTempletBean.getDataType(), sqlldrTempletBean.getId()});
					continue;
				}
				sqlldrTempletBean.setExportTargetBean(exportBean);
				
				DatabaseExporterBean dbTargetBean = (DatabaseExporterBean) sqlldrTempletBean.getExportTargetBean(); 
				DatabaseConnectionInfo dbInfo = dbTargetBean.getConnectionInfo();
				StringBuilder sbd = new StringBuilder();
				sbd.append(dbInfo.getDriver()).append("|");
				sbd.append(dbInfo.getUrl()).append("|");
				sbd.append(dbInfo.getUserName()).append("|");
				sbd.append(dbInfo.getPassword());
				exportTemplateBeans.add(sqlldrTempletBean);
				SqlldrManager.getInstance().setDbTable(sbd.toString(), sqlldrTempletBean.getTable());
				continue;
			} else if (dataType == ExportTemplateBean.REMOTE_FILE_EXPORTER || dataType == ExportTemplateBean.EXTEND_REMOTE_FILE_EXPORTER) {
				// 加载db输出对象
				RemoteFileExportTemplateBean oldTempletBean = (RemoteFileExportTemplateBean) templetBean;
				// 克隆一个新的数据库输出对象模版
				RemoteFileExportTemplateBean remoteFileTempletBean = new RemoteFileExportTemplateBean(oldTempletBean);
				ExporterBean exportBean = ExportTargetTempletContext.getInstance().getRemoteFileExportTargetBean(task, remoteFileTempletBean);
				// 数据库输出的开关使用IGP_CFG_DB_EXPORT表中IS_USED配置
				if (exportBean == null) {
					LOGGER.warn("task_id={},dataType={},exportTemplateId={} 未找到文件输出配置或者已关闭文件输出", new Object[]{task.getId(),
							remoteFileTempletBean.getDataType(), remoteFileTempletBean.getId()});
					continue;
				}
				remoteFileTempletBean.setExportTargetBean(exportBean);
				exportTemplateBeans.add(remoteFileTempletBean);
				
				continue;
			}else if (dataType == ExportTemplateBean.PARQUET_EXPORTER 
					|| dataType == ExportTemplateBean.CONFIGURE_PARQUET_EXPORTER) {
				// 加载db输出对象
				ParqExportTemplateBean oldTempletBean = (ParqExportTemplateBean) templetBean;
				// 克隆一个新的数据库输出对象模版
				ParqExportTemplateBean parqTempletBean = new ParqExportTemplateBean(oldTempletBean);
				ExporterBean exportBean = ExportTargetTempletContext.getInstance().getParqExportTargetBean(task, parqTempletBean);
				// 数据库输出的开关使用IGP_CFG_DB_EXPORT表中IS_USED配置
				if (exportBean == null) {
					LOGGER.warn("task_id={},dataType={},exportTemplateId={} 未找到文件输出配置或者已关闭文件输出", new Object[]{task.getId(),
							parqTempletBean.getDataType(), parqTempletBean.getId()});
					continue;
				}
				parqTempletBean.setExportTargetBean(exportBean);
				exportTemplateBeans.add(parqTempletBean);
				
				continue;
			}
			
			// 加载db输出对象
			DbExportTemplateBean oldTempletBean = (DbExportTemplateBean) templetBean;
			// 克隆一个新的数据库输出对象模版
			DbExportTemplateBean dbTempletBean = new DbExportTemplateBean(oldTempletBean);
			ExporterBean exportBean = ExportTargetTempletContext.getInstance().getDbExportTargetBean(task, dbTempletBean);
			// 数据库输出的开关使用IGP_CFG_DB_EXPORT表中IS_USED配置
			if (exportBean == null) {
				LOGGER.warn("task_id={},dataType={},exportTemplateId={} 未找到数据库输出配置或者已关闭数据库输出", new Object[]{task.getId(),
						dbTempletBean.getDataType(), dbTempletBean.getId()});
				continue;
			}
			dbTempletBean.setExportTargetBean(exportBean);
			exportTemplateBeans.add(dbTempletBean);
		}
		
		if(sqlldrTypeExit){
			//刷新sqlldrClt缓存
			SqlldrManager.getInstance().createTableClt();
		}
	}

	/**
	 * 线程重命名<br>
	 * 主要是为了打印日志上显示线程方法
	 */
	protected void renameThread() {
		if (task instanceof DelayTask) {
			//延迟任务
			DelayTask dTask = (DelayTask) task;
			Thread.currentThread().setName("[" + task.getId() + "-" + dTask.getDelayId() + " D]Job");
		}else if (task instanceof ReTask) {
			// 补采任务
			Thread.currentThread().setName("[" + task.getId() + "-" + rightTaskId + "]Job");
		}else{
			Thread.currentThread().setName("[" + rightTaskId + "]Job");
		}
	}

	// 数据接入前的shell执行处理
	protected void accessBefore() {
		beforeAccess(solution.getBeforeAccessShell());
		// 开始接入数据
		accessor.beforeAccess();
	}

	/**
	 * 处理流程<br>
	 * 1、初始化solution、ExportTemplate.<br>
	 * 2、调用access,如失败则线程结束<br>
	 * 3、调用parser.parser()。完成解码初始化<br>
	 * 4、如果解码出数据 则初始化仓库<br>
	 * 5、调用仓库分发数据，数据分发完成处理AccessorReport/ParserReport/WarehouseReport<br>
	 * 6、线程返回JobFuture<br>
	 */
	public JobFuture call() {
		renameThread();
		// 给线程设置线程名。
		LOGGER.debug("taskId={},AbstractJob线程开始。", rightTaskId);
		// 输出模版处理
		createExportTemplate();
		if (exportTemplateBeans == null || exportTemplateBeans.size() == 0) {
			LOGGER.error("初始化数据库输出目的地失败.没有找到输出目的地");
			return new JobFuture(-1, "[初始化数据库输出目的地失败]");
		}
		accessBefore();
		// 开始进行Access
		AccessOutObject accessOutObject = null;
		LOGGER.debug("[access start]");
		try {
			accessOutObject = accessor.access(jobParam.getPathEntry());
			accessOutObject.setTask(task);
		} catch (Exception e) {
			LOGGER.error("Accessor接入异常", e);
			if (parser != null && parser instanceof DBParser) {
				DBParser dbParser = (DBParser) parser;
				dbParser.setTempletIds(new StringBuilder(jobParam.getPathEntry().getPath()));
				dbParser.setCause("补采原因：" + e.getMessage());
				dbParser.regatherHandle(task);
			}
			accessor.close();
			return new JobFuture(-1, "[AbstractJob]Accessor接入异常");
		}

		// 将任务和采集对象名字设置到接入结果对象中
		if (accessOutObject.getRawAccessName() == null)
			accessOutObject.setRawAccessName(jobParam.getPathEntry().getPath());
		// 将采集对象名放入到entryName中
		entryNames.add(FilenameUtils.getName(accessOutObject.getRawAccessName()));
		// 数据解码前的shell执行处理
		beforeParse(solution.getBeforePaserShell());
		// 调用parser.parse 初始化 解析器 如果解析器初始化失败 异常返回
		try {
			LOGGER.debug("[parse start]");
			parser.setDataTime(task instanceof PeriodTask ? task.getDataTime() : jobParam.getPathEntry().getDateTime());
			parser.parse(accessOutObject);
		} catch (Exception e) {
			LOGGER.debug("解析过程中parse()发生错误。");
			if (parser != null && parser instanceof DBParser) {
				DBParser dbParser = (DBParser) parser;
				dbParser.setTempletIds(new StringBuilder(jobParam.getPathEntry().getPath()));
				dbParser.setCause("补采原因：" + e.getMessage());
			}
			if (parser != null) {
				parser.close();
			}
			accessor.close();
			
			if (e instanceof TaskWorkTerminateException) {
				LOGGER.debug("解析过程中parse()被要求终止。");
				return new JobFuture(TaskWorkTerminateException.exceptionCode, "解析被要求终止，下个周期再尝试." + e.getMessage());
			} else {
				LOGGER.error("解析过程中parse()发生异常。", e);
				return new JobFuture(-1, "解析器初始化失败," + e.getMessage());
			}
		}
		Status status;
		try {
			LOGGER.debug("[create status start]");
			status = createGatherObjStatus(task);
			LOGGER.debug("[create status close]");
			statusList.add(status);
			status.initDataParse();
		} catch (Exception e) {
			LOGGER.error("创建采集状态异常。", e);
			parser.close();
			accessor.close();
			return new JobFuture(-1, "创建采集状态异常," + e.getMessage());
		}
		// 循环取出所有解码后的记录
		status.initDataExport();
		// 开始工作
		boolean jobProcessRet = extractRecordToWareHouse(accessOutObject);
		// 处理报告和采集对象状态
		JobFuture jobFutrue = afterJob(status, jobProcessRet);
		LOGGER.debug("AbstractJob线程结束，taskId=" + rightTaskId);

		return jobFutrue;
	}

	/**
	 * 开始工作：解析，启动warehouse，向warehouse传送数据
	 * 
	 * @param accessOutObject
	 * @return true:处理正常结束 false:处理过程中发生了异常
	 */
	public boolean extractRecordToWareHouse(AccessOutObject accessOutObject) {
		boolean exceptionFlag = false;
		boolean ignoreExceptin = false;
		try {
			// 开始工作
			while (parser.hasNextRecord()) {
				// 如果仓库未初始化 则初始化仓库 延迟初始化
				// 把代码前置是因为要从仓库中获取export需要的属性列表
				if (repository == null) {
					startExport();
				}

				// 获取一条解码后的记录
				ParseOutRecord parseOutRecord = null;
				long start = System.currentTimeMillis();
				parseOutRecord = parser.nextRecord();
				long end = System.currentTimeMillis();
				totalTime += (end - start);
				count++;
				if (parseOutRecord == null) {
					invalideNum++;
					continue;
				}
				repository.transport(parseOutRecord);
			}
			if (parser instanceof ExportCountStatistics) {
				ExportCountStatistics stat = (ExportCountStatistics) parser;
				Map<String, Integer> map = stat.getExportCountStatics();
				for (Entry<String, Integer> entry : map.entrySet()) {
					if (entry.getValue().intValue() == 0) {
						DBLogger.getInstance().insert(task.getId(), task.getExtraInfo().getOmcId(), entry.getKey(), task.getDataTime(), 0, "");
						LOGGER.debug("{}入库条数为0，已记入log_clt_insert表。", entry.getKey());
					}
				}
				stat.resetExportCountStatics();
			}
		} catch (SQLException e) {
			LOGGER.error("数据库操作出现异常，taskId=" + rightTaskId, e);
			exceptionFlag = true;
			if (parser instanceof DBParser) {
				// 数据库采集的，忽略异常
				ignoreExceptin = true;
				sqlExceptionHandler(accessOutObject);
			}
		} catch (SocketTimeoutException e) {
			LOGGER.error("解析时异常(ftp连接超时)，taskId=" + rightTaskId, e);
			exceptionFlag = true;
		} catch (Exception e) {
			LOGGER.error("解析时异常，taskId=" + rightTaskId, e);
			exceptionFlag = true;
		} finally {
			try {
				LOGGER.debug("任务丢失记录数量：{}", invalideNum);
				LOGGER.debug("【解析时间统计】解码耗时{}秒，{}条记录被解析。parser closed", new Object[]{totalTime / 1000.00, count});
				parser.close();
				parser.afterClose();

				// 空数据源处理
				if (count == 0)
					emptyDataSourceHandler();

				if (repository != null)
					endExport(repository, exceptionFlag);
				LOGGER.debug("\"{}\"中的最大释放时间为：{}", new Object[]{accessOutObject.getRawAccessName(), getDateString(parser.getCurrentDataTime())});
				if (accessor != null) {
					accessor.close();
					LOGGER.debug("[accessor close]");
				}
			} catch (Exception e) {
				LOGGER.debug("解析结束时异常，taskId=" + rightTaskId, e);
			}
		}

		return !exceptionFlag || ignoreExceptin;
	}

	/**
	 * @param status
	 *            状态对象
	 * @return Map<datatype, tableName>
	 */
	public Map<Integer, String> dataTypeTableMappingMap(Status status) {

		Map<Integer, String> resultMap = new HashMap<Integer, String>();
		if (exporterArgs == null) {
			return null;
		}
		List<ExportTemplateBean> exportTemplateList = exporterArgs.getExportTempletBeans();
		for (ExportTemplateBean templet : exportTemplateList) {

			if (templet instanceof DbExportTemplateBean) {
				DbExportTemplateBean dbExportTemplet = (DbExportTemplateBean) templet;
				resultMap.put(templet.getDataType(), dbExportTemplet.getTable().getTableName());
			}
		}
		return resultMap;

	}

	/**
	 * sql异常处理：补采处理
	 * 
	 * @param accessOutObject
	 * @param e
	 * @return
	 */
	public void sqlExceptionHandler(AccessOutObject accessOutObject) {
		DBParser dbParser = (DBParser) parser;
		// 记录补采
		dbParser.rememberTempletIds();
		try {
			// 获取查询实例，继续工作
			if (dbParser.getNextQuery())
				extractRecordToWareHouse(accessOutObject);
		} catch (SQLException e) {
			LOGGER.error("数据库操作出现异常，taskId=" + rightTaskId, e);
		}
	}

	/**
	 * 空文件处理：1）朗讯do空文件；2）LTE汇总前置的空数据源（如空文件）
	 * 
	 * @param count
	 */
	public void emptyDataSourceHandler() {
		// 朗讯do空文件（统计分拆后的文件总数，空文件也计入，共12个）
		if (accessor != null && accessor instanceof LucDoAccessor) {
			emptyDataSourceHandler(ExportTemplateBean.LOACL_FILE_EXPORTER);
		}

		// LTE汇总前置(用于计算汇总条件，空文件也计入)
		if (exportTemplatePojo.isSummary) {
			emptyDataSourceHandler(ExportTemplateBean.EXPORT_SUMFILE);
		}
	}

	/**
	 * 空数据源处理，使其接下来的流程走完，如汇总条件计算等
	 * 
	 * @param type
	 */
	public void emptyDataSourceHandler(int type) {
		exporterArgsInit();
		for (ExportTemplateBean templateBean : exportTemplateBeans) {
			if (templateBean.getType() != type)
				continue;
			if (!templateBean.isOn())
				continue;
			AbstractExporter exporter = null;
			if (type == ExportTemplateBean.LOACL_FILE_EXPORTER) {
				FileExportTemplateBean fileExportTemplateBean = (FileExportTemplateBean) templateBean;
				exporter = new MapBufferedFileExporter(fileExportTemplateBean, exporterArgs);
			}
			
			if (exporter != null) {
				exporter.setTotal(1);// 造假 ，其实没有数据，count = 0
				exporter.close();
			}
		}
	}

	/**
	 * 组装ExporterArg,并且初始化warehouse输出
	 */
	void startExport() {
		exporterArgsInit();
		repository = new SyncDirectExportRepository(exporterArgs);
		// 调用通知方法 通知warehouse当前Job线程开始使用warehouse
		wareHouse.applyNotice(rightTaskId);
	}

	/**
	 * 初始化全局变量exporterArgs
	 */
	private void exporterArgsInit() {
		exporterArgs = new ExporterSummaryArgs();
		exporterArgs.setExportTempletBeans(exportTemplateBeans);
		exporterArgs.setTask(task);
		exporterArgs.setEntryNames(entryNames);
		exporterArgs.setDataTime(parser.getCurrentDataTime());
		exporterArgs.setObjStatus(statusList);
		((ExporterSummaryArgs) exporterArgs).setRepair(repairJob);
	}

	/**
	 * 通知数据仓库数据写入结束
	 * 
	 * @param exporterArgs
	 *            输出器参数 主要用户获取最新的数据时间
	 * @param repository
	 * @param exceptionFlag
	 *            是否发生异常
	 */
	protected void endExport(Repository repository, boolean exceptionFlag) {
		if (exporterArgs == null)
			return;
		exporterArgs.setDataTime(parser.getCurrentDataTime());
		if (repository != null)
			repository.commit(exceptionFlag);
	}

	/**
	 * @param task
	 * @return
	 */
	protected Status createGatherObjStatus(Task task) {
		Status gatherObjStatus = new Status();
		// 此处用task.getId()，不用rightTaskId，防止向状态表写入过多记录，且便于控制
		gatherObjStatus.setTaskId(task.getId());
		gatherObjStatus.setGatherObj(getPathEntry());
		gatherObjStatus.setPcName(task.getPcName());
		// 周期性任务需要加上数据时间进行判断
		if (task instanceof PeriodTask)
			gatherObjStatus.setDataTime(currentDateTime);
		gatherObjStatus = objStatusInitialize(gatherObjStatus);
		gatherObjStatus.initDataAccess();
		return gatherObjStatus;
	}

	/**
	 * 获取采集对象的名称<br>
	 * 对数据库形式是表名，文件方式为文件名<br>
	 * 
	 * @return
	 */
	final String getPathEntry() {
		String path = jobParam.getPathEntry().getPath();
		/*
		 * gatherPath这个路径的来源是从FTP获取的文件列表，需要按照本地编码格式转换
		 * 
		 * @author Niow 2014-6-12
		 */
		if (jobParam.getConnInfo() instanceof FTPConnectionInfo) {
			FTPConnectionInfo ftpConnInfo = (FTPConnectionInfo) jobParam.getConnInfo();
			path = StringUtil.decodeFTPPath(path, ftpConnInfo.getCharset());
		}
		return StringUtil.getFilename(path);
	}

	protected JobFuture afterJob(Status status, boolean jobProcessRet) {
		JobFuture jobFuture = dealReport(status, jobProcessRet);

		// statusDAO.update(status, status.getId());
		status.updateBySynchronized(statusDAO, status.getId());

		return jobFuture;
	}

	protected JobFuture dealReport(WarehouseReport warehouseReport, Status status, boolean jobProcessRet) {
		JobFuture jobFuture = new JobFuture();
		AccessorReport accessorReport = null;
		ParserReport parserReport = null;
		if (accessor != null) {
			accessorReport = accessor.report();
			jobFuture.setAccessorReport(accessorReport);
			status.setAccessStartTime(accessorReport.getStartTime());
			status.setAccessEndTime(accessorReport.getEndTime());
			if (accessorReport.getCause() != null) {
				status.setStatus(Status.DATA_ACCESS_FAIL);
				status.setAccessCause(accessorReport.getCause());
				jobFuture.setCause(accessorReport.getCause());
				jobFuture.setCode(Status.DATA_ACCESS_FAIL);
			}
		}
		if (parser != null) {
			parserReport = parser.getReport();
			jobFuture.setParserReport(parserReport);
			status.setParseStartTime(parserReport.getStartTime());
			status.setParseEndTime(parserReport.getEndTime());
			status.setParseCause(parserReport.getCause());
			if (parserReport.getFileLines() > -1)
				status.setSubGatherObj(String.valueOf(parserReport.getFileLines()));
			if (parserReport.getCause() != null && status.getStatus() != Status.DATA_ACCESS_FAIL) {
				status.setStatus(Status.DATA_PARSE_FAIL);
				jobFuture.setCode(Status.DATA_PARSE_FAIL);
				jobFuture.setCause(parserReport.getCause());
			}
		}
		if (warehouseReport != null) {
			jobFuture.setWarehouseReport(warehouseReport);
			status.setWarehouseStartTime(warehouseReport.getStartTime());
			status.setWarehouseEndTime(warehouseReport.getEndTime());
			status.setGatherNum(warehouseReport.getDistributedNum());
			if (warehouseReport.getCause() != null && status.getStatus() != Status.DATA_ACCESS_FAIL && status.getStatus() != Status.DATA_PARSE_FAIL) {
				status.setStatus(Status.DATA_EXPORT_FAIL);
				jobFuture.setCode(Status.DATA_EXPORT_FAIL);
				jobFuture.setCause(warehouseReport.getCause());
			}
		}
		if (!status.isError())
			status.setStatus(Status.FINISH_SUCCESS);

		// 如果解码没有错误，并且解码条数为0，也设置export_status为1.表示采集成功
		if (jobProcessRet && (warehouseReport == null || (warehouseReport != null && warehouseReport.getTotal() == 0))) {
			// statusDAO.updateExportStatus(status.getId(), 1);
			statusDAO.updateExportStatusUnsynchronized(status.getId(), Status.FINISH_SUCCESS);
		}

		return jobFuture;
	}

	/**
	 * 从接入器、解析器、仓库报告中提取各自报告
	 * 
	 * @param status
	 * @param jobProcessRet
	 *            任务处理结果
	 */
	private JobFuture dealReport(Status status, boolean jobProcessRet) {
		WarehouseReport warehouseReport = null;
		if (repository != null)
			warehouseReport = repository.getReport();
		return dealReport(warehouseReport, status, jobProcessRet);
	}

	/**
	 * 状态处理逻辑 <br>
	 * 1、检查在采集表中是否已经有该记录 如记录已经存在 则直接使用该记录.特别是断点信息 <br>
	 * 2、对已经存在的记录状态位必须进行初始化<br>
	 * 3、如不存在 则新增一条采集记录
	 */
	protected Status objStatusInitialize(Status obj, String decodedFTPPath) {
		Status gatherObjStatus = statusDAO.searchGatherObjStatus(obj);
		// 没有采集过 则初始化
		if (gatherObjStatus == null) {
			gatherObjStatus = obj;
			gatherObjStatus.init();
			gatherObjStatus.setExportStatus(Status.EXPORT_START);
			// 如果时间信息为空 则从文件中获取时间信息
			if (gatherObjStatus.getDataTime() == null)
				gatherObjStatus.setDataTime(parser.getCurrentDataTime());
			
			// 尝试从文件名中获取时间，尽可能保证gatherObjStatus.datetime != null;
			if (gatherObjStatus.getDataTime() == null && decodedFTPPath != null) {
				Date rawFileTime = AbstractTaskWorker.getGatherObjectDateTimeFromFileName(decodedFTPPath);
				LOGGER.warn("尝试从文件名中获取时间，获取结果为:{}, 原始文件名:{}", new Object[]{rawFileTime, decodedFTPPath});
				gatherObjStatus.setDataTime(rawFileTime);
			}
			
			gatherObjStatus.setId(statusDAO.log(gatherObjStatus));
			return gatherObjStatus;
		}
		// 已经采集过 取数据库中采集信息
		// gatherObjStatus.init();
		// statusDAO.update(gatherObjStatus, gatherObjStatus.getId());
		gatherObjStatus.updateBySynchronized(statusDAO, gatherObjStatus.getId());
		return gatherObjStatus;
	}
	
	protected Status objStatusInitialize(Status obj) {
		return objStatusInitialize(obj, null);
	}
	
	public String getDateString(Date date) {
		if (date == null)
			return "";
		return this.dateFormat.format(date);
	}

	/**
	 * 根据指定的数据类型，创建对应的输出字段数据Map;
	 * 
	 * @param dataType
	 *            数据类型
	 * @return 如果输出模板配置正确，将输出ArrayMap,否则输出HashMap
	 */
	public Map<String, String> createExportPropertyMap(int dataType) {
		if (repository == null) {
			startExport();
		}

		if (repository != null && repository instanceof BufferedMultiExportRepository) {
			BufferedMultiExportRepository buffMultiExportRepository = (BufferedMultiExportRepository) repository;
			ArrayMapKeyIndex<String> mapKeyIndex = buffMultiExportRepository.getBlockDataHelper().getDataKeyIndexByDataType(dataType);
			if (mapKeyIndex != null) {
				Map<String, String> exportPropertysMap = new ParseOutRecordArrayMap(mapKeyIndex, false);
				return exportPropertysMap;
			} else {
				LOGGER.warn("repository 获取输出字段列表失败. taskId:{} dataType:{}", rightTaskId, dataType);
			}
		}

		return new HashMap<String, String>();
	}

	public List<ExportTemplateBean> getExportTemplateBeans() {
		return exportTemplateBeans;
	}

	public void setExportTemplateBeans(List<ExportTemplateBean> exportTemplateBeans) {
		this.exportTemplateBeans = exportTemplateBeans;
	}
	
	public String getFileRuleExpression() {
		return fileRuleExpression;
	}

	public String getPackSubFileMatchExpression() {
		return packSubFileMatchExpression;
	}
	
	public int getExtractRecordMode() {
		return extractRecordMode;
	}

	public void setExtractRecordMode(int extractRecordMode) {
		this.extractRecordMode = extractRecordMode;
	}


	/**
	 * 数据接入前的命令执行处理
	 * 
	 * @param beforeAccessShell
	 *            shell命令
	 */
	public abstract void beforeAccess(String beforeAccessShell);

	/**
	 * 数据解码前的命令执行处理
	 * 
	 * @param beforeParseShell
	 *            shell命令
	 */
	public abstract void beforeParse(String beforeParseShell);
	
	/**
	 * 现有igp是不支持上传采集异常日志到服务器的，但是北京要求这么做，所以添加此方法
	 * @return ftp连接信息
	 */
	public ConnectionInfo getConnectionInfo(){
		return jobParam.getConnInfo();
	}
}
