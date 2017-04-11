package cn.uway.framework.task.worker;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPClient;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.context.Vendor;
import cn.uway.framework.job.AdaptiveStreamJob;
import cn.uway.framework.job.AsynchronousGroupingJob;
import cn.uway.framework.job.GroupFilesJob;
import cn.uway.framework.job.Job;
import cn.uway.framework.job.JobParam;
import cn.uway.framework.solution.GatherSolution;
import cn.uway.framework.solution.SolutionLoader;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.MultiElementGatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.util.FTPClientUtil;
import cn.uway.util.FTPUtil;
import cn.uway.util.StringUtil;

/**
 * @author chenrongqiang @ 2013-4-29
 */
public class GroupingFTPTaskWorker extends FTPTaskWorker{
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
	
	//　自动识别网络类型
	public final static int AUTO_DIST_NET_TYPE = 6;
	
	public GroupingFTPTaskWorker(Task task){
		super(task);
		
		String maxGroupFileNumCheckMinuteTime = AppContext.getBean("maxGroupFileNumCheckMinuteTime", java.lang.String.class);
		if (maxGroupFileNumCheckMinuteTime != null && maxGroupFileNumCheckMinuteTime.length() > 0 && maxGroupFileNumCheckMinuteTime.indexOf('$') < 0) {
			MAX_GROUP_FILE_NUM_CHECKE_MINUTE_TIME = Integer.parseInt(maxGroupFileNumCheckMinuteTime);
		}
	}

	/**
	 * 在开始执行任务前 FTPTaskWorder完成通配符转换、FTP文件检查等操作
	 */
	public void beforeWork(){
		FTPClient client = null;
		try{
			client = FTPClientUtil.connectFTP(sourceConnectionInfo);
			TreeMap<GatherObjEntry, Long> gatherObjs =getGatherObjs(client);
			Set<GatherObjEntry> sortedFiles = gatherObjs.keySet();
			FileNamesCache fileNamesCache = FileNamesCache.getInstance();
			
			// 爱立信的文件是15分钟一组文件，15分钟文件处理一次。
			if(Vendor.VENDOR_ERICSSON.equalsIgnoreCase(this.task.getExtraInfo().getVendor()) && this.task.getExtraInfo().getNetType() != AUTO_DIST_NET_TYPE){
				// 中兴LTE FDD和HDD
				// 这里锁的粒度大一些，锁住判断所有文件的过程。如果每个文件锁一次，切换太频繁。
				// 并且，可以降低对状态表访问的并发量。
				List<List<String>> groupedEntry = groupEricssonWcdmaFilesEntry(sortedFiles);
				if(groupedEntry.size() == 0)
					return;
				synchronized(fileNamesCache.getUseLock()){
					int groupedEntyNum = groupedEntry.size();
					for(int i = 0; i < groupedEntyNum; i++){
						List<String> entry = groupedEntry.get(i);
						int entryNum = entry.size();
						for(int j = 0; j < entryNum; j++){
							// 只要一组文件中有一个文件没有做完。整个文件都需要重新做
							String gatherEntry = entry.get(j);
							if(fileNamesCache.isAlreadyGather(gatherEntry, task.getId()))
								continue;
							if(checkGatherObject(gatherEntry, this.getGatherObjectDateTime(gatherEntry))){
								MultiElementGatherPathEntry multiElementGatherPathEntry = new MultiElementGatherPathEntry(
										entry);
								pathEntries.add(multiElementGatherPathEntry);
								break;
							}
							fileNamesCache.putToCache(gatherEntry, task.getId());
						}
					}
				}
			}else if ((Vendor.VENDOR_DT.equalsIgnoreCase(this.task.getExtraInfo().getVendor()) 
						|| Vendor.VENDOR_PT.equalsIgnoreCase(this.task.getExtraInfo().getVendor())
						|| Vendor.VENDOR_HW.equalsIgnoreCase(this.task.getExtraInfo().getVendor())
						|| Vendor.VENDOR_ZTE.equalsIgnoreCase(this.task.getExtraInfo().getVendor())) 
					&& (this.task.getExtraInfo().getNetType() == 4 || this.task.getExtraInfo().getNetType() == 5) || this.task.getExtraInfo().getNetType() == AUTO_DIST_NET_TYPE) {
				
				int netType = this.task.getExtraInfo().getNetType();
				//NET_TYPE:(4-LTE_FDD)	(5-LTE_TDD) 参数性能
				// 这里锁的粒度大一些，锁住判断所有文件的过程。如果每个文件锁一次，切换太频繁。
				// 并且，可以降低对状态表访问的并发量。
				List<GroupFileInfo> groupedEntry = null;
				if (netType == 4 || netType == 5) {
					if (Vendor.VENDOR_HW.equalsIgnoreCase(this.task.getExtraInfo().getVendor())) {
						groupedEntry = groupHwLteFilesEntry(sortedFiles);
					} else if (Vendor.VENDOR_DT.equalsIgnoreCase(this.task.getExtraInfo().getVendor())) {
						groupedEntry = groupDTLteFilesEntry(sortedFiles);
					} else if (Vendor.VENDOR_PT.equalsIgnoreCase(this.task.getExtraInfo().getVendor())) {
						groupedEntry = groupPTLteFilesEntry(sortedFiles);
					} else if (Vendor.VENDOR_ZTE.equalsIgnoreCase(this.task.getExtraInfo().getVendor())) {
						groupedEntry = groupZteLteFilesEntry(sortedFiles);
					}
				} else if (netType == AUTO_DIST_NET_TYPE) {
					groupedEntry = groupFilesEntryByteFileTime(sortedFiles);
				}
				
				if(groupedEntry ==null || (groupedEntry!=null && groupedEntry.size() == 0))
					return;
				
				boolean bPeriodTask = this.task instanceof PeriodTask;
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
						 *  周期性的任务内的分组，不进行文件完整性检测
						 */
						if (i == groupedEntyNum-1 && !bPeriodTask) {
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
							if(checkGatherObject(gatherEntry,  groupEntryTime)){
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
			}else{
				throw new UnsupportedOperationException("GroupingFTPTaskWorker 未实现指定的厂家或分组依据代码.");
			}
		}catch(Exception e){
			LOGGER.error("开始在FTP上查找文件失败", e);
		}finally{
			FTPUtil.logoutAndCloseFTPClient(client);
		}
	}

	/**
	 * @param sortedFiles
	 * @return
	 */
	private List<List<String>> groupEricssonWcdmaFilesEntry(Set<GatherObjEntry> sortedFiles){
		List<List<String>> groupedEntry = new ArrayList<List<String>>();
		List<String> singleGroupEntry = new LinkedList<String>();
		String sortKey = null;
		// 对采集对象进行分组 目前针对GPEH使用 建议在任务表进行配置
		for(GatherObjEntry sortedFile : sortedFiles){
			// A20130410.1700+0800-1715+0800
			String fileName = FilenameUtils.getBaseName(sortedFile.fileName);
			String groupingInfo = fileName.substring(0, 21);
			if(sortKey == null)
				sortKey = groupingInfo;
			if(sortKey.equals(groupingInfo)){
				singleGroupEntry.add(sortedFile.fileName);
				continue;
			}
			// 如果碰到新的时间的文件 则把singleGroupEntry加入到groupedEntry中
			// ，并且创建一个新的singleGroupEntry
			groupedEntry.add(singleGroupEntry);
			sortKey = groupingInfo;
			singleGroupEntry = new LinkedList<String>();
			singleGroupEntry.add(sortedFile.fileName);
		}
		
		// 将最后一个group添加到组中
		if (singleGroupEntry != null && singleGroupEntry.size()>0) {
			groupedEntry.add(singleGroupEntry);
		}
		
		// //15分钟处理一组文件，等待看到第二组15分钟内的文件出现以后才开始处理第一组15分钟的文件。
		// if(groupedEntry.size() > 1)
		// groupedEntry.remove(groupedEntry.size() -1);
		// 直接从厂家ftp下载不会出现上述问题，厂家一次性全部生成一组15分钟的文件。
		
		return groupedEntry;
	}
	
	/**
	 * 对华为LTE散列文件按时间相同的进行分组
	 * @param sortedFiles
	 * @return
	 */
	private List<GroupFileInfo> groupHwLteFilesEntry(Set<GatherObjEntry> sortedFiles){
		// sortedFile::CMExport_L安义县粮食局-XC_10.100.74.70_2014050505.xml
		// A20160218.1100+0800-1200+0800_FLQDB高新区东大洋(大桥)m1（SDR）.xml.gz
		if(null != StringUtil.getPattern(sortedFiles.iterator().next().fileName, "A\\d{8}[.]\\d{4}")){
			return groupFilesEntryByteFileTime(sortedFiles, "A\\d{8}[.]\\d{4}", 1, 0);
		}
		return groupFilesEntryByteFileTime(sortedFiles, "[_]\\d{10}[.]", 1, 1);
	}
	
	/**
	 * 对大唐LTE散列文件按时间相同的进行分组
	 * @param sortedFiles
	 * @return
	 */
	private List<GroupFileInfo> groupDTLteFilesEntry(Set<GatherObjEntry> sortedFiles){
		// sortedFile::ENB-PM-V2.1.0-EpRpDynS1uEnb-20140428-1315P00.xml
		return groupFilesEntryByteFileTime(sortedFiles, "[-]\\d{8}[-]\\d{4}", 1, 0);
	}
	
	/**
	 * 对普天LTE散列文件按时间相同的进行分组
	 * @param sortedFiles
	 * @return
	 */
	private List<GroupFileInfo> groupPTLteFilesEntry(Set<GatherObjEntry> sortedFiles){
		// sortedFile::eNodeB33587202201406050800.txt
		return groupFilesEntryByteFileTime(sortedFiles, "\\d{12}[.]", 0, 1);
	}
	
	/**
	 * 对中兴LTE散列文件按时间相同的进行分组
	 * @param sortedFiles
	 * @return
	 */
	private List<GroupFileInfo> groupZteLteFilesEntry(Set<GatherObjEntry> sortedFiles){
		// 100001_LTEFDD_PM_CUCC_20140509_0000.tar.gz
		return groupFilesEntryByteFileTime(sortedFiles, "[_]\\d{8}[_]\\d{4}[.]", 1, 1);
	}
	
	/**
	 * 对LTE散列文件按时间相同的进行分组
	 * @param sortedFiles
	 * @return
	 */
	private List<GroupFileInfo> groupFilesEntryByteFileTime(Set<GatherObjEntry> sortedFiles, String fileTimeRegex, int nCutStartByte, int nCutRightByte){
		List<GroupFileInfo> groupedEntry = new ArrayList<GroupFileInfo>();
		GroupFileInfo singleGroupEntry = null;
		String sortKey = null;
		// 对采集对象进行分组 目前针对GPEH使用 建议在任务表进行配置
		for(GatherObjEntry sortedFile : sortedFiles){
			// 华为的按时间进行分组
			String groupingInfo = StringUtil.getPattern(sortedFile.fileName, fileTimeRegex);
			// 10 是key的长度
			if (groupingInfo == null || groupingInfo.length()<10)
				continue;
			
			groupingInfo = groupingInfo.substring(nCutStartByte, groupingInfo.length()-nCutRightByte);
			if(sortKey == null)
				sortKey = groupingInfo;
			if(sortKey.equals(groupingInfo)){
				if (singleGroupEntry == null) {
					singleGroupEntry = new GroupFileInfo(groupingInfo);
				}
				singleGroupEntry.fileList.add(sortedFile.fileName);
				continue;
			}
			// 如果碰到新的时间的文件 则把singleGroupEntry加入到groupedEntry中
			// ，并且创建一个新的singleGroupEntry
			groupedEntry.add(singleGroupEntry);
			sortKey = groupingInfo;
			singleGroupEntry = new GroupFileInfo(groupingInfo);
			singleGroupEntry.fileList.add(sortedFile.fileName);
		}
		// //15分钟处理一组文件，等待看到第二组15分钟内的文件出现以后才开始处理第一组15分钟的文件。
		// if(groupedEntry.size() > 1)
		// groupedEntry.remove(groupedEntry.size() -1);
		// 直接从厂家ftp下载不会出现上述问题，厂家一次性全部生成一组15分钟的文件。
		
		// 将最后一个group添加到组中
		if (singleGroupEntry != null && singleGroupEntry.fileList.size()>0) {
			groupedEntry.add(singleGroupEntry);
		}
		
		return groupedEntry;
	}

	/**
	 * 创建异步输出JOB
	 */
	protected Job createJob(GatherPathEntry pathEntry){
		GatherSolution solution = SolutionLoader.getSolution(task);
		JobParam param = new JobParam(task, connInfo, solution, pathEntry);
		if(this.task.getExtraInfo().getNetType() != AUTO_DIST_NET_TYPE && Vendor.VENDOR_ERICSSON.equalsIgnoreCase(this.task.getExtraInfo().getVendor())){
			return new AsynchronousGroupingJob(param);
		}
		
		if (solution.isAdaptiveStreamJobAvaliable())
			return new AdaptiveStreamJob(param);
		else
			return new GroupFilesJob(param);
	}
}
