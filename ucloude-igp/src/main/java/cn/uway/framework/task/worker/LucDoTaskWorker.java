package cn.uway.framework.task.worker;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.framework.connection.TelnetConnectionInfo;
import cn.uway.framework.connection.pool.ftp.BasicFTPClientPool;
import cn.uway.framework.connection.pool.ftp.FTPClientPool;
import cn.uway.framework.connection.pool.ftp.FTPClientPoolFactory;
import cn.uway.framework.connection.pool.ftp.KeyedFTPClientPoolMgr;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.job.Job;
import cn.uway.framework.job.JobFuture;
import cn.uway.framework.task.GatherPathDescriptor;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.framework.task.TaskFuture;
import cn.uway.framework.task.worker.luc.EvdoCutterParam;
import cn.uway.framework.task.worker.luc.LucEvdoCutter;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.FTPUtil;
import cn.uway.util.FileUtil;
import cn.uway.util.IoUtil;
import cn.uway.util.StringUtil;

/**
 * LucDo 边下载边分拆方式 TaskWorker
 * 
 * @author yuy @ 2013-9-5
 */
public class LucDoTaskWorker extends AbstractTaskWorker {

	public static final ILogger LOGGER = LoggerManager.getLogger(LucDoTaskWorker.class);

	protected TelnetConnectionInfo sourceConnectionInfo = null;

	public static final String LucDoTmpFileDir = AppContext.getBean("LucDoTmpFileDir", String.class);

	protected List<FTPFile> remoteFilePaths = null;

	protected File[] localOkFiles = null;

	protected File[] localEvdoFiles = null;

	protected String lucdoFileExtentName = ".EVDOPCMD";

	protected String okFileExtentName = ".OK";

	protected String prompt = "";

	protected int flagCount = 0;

	/* 保存最后一次取得的流。 */
	private InputStream currIn;

	/* 要连接的FTP的IP. */
	public String ftpIP;

	/* FTP连接池。 */
	public FTPClientPool ftpPool;

	/* FTP连接。 */
	public FTPClient ftpClient;

	/* 是否使用FTP被动模式。 */
	public boolean bPasv;

	/* 默认FTP测试命令，用于验证FTP连接是否仍然存活。 */
	public static final String DEFAULT_FTP_VALIDATE_CMD = "pwd";

	/* 默认FTP最大连接数量。 */
	public static final int DEFAULT_FTP_MAX_CONNECTION = 5;

	/* 默认FTP最大等待时长（秒），连接池无空闲连接时的最大等待时间，超过此时间后抛出异常。 */
	public static final int DEFAULT_FTP_MAX_WAIT_SECOND = 300;

	/* 默认FTP操作的重试次数。 */
	public static final int DEFAULT_FTP_RETRY_TIMES = 3;

	/* 默认FTP操作的重试间隔时间（毫秒）。 */
	public static final long DEFAULT_FTP_RETRY_DELAY_MILLS = 3 * 1000;

	/* FTP测试命令，用于验证FTP连接是否仍然存活。 */
	public String validateCmd;

	/* FTP最大连接数量。 */
	public int maxConnections;

	/* FTP最大等待时长（秒），连接池无空闲连接时的最大等待时间，超过此时间后抛出异常。 */
	public int maxWaitSecond;

	/* FTP操作的重试次数。 */
	public int retryTimes;

	/* FTP连接池管理器，以FTP的IP为主键，存放及获取FTP连接池。 */
	@SuppressWarnings("unchecked")
	public KeyedFTPClientPoolMgr<String> poolMgr = AppContext.getBean("syncStringKeyFTPClientPoolMgr", KeyedFTPClientPoolMgr.class);;

	/* FTP连接池工厂。 */
	public FTPClientPoolFactory poolFactory = AppContext.getBean("basicFTPClientPoolFactory", FTPClientPoolFactory.class);;

	/* FTP操作的重试间隔时间（毫秒）。 */
	public long retryDelayMills;

	static {
		File dir = new File(LucDoTmpFileDir);
		if (!dir.exists() || !dir.isDirectory())
			dir.mkdirs();
	}

	public LucDoTaskWorker(Task task) {
		super(task);
		if (!(connInfo instanceof TelnetConnectionInfo))
			throw new IllegalArgumentException("连接信息不正确，错误的连接类型");
		sourceConnectionInfo = (TelnetConnectionInfo) connInfo;
		systemMaxJobConcurrent = AppContext.getBean("maxLucDoJobSize", Integer.class);
		pathEntries = Collections.synchronizedList(pathEntries);
		localFileHanlder(task);
	}

	public void localFileHanlder(Task task) {
		File splitFileDir = new File(LucDoTmpFileDir + File.separator + task.getExtraInfo().getBscId());
		if (!splitFileDir.exists() || !splitFileDir.isDirectory()) {
			splitFileDir.mkdirs();
		}

		// 扫描.OK文件
		localOkFiles = scanLocalFiles(splitFileDir, okFileExtentName);

		// 扫描.EVDOPCMD文件
		localEvdoFiles = scanLocalFiles(splitFileDir, lucdoFileExtentName);

		LOGGER.debug("扫描到的本地OK文件个数：" + localOkFiles.length);
		LOGGER.debug("扫描到的本地EVDOPCMD文件个数：" + localEvdoFiles.length);
		if (localEvdoFiles == null || localEvdoFiles.length == 0) {
			return;
		}
		for (File f : localEvdoFiles) {
			GatherPathEntry entry = new GatherPathEntry(f.getPath());
			// 这里第二个参数设置为null, 是因为先前人家在本类中的checkBeginTime和checkEndTime，直接返回true
			if (this.checkGatherObject(entry.getPath(), null)) {
				pathEntries.add(entry);
				if (entry.getPath().toUpperCase().endsWith("55" + lucdoFileExtentName.toUpperCase())) {
					entry.setLast(true);
					this.flagCount--;
				}
			}
		}
	}

	/**
	 * 扫描本地文件
	 * 
	 * @param splitFileDir
	 *            本地目录
	 * @param extentName
	 *            扩展名
	 * @return
	 */
	public File[] scanLocalFiles(File splitFileDir, final String extentName) {
		File[] files = splitFileDir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				name = name.toUpperCase();
				return name.endsWith(extentName.toUpperCase());
			}
		});
		return files;
	}

	/**
	 * 在开始work前 先检查目录上的文件 并排序
	 */
	public void beforeWork() {
		GatherPathDescriptor gatherPath = task.getGatherPathDescriptor();
		try {
			List<GatherPathEntry> pathEntrys = gatherPath.getPaths();
			ftpInit();

			// 先扫描服务器上匹配的文件
			for (GatherPathEntry pathEntry : pathEntrys) {
				// 查找配置的所有路径
				String path = pathEntry.getPath();
				if (task instanceof PeriodTask)
					path = pathEntry.getConvertedPath(task.getDataTime(), task.getWorkerType());

				if (remoteFilePaths == null) {
					remoteFilePaths = new ArrayList<FTPFile>();
				}
				try {
					remoteFilePaths.addAll(FTPUtil.listFilesRecursive(ftpClient, path, ""));
				} catch (IOException e) {
					LOGGER.error("在FTP上查找文件{}失败", gatherPath, e);
					BasicFTPClientPool.badFTPConnectsMap.put(ftpClient.hashCode(), true);
				}
				LOGGER.debug("在FTP上查找到（{}）的文件个数：{}", new Object[]{gatherPath, remoteFilePaths != null ? remoteFilePaths.size() : "<NULL>"});
			}

			FileNamesCache fileNamesCache = FileNamesCache.getInstance();
			// 这里锁的粒度大一些，锁住判断所有文件的过程。如果每个文件锁一次，切换太频繁。
			// 并且，可以降低对状态表访问的并发量。
			synchronized (fileNamesCache.getUseLock()) {
				for (FTPFile sortedFile : remoteFilePaths) {
					String filename = FileUtil.getFileFullName(sortedFile.getName());
					// 在之前的状态查询中，已经确定该文件不用采集，或是已经超过end_data_time.
					// 则跳过，不再在状态表中查询。
					if (fileNamesCache.isAlreadyGather(filename, task.getId())) {
						remoteFilePaths.remove(sortedFile);
						continue;
					}
					// 在状态表判断。
					// 这里第二个参数设置为null, 是因为先前人家在本类中的checkBeginTime和checkEndTime，直接返回true
					if (!checkGatherObject(filename, null)) {
						// 已经在状态表查询过，确定不用采这个文件了，则放入缓存，避免下次再在状态表查询。
						fileNamesCache.putToCache(filename, task.getId());
						remoteFilePaths.remove(sortedFile);
					}
				}
			}

			// 排序 递增
			sortedRemoteFilePaths();

		} catch (Exception e) {
			LOGGER.debug("error", e);
		} finally {
		}
	}

	/**
	 * 对文件进行递增排序
	 */
	public void sortedRemoteFilePaths() {
		if (remoteFilePaths != null && remoteFilePaths.size() > 0) {
			Map<Integer, FTPFile> map = new HashMap<Integer, FTPFile>();
			for (FTPFile file : remoteFilePaths) {
				String pathStr = StringUtil.convertCollectPath(file.getName(), task.getDataTime());
				String fileName = pathStr.substring(pathStr.lastIndexOf("/") + 1, pathStr.length());
				String name = fileName.substring(0, fileName.indexOf("."));
				map.put(Integer.parseInt(name), file);
			}
			Set<Integer> set = map.keySet();
			Integer[] array = new Integer[set.size()];
			set.toArray(array);

			// 冒泡排序
			Integer tmpVal = 0;
			for (int i = 0; i < array.length; i++) {
				for (int j = i + 1; j < array.length; j++) {
					if (array[i] > array[j]) {
						tmpVal = array[i];
						array[i] = array[j];
						array[j] = tmpVal;
					}
				}
			}
			remoteFilePaths.clear();
			for (Integer key : array) {
				remoteFilePaths.add(map.get(key));
			}
		}
	}

	/**
	 * 去掉返回回来的指令
	 * 
	 * @param cmd
	 * @param remoteFiles
	 * @return
	 */
	public String removeCmdStr(String cmd, String remoteFiles) {
		if (remoteFiles.indexOf(cmd) == 0) {
			remoteFiles = remoteFiles.substring(remoteFiles.indexOf(cmd) + cmd.length() + 2);
		}
		return remoteFiles;
	}

	/**
	 * 线程执行方法
	 */
	public TaskFuture call() throws Exception {
		renameThread();
		// 由子类具体实现 如进行通配符转换等
		beforeWork();

		// 过滤掉最新一个
		if (remoteFilePaths != null && remoteFilePaths.size() >= 1)
			remoteFilePaths.remove(remoteFilePaths.size() - 1);

		TaskFuture taskFuture = new TaskFuture();
		taskFuture.setTask(this.task);
		if ((remoteFilePaths == null || remoteFilePaths.size() == 0) && pathEntries.size() == 0) {
			LOGGER.debug("当前任务没有采集对象，TaskWorker退出。");
			close();
			return taskFuture;
		}

		// 远程文件下载切割
		cutRemoteLucDoFile();

		// 创建线程池
		maxConcurentJobThreadCount = getMaxConcurentJobThreadCount();
		if (maxConcurentJobThreadCount <= 0)
			maxConcurentJobThreadCount = 1;
		LOGGER.debug("待采集本地对象个数：{}", pathEntries.size());
		LOGGER.debug("实际单任务并发JOB个数（创建线程数）：{}", maxConcurentJobThreadCount);
		es = Executors.newFixedThreadPool(maxConcurentJobThreadCount);
		jobPool = new ExecutorCompletionService<JobFuture>(es);
		LOGGER.debug("Job线程池创建。");
		try {
			taskFuture = work();
		} finally {
			if (this.es != null) {
				this.es.shutdown();
				this.es = null;
				this.jobPool = null;
			}
			// afterWork由子类具体实现 如果是IGP自己任务 则更新任务时间，如果是JMS任务 则调用JMS通知OSP
			afterWork(taskFuture);
			close();
		}
		return taskFuture;
	}

	/**
	 * 拆分文件
	 * 
	 * @throws Exception
	 */
	public void cutRemoteLucDoFile() throws Exception {
		if (remoteFilePaths == null || remoteFilePaths.size() == 0)
			return;

		// 只采集倒数第二新的文件
		int index = remoteFilePaths.size() - 1;
		while (index > 0) {
			remoteFilePaths.remove(0);// 去掉最老的文件
			index = remoteFilePaths.size() - 1;
		}
		FTPFile remoteFile = remoteFilePaths.get(0);
		String path = remoteFile.getName();
		// 判断是否做完
		if (isDone(path)) {
			LOGGER.info("[{}]文件{}已采集过，此次不作采集", task.getId(), path);
			remoteFilePaths.remove(0);
		}
		// 判断是否分拆完
		else if (isCuttedDone(path)) {
			LOGGER.info("[{}]文件{}已分拆完，此次不作分拆", task.getId(), path);
			remoteFilePaths.remove(0);
		}
		// 开始下载切割
		else {
			beforeDown(path);

			// 开始拆分
			LucEvdoCutter cutter = new LucEvdoCutter(setEvdoCutterParam(remoteFile));
			Thread cutterThread = new Thread(cutter);
			cutterThread.start();
		}
	}

	@Override
	public TaskFuture work() {
		TaskFuture taskFuture = new TaskFuture();
		taskFuture.setTask(task);
		boolean flag = true;
		int submitNum = 0;
		while (flag && this.flagCount < remoteFilePaths.size()) {
			if (pathEntries.size() == 0) {
				try {
					Thread.sleep(100);
					continue;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					LOGGER.error("线程异常中断", e);
				}
			}
			GatherPathEntry pathEntry = pathEntries.remove(0);
			Job job = createJob(pathEntry);
			jobPool.submit(job);
			if (pathEntry.isLast()) {
				this.flagCount++;
			}
			submitNum++;
		}
		// 提取job线程返回结果并处理(把做完的文件改为OK文件)
		take(taskFuture, submitNum);

		return taskFuture;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.uway.igp3.task.worker.AbstractTaskWorker#take(cn.uway.igp3.task.TaskFuture , int)
	 */
	void take(TaskFuture taskFuture, int submitNum) {
		while (submitNum-- > 0) {
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
				int code = jobFuture.getCode();
				if (code != 0) {
					LOGGER.error("taskId={} job执行异常,cause={}", task.getId(), jobFuture.getCause());
					taskFuture.setCode(-1);
					taskFuture.setCause(jobFuture.getCause());
				}
				if (jobFuture.getAccessorReport() == null) {
					continue;
				}
				// 做完之后的操作
				String evdoFile = jobFuture.getAccessorReport().getGatherObj();
				if (evdoFile != null) {
					// 把做完的文件改为OK文件
					File file = new File(evdoFile);
					file.renameTo(new File(evdoFile + okFileExtentName));
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
			} catch (Exception e) {
				LOGGER.error("taskId={} 提取job线程返回结果异常。", task.getId(), e);
				taskFuture.setCode(-1);
				taskFuture.setCause(e.getMessage());
				break;
			}
		}
	}

	/**
	 * 设置切割参数
	 * 
	 * @param remoteFile
	 * @return
	 */
	public EvdoCutterParam setEvdoCutterParam(FTPFile file) {
		EvdoCutterParam evdoCutterParam = new EvdoCutterParam();
		evdoCutterParam.setHistoryFlag(true);
		evdoCutterParam.setEvdoName(file.getName());
		evdoCutterParam.setInputstream(currIn);
		evdoCutterParam.setPathEntries(pathEntries);
		evdoCutterParam.setTaskId(task.getId());
		evdoCutterParam.setBscId(task.getExtraInfo().getBscId());
		evdoCutterParam.setSize(file.getSize());
		return evdoCutterParam;
	}

	/**
	 * 下载之前的操作
	 * 
	 * @param remoteFile
	 * @throws Exception
	 */
	public void beforeDown(String path) throws Exception {
		if (task instanceof PeriodTask) {
			path = StringUtil.convertCollectPath(path, task.getDataTime());
		}

		LOGGER.debug("开始拆分：" + path);

		InputStream in = this._down(path);

		// 如果多次重试均无法获取FTP连接流 则直接异常退出
		if (in == null) {
			throw new Exception(this.retryTimes + "次重试下载失败,放弃此文件：" + path);
		}
		LOGGER.debug("生成InputStream");

		this.currIn = in;
	}

	/**
	 * ftp初始化 生成连接池
	 */
	public void ftpInit() throws Exception {
		this.ftpIP = sourceConnectionInfo.getIp();
		this.bPasv = true;
		this.maxConnections = 5;
		this.validateCmd = DEFAULT_FTP_VALIDATE_CMD;
		this.maxWaitSecond = DEFAULT_FTP_MAX_WAIT_SECOND;
		this.retryTimes = DEFAULT_FTP_RETRY_TIMES;
		this.retryDelayMills = DEFAULT_FTP_RETRY_DELAY_MILLS;

		// 检查FTP连接池管理器中是否已有针对此IP的FTP连接池，如果没有，先创建。
		this.ftpPool = this.getPoolMgr().getFTPClientPool(ftpIP);
		if (this.ftpPool == null) {
			// 创建FTP连接池。
			FTPConnectionInfo connectInfo = new FTPConnectionInfo(ConnectionInfo.CONNECTION_TYPE_FTP);
			connectInfo.setIp(sourceConnectionInfo.getIp());
			connectInfo.setPassword(sourceConnectionInfo.getPassword());
			connectInfo.setUserName(sourceConnectionInfo.getUserName());
			connectInfo.setPort(21);
			connectInfo.setValidateCmd(this.validateCmd);
			connectInfo.setMaxConnections(this.maxConnections);
			connectInfo.setMaxWaitSecond(this.maxWaitSecond);
			try {
				this.ftpPool = getPoolFactory().create(connectInfo);
				this.getPoolMgr().putFTPClientPool(ftpIP, this.ftpPool);
			} catch (Exception e) {
				LOGGER.error("创建FTP连接池异常。", e);
				return;
			}
		}

		try {
			this.ftpClient = this.ftpPool.getFTPClient();
		}
		// 异常处理，目前，有一个连接获取失败，所有路径都不再处理。
		catch (NoSuchElementException e) {
			throw new Exception("从FTP连接池中获取连接失败，等待超时，无可用的空闲连接。连接池最大连接数量：" + this.ftpPool.getMaxConnection() + "，当前已用连接数量："
					+ this.ftpPool.getCurrentActiveCount() + "，最大等待时间（秒）：" + this.ftpPool.getMaxWaitSecond(), e);
		} catch (Exception e) {
			throw new Exception("从FTP连接池中获取连接时发生异常", e);
		}
		LOGGER.debug("[FTPAccessor]获取FTP连接成功，KEY={}，当前池中活动连接数={}，最大连接数={}",
				new Object[]{this.ftpIP, ftpPool.getCurrentActiveCount(), ftpPool.getMaxConnection()});
		/* 主被动模式切换。 */
		if (this.bPasv) {
			this.ftpClient.enterLocalPassiveMode();
			LOGGER.debug("使用FTP被动模式。");
		} else {
			this.ftpClient.enterLocalActiveMode();
			LOGGER.debug("使用FTP主动模式。");
		}
	}

	/* FTP 下载过程，包括重试。 */
	public InputStream _down(String ftpPath) {
		InputStream in = _retrNoEx(ftpPath, ftpClient);
		if (in != null) {
			return in;
		}
		LOGGER.warn("FTP下载失败，开始重试，文件：{}，reply={}", new Object[]{ftpPath, ftpClient.getReplyString() != null ? ftpClient.getReplyString().trim() : ""});
		for (int i = 0; i < this.retryTimes; i++) {
			try {
				Thread.sleep(this.retryDelayMills);
			} catch (InterruptedException e) {
				LOGGER.warn("FTP 下载重试过程中线程被中断。", e);
				return null;
			}
			LOGGER.debug("第{}次重试下载，准备重新创建连接……", i + 1);
			this._completePendingCommandNoEx(ftpClient);
			FTPUtil.logoutAndCloseFTPClient(ftpClient);
			try {
				ftpClient = this.ftpPool.getFTPClient();
				LOGGER.debug("第{}次重试下载，重新创建连接成功。", i + 1);
			} catch (Exception e) {
				LOGGER.debug("第" + (i + 1) + "次重试下载，重新创建连接失败。", e);
			}
			in = _retrNoEx(ftpPath, ftpClient);
			if (in != null) {
				LOGGER.debug("第{}次重试下载成功。", i + 1);
				break;
			}
		}
		return in;

	}

	// FTP接收，处理异常。
	public InputStream _retrNoEx(String ftpPath, FTPClient ftpClient) {
		InputStream in = null;
		try {
			in = ftpClient.retrieveFileStream(ftpPath);
		} catch (IOException e) {
			LOGGER.error("FTP下载异常：" + ftpPath, e);
		}
		return in;

	}

	// 从FTP读取了流之后，需要读取FTP响应消息，否则下次操作时将会失败。
	public boolean _completePendingCommandNoEx(FTPClient ftpClient) {
		boolean b = true;
		try {
			b = ftpClient.completePendingCommand();
			if (!b)
				LOGGER.warn("FTP失败响应：{}", ftpClient.getReplyString());
		} catch (IOException e) {
			LOGGER.error("获取FTP响应异常。", e);
			return false;
		}
		return b;
	}

	public void close() {
		if (currIn != null) {
			IoUtil.readFinish(currIn); // 这里是将InputStream内容全部read完，否则FTP服务器会返回失败响应。
			IoUtil.closeQuietly(currIn);
		}
		if (this.ftpClient != null) {
			LOGGER.debug("读取FTP响应……");
			this._completePendingCommandNoEx(this.ftpClient); // 读取FTP服务器响应字符串。
			LOGGER.debug("读取FTP响应完成：{}", (this.ftpClient.getReplyString() != null ? this.ftpClient.getReplyString().trim() : "[null]"));
			LOGGER.debug("[FTPAccessor]准备将FTP连接归还入池，KEY=：{}，最大连接数：{}，当前连接数：{}",
					new Object[]{ftpIP, this.ftpPool.getMaxConnection(), this.ftpPool.getCurrentActiveCount()});
			FTPUtil.logoutAndCloseFTPClient(this.ftpClient);// 归还到池中。
			LOGGER.debug("[FTPAccessor]FTP连接归还入池，KEY=：{}，最大连接数：{}，当前连接数：{}",
					new Object[]{ftpIP, this.ftpPool.getMaxConnection(), this.ftpPool.getCurrentActiveCount()});
		}
	}

	/**
	 * 获取FTP连接池工厂。
	 * 
	 * @return FTP连接池工厂。
	 */
	public FTPClientPoolFactory getPoolFactory() {
		return poolFactory;
	}

	/**
	 * 获取FTP连接池管理器。
	 * 
	 * @return FTP连接池管理器。
	 */
	public KeyedFTPClientPoolMgr<String> getPoolMgr() {
		return poolMgr;
	}

	/**
	 * 判断是否做完。如果找到12个ok文件，说明已做完，此次不采集
	 * 
	 * @param pathStr
	 * @return
	 */
	private boolean isDone(String path) {
		String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
		String name = fileName.substring(0, fileName.indexOf("."));
		int c = 0;
		for (File okFile : localOkFiles) {
			// 统计ok文件个数
			if (okFile.getPath().contains(name) && okFile.getPath().endsWith(okFileExtentName)) {
				c++;
			}
		}
		if (c == 12)
			return true;
		return false;
	}

	/**
	 * 判断是否分拆完。如果ok文件的数量加上evdo文件的数量等于12，说明已分拆完
	 * 
	 * @param pathStr
	 * @return
	 */
	private boolean isCuttedDone(String path) {
		String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
		String name = fileName.substring(0, fileName.indexOf("."));
		int okCount = 0;
		int evdoCount = 0;
		// 统计ok文件个数
		for (File okFile : localOkFiles) {
			if (okFile.getPath().contains(name) && okFile.getPath().endsWith(okFileExtentName)) {
				okCount++;
			}
		}
		// 统计evdopcmd文件个数
		for (File evdoFile : localEvdoFiles) {
			if (evdoFile.getPath().contains(name) && evdoFile.getPath().endsWith(lucdoFileExtentName)) {
				evdoCount++;
			}
		}
		if (okCount + evdoCount == 12)
			return true;
		return false;
	}

	/**
	 * 线程重命名<br>
	 * 主要是为了打印日志上显示线程方法
	 */
	public void renameThread() {
		Thread.currentThread().setName(new StringBuilder("[").append(task.getId()).append("]Worker线程").toString());
	}
	
	@Override
	protected boolean checkBeginTime(String gatherPath, Date timeEntry) {
		return true;
	}
	
	@Override
	protected boolean checkEndTime(String gatherPath, Date timeEntry) {
		return true;
	}

	protected int getMaxConcurentJobThreadCount() {
		// return Math.min(remoteFilePaths.size(), systemMaxJobConcurrent);
		return Math.min(3, systemMaxJobConcurrent);
	}
}
