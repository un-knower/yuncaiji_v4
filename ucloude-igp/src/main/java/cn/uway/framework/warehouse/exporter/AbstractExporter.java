package cn.uway.framework.warehouse.exporter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cn.uway.framework.cache.AbstractCacher;
import cn.uway.framework.cache.BlockingCacher;
import cn.uway.framework.cache.Cacher;
import cn.uway.framework.cache.Element;
import cn.uway.framework.cache.MemoryCacher;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.log.DBLogger;
import cn.uway.framework.status.Status;
import cn.uway.framework.task.Task;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.NumberUtil;
import cn.uway.util.StringUtil;

/**
 * Exporter抽象类 提供Exporter通用功能<br>
 * 1、线程控制<br>
 * 2、ExportReport定义和输出
 * 
 * @author chenrongqiang 2012-11-1
 * @version 1.0
 * @since 3.0
 */
public abstract class AbstractExporter implements Exporter {

	private static final ILogger LOGGER = LoggerManager.getLogger(AbstractExporter.class); // 日志

	private int dataType;

	private Cacher cacher;

	// 输出模版ID
	protected int exportId;

	// 输出器类型
	protected int exportType;

	// 当前处理条数
	protected long current = 0L;

	// 总共条数
	protected long total = 0L;

	// 成功条数
	protected long succ = 0L;

	// 失败条数
	protected long fail = 0L;

	// 失败码
	protected long errorCode;

	// 失败原因
	protected String cause;

	// 输出目的地
	protected String dest;

	// 输出开始时间
	protected Date startTime;

	// 输出结束时间
	protected Date endTime;

	// 输出断点信息
	protected long breakPoint = 0L;

	protected String encode;

	// 异常标志 主要用于记录处理输出器初始化异常
	protected boolean exporterInitErrorFlag = false;

	protected Task task;

	protected List<String> entryNames = new LinkedList<String>();

	/** 输出器参数定义 **/
	protected ExporterArgs exporterArgs;

	// 终止处理标识(当向cacher加入blockData发生异常时，此标识将被启用)
	public volatile boolean breakProcessFlag = false;

	// 终止原因
	public volatile String breakProcessCause;
	
	/**
	 * 日志表写入开关
	 */
	protected boolean dbLoggerFlag = AppContext.getBean("dbLoggerFlag", Boolean.class);

	/**
	 * log_clt_insert日志表写入开关
	 */
	protected String logCltInsertFlag = AppContext.getBean("logCltInsertFlag", String.class);


	public AbstractExporter(ExporterArgs exporterArgs, int exportId) {
		super();
		this.exporterArgs = exporterArgs;
		this.exportId = exportId;
		this.task = exporterArgs.getTask();
		this.entryNames = exporterArgs.getEntryNames();
	}

	public int getExportId() {
		return exportId;
	}

	public String getDest() {
		return dest;
	}

	public void setExportId(int exportId) {
		this.exportId = exportId;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public int getExportType() {
		return exportType;
	}

	public long getTotal() {
		return total;
	}

	public long getSucc() {
		return succ;
	}

	public long getFail() {
		return fail;
	}

	public long getErrorCode() {
		return errorCode;
	}

	public String getCause() {
		return cause;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setExportType(int exportType) {
		this.exportType = exportType;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public void setSucc(long succ) {
		this.succ = succ;
	}

	public void setFail(long fail) {
		this.fail = fail;
	}

	public void setErrorCode(long errorCode) {
		this.errorCode = errorCode;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Cacher getCacher() {
		return cacher;
	}

	public void setCacher(Cacher cacher) {
		this.cacher = cacher;
	}

	/**
	 * 判断是否输出日志记录
	 * 
	 * @return
	 */
	protected boolean isLogCltInsertFlag() {
		if (StringUtil.isNotEmpty(logCltInsertFlag)) {
			if ("1".equalsIgnoreCase(logCltInsertFlag.trim()) || "on".equalsIgnoreCase(logCltInsertFlag.trim())
					|| "true".equalsIgnoreCase(logCltInsertFlag.trim()))
				return true;
		}
		return false;
	}

	/**
	 * 记录日志记录到 log_clt_insert
	 * 
	 * @param status
	 */
	public void logCltInsert() {
		Task task = exporterArgs.getTask();
		DBLogger.getInstance().insertMinute(task.getId(), task.getExtraInfo().getOmcId(), dest, exporterArgs.getDataTime(), succ,
				exporterArgs.getEntryNames().get(0));
	}

	/**
	 * 设置输出断点
	 * 
	 * @param objStatus
	 */
	protected void setBreakPoint() {
		List<Status> statusList = exporterArgs.getObjStatus();
		if (statusList == null || statusList.size() == 0)
			return;
		Status objStatus = statusList.get(0);
		if (objStatus == null || objStatus.getWarehousePoint() == null)
			return;
		String[] breakPoints = objStatus.getWarehousePoint().split(";");
		try {
			for (int i = 0; i < breakPoints.length; i++) {
				String breakPoint = breakPoints[i];
				String[] exportPoint = breakPoint.split(":");
				if (exportPoint != null && exportPoint.length == 2) {
					int pointId = NumberUtil.parseInt(exportPoint[0], 0);
					if (pointId != 0 && this.exportId == pointId) {
						this.breakPoint = NumberUtil.parseLong(exportPoint[1], 0L);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("分析断点时发生异常。断点信息：" + (objStatus != null ? objStatus.getWarehousePoint() : ""), e);
		}

	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public ExporterArgs getExporterArgs() {
		return exporterArgs;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * 线程方法<br>
	 * 从cache中抽取
	 */
	public ExportFuture call() throws Exception {
		startTime = new Timestamp(new Date().getTime());
		int exportedNum = 0;
		ExportFuture exportFuture = new ExportFuture();
		ExportReport exportReport = null;
		if (exporterInitErrorFlag) {
			this.endTime = new Timestamp(new Date().getTime());
			exportReport = createExportReport();
			LOGGER.debug("Exporter初始化失败,输出中止,报表={}", exportReport);
			exportFuture.setExportReport(exportReport);
			return exportFuture;
		}
		// 如果初始化没有失败 则进行输出
		while (!breakProcessFlag) {
			try {
				if (cacher.isCommit() && exportedNum >= cacher.size()) {
					this.close();
					this.cacher.shutdown();
					break;
				}
				Element element = this.cacher.getNextElement();
				// cacher.getNextElement只有当所有element都输出完成才返回空。但是需要使用continue再次执行一下判断。
				if (element == null) {
					continue;
				}
				BlockData blockData = (BlockData) element.getElementValue();
				exportedNum++;
				this.total += blockData.getData().size();
				this.export(blockData);
			} catch (Exception e) {
				LOGGER.debug("输出异常", e);
				this.setErrorCode(-1);
				this.setCause(e.getMessage());
				this.close();
				cacher.shutdown();
				break;
			}
		}

		if (breakProcessFlag) {
			LOGGER.error("ExportFuture::call() 收到终止处理标识，export线程退出。 终止原因:{}", breakProcessCause);
		}

		LOGGER.debug("Cacher.size()={},Exporter counter={}", new Object[]{this.cacher.size(), exportedNum});
		endTime = new Timestamp(new Date().getTime());
		exportReport = createExportReport();
		exportFuture.setExportReport(exportReport);
		LOGGER.debug(Thread.currentThread().getName() + "，输出完毕，产生报表={}", exportReport.toString());
		return exportFuture;
	}

	protected ExportReport createExportReport() {
		ExportReport exportReport = new ExportReport();
		exportReport.setStartTime(this.startTime);
		exportReport.setEndTime(this.endTime);
		exportReport.setDest(this.dest);
		exportReport.setExportType(this.exportType);
		exportReport.setSucc(this.succ);
		exportReport.setExportId(this.exportId);
		exportReport.setBreakPoint(this.breakPoint);
		exportReport.setFail(this.fail);
		exportReport.setTotal(this.total);
		exportReport.setErrorCode(errorCode);
		exportReport.setCause(this.cause);
		return exportReport;
	}

	
	@Override
	public int getType() {
		return this.dataType;
	}

	public String getEncode() {
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	@Override
	public void breakProcess(String breakCause) {
		this.breakProcessFlag = true;
		this.breakProcessCause = breakCause;
	}
	
	protected void createCacher(int cacherType) {
		// 非自调度的exporter不需要创建cache
		if (!exporterArgs.isDispatcher())
			return;
		
		if (cacherType == AbstractCacher.BLOCK_CACHER) {
			this.cacher = new MemoryCacher();
		} else if (cacherType == AbstractCacher.BLOCK_CACHER) {
			this.cacher = new BlockingCacher(createCacherName(), exporterArgs.getRepository());
		}
	}
	
	private String createCacherName() {
		Task task = exporterArgs.getTask();
		List<String> entryNames = exporterArgs.getEntryNames();
		return new StringBuilder().append(task.getId()).append("_").append(entryNames.get(0)).append("_").append(exportId).append("_").append(System.currentTimeMillis()).toString();
	}
}
