package cn.uway.framework.task.worker;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import cn.uway.framework.accessor.SFTPAccessor;
import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.framework.connection.pool.sftp.SFTPClient;
import cn.uway.framework.connection.pool.sftp.SFTPClient.SFTPFileEntry;
import cn.uway.framework.connection.pool.sftp.SFTPClientPool;
import cn.uway.framework.context.AppContext;
import cn.uway.framework.job.AdaptiveStreamJob;
import cn.uway.framework.job.GenericJob;
import cn.uway.framework.job.Job;
import cn.uway.framework.job.JobParam;
import cn.uway.framework.solution.GatherSolution;
import cn.uway.framework.solution.SolutionLoader;
import cn.uway.framework.task.GatherPathDescriptor;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.util.StringUtil;


public class SFTPTaskWorker extends AbstractTaskWorker {
	/**
	 * 数据生命周期，如配置值小于0，则取5天<br>
	 */
	protected final int FTP_FILE_LIFE_DAY = AppContext.getBean(
			"ftpFileLifeDay", Integer.class);
	
	/**
	 * FTP数据连接信息
	 */
	protected FTPConnectionInfo sourceConnectionInfo;
	protected SFTPClientPool sftpClientPool;
		
	public SFTPTaskWorker(Task task) {
		super(task);
		
		if (!(connInfo instanceof FTPConnectionInfo))
			throw new IllegalArgumentException("连接信息不正确，错误的连接类型");
		sourceConnectionInfo = (FTPConnectionInfo) connInfo;
	}

	@Override
	public void beforeWork() {
		try {
			int connID = sourceConnectionInfo.getId();
			this.sftpClientPool = SFTPClientPool.getSFTPClientPool(connID, this.sourceConnectionInfo.getMaxConnections());
			
			GatherPathDescriptor gatherPaths = task.getGatherPathDescriptor();
			// 附加功能点 增加FTP文件开始时间和结束时间的校验
			this.dataTime = getFTPFileLifeDay();
			if (task instanceof PeriodTask) {
				String rawData = gatherPaths.getRawData();
				rawData = StringUtil.convertCollectPath(rawData,
						task.getDataTime());
				gatherPaths = new GatherPathDescriptor(rawData);
			}
			/**
			 * 使用TreeMap 比较文件名 进行排序 优先使用文件进行排序
			 */
			TreeMap<GatherObjEntry, Long> gatherObjs = new TreeMap<GatherObjEntry, Long>(new FileDateTimeComparator());
			for (GatherPathEntry pathEntry : gatherPaths.getPaths()) {
				getGatherFiles(pathEntry.getPath(), gatherObjs);
			}
			Set<GatherObjEntry> sortedFiles = gatherObjs.keySet();

			FileNamesCache fileNamesCache = FileNamesCache.getInstance();
			// 这里锁的粒度大一些，锁住判断所有文件的过程。如果每个文件锁一次，切换太频繁。
			// 并且，可以降低对状态表访问的并发量。
			synchronized (fileNamesCache.getUseLock()) {
				// 当扫描到的文件数过多时，并且fileNamesCache为空，则从数据库中初始化一次fileNamesCache
				if (sortedFiles.size() >= MAX_INIT_CACHE_FILE_COUNT && fileNamesCache.getTaskGatherCacheCount(this.task.getId()) < 1) {
					Date startTime = this.getTaskMinGatherTime(sortedFiles);
					Date endTime = this.getTaskMaxGatherTime(sortedFiles);
					if (startTime != null && endTime != null) {
						statusDAO.initTaskGatherEntryCache(fileNamesCache, this.task.getId(), startTime, endTime);
					}
				}
				
				this.currPeriodScanedObjectEntryNumber = sortedFiles.size();
				long nCheckBeginTime = System.currentTimeMillis();
				for (GatherObjEntry fileEntry : sortedFiles) {
					if (pathEntries.size()>0 
							&& (System.currentTimeMillis()-nCheckBeginTime)>MAX_GATHER_FILES_QUERY_DB_TIMES) {
						LOGGER.debug("本次已找描到一个及以上的采集对象，因为对状态表检测时间已超过90秒，本次检测终止，其它文件将在下次采集.");
						break;
					}
					
					String sortedFile = fileEntry.fileName;
					String decodedPath = getFtpFileDecodeShortName(sortedFile);
					// 在之前的状态查询中，已经确定该文件不用采集，或是已经超过end_data_time.
					// 则跳过，不再在状态表中查询。
					if (fileNamesCache
							.isAlreadyGather(decodedPath, task.getId())) {
						++this.currPeriodCollectedAlreadyObjectEntryNumber;
						continue;
					}
					
					// 在状态表判断。
					if (checkGatherObject(sortedFile, fileEntry.date)) {
						GatherPathEntry entry = new GatherPathEntry(sortedFile);
						entry.setSize(gatherObjs.get(fileEntry));
						pathEntries.add(entry);
					} else {
						// 已经在状态表查询过，确定不用采这个文件了，则放入缓存，避免下次再在状态表查询。
						fileNamesCache.putToCache(decodedPath, task.getId());
						++this.currPeriodCollectedAlreadyObjectEntryNumber;
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("在SFTP路径上查找文件失败", e);
		}
	}
	
	/**
	 * 通过配置路径查找SFTP上匹配的文件
	 * 
	 * @param ftpClient
	 * @param gatherPath
	 * @param gatherObjs
	 */
	public void getGatherFiles(String gatherPath,
			TreeMap<GatherObjEntry, Long> gatherObjs) {
		
		gatherPath = StringUtil.convertCollectPath(gatherPath, new Date());
		LOGGER.debug("开始在:{}上查找文件：", gatherPath);
		List<SFTPFileEntry> gatherFiles = null;
		try {
			gatherFiles = findFiles(gatherPath);
		} catch (Exception e) {
			LOGGER.error("查找文件{}失败", gatherPath, e);
		}
		LOGGER.debug("在SFTP路径：（{}）的文件个数：{}", new Object[]{gatherPath,
				gatherFiles != null ? gatherFiles.size() : "<NULL>"});
		if (gatherFiles == null || gatherFiles.size() == 0)
			return;

		for (SFTPFileEntry entry : gatherFiles) {
			gatherObjs.put(new GatherObjEntry(entry.fileName, this.getGatherObjectDateTime(entry.fileName)), entry.fileSize);
		}
	}
	
	public List<SFTPFileEntry> findFiles(String gatherPath) throws Exception {
		SFTPClient sftpClient = null;
		try {
			LOGGER.debug("[SFTPTaskWorker]等待获取SFTP连接......KEY={}", new Object[]{sourceConnectionInfo.getId()});
			sftpClient = sftpClientPool.getSftpClient(sourceConnectionInfo.getIp(), sourceConnectionInfo.getPort(), sourceConnectionInfo.getUserName(), sourceConnectionInfo.getPassword(), sourceConnectionInfo.getCharset());
			if(sftpClient == null) {
				LOGGER.debug("[SFTPTaskWorker]获取SFTP连接失败，KEY={}",	new Object[]{sourceConnectionInfo.getId()});
				return null;
			}
				
			LOGGER.debug("[SFTPTaskWorker]获取SFTP连接成功，KEY={}",	new Object[]{sourceConnectionInfo.getId()});
			List<SFTPFileEntry> fileEntryList = sftpClient.listSFTPFile(gatherPath);
			
			return fileEntryList;
		} catch (Exception e) {
			throw e;
		} finally {
			if (sftpClient != null) {
				sftpClientPool.returnSftpChannel(sftpClient);
			}
		}
	}
	
	/**
	 * 创建JOB,LoalTaskWorker固定采用SFTPAccessor接入器，
	 * 不使用solution配置的接入器，这样可以做到solutin的一些共用
	 */
	@Override
	protected Job createJob(GatherPathEntry pathEntry) {
		GatherSolution solution = SolutionLoader.getSolution(task);
		solution.setAccessor(new SFTPAccessor());
		JobParam param = new JobParam(task, connInfo, solution, pathEntry);
		if (solution.isAdaptiveStreamJobAvaliable())
			return new AdaptiveStreamJob(param);
		else
			return new GenericJob(param);
	}
	
	/**
	 * 根据配置查询FTP文件的最大有效时间<br>
	 * 1、如果没有采集过，则取任务表上配置的数据时间DATA_TIME的值。
	 * 2、取状态表中该任务最大的数据时间。往前推FTP_FILE_LIFE_DAY天。<br>
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

	@Override
	protected boolean checkBeginTime(String gatherPath, Date timeEntry) {
		return super.checkBeginTimeDefault(timeEntry);
	}

	@Override
	protected boolean checkEndTime(String gatherPath, Date timeEntry) {
		return super.checkEndTimeDefault(timeEntry);
	}

	@Override
	protected int getMaxConcurentJobThreadCount() {
		return Math.min(systemMaxJobConcurrent, pathEntries.size());
	}
	
}
