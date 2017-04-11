package cn.uway.framework.task.worker;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import cn.uway.framework.accessor.LocalAccessor;
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
import cn.uway.util.FileUtil;
import cn.uway.util.StringUtil;

/**
 * FTPTaskWorker
 * 
 * @author chenrongqiang
 */
public class LocalTaskWorker extends AbstractTaskWorker {

	/**
	 * 数据生命周期，如配置值小于0，则取5天<br>
	 */
	protected final int FTP_FILE_LIFE_DAY = AppContext.getBean(
			"ftpFileLifeDay", Integer.class);

	/**
	 * 构造方法
	 * 
	 * @param task
	 */
	public LocalTaskWorker(Task task) {
		super(task);
	}

	/**
	 * 获取任务并发线程数，取采集对象数、系统配置最大并发数的最小值
	 */
	protected int getMaxConcurentJobThreadCount() {
		return Math.min(systemMaxJobConcurrent, pathEntries.size());
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

	/**
	 * 在开始执行任务前 FTPTaskWorder完成通配符转换、FTP文件检查等操作
	 */
	public void beforeWork() {
		try {
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
				this.currPeriodScanedObjectEntryNumber = sortedFiles.size();
				for (GatherObjEntry sortedFileEntry : sortedFiles) {
					String sortedFile = sortedFileEntry.fileName;
					// 在之前的状态查询中，已经确定该文件不用采集，或是已经超过end_data_time.
					// 则跳过，不再在状态表中查询。
					if (fileNamesCache
							.isAlreadyGather(sortedFile, task.getId())) {
						++this.currPeriodCollectedAlreadyObjectEntryNumber;
						continue;
					}
					// 在状态表判断。
					if (checkGatherObject(sortedFile, sortedFileEntry.date)) {
						GatherPathEntry entry = new GatherPathEntry(sortedFile);
						entry.setSize(gatherObjs.get(sortedFileEntry));
						pathEntries.add(entry);
					} else {
						// 已经在状态表查询过，确定不用采这个文件了，则放入缓存，避免下次再在状态表查询。
						fileNamesCache.putToCache(sortedFile, task.getId());
						++this.currPeriodCollectedAlreadyObjectEntryNumber;
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("在本地路径上查找文件失败", e);
		}
	}

	/**
	 * 通过配置路径查找本地上匹配的文件
	 * 
	 * @param gatherPath
	 * @param gatherObjs
	 */
	public void getGatherFiles(String gatherPath,
			TreeMap<GatherObjEntry, Long> gatherObjs) {
		gatherPath = StringUtil.convertCollectPath(gatherPath, new Date());
		LOGGER.debug("开始在:{}上查找文件：", gatherPath);
		List<String> gatherFiles = null;
		try {
			gatherFiles = findFiles(gatherPath);
		} catch (Exception e) {
			LOGGER.error("查找文件{}失败", gatherPath, e);
		}
		LOGGER.debug("在本地路径：（{}）的文件个数：{}", new Object[]{gatherPath,
				gatherFiles != null ? gatherFiles.size() : "<NULL>"});
		if (gatherFiles == null || gatherFiles.size() == 0)
			return;

		for (String fileName : gatherFiles) {
			try {
				File file = new File(fileName);
				if (file.exists()) {
					gatherObjs.put(new GatherObjEntry(fileName, this.getGatherObjectDateTime(fileName)), file.length());
				}
			} catch (Exception e) {
				continue;
			}
		}
	}
	
	/**
	 * 创建JOB,LoalTaskWorker固定采用LocalAccessor接入器，
	 * 不使用solution配置的接入器，这样可以做到solutin的一些共用
	 */
	@Override
	protected Job createJob(GatherPathEntry pathEntry) {
		GatherSolution solution = SolutionLoader.getSolution(task);
		solution.setAccessor(new LocalAccessor());
		JobParam param = new JobParam(task, connInfo, solution, pathEntry);
		if (solution.isAdaptiveStreamJobAvaliable())
			return new AdaptiveStreamJob(param);
		else
			return new GenericJob(param);
	}

	public static List<String> findFiles(String gatherPath) throws IOException {
		gatherPath = gatherPath.replaceAll("[\\\\]", "/");
		int matchPos = gatherPath.indexOf("*");
		List<String> fileList = null;
		if (matchPos < 0) {
			// 无通配符
			try {
				fileList = FileUtil.getFileNames(gatherPath, "", false);
			} catch (Exception e) {
				throw new IOException("无效的采集路径:" + gatherPath, e);
			}
		} else {
			int lastDirSplitTagPos = gatherPath.lastIndexOf("/");
			if (lastDirSplitTagPos < 0) {
				throw new IOException("无效的采集路径:" + gatherPath);
			}
			++lastDirSplitTagPos;

			String dir = gatherPath.substring(0, lastDirSplitTagPos);
			String parten = gatherPath.substring(lastDirSplitTagPos,
					gatherPath.length());
			if (dir.indexOf('*') >= 0) {
				throw new IOException("无效的采集路径:" + gatherPath
						+ ", IGP暂不支持采集路径中间文件夹采用通配符.");
			}
			try {
				fileList = FileUtil.getFileNames(dir, parten, false);
			} catch (Exception e) {
				throw new IOException("无效的采集路径:" + gatherPath, e);
			}
		}

		return fileList;
	}

	@Override
	protected boolean checkBeginTime(String gatherPath, Date timeEntry) {
		return super.checkBeginTimeDefault(timeEntry);
	}

	@Override
	protected boolean checkEndTime(String gatherPath, Date timeEntry) {
		return super.checkEndTimeDefault(timeEntry);
	}
}
