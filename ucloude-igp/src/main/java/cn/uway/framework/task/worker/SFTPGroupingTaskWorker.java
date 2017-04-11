package cn.uway.framework.task.worker;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cn.uway.framework.accessor.SFTPAccessor;
import cn.uway.framework.connection.pool.sftp.SFTPClientPool;
import cn.uway.framework.context.Vendor;
import cn.uway.framework.job.AdaptiveStreamJob;
import cn.uway.framework.job.AsynchronousGroupingJob;
import cn.uway.framework.job.GroupFilesJob;
import cn.uway.framework.job.Job;
import cn.uway.framework.job.JobParam;
import cn.uway.framework.solution.GatherSolution;
import cn.uway.framework.solution.SolutionLoader;
import cn.uway.framework.task.GatherPathDescriptor;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.MultiElementGatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.util.StringUtil;


public class SFTPGroupingTaskWorker extends SFTPTaskWorker {
	/**
	 * 监控任务最后一个分组文件数量
	 */
	protected static Map<Long, TaskGroupFilesInfo> mapTaskLastGroupFileInfo = new HashMap<Long, TaskGroupFilesInfo>();
	
	/**
	 * 分组文件个数最大检测时间(单位：分钟)
	 * <pre>
	 * 当文件扫描到服务一个最近分组时，发现比上一个分组少时，将在指定N分钟内，如果文件个数未变，则提交到解码parser中，
	 * 否则继续检测，直至有下一个分组的文件生成或超时
	 * </pre>
	 */
	protected int MAX_GROUP_FILE_NUM_CHECKE_MINUTE_TIME = 6;
	
	public SFTPGroupingTaskWorker(Task task) {
		super(task);
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
			
			LOGGER.debug("开始对服务器扫描到的{}个文件，进行分组...", sortedFiles.size());
			List<GroupFileInfo> groupedEntry = groupFilesEntry(sortedFiles); 
			LOGGER.debug("分组完成， 文件个数：{}，　分组个数：{}.", sortedFiles.size(), groupedEntry==null?0:groupedEntry.size());
			if(groupedEntry ==null || (groupedEntry!=null && groupedEntry.size() == 0))
				return;
			
			synchronized(fileNamesCache.getUseLock()){
				// 当扫描到的文件数过多时，并且fileNamesCache为空，则从数据库中初始化一次fileNamesCache
				if (sortedFiles.size() >= MAX_INIT_CACHE_FILE_COUNT && fileNamesCache.getTaskGatherCacheCount(this.task.getId()) < 1) {
					Date startTime = this.getTaskMinGatherTime(sortedFiles);
					Date endTime = this.getTaskMaxGatherTime(sortedFiles);
					if (startTime != null && endTime != null) {
						statusDAO.initTaskGatherEntryCache(fileNamesCache, this.task.getId(), startTime, endTime);
					}
				}
				
				//获取最后一个周期分组，文件个数
				TaskGroupFilesInfo currTaskLastGroupFileInfo = mapTaskLastGroupFileInfo.get(this.task.getId());
				if (currTaskLastGroupFileInfo == null) {
					currTaskLastGroupFileInfo = new TaskGroupFilesInfo();
					mapTaskLastGroupFileInfo.put(this.task.getId(), currTaskLastGroupFileInfo);
				}
				
				long nCheckBeginTime = System.currentTimeMillis();
				int groupedEntyNum = groupedEntry.size();
				for(int i = 0; i < groupedEntyNum; i++){
					if (pathEntries.size()>0 
							&& (System.currentTimeMillis()-nCheckBeginTime)>MAX_GATHER_FILES_QUERY_DB_TIMES) {
						LOGGER.debug("本次已找描到一个及以上的采集对象，因为对状态表检测时间已超过90秒，本次检测终止，其它文件将在下次采集.");
						break;
					}
					
					GroupFileInfo entry = groupedEntry.get(i);
					int entryNum = entry.fileList.size();
					
					/**
					 *  如果是最后一个分组，代表是最近一个时间的分组，判断一下文件个数是否比上一个分组的文件个数少，
					 *  如果少，则等待X分钟内，文件个数没有变化，则直接采，否则等待
					 */
					if (i == groupedEntyNum-1) {
						if (currTaskLastGroupFileInfo.lastGroupFileCount > 0 
								&& entryNum < currTaskLastGroupFileInfo.lastGroupFileCount) {
							
							boolean bExcepGroup = true;
							if (currTaskLastGroupFileInfo.lastExcpGroupFileScanTime == null)
								currTaskLastGroupFileInfo.lastExcpGroupFileScanTime = new Date(System.currentTimeMillis());
							
							// 上一次扫到到最后分组等于currTaskLastGroupFileInfo.lastExcpGroupFileHitCount的时间
							Date preExcpGroupScanTime = currTaskLastGroupFileInfo.lastExcpGroupFileScanTime;
							if (entryNum == currTaskLastGroupFileInfo.lastExcpGroupFileHitCount) {
								//如果本次扫描到的文件个数和上次扫描时的个数一致，并且时间超过X分种，则也视为正常，直接解码
								long timeElapse = (System.currentTimeMillis() - preExcpGroupScanTime.getTime()) / (60 * 1000L);
								if (timeElapse >= MAX_GROUP_FILE_NUM_CHECKE_MINUTE_TIME) {
									bExcepGroup = false;
								} else {
									LOGGER.debug("本次扫描到最近一个分组的文件数：{}， 少于上一个周期分组的文件数:{}，服务器文件可能生成不完整，该任务在服务器文件生成个数与上一次分组相等或在{}分钟内，一直稳定在{}个后，或者下一个分组文件产生时将再被解析.当前已等待:{}分钟."
											, new Object[]{entryNum, currTaskLastGroupFileInfo.lastGroupFileCount, MAX_GROUP_FILE_NUM_CHECKE_MINUTE_TIME, currTaskLastGroupFileInfo.lastExcpGroupFileHitCount, timeElapse});										
								}
							}
							else {
								//如果文件个数变化了，以最后一次扫描的个数和时间作为下一次比较标准
								currTaskLastGroupFileInfo.lastExcpGroupFileHitCount = entryNum;
								currTaskLastGroupFileInfo.lastExcpGroupFileScanTime = new Date(System.currentTimeMillis());
								
								LOGGER.debug("本次扫描到最后一个分组的文件数：{}， 少于上一个周期分组的文件数:{}，服务器文件可能生成不完整，该任务在服务器文件生成个数与上一次分组相等或在{}分钟内，一直稳定在{}个后，或者下一个分组文件产生时将再被解析."
										, new Object[]{entryNum, currTaskLastGroupFileInfo.lastGroupFileCount, MAX_GROUP_FILE_NUM_CHECKE_MINUTE_TIME, currTaskLastGroupFileInfo.lastExcpGroupFileHitCount});
							}
							
							if (bExcepGroup) {
								continue;
							}
						}
					}
					
					Date groupEntryTime = null;
					for(int j = 0; j < entryNum; j++){
						// 只要一组文件中有一个文件没有做完。整个文件都需要重新做
						String gatherEntry = entry.fileList.get(j);
						String decodedPath = getFtpFileDecodeShortName(gatherEntry);
						if(fileNamesCache.isAlreadyGather(decodedPath, task.getId()))
							continue;
						
						if (groupEntryTime == null)
							groupEntryTime = this.getGatherObjectDateTime(entry.groupName);
						
						//entry.groupName就是时间信息
						if(checkGatherObject(gatherEntry, groupEntryTime)){
							MultiElementGatherPathEntry multiElementGatherPathEntry = new MultiElementGatherPathEntry(
									entry.fileList);
							
							//如果文件有新生成的，设置为补采标识
							if (j > 0) {
								multiElementGatherPathEntry.setRepairTask(true);
							}
							pathEntries.add(multiElementGatherPathEntry);
							
							//更新最后一次分组文件的数量
							currTaskLastGroupFileInfo.lastGroupFileCount = entryNum;
							
							//只要当前任务有一个分组文件被加入到了pathEntries中，那么用于文件个数异常检测的信息就要复位
							currTaskLastGroupFileInfo.lastExcpGroupFileScanTime = null;
							currTaskLastGroupFileInfo.lastExcpGroupFileHitCount = 0;
							
							break;
						}
						fileNamesCache.putToCache(decodedPath, task.getId());
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("在SFTP路径上查找文件失败", e);
		}
	}
	
	/**
	 * 创建JOB,LoalTaskWorker固定采用SFTPAccessor接入器，
	 * 不使用solution配置的接入器，这样可以做到solutin的一些共用
	 */
	@Override
	/**
	 * 创建异步输出JOB
	 */
	protected Job createJob(GatherPathEntry pathEntry){
		GatherSolution solution = SolutionLoader.getSolution(task);
		solution.setAccessor(new SFTPAccessor());
		JobParam param = new JobParam(task, connInfo, solution, pathEntry);
		if(Vendor.VENDOR_ERICSSON.equalsIgnoreCase(this.task.getExtraInfo().getVendor())){
			return new AsynchronousGroupingJob(param);
		}
		
		if (solution.isAdaptiveStreamJobAvaliable())
			return new AdaptiveStreamJob(param);
		else
			return new GroupFilesJob(param);
	}
	
	protected List<GroupFileInfo> groupFilesEntry(Set<GatherObjEntry> sortedFiles){
		return groupFilesEntryByteFileTime(sortedFiles);
	}
}
