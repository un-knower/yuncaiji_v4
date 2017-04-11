package cn.uway.framework.task.worker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.framework.connection.dao.ConnectionInfoDAO;
import cn.uway.framework.connection.dao.impl.DatabaseConnectionInfoDAO;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.job.AdaptiveStreamJob;
import cn.uway.framework.job.GenericJob;
import cn.uway.framework.job.Job;
import cn.uway.framework.job.JobFuture;
import cn.uway.framework.job.JobParam;
import cn.uway.framework.log.ImportantLogger;
import cn.uway.framework.log.SummaryDAO;
import cn.uway.framework.solution.GatherSolution;
import cn.uway.framework.solution.SolutionLoader;
import cn.uway.framework.status.Status;
import cn.uway.framework.status.dao.StatusDAO;
import cn.uway.framework.task.DelayTask;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.ReTask;
import cn.uway.framework.task.Task;
import cn.uway.framework.task.TaskFuture;
import cn.uway.framework.task.dao.TaskDAO;
import cn.uway.framework.warehouse.GenericWareHouse;
import cn.uway.framework.warehouse.WarehouseReport;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.LocalDiskDetector;
import cn.uway.util.StringUtil;
import cn.uway.util.ThreadUtil;

/**
 * AbstractTaskWorker 抽象的taskWorder实现 给出TaskWorder具体实现思路 子类只需要实现具体的执行方式
 * 
 * @author chenrongqiang 2012-12-2
 */
public abstract class AbstractTaskWorker implements TaskWorker {
	/**
	 * list 对象，　加一个date，以便可以快速排序
	 */
	public static class GatherObjEntry {

		/**
		 * 文件名
		 */
		public String fileName;

		/**
		 * 文件时间
		 */
		public Date date;

		public GatherObjEntry(String fileName, Date date) {
			this.fileName = fileName;
			this.date = date;
		}
	}

	/**
	 * 任务的分组信息 @ 2015-3-6
	 */
	public static class TaskGroupFilesInfo {

		/**
		 * 最后一次用于解析的分组文件个数
		 */
		public int lastGroupFileCount;

		/**
		 * 最后一次文件个数异常的分组数量
		 */
		public int lastExcpGroupFileHitCount;

		/**
		 * 最后一次文件个数异常的分组扫描时间
		 */
		public Date lastExcpGroupFileScanTime;
	}

	/**
	 * 任务分组 @ 2015-3-6
	 */
	public static class GroupFileInfo {

		public GroupFileInfo(String groupName) {
			this.groupName = groupName;
		}

		/**
		 * 分组名称
		 */
		public String groupName;

		/**
		 * 文件列表
		 */
		public List<String> fileList = new ArrayList<String>();
	}

	protected static final int MAX_INIT_CACHE_FILE_COUNT = 30;

	/**
	 * 任务对象
	 */
	protected Task task;

	/**
	 * 当前周期已经扫描到的采集对象
	 */
	protected int currPeriodScanedObjectEntryNumber;

	/**
	 * 当前周期扫描到文件已在igp_data_gather_obj_status和fileNamesCache中的已采集过对象数;
	 */
	protected int currPeriodCollectedAlreadyObjectEntryNumber;

	/**
	 * 任务数据源连接信息
	 */
	protected ConnectionInfo connInfo;

	/**
	 * 任务采集对象
	 */
	protected List<GatherPathEntry> pathEntries = new ArrayList<GatherPathEntry>(1024);

	/**
	 * 状态表操作DAO
	 */
	protected StatusDAO statusDAO = AppContext.getBean("statusDAO", StatusDAO.class);

	/**
	 * 连接信息查询DAO
	 */
	protected ConnectionInfoDAO connectionInfoDAO = AppContext.getBean("connectionInfoDAO", DatabaseConnectionInfoDAO.class);

	/**
	 * 任务DAO对象，此处用于给任务修改时间点。
	 */
	protected TaskDAO taskDAO = AppContext.getBean("taskDAO", TaskDAO.class);

	/**
	 * 汇总日志表,数据库汇总将根据此表中的记录为依据
	 */
	protected SummaryDAO summaryDAO = AppContext.getBean("summaryDAO", SummaryDAO.class);

	/**
	 * 每个Job结束后返回的报告数据,主要用于ds_log_clt_to_group中插入数据
	 */
	protected List<JobFuture> jfList;

	/**
	 * Job执行线程池
	 */
	protected CompletionService<JobFuture> jobPool;

	protected ExecutorService es;

	/**
	 * 日志
	 */
	protected static final ILogger LOGGER = LoggerManager.getLogger(AbstractTaskWorker.class);

	/**
	 * 单任务最大并发Job线程数
	 */
	protected int systemMaxJobConcurrent = AppContext.getBean("maxJobSize", Integer.class);

	protected boolean isDsLogOn = false;

	protected int maxConcurentJobThreadCount = 0;

	protected TimeWildcardHandler timeWildcardHandler;

	/**
	 * 采集的数据开始时间
	 */
	protected Date dataTime;

	/**
	 * 当从ftp或sftp等list出来文件后，查数据库状态表的最大时间(防止同一次查找次数太多，导致采集假死现象)
	 */
	protected int MAX_GATHER_FILES_QUERY_DB_TIMES = 90 * 1000;

	/**
	 * 周期性采集任务缺失文件检测时间，单位小时，默认72小时 (当采集任务以周期性采集时，假如当前时间是6有10日0，那么在采集6-7号前的所有文件， 无论文件是否完整或存在，系统每次运行后，跳到下一个时间点)
	 */
	protected final static int DEFAULT_PERIOD_TASK_MISS_FILE_CHECK_HOUR = 72;

	protected long periodTaskMissFileCheckHour = DEFAULT_PERIOD_TASK_MISS_FILE_CHECK_HOUR;

	/** 周期性ftp采集双周期检查标志,和skipNextPeriod配套使用 */
	protected boolean periodFtpDoublePeriodCheck = AppContext.getBean("periodFtpDoublePeriodCheck", Boolean.class);

	/** 当前任务是否可以跳到下一采集周期，和periodFtpDoublePeriodCheck配套使用 */
	protected boolean skipNextPeriod;
	
	/**
	 * 任务交付接口
	 */
	protected ITaskDeliver deliver;

	/**
	 * 文件时间比较器(仅适用于采集路径带时间通配符)。 @ 2015-3-4
	 */
	public class FileDateTimeComparator implements Comparator<GatherObjEntry> {

		@Override
		public int compare(GatherObjEntry s1, GatherObjEntry s2) {
			Date d1 = s1.date;
			Date d2 = s2.date;

			int ret = 0;
			if (d1 == null && d2 == null)
				ret = 0;
			else if (d1 == null)
				ret = 1;
			else if (d2 == null)
				ret = -1;
			else
				ret = d1.compareTo(d2);

			// 如果不包含时间信息 则使用自然排序
			if (ret == 0)
				ret = s1.fileName.compareTo(s2.fileName);

			return ret;
		}

		public boolean equals(Object obj) {
			return obj.equals(this);
		}
	}

	public AbstractTaskWorker(Task task) {
		String sPeriodTaskMissFileCheckHour = AppContext.getBean("periodTaskMissFileCheckHour", String.class);
		if (sPeriodTaskMissFileCheckHour.indexOf("$") < 0) {
			try {
				this.periodTaskMissFileCheckHour = Long.parseLong(sPeriodTaskMissFileCheckHour);
			} catch (Exception e) {
				this.periodTaskMissFileCheckHour = DEFAULT_PERIOD_TASK_MISS_FILE_CHECK_HOUR;
			}
		}

		String dsLogOn = AppContext.getBean("isDsLogOn", String.class);
		if (!dsLogOn.contains("$") && dsLogOn.trim().toLowerCase().equals("true")) {
			isDsLogOn = true;
		}

		this.task = task;
		getConnectionInfo();
	}

	/**
	 * 加载任务对应的连接信息<br>
	 */
	private void getConnectionInfo() {
		int connId = this.task.getConnectionId();
		ConnectionInfo connInfo = connectionInfoDAO.getConnectionInfo(connId);
		if (connInfo == null) {
			ImportantLogger.getLogger().error("任务配置错误。连接ID={}配置不存在", connId);
			throw new IllegalArgumentException("任务配置错误。连接ID=" + connId + "配置不存在");
		}
		this.connInfo = connInfo;
		this.task.getExtraInfo().setVendor(connInfo.getVendor());
	}

	/**
	 * 线程重命名<br>
	 * 主要是为了打印日志上显示线程方法
	 */
	protected void rename() {
		if (task instanceof DelayTask) {
			// 延迟任务
			DelayTask dTask = (DelayTask) task;
			Thread.currentThread().setName("[" + task.getId() + "-" + dTask.getDelayId() + " D]Worker");
		} else if (task instanceof ReTask) {
			// 补采任务
			ReTask reTask = (ReTask) task;
			Thread.currentThread().setName("[" + task.getId() + "-" + reTask.getrTaskId() + "]Worker");
		} else {
			Thread.currentThread().setName("[" + task.getId() + "]Worker");
		}
	}

	/**
	 * 线程执行方法
	 */
	public TaskFuture call() throws Exception {
		rename();
		// 由子类具体实现 如进行通配符转换等
		this.currPeriodScanedObjectEntryNumber = 0;
		this.currPeriodCollectedAlreadyObjectEntryNumber = 0;
		beforeWork();
		TaskFuture taskFuture = new TaskFuture();
		taskFuture.setTask(this.task);
		if (pathEntries == null || pathEntries.size() == 0) {
			LOGGER.debug("当前任务没有采集对象，TaskWorker退出。本次共扫描到:{}个对象, 已经采集过:{}个对象 ", new Object[]{this.currPeriodScanedObjectEntryNumber,
					this.currPeriodCollectedAlreadyObjectEntryNumber});
			// 提交运行过程中所产生的补采任务。
			if (task instanceof PeriodTask) {
				// 如果是延迟数据采集，如果没有发现有新文件到达，那么将data_scan_time更新到下一时间点，
				// 并且不添加补采，因为延迟数据任务是轻量级的
				if (task instanceof DelayTask) {
					taskDAO.skipDelayTaskNextTime((DelayTask) task);
				} else if (task instanceof ReTask) {
					for (ReTask regatherInfo : task.getRegatherInfoStatics()) {
						this.taskDAO.insertIntoRTaskRecords(regatherInfo.getId(), regatherInfo.getRegatherPath(),
								regatherInfo.getRegather_datetime(), regatherInfo.getCause());
					}
				} else {
					/**
					 * 以下几种情况，任意满足其中一种条件，周期性任务在没有扫描到采集数据情况时，<br/>
					 * 也将igp_cfg_task.data_time跳到下一个时间点:<br/>
					 * 
					 * 1:当前时间已干过文件，并没有新文件产生时;(20161212新增:没有开启ftp文件double period check功能)<br/>
					 * 2:如果task数据采集数据时间点超过当前系统时间的X小时;<br/>
					 * 3:开启了ftp文件double period check,并且下个周期已经有数据;<br/>
					 */
					if (!periodFtpDoublePeriodCheck && currPeriodCollectedAlreadyObjectEntryNumber > 0) {
						LOGGER.debug("当前周期扫描到文件已在igp_data_gather_obj_status和fileNamesCache中的已采集过对象数为{};并且没有开启ftp文件double period check功能,任务时间点将更新至下一周期.",currPeriodCollectedAlreadyObjectEntryNumber);
						this.taskDAO.skipTime((PeriodTask) task);
					}else if (periodFtpDoublePeriodCheck && skipNextPeriod) {
						LOGGER.debug("当前周期没有扫描到文件,但下个周期扫描到了文件,并且已经开启ftp文件double period check功能,任务时间点将更新至下一周期.");
						this.taskDAO.skipTime((PeriodTask) task);
					}else{
						Date currTime = new Date();
						if (currTime.getTime() - task.getDataTime().getTime() > periodTaskMissFileCheckHour * 60 * 60 * 1000L) {
							LOGGER.debug("系统当前时间{}已经大于任务当前周期时间{},{}小时以上,任务时间点将更新至下一周期.",new Object[]{currTime,task.getDataTime(),periodTaskMissFileCheckHour});
							this.taskDAO.skipTime((PeriodTask) task);
						}
					}
				}
			}

			return taskFuture;
		}
		
		try {
			/*maxConcurentJobThreadCount = getMaxConcurentJobThreadCount();
			if (maxConcurentJobThreadCount <= 0)
				maxConcurentJobThreadCount = 1;*/
			LOGGER.debug("待采集对象个数：{}, 本次共扫描到对象个数:{}，已采集对象个数：{} ", new Object[]{pathEntries.size(), this.currPeriodScanedObjectEntryNumber,
					this.currPeriodCollectedAlreadyObjectEntryNumber});
			//LOGGER.debug("实际单任务并发JOB个数（创建线程数）：{}", maxConcurentJobThreadCount);
			
			for (int i = 0; i < pathEntries.size(); i++) {
				// 创建具体job，并且提交至线程池中
				GatherPathEntry pathEntry = pathEntries.get(i);
				boolean delived = deliver.submit(task, connInfo, pathEntry, i);
				if (delived) {
					String decodedFTPPath = getPathEntry(pathEntry, connInfo);
					
					Status gatherObjStatus = new Status();
					gatherObjStatus.setTaskId(task.getId());
					gatherObjStatus.setGatherObj(decodedFTPPath);
					gatherObjStatus.setPcName(task.getPcName());
					// 周期性任务需要加上数据时间进行判断
					if (task instanceof PeriodTask)
						gatherObjStatus.setDataTime(task.getDataTime());
					
					Date rawFileTime = AbstractTaskWorker.getGatherObjectDateTimeFromFileName(decodedFTPPath);
					if (rawFileTime != null) {
						gatherObjStatus.setDataTime(rawFileTime);
					}
					
					gatherObjStatus.setExportStatus(Status.JOB_COMMIT);
					gatherObjStatus.setStatus(Status.JOB_COMMIT);
					
					long statusID = statusDAO.log(gatherObjStatus);
					gatherObjStatus.setId(statusID);
					//gatherObjStatus.updateBySynchronized(statusDAO, gatherObjStatus.getId());
				}
			}
			
			//taskFuture = work();
		} finally {
			// afterWork可以被子类具体实现.
			afterWork(taskFuture);
		}
		
		return taskFuture;
	}

	/**
	 * 在Task执行完成以后 将周期性任务的数据时间修改为下一次执行时间 2015.10.21:如果文件正常解析，那么向ds_log_clt_to_group中插入一条记录
	 */
	public void afterWork(TaskFuture taskFuture) {
		LOGGER.debug("afterWork(), code={} cause={}", taskFuture.getCode(), taskFuture.getCause());
		if (taskFuture.getCode() < 0)
			return;

		// 添加补采任务（特殊采集)。延迟数据任务不添加补采
		if (task instanceof PeriodTask && !(task instanceof DelayTask)) {
			for (ReTask regatherInfo : task.getRegatherInfoStatics()) {
				this.taskDAO.insertIntoRTaskRecords(regatherInfo.getId(), regatherInfo.getRegatherPath(), regatherInfo.getRegather_datetime(),
						regatherInfo.getCause());
			}
		}

		// 正常周期性任务，修改data_time至下一时间点。补采任务不需要，
		if (task instanceof PeriodTask && !(task instanceof ReTask)) {
			if (task instanceof DelayTask) {
				// 如果是延迟数据采集，那么将data_scan_time更新到下一时间点,
				// 并将采集到的文件名记录在igp_data_gather_obj_status中(已经在AbstractJob中记录过)
				taskDAO.skipDelayTaskNextTime((DelayTask) task);
			} else {
				// add by tyler 20161212.如果doublecheck的功能开启，那么只有当下一个周期有数据的时候，
				// 任务才会更新到下一周期时间点，这样就不需要设置任务延时时间；
				// 否则还是按原来的逻辑执行，只要一个周期有文件采集到就更新到下一个时间点；
				boolean flag = false;
				if (periodFtpDoublePeriodCheck) {
					if (skipNextPeriod) {
						flag = this.taskDAO.skipTime((PeriodTask) task);
					}
				} else {
					flag = this.taskDAO.skipTime((PeriodTask) task);
				}
				// 如果是周期性任务并且其对应延迟任务的延迟扫描周期字段和延迟时长字段都不等于0，则新增一条延迟任务
				if (flag && task.getDelayDataScanPeriod() != 0 && task.getDelayDataTimeDelay() != 0) {
					taskDAO.insertNewDelayTask(task);
				}
			}
		} else if (task instanceof ReTask) {
			// 补采状态是成功还是失败，是这样判断的，如果补采任务在此次运行过程中，没有再次产生补采，那么就认为是成功的。
			int status = (task.getRegatherInfoStatics().isEmpty() ? ReTask.SUCCESS_COLLECT_STATUS : ReTask.FAIL_COLLECT_STATUS);
			this.taskDAO.updateReTaskStatus(((ReTask) task).getrTaskId(), status, (status == ReTask.SUCCESS_COLLECT_STATUS ? new Date() : null));
		}

		LOGGER.debug("data_time={},task={},code={" + String.valueOf(taskFuture.getCode()) + "}", task.getDateString(task.getDataTime()),
				(task instanceof DelayTask) ? "DelayTask" : (task instanceof ReTask) ? "ReTask" : (task instanceof PeriodTask)
						? "PeriodTask"
						: "Task");
		
		ThreadUtil.sleep(3000);
	}

	/**
	 * 往ds_log_clt_to_group中插入一条记录
	 * 
	 * @param isRepair
	 */
	protected void insertDsLog(int isRepair) {
		int totalNum = 0;
		int succNum = 0;
		int failNum = 0;
		for (JobFuture jobFuture : jfList) {
			WarehouseReport wr = jobFuture.getWarehouseReport();
			if (wr != null) {
				totalNum += wr.getTotal();
				succNum += wr.getSucc();
				failNum += wr.getFail();
			}
		}
		LOGGER.debug("向ds_log_clt_to_group表中插入记录，data_time={},task={}", task.getDateString(task.getDataTime()), (task instanceof DelayTask)
				? "DelayTask"
				: (task instanceof ReTask) ? "ReTask" : "Task");
		summaryDAO.insert(task, " ", 0, totalNum, succNum, failNum, isRepair);
	}

	/**
	 * 1、检查采集对象是否已经被采集<br>
	 * 2、如果任务的数据时间和结束时间不为空 并且采集对象中包含时间信息 则校验时间
	 * 
	 * @param gatherPath
	 *            文件名整串
	 * @param timeEntry
	 *            用于标识文件时间的串(防止在正则表达式匹配时出错)
	 * @return 是否需要采集
	 */
	protected boolean checkGatherObject(String gatherPath, Date timeEntry) {
		if (task instanceof PeriodTask) {
			timeEntry = task.getDataTime();
		}
		
		// 如果FTP文件小于系统设置的文件生命周期 则忽略
		if (!checkBeginTime(gatherPath, timeEntry))
			return false;

		Status objStatus = new Status();
		Date dateTime = task.getDataTime();
		long taskId = task.getId();
		// 补采另行处理
		if (task instanceof ReTask) {
			ReTask reTask = (ReTask) task;
			taskId = reTask.getrTaskId();
			dateTime = reTask.getRegather_datetime();
		}
		objStatus.setTaskId(taskId);

		String decodedPath = getFtpFileDecodeShortName(gatherPath);
		objStatus.setGatherObj(decodedPath);
		if (task instanceof PeriodTask)
			objStatus.setDataTime(dateTime);
		boolean flag = statusDAO.needToGather(objStatus);
		// 如果还没采集过 则判断一下数据结束时间
		return flag == false ? flag : checkEndTime(gatherPath, timeEntry);
	}

	/*
	 * gatherPath这个路径的来源是从FTP获取的文件列表，需要按照本地编码格式转换
	 */
	public String getFtpFileDecodeShortName(String gatherPath) {
		String decodedPath = gatherPath;
		if (connInfo instanceof FTPConnectionInfo) {
			FTPConnectionInfo ftpConnInfo = (FTPConnectionInfo) connInfo;
			decodedPath = StringUtil.decodeFTPPath(gatherPath, ftpConnInfo.getCharset());
		}
		return StringUtil.getFilename(decodedPath);
	}

	/**
	 * 采集开始时间校验
	 * 
	 * @param gatherPath
	 * @return
	 */
	protected abstract boolean checkBeginTime(String gatherPath, Date timeEntry);

	/**
	 * 结束时间校验
	 * 
	 * @param gatherPath
	 * @return
	 */
	protected abstract boolean checkEndTime(String gatherPath, Date timeEntry);

	/**
	 * 创建作业对象
	 * 
	 * @param pathEntry
	 * @return 作业对象
	 */
	protected Job createJob(GatherPathEntry pathEntry) {
		GatherSolution solution = SolutionLoader.getSolution(task);
		JobParam param = new JobParam(task, connInfo, solution, pathEntry);
		if (solution.isAdaptiveStreamJobAvaliable())
			return new AdaptiveStreamJob(param);
		else
			return new GenericJob(param);
	}

	public TaskFuture work() {
		// 如果使用warehouse已经超过系统配置 则提交线程一直休眠 每次休眠1秒钟
		long nWaitTickCount = 0;
		long nStartTime = new Date().getTime();
		while (!GenericWareHouse.getInstance().isWarehouseReady(task.getId())) {
			if ((++nWaitTickCount % 30) == 0) {
				long timeElase = (new Date().getTime() - nStartTime) / 1000;
				LOGGER.error("taskId={} 超过了warehouse的最大使用数，现暂停运行，已等待了{}秒", new Object[]{task.getId(), timeElase});
			}
			ThreadUtil.sleep(1000);
		}

		TaskFuture taskFuture = new TaskFuture();
		taskFuture.setTask(task);
		int submitNum = 0;
		// 第一次最多值提交maxConcurentJobThreadCount个
		for (int i = 0; i < maxConcurentJobThreadCount && pathEntries.size() > i; i++) {
			// 创建具体job，并且提交至线程池中
			GatherPathEntry pathEntry = pathEntries.get(i);
			Job job = createJob(pathEntry);
			// 增加磁盘空间检测
			LocalDiskDetector.getInstance().detect();
			jobPool.submit(job);
			submitNum++;
		}
		
		take(taskFuture, submitNum);
		// 只要有一个线程执行出错 则线程池停止
		return taskFuture;
	}

	void take(TaskFuture taskFuture, int submitNum) {
		for (int i = 0; i < submitNum; i++) {
			Future<JobFuture> future;
			try {
				future = jobPool.take();
				if (future == null) {
					LOGGER.error("taskId={} 提取job线程返回结果为空。", task.getId());
					taskFuture.setCode(-1);
					taskFuture.setCause("提取job线程返回结果为空");
					break;
				}
				JobFuture jobFuture = future.get();
				jfList.add(jobFuture);
				int code = jobFuture.getCode();

				if (code == TaskWorkTerminateException.exceptionCode) {
					LOGGER.debug("taskId={} job被要求终止执行,cause={}", task.getId(), jobFuture.getCause());
					taskFuture.setCode(TaskWorkTerminateException.exceptionCode);
					taskFuture.setCause(jobFuture.getCause());
					// 采集要求终止.
					break;
				} else if (code != 0) {
					LOGGER.error("taskId={} job执行异常,cause={}", task.getId(), jobFuture.getCause());
					taskFuture.setCode(-1);
					taskFuture.setCause(jobFuture.getCause());
					// break;
				}
			} catch (InterruptedException e) {
				LOGGER.error("taskId={} 提取job线程返回结果异常。", task.getId(), e);
				taskFuture.setCode(-1);
				taskFuture.setCause(e.getMessage());
				break;
			} catch (ExecutionException e) {
				LOGGER.error("taskId={} 提取job线程返回结果异常。", task.getId(), e);
				taskFuture.setCode(-1);
				taskFuture.setCause(e.getMessage());
				break;
			}
			if (!hasMoreEntry(submitNum)) {
				continue;
			}
			// 如果使用warehouse已经超过系统配置 则提交线程一直休眠 每次休眠1秒钟
			while (!GenericWareHouse.getInstance().isWarehouseReady(task.getId())) {
				ThreadUtil.sleep(1000);
			}
			GatherPathEntry pathEntry = pathEntries.get(submitNum);
			Job job = createJob(pathEntry);
			jobPool.submit(job);
			submitNum++;
		}
	}

	/**
	 * 是否还有更多的采集对象需要采集<br>
	 * 
	 * @param submitNum
	 * @return boolean
	 */
	boolean hasMoreEntry(int submitNum) {
		return submitNum < pathEntries.size();
	}

	/**
	 * 获取最大并发运行Job数,根据采集对象和资源情况决定
	 */
	protected abstract int getMaxConcurentJobThreadCount();

	/**
	 * 获取采集对象的采集时间，如果用户未在采集路径中指定时间，则从文件路径中，按常用的日期格式，获取文件时间
	 * 
	 * @param gatherPath
	 *            文件路径
	 * @return 文件时间
	 */
	public Date getGatherObjectDateTime(String gatherPath) {
		String patternTime = null;
		if (timeWildcardHandler != null) {
			patternTime = StringUtil.getPattern(gatherPath, timeWildcardHandler.regexExpression);
			if (patternTime != null) {
				return timeWildcardHandler.getDateTime(gatherPath);
			}
		}

		return getGatherObjectDateTimeFromFileName(gatherPath);
	}

	/**
	 * 从文件路径中，按常用的日期格式，获取文件时间
	 * 
	 * @param gatherPath
	 *            文件路径
	 * @return 文件时间
	 */
	public static Date getGatherObjectDateTimeFromFileName(String gatherPath) {
		String patternTime = null;
		patternTime = StringUtil.getPattern(gatherPath, "\\d{4}[-]\\d{2}[-]\\d{2}[_]\\d{2}[-]\\d{2}");
		if (patternTime != null) {
			return getDateTime(patternTime, "yyyy-MM-dd_HH-mm");
		}

		patternTime = StringUtil.getPattern(gatherPath, "\\d{8}[_]\\d{4}[.]");
		if (patternTime != null) {
			patternTime = patternTime.replace(".", "");
			return getDateTime(patternTime, "yyyyMMdd_HHmm");
		}

		patternTime = StringUtil.getPattern(gatherPath, "\\d{4}[-]\\d{2}[-]\\d{2}[-]\\d{2}[-]\\d{2}");
		if (patternTime != null) {
			return getDateTime(patternTime, "yyyy-MM-dd-HH-mm");
		}

		patternTime = StringUtil.getPattern(gatherPath, "\\d{4}[_]\\d{2}[_]\\d{2}[_]\\d{2}[_]\\d{2}");
		if (patternTime != null) {
			return getDateTime(patternTime, "yyyy_MM_dd_HH_mm");
		}

		// 普天LTE
		patternTime = StringUtil.getPattern(gatherPath, "20\\d{10}[.]");
		if (patternTime != null) {
			patternTime = patternTime.replace(".", "");
			return getDateTime(patternTime, "yyyyMMddHHmm");
		}

		// 大唐LTE
		patternTime = StringUtil.getPattern(gatherPath, "[-]\\d{8}[-]\\d{4}");
		if (patternTime != null) {
			patternTime = patternTime.replace("-", "");
			return getDateTime(patternTime, "yyyyMMddHHmm");
		}

		patternTime = StringUtil.getPattern(gatherPath, "\\d{8}[.]\\d{4}");
		if (patternTime != null) {
			return getDateTime(patternTime, "yyyyMMdd.HHmm");
		}

		// 山东LTE中兴参数
		patternTime = StringUtil.getPattern(gatherPath, "[_]\\d{14}");
		if (patternTime != null) {
			patternTime = patternTime.replace("_", "");
			return getDateTime(patternTime, "yyyyMMddHHmmss");
		}

		// lte华为参数
		patternTime = StringUtil.getPattern(gatherPath, "[_]20\\d{8}");
		if (patternTime != null) {
			patternTime = patternTime.replace("_", "");
			return getDateTime(patternTime, "yyyyMMddHH");
		}

		// lte华为参数 江苏电信 HW_CM_20150810_G0312.tar.gz
		patternTime = StringUtil.getPattern(gatherPath, "[_]20\\d{6}[_]");
		if (patternTime != null) {
			patternTime = patternTime.replace("_", "");
			return getDateTime(patternTime, "yyyyMMdd");
		}

		patternTime = StringUtil.getPattern(gatherPath, "\\d{12}");
		if (patternTime != null) {
			return getDateTime(patternTime, "yyyyMMddHHmm");
		}

		patternTime = StringUtil.getPattern(gatherPath, "\\d{10}");
		if (patternTime != null) {
			return getDateTime(patternTime, "yyyyMMddHH");
		}

		patternTime = StringUtil.getPattern(gatherPath, "\\d{8}");
		if (patternTime != null) {
			return getDateTime(patternTime, "yyyyMMdd");
		}

		return null;
	}

	/**
	 * 开始时间检查<br>
	 * FTP方式 如果厂家文件保留时间较长 则会引起IGP 状态记录表数据暴增 增加开始时间校验
	 */
	protected boolean checkBeginTimeDefault(Date gatherDateTime) {
		if (this.dataTime == null)
			return true;

		Date fileDate = gatherDateTime;
		if (fileDate == null)
			return false;

		if (fileDate.compareTo(this.dataTime) < 0)
			return false;

		return true;
	}

	/**
	 * 结束时间校验
	 * 
	 * @param gatherPath
	 * @return
	 */
	protected boolean checkEndTimeDefault(Date gatherDateTime) {
		if (this.task.getEndDataTime() == null)
			return true;

		Date fileDate = gatherDateTime;
		if (fileDate == null)
			return false;

		if (fileDate.compareTo(this.task.getEndDataTime()) > 0)
			return false;

		return true;
	}

	// 将时间转换成format格式的字符串
	public String getDateTimeString(Date date, String format) {
		if (date == null) {
			return null;
		}
		if (format == null) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		try {
			DateFormat df = new SimpleDateFormat(format);
			return df.format(date);
		} catch (Exception e) {
			return null;
		}
	}

	// 将时间转换成format格式的Date
	public static final Date getDateTime(String date, String format) {
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

	/**
	 * 根据任务运行时间区间，获得sortedFiles中，最小的日期
	 * 
	 * @param sortedFiles
	 *            已排序过的文件属性set
	 * @return
	 */
	public Date getTaskMinGatherTime(Set<GatherObjEntry> sortedFiles) {
		Date taskBeginTime = this.dataTime;
		for (GatherObjEntry entry : sortedFiles) {
			if (entry.date == null)
				continue;

			if (taskBeginTime != null && entry.date.compareTo(taskBeginTime) < 0)
				continue;

			return entry.date;
		}

		return null;
	}

	/**
	 * 根据任务运行时间区间，获得sortedFiles中，最大的日期
	 * 
	 * @param sortedFiles
	 *            已排序过的文件属性set
	 * @return
	 */
	public Date getTaskMaxGatherTime(Set<GatherObjEntry> sortedFiles) {
		Date taskEndTime = this.task.getEndDataTime();
		Date maxDate = null;

		for (GatherObjEntry entry : sortedFiles) {
			if (entry.date == null)
				continue;

			if (taskEndTime != null && entry.date.compareTo(taskEndTime) > 0)
				return maxDate;

			if (maxDate == null || maxDate.compareTo(entry.date) < 0) {
				maxDate = entry.date;
			}
		}

		return maxDate;
	}

	/**
	 * 对散列文件按时间相同的进行分组
	 * 
	 * @param sortedFiles
	 * @return
	 */
	protected List<GroupFileInfo> groupFilesEntryByteFileTime(Set<GatherObjEntry> sortedFiles) {
		List<GroupFileInfo> groupedEntry = new ArrayList<GroupFileInfo>();
		GroupFileInfo singleGroupEntry = null;
		long sortKey = -1;
		// 对采集对象进行分组 目前针对GPEH使用 建议在任务表进行配置
		for (GatherObjEntry sortedFile : sortedFiles) {
			Date fileDateTime = sortedFile.date;
			Long groupingInfo = 0L;
			if (fileDateTime != null)
				groupingInfo = fileDateTime.getTime();

			// 以分钟为单位分组
			groupingInfo -= groupingInfo % (60 * 1000L);
			if (sortKey == -1)
				sortKey = groupingInfo;

			if (sortKey == groupingInfo) {
				if (singleGroupEntry == null) {
					singleGroupEntry = new GroupFileInfo(getDateTimeString(new Date(groupingInfo), "yyyyMMddHHmmss"));
				}
				singleGroupEntry.fileList.add(sortedFile.fileName);
				continue;
			}
			// 如果碰到新的时间的文件 则把singleGroupEntry加入到groupedEntry中
			// ，并且创建一个新的singleGroupEntry
			groupedEntry.add(singleGroupEntry);
			sortKey = groupingInfo;
			singleGroupEntry = new GroupFileInfo(getDateTimeString(new Date(groupingInfo), "yyyyMMddHHmmss"));
			singleGroupEntry.fileList.add(sortedFile.fileName);
		}

		// 将最后一个group添加到组中
		if (singleGroupEntry != null && singleGroupEntry.fileList.size() > 0) {
			groupedEntry.add(singleGroupEntry);
		}

		return groupedEntry;
	}
	
	public ITaskDeliver getDeliver() {
		return deliver;
	}

	public void setDeliver(ITaskDeliver deliver) {
		this.deliver = deliver;
	}
	
	public static final String getPathEntry(GatherPathEntry pathEntry, ConnectionInfo connInfo) {
		String path = pathEntry.getPath();
		/*
		 * gatherPath这个路径的来源是从FTP获取的文件列表，需要按照本地编码格式转换
		 * 
		 * @author Niow 2014-6-12
		 */
		if (connInfo instanceof FTPConnectionInfo) {
			FTPConnectionInfo ftpConnInfo = (FTPConnectionInfo)connInfo;
			path = StringUtil.decodeFTPPath(path, ftpConnInfo.getCharset());
		}
		return StringUtil.getFilename(path);
	}
}
