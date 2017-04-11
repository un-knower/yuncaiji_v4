package cn.uway.framework.job;

import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.framework.parser.DBParser;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.status.Status;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.MultiElementGatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.framework.task.worker.TaskWorkTerminateException;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.StringUtil;

/**
 * 抽象作业类
 * 
 */
public class GroupFilesJob extends GenericJob {

	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(GroupFilesJob.class);

	/**
	 * 构造方法
	 * 
	 * @param jobParam
	 *            作业运行参数{@link JobParam}
	 */
	public GroupFilesJob(JobParam jobParam) {
		super(jobParam);
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
		LOGGER.debug("taskId={},GroupFilesJob线程开始。", rightTaskId);
		// 输出模版处理
		createExportTemplate();
		if (exportTemplateBeans == null || exportTemplateBeans.size() == 0) {
			LOGGER.error("初始化数据库输出目的地失败.没有找到输出目的地");
			return new JobFuture(-1, "[初始化数据库输出目的地失败]");
		}
		accessBefore();

		// pathEntry是MultiElementGatherPathEntry 所以包含多个采集实体
		GatherPathEntry pathEntry = this.jobParam.getPathEntry();
		MultiElementGatherPathEntry multiElementGatherPathEntry = (MultiElementGatherPathEntry) pathEntry;
		this.repairJob = multiElementGatherPathEntry.isRepairTask();
		List<String> pathEntrys = multiElementGatherPathEntry.getGatherPaths();
		int entryNum = pathEntrys.size();
		boolean jobProcessRet = false;
		boolean bNeedCloseAccesser = false;
		LOGGER.debug("本次分组解码共有{}个文件需要处理，记录抽取模式：{}", new Object[]{entryNum, extractRecordMode});

		try {
			for (int i = 0; i < entryNum; i++) {
				String path = pathEntrys.get(i);
				String decodedFTPPath = path;
				/*
				 * path这个路径的来源是从FTP获取的文件列表，需要按照本地编码格式转换
				 * 
				 * @author Niow 2014-6-12
				 */
				if(jobParam.getConnInfo() instanceof FTPConnectionInfo){
					FTPConnectionInfo ftpConnInfo = (FTPConnectionInfo)jobParam.getConnInfo();
					decodedFTPPath = StringUtil.decodeFTPPath(path, ftpConnInfo.getCharset());
				}
				LOGGER.debug("开始下载解析第{}个文件，该分组总共{}个文件", new Object[]{i + 1, entryNum});

				// 开始进行Access
				AccessOutObject accessOutObject = null;
				LOGGER.debug("[access start] fileName:{}", decodedFTPPath);
				try {
					accessor.setConnectionInfo(jobParam.getConnInfo());
					accessOutObject = accessor.access(new GatherPathEntry(path));
					accessOutObject.setTask(task);
					bNeedCloseAccesser = true;
				} catch (Exception e) {
					LOGGER.error("Accessor接入异常", e);
					// parser、accessor的close动作在finnaly中关闭
					return new JobFuture(-1, "[GroupFilesJob]Accessor接入异常");
				}
				// 将任务和采集对象名字设置到接入结果对象中
				accessOutObject.setRawAccessName(decodedFTPPath);
				// 将采集对象名放入到entryName中
				entryNames.add(FilenameUtils.getName(decodedFTPPath));
				// 数据解码前的shell执行处理
				beforeParse(solution.getBeforePaserShell());
				// 调用parser.parse 初始化 解析器 如果解析器初始化失败 异常返回
				try {
					LOGGER.debug("[parse start]");
					parser.parse(accessOutObject);
				} catch (Exception e) {
					LOGGER.debug("解析过程中parse()发生错误。");
					//access, parser会在finally中关闭掉.
					if (e instanceof TaskWorkTerminateException) {
						LOGGER.debug("解析过程中parse()被要求终止。");
						return new JobFuture(TaskWorkTerminateException.exceptionCode, "解析被要求终止，下个周期再尝试." + e.getMessage());
					} else {
						LOGGER.error("解析过程中parse()发生异常。", e);
						return new JobFuture(-1, "解析器初始化失败," + e.getMessage());
					}
				}

				Status status = null;
				//entryNames.add(StringUtil.getFilename(decodedFTPPath));
				try {
					LOGGER.debug("[create status start]");
					// 创建采集对象 此时会进行状态更新 入库等
					status = this.createGatherObjStatus(task, decodedFTPPath);
					LOGGER.debug("[create status close]");
					statusList.add(status);
					status.initDataParse();
				} catch (Exception e) {
					LOGGER.error("创建采集状态异常。", e);
					// parser、accessor的close动作在finnaly中关闭
					return new JobFuture(-1, "创建采集状态异常," + e.getMessage());
				}

				// 循环取出所有解码后的记录
				status.initDataExport();
				// 开始工作
				if (extractRecordMode == EXTRACT_RECORD_NORMAL) {
					jobProcessRet = extractRecordToWareHouse(accessOutObject);
				}
				// 做完一个文件，要将当前的accessor关闭，但不能置为null值，否则在afterJob不能成功生成输出状态报告
				if (accessor != null) {
					accessor.close();
					bNeedCloseAccesser = false;
				}

				if (!jobProcessRet && extractRecordMode == EXTRACT_RECORD_NORMAL)
					break;
			}
			
			if (extractRecordMode == EXTRACT_RECORD_AFTER_PARSED_ALL_GROUPFILES) {
				LOGGER.debug("文件解析完成，开始抽取话单:");
				// 调用一下after，通知下parser，本次分组的文件都处理完成了．
				parser.after();
				jobProcessRet = extractRecordToWareHouse(null);;
			}

			// doJob都成功完成了，才打印成功解析日志。
			if (jobProcessRet) {
				LOGGER.debug("任务丢失话单数量：{}", invalideNum);
				LOGGER.debug("【解析时间统计】解码耗时{}秒，{}条记录被解析。parser closed", new Object[]{totalTime / 1000.00, count});

				// 空数据源处理
				if (count == 0) {
					emptyDataSourceHandler();
				}
			}

			// 正常退出要将accessor置空，以防finally两次调用close();
			//accessor = null;
		} catch (Exception e) {
			LOGGER.debug("解析发生异常，taskId=" + rightTaskId, e);
		} finally {
			try {
				if (repository != null)
					endExport(repository, !jobProcessRet);

				if (accessor != null && bNeedCloseAccesser) {
					accessor.close();
					LOGGER.debug("[accessor close]");
				}

				if (parser != null) {
					parser.close();
					parser.afterClose();
				}

			} catch (Exception e) {
				LOGGER.debug("解析退出时产生异常，taskId=" + rightTaskId, e);
			}
		}

		// 处理报告和采集对象状态
		JobFuture jobFutrue = this.afterJob(jobProcessRet);
		LOGGER.debug("GroupFilesJob线程结束，taskId=" + rightTaskId);
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
			//
		}

		return !exceptionFlag || ignoreExceptin;
	}

	protected Status createGatherObjStatus(Task task, String pathEntry) {
		Status gatherObjStatus = new Status();
		gatherObjStatus = new Status();
		gatherObjStatus.setTaskId(task.getId());
		gatherObjStatus.setGatherObj(StringUtil.getFilename(pathEntry));
		gatherObjStatus.setPcName(task.getPcName());
		// 周期性任务需要加上数据时间进行判断
		if (task instanceof PeriodTask)
			gatherObjStatus.setDataTime(task.getDataTime());
		gatherObjStatus = objStatusInitialize(gatherObjStatus);
		gatherObjStatus.initDataAccess();
		return gatherObjStatus;
	}

	private JobFuture afterJob(boolean jobProcessRet) {
		int size = statusList.size();
		JobFuture jobFuture = null;
		for (int i = 0; i < size; i++) {
			Status status = statusList.get(i);
			// 因为解码出错就会中断，所以除最后一个外，其余的jobProcessRet都等于true
			if (i < size - 1)
				this.afterJob(status, true);
			else {
				// 返回最后一次的jobFuture
				jobFuture = this.afterJob(status, jobProcessRet);
			}
		}

		return jobFuture;
	}

}
