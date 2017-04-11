package cn.uway.framework.task.worker;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.framework.connection.pool.ftp.BasicFTPClientPool;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.task.DelayTask;
import cn.uway.framework.task.GatherPathDescriptor;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.util.FTPClientUtil;
import cn.uway.util.FTPUtil;
import cn.uway.util.StringUtil;

/**
 * FTPTaskWorker
 * <p>
 * 2014-6-11 修复日志乱码和中文路径不可用的问题，另外特别注意在本类的生命周期中，FTP路径应该分为两种:
 * <ul>
 * <li>第一种用于本地日志输出的路径，使用的是本地编码集</li>
 * <li>第二种用于FtpClient向服务端输出的路径，使用的是ISO-8859-1编码集</li>
 * </ul>
 * </p>
 * 
 * @author chenrongqiang
 * @author Niow 2014-6-11
 */
public class FTPTaskWorker extends AbstractTaskWorker {

	/**
	 * 数据生命周期，如配置值小于0，则取5天<br>
	 */
	protected static final int FTP_FILE_LIFE_DAY = AppContext.getBean("ftpFileLifeDay", Integer.class);

	//public FTPClient client = null;

	/**
	 * FTP数据连接信息
	 */
	protected FTPConnectionInfo sourceConnectionInfo;

	/**
	 * 构造方法
	 * 
	 * @param task
	 */
	public FTPTaskWorker(Task task) {
		super(task);
		if (!(connInfo instanceof FTPConnectionInfo))
			throw new IllegalArgumentException("连接信息不正确，错误的连接类型");
		sourceConnectionInfo = (FTPConnectionInfo) connInfo;
	}

	/**
	 * 获取任务并发线程数，取采集对象数、系统配置最大并发数、1/2 FTP最大连接数的最小值
	 */
	protected int getMaxConcurentJobThreadCount() {
		int maxFTPConnections = sourceConnectionInfo.getMaxConnections();
		int maxConcurrentJob = Math.min(maxFTPConnections / 2, systemMaxJobConcurrent);
		return Math.min(maxConcurrentJob, pathEntries.size());
	}

	/**
	 * 根据配置查询FTP文件的最大有效时间<br>
	 * 1、如果没有采集过，则取任务表上配置的数据时间DATA_TIME的值。 2、取状态表中该任务最大的数据时间。往前推FTP_FILE_LIFE_DAY天。<br>
	 * 3、如果步骤2结果时间早于任务DATA_TIME,则取DATA_TIME<br>
	 * 注：处理流程2中，时间会截取到整天
	 * 
	 * @return FTP文件的有效日期
	 */
	protected Date getFTPFileLifeDay() {
		Date date = statusDAO.getMaxTaskDataTime(task.getId());
		if (date == null)
			return task.getDataTime();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, FTP_FILE_LIFE_DAY < 0 ? -5 : -FTP_FILE_LIFE_DAY);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		date = cal.getTime();
		if (date.compareTo(task.getDataTime()) < 0)
			return task.getDataTime();
		return cal.getTime();
	}

	/**
	 * 获取最终的采集列表
	 * @param client
	 * @return
	 */
	protected TreeMap<GatherObjEntry, Long> getGatherObjs(FTPClient client){
		GatherPathDescriptor gatherPaths = task.getGatherPathDescriptor();
		// 附加功能点 增加FTP文件开始时间和结束时间的校验
		this.dataTime = getFTPFileLifeDay();
		if (task instanceof PeriodTask) {
			String rawData = gatherPaths.getRawData();
			String currPeriodData = StringUtil.convertCollectPath(rawData, task.getDataTime());
			gatherPaths = new GatherPathDescriptor(currPeriodData);
			/*for 周期性ftp采集双周期检查机制*/
			if(periodFtpDoublePeriodCheck){
				Date nextDataTime = new Date(task.getDataTime().getTime()+task.getPeriod()*60*1000);
				String nextPeriodData = StringUtil.convertCollectPath(rawData, nextDataTime);
				GatherPathDescriptor nextPeriodGatherPaths = new GatherPathDescriptor(nextPeriodData);
				for (GatherPathEntry pathEntry : nextPeriodGatherPaths.getPaths()) {
					if(getGatherFilesNextPeriod(client, pathEntry.getPath())){
						skipNextPeriod=true;
						break;
					}
				}
			}
		}else{
			boolean flag = true;
			for (GatherPathEntry pathEntry : gatherPaths.getPaths()) {
				if (pathEntry.getPath().indexOf("{") == -1 || pathEntry.getPath().indexOf("}") == -1) {
					LOGGER.info("提示：路径中不包含大括号，dataTime将从解析器或任务时间中得出，path = " + pathEntry.getPath());
					flag = false;
					break;
				}
			}
			if (flag) {
				LOGGER.info("提示：路径中包含大括号，dataTime将从路径中自动匹配得出");
				//gatherObjs = new TreeMap<String, Long>(new FileDateTimeWithWildcardComparator());
				timeWildcardHandler = new TimeWildcardHandler(gatherPaths.getPaths().get(0).getPath());
			}
		}

		/**
		 * 使用TreeMap 比较文件名 进行排序 优先使用文件进行排序
		 */
		TreeMap<GatherObjEntry, Long> gatherObjs = new TreeMap<GatherObjEntry, Long>(new FileDateTimeComparator());
		for (GatherPathEntry pathEntry : gatherPaths.getPaths()) {
			getGatherFiles(client, pathEntry.getPath(), gatherObjs);
		}
		return gatherObjs;
	}
	
	
	/**
	 * 在开始执行任务前 FTPTaskWorder完成通配符转换、FTP文件检查等操作
	 */
	public void beforeWork() {
		FTPClient client = null;
		try {
			client = FTPClientUtil.connectFTP(sourceConnectionInfo);
			TreeMap<GatherObjEntry, Long> gatherObjs =getGatherObjs(client);
			Set<GatherObjEntry> sortedFilesEntry = gatherObjs.keySet();

			FileNamesCache fileNamesCache = FileNamesCache.getInstance();
			// 这里锁的粒度大一些，锁住判断所有文件的过程。如果每个文件锁一次，切换太频繁。
			// 并且，可以降低对状态表访问的并发量。
			synchronized (fileNamesCache.getUseLock()) {
				// 当扫描到的文件数过多时，并且fileNamesCache为空，则从数据库中初始化一次fileNamesCache
				if (sortedFilesEntry.size() >= MAX_INIT_CACHE_FILE_COUNT && fileNamesCache.getTaskGatherCacheCount(this.task.getId()) < 1) {
					Date startTime = this.getTaskMinGatherTime(sortedFilesEntry);
					Date endTime = this.getTaskMaxGatherTime(sortedFilesEntry);
					if (startTime != null && endTime != null) {
						statusDAO.initTaskGatherEntryCache(fileNamesCache, this.task.getId(), startTime, endTime);
					}
				}
				
				this.currPeriodScanedObjectEntryNumber = sortedFilesEntry.size();
				long nCheckBeginTime = System.currentTimeMillis();
				for (GatherObjEntry fileEntry : sortedFilesEntry) {
					if (pathEntries.size()>0 
							&& (System.currentTimeMillis()-nCheckBeginTime)>MAX_GATHER_FILES_QUERY_DB_TIMES) {
						LOGGER.debug("本次已找描到一个及以上的采集对象，因为对状态表检测时间已超过90秒，本次检测终止，其它文件将在下次采集.");
						break;
					}
					
					String sortedFile = fileEntry.fileName;
					String decodedPath = getFtpFileDecodeShortName(sortedFile);
					// 在之前的状态查询中，已经确定该文件不用采集，或是已经超过end_data_time.
					// 则跳过，不再在状态表中查询。
					if (fileNamesCache.isAlreadyGather(decodedPath, task.getId())) {
						++this.currPeriodCollectedAlreadyObjectEntryNumber;
						continue;
					}
					// 在状态表判断。
					if (checkGatherObject(sortedFile, fileEntry.date)) {
						if(timeWildcardHandler != null)
						{
							// 当文件匹配不到时，不解析该文件
							Date date = timeWildcardHandler.getDateTime(sortedFile);
							if(date == null)
							{
								continue;
							}
							GatherPathEntry entry = new GatherPathEntry(sortedFile);
							entry.setSize(gatherObjs.get(fileEntry));
							entry.setDateTime(date);
							pathEntries.add(entry);
						}
						else
						{
							GatherPathEntry entry = new GatherPathEntry(sortedFile);
							entry.setSize(gatherObjs.get(fileEntry));
							entry.setDateTime(null);
							pathEntries.add(entry);
						}
						
					} else {
						// 已经在状态表查询过，确定不用采这个文件了，则放入缓存，避免下次再在状态表查询。
						fileNamesCache.putToCache(decodedPath, task.getId());
						++this.currPeriodCollectedAlreadyObjectEntryNumber;
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("开始在FTP上查找文件失败", e);
		} finally {
			FTPUtil.logoutAndCloseFTPClient(client);
		}
	}
	
	/**
	 * 通过配置路径查找FTP上匹配的文件(任务的下一个时间周期)
	 * 
	 * @param ftpClient
	 * @param gatherPath
	 * @param gatherObjs
	 */
	public boolean getGatherFilesNextPeriod(FTPClient ftpClient, String gatherPath) {
		TreeMap<GatherObjEntry, Long> gatherObjs = new TreeMap<GatherObjEntry, Long>(new FileDateTimeComparator());
		getGatherFiles(ftpClient, gatherPath, gatherObjs);
		return gatherObjs.size()>=1;
	}
	
	/**
	 * 通过配置路径查找FTP上匹配的文件
	 * 
	 * @param ftpClient
	 * @param gatherPath
	 * @param gatherObjs
	 */
	public void getGatherFiles(FTPClient ftpClient, String gatherPath, TreeMap<GatherObjEntry, Long> gatherObjs) {
		// gatherPath = StringUtil.convertCollectPath(gatherPath, new Date());
		if (timeWildcardHandler != null) {
			gatherPath = gatherPath.replace(timeWildcardHandler.timeWildcard, "*").replace("{", "").replace("}", "");
		} else {
			gatherPath = StringUtil.convertCollectPath(gatherPath, new Date());
		}
		LOGGER.debug("开始在FTP上查找文件：{}", gatherPath);
		List<FTPFile> gatherFiles = null;
		try {
			/*
			 * 需要把本地的编码的字符路径转换成FTP服务器的编码类型，再转换成FTP规范编码ISO-8859-1格式，<br> 才能保证使用中文路径能找到文件。
			 * 
			 * @author Niow 2014-6-11
			 * 
			 * @author Niow 2014-7-8
			 */
			// String encodedPath = StringUtil.encodeFTPPath(gatherPath, sourceConnectionInfo.getCharset());
			gatherFiles = FTPUtil.listFilesRecursive(ftpClient, gatherPath, sourceConnectionInfo.getCharset());
		} catch (IOException e) {
			LOGGER.error("在FTP上查找文件{}失败", gatherPath, e);
			BasicFTPClientPool.badFTPConnectsMap.put(ftpClient.hashCode(), true);
		}
		LOGGER.debug("在FTP上查找到（{}）的文件个数：{}", new Object[]{gatherPath, gatherFiles != null ? gatherFiles.size() : "<NULL>"});
		if (gatherFiles == null || gatherFiles.size() == 0)
			return;
		for (int i = 0; i < gatherFiles.size(); i++) {
			// FTPUtil中已经判断过了，此处不用判断，而且要兼容软／硬连接的文件．
			//if (gatherFiles.get(i).isFile()) {
				// if (!gatherPath.equals("/")) {
					String fileName = gatherFiles.get(i).getName();
					gatherObjs.put(new GatherObjEntry(fileName, this.getGatherObjectDateTime(fileName)), gatherFiles.get(i).getSize());
				/*} else {
					gatherObjs.put(gatherFiles.get(i).getName(), gatherFiles.get(i).getSize());
				}*/
			//}
		}
	}

	@Override
	protected boolean checkBeginTime(String gatherPath, Date timeEntry) {
		//如果是延迟数据采集，则不进行开始时间检查,因为可能会存在很久之后还是采集很久以前的数据；
		if(task instanceof DelayTask)
			return true;
		return super.checkBeginTimeDefault(timeEntry);
	}

	@Override
	protected boolean checkEndTime(String gatherPath, Date timeEntry) {
		//如果是延迟数据采集，则不进行开始时间检查,因为可能会存在很久之后还是采集很久以前的数据；
		if(task instanceof DelayTask)
			return true;
		return super.checkEndTimeDefault(timeEntry);
	}

}
