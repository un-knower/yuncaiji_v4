package cn.uway.framework.job;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import cn.uway.framework.accessor.AccessOutObject;
import cn.uway.framework.accessor.StreamAccessOutObject;
import cn.uway.framework.connection.FTPConnectionInfo;
import cn.uway.framework.job.AdaptiveInputStream.CompressionFileEntry;
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

public class AdaptiveStreamJob extends GenericJob {

	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(AdaptiveStreamJob.class);

	/**
	 * 是否补采任务
	 */
	protected boolean repairJob;

	protected boolean isStreamAccessor;

	/**
	 * 构造方法
	 * 
	 * @param jobParam
	 *            作业运行参数{@link JobParam}
	 */
	public AdaptiveStreamJob(JobParam jobParam) {
		super(jobParam);
		this.isStreamAccessor = true;
	}

	@Override
	public JobFuture call() {
		renameThread();
		// 给线程设置线程名。
		LOGGER.debug("taskId={},AdaptiveStreamJob线程开始。", rightTaskId);
		// 输出模版处理
		createExportTemplate();
		if (exportTemplateBeans == null || exportTemplateBeans.size() == 0) {
			LOGGER.error("初始化数据库输出目的地失败.没有找到输出目的地");
			return new JobFuture(-1, "[初始化数据库输出目的地失败]");
		}
		accessBefore();
		
		List<String> pathEntrys = null;
		GatherPathEntry pathEntry = this.jobParam.getPathEntry();
		if (pathEntry instanceof MultiElementGatherPathEntry) {
			MultiElementGatherPathEntry multiElementGatherPathEntry = (MultiElementGatherPathEntry) pathEntry;
			this.repairJob = multiElementGatherPathEntry.isRepairTask();
			pathEntrys = multiElementGatherPathEntry.getGatherPaths();

			LOGGER.debug("本次分组解码共有{}个文件需要处理", pathEntrys.size());
		} else {
			pathEntrys = new ArrayList<String>(1);
			pathEntrys.add(pathEntry.getPath());
		}
		int entryNum = pathEntrys.size();
		boolean jobProcessRet = false;
		boolean bNeedCloseAccesser = false;

		try {
			for (int i = 0; i < entryNum; i++) {
				String path = pathEntrys.get(i);
				String decodedFTPPath = path;
				int parsedSuccFileNum = 0;
				int parsedIgnoreFileNum = 0;

				if (jobParam.getConnInfo() instanceof FTPConnectionInfo) {
					FTPConnectionInfo ftpConnInfo = (FTPConnectionInfo) jobParam.getConnInfo();
					decodedFTPPath = StringUtil.decodeFTPPath(path, ftpConnInfo.getCharset());
				}

				if (entryNum > 1) {
					LOGGER.debug("开始下载解析第{}个文件，该分组总共{}个文件", new Object[]{i + 1, entryNum});
				}
				// 开始进行Access
				AccessOutObject accessOutObject = null;
				LOGGER.debug("[access start] fileName:{}", decodedFTPPath);
				try {
					accessor.setConnectionInfo(jobParam.getConnInfo());
					accessOutObject = accessor.access(new GatherPathEntry(path));
					accessOutObject.setTask(task);
					bNeedCloseAccesser = true;

					if (!(accessOutObject instanceof StreamAccessOutObject)) {
						// return new JobFuture(-1, "[AdaptiveStreamJob]非流式接入器，不能使用AdaptiveStreamJob");
						this.isStreamAccessor = false;
					}
				} catch (Exception e) {
					LOGGER.error("Accessor接入异常", e);
					if (parser != null && parser instanceof DBParser) {
						DBParser dbParser = (DBParser) parser;
						dbParser.setTempletIds(new StringBuilder(decodedFTPPath));
						dbParser.setCause("补采原因：" + e.getMessage());
						dbParser.regatherHandle(task);
					}
					// accessor.close();
					// parser、accessor的close动作在finnaly中关闭
					return new JobFuture(-1, "[AdaptiveStreamJob]Accessor接入异常");
				}

				// 将任务和采集对象名字设置到接入结果对象中
				accessOutObject.setRawAccessName(decodedFTPPath);
				// 将采集对象名放入到entryName中
				entryNames.add(FilenameUtils.getName(decodedFTPPath));
				// 数据解码前的shell执行处理
				beforeParse(solution.getBeforePaserShell());

				// 每一条Status一代表一条状态表日志，所以一个文件一条，一个压缩包也是一条，status的创建要在parser之后，不然状态初始化不正确
				Status status = null;
				if (!this.isStreamAccessor) {
					// 非流式文件的处理
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
						accessor = null;
						bNeedCloseAccesser = false;
						
						if (e instanceof TaskWorkTerminateException) {
							LOGGER.debug("解析过程中parse()被要求终止。");
							return new JobFuture(TaskWorkTerminateException.exceptionCode, "解析被要求终止，下个周期再尝试." + e.getMessage());
						} else {
							LOGGER.error("解析过程中parse()发生异常。", e);
							return new JobFuture(-1, "解析器初始化失败," + e.getMessage());
						}
					}

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
					jobProcessRet = extractRecordToWareHouse(accessOutObject);
				} else {
					StreamAccessOutObject streamAccessOutObject = (StreamAccessOutObject) accessOutObject;
					// 设置接入对象流的包名
					streamAccessOutObject.setRawAccessPackName(streamAccessOutObject.getRawAccessName());
					InputStream srcStream = streamAccessOutObject.getOutObject();
					AdaptiveInputStream adaptiveStream = new AdaptiveInputStream(srcStream, decodedFTPPath);
					CompressionFileEntry entry = null;
					boolean bExceptionOcurred = false;
					
					try {
						while ((entry = adaptiveStream.getNextEntry()) != null) {
							if (this.packSubFileMatchExpression != null) {
								if (!FilenameUtils.wildcardMatch(entry.fileName, packSubFileMatchExpression)) {
									++parsedIgnoreFileNum;
									LOGGER.debug("在压缩包中找到子文件：\"{}\"，与通匹符：\"{}\"不匹配，跳过", entry.fileName, packSubFileMatchExpression);
									continue;
								}
							}
							++parsedSuccFileNum;
							
							streamAccessOutObject.setOutObject(entry.inputStream);
							streamAccessOutObject.setRawAccessName(entry.fileName);							
							
							// 调用parser.parse 初始化 解析器 如果解析器初始化失败 异常返回
							try {
								if (adaptiveStream.isArachiveFile())
									LOGGER.debug("[parse start] entry file={} package file={}", entry.fileName,
											streamAccessOutObject.getRawAccessPackName());
								else
									LOGGER.debug("[parse start] raw file={}", streamAccessOutObject.getRawAccessPackName());
								
								// 设置解析文件的默认时间
								if (parser.getCurrentDataTime() == null) {
									parser.setDataTime(task instanceof PeriodTask ? task.getDataTime() : jobParam.getPathEntry().getDateTime());
								}
								parser.parse(streamAccessOutObject);
							} catch (Exception e) {
								bExceptionOcurred = true;
								LOGGER.error("解析过程中parse()发生异常。", e);
								if (parser != null && parser instanceof DBParser) {
									DBParser dbParser = (DBParser) parser;
									dbParser.setTempletIds(new StringBuilder(decodedFTPPath));
									dbParser.setCause("补采原因：" + e.getMessage());
								}
								// parser、accessor的close动作在最外层finnaly中关闭
								//return new JobFuture(-1, "解析器初始化失败," + e.getMessage());
								if (e instanceof TaskWorkTerminateException) {
									LOGGER.debug("解析过程中parse()被要求终止。");
									return new JobFuture(TaskWorkTerminateException.exceptionCode, "解析被要求终止，下个周期再尝试." + e.getMessage());
								} else {
									LOGGER.error("解析过程中parse()发生异常。", e);
									return new JobFuture(-1, "解析器初始化失败," + e.getMessage());
								}
							}
							
							// status必须放在parser后面，因为status.datetime需要在parser中获取
							if (status == null) {
								try {
									LOGGER.debug("[create status start]");
									// 创建采集对象 此时会进行状态更新 入库等
									status = this.createGatherObjStatus(task, decodedFTPPath);
									LOGGER.debug("[create status close]");
									statusList.add(status);
									// 初始化Status解析状态(Status.DATA_PARSE==3)
									status.initDataParse();
								} catch (Exception e) {
									LOGGER.error("创建采集状态异常。", e);
									// parser、accessor的close动作在finnaly中关闭
									return new JobFuture(-1, "创建采集状态异常," + e.getMessage());
								}
							}
							// 初始化Status输出状态(Status.DATA_EXPORT==5)
							status.initDataExport();
							
							// 开始工作
							jobProcessRet = extractRecordToWareHouse(streamAccessOutObject);
							if (!jobProcessRet)
								break;
						}
					} catch (Exception e) {
						bExceptionOcurred = true;
						LOGGER.error("解析中发生了异常", e);
					} finally {
						LOGGER.debug("从压缩包\"{}\"中，本次共成功解析了:{}个子文件，忽略了:{}个子文件.", new Object[]{decodedFTPPath, parsedSuccFileNum, parsedIgnoreFileNum});
						// 这里在判断一次，主要是因为有的压缩包是空文件，必须要建一个，否则会重复不断采集
						if (status == null && !bExceptionOcurred) {
							LOGGER.warn("压缩包为空包或压缩包中无符合通匹符的解析文件，现在IGP状态表中插入一条采集记录，以免IGP重复采集该文件. 文件名:{}", decodedFTPPath);
							try {
								LOGGER.debug("[create status start]");
								// 创建采集对象 此时会进行状态更新 入库等
								status = this.createGatherObjStatus(task, decodedFTPPath);
								LOGGER.debug("[create status close]");
								statusList.add(status);
								// 初始化Status解析状态(Status.DATA_PARSE==3)
								status.initDataParse();
							} catch (Exception e) {
								LOGGER.error("创建采集状态异常。", e);
								// parser、accessor的close动作在finnaly中关闭
								return new JobFuture(-1, "创建采集状态异常," + e.getMessage());
							}
						}

						adaptiveStream.close();
						// 将流设回原来的
						streamAccessOutObject.setOutObject(srcStream);
					}
				}

				// 做完一个文件，要将当前的accessor关闭，但不能置为null值，否则在afterJob不能成功生成输出状态报告
				if (accessor != null) {
					accessor.close();
					bNeedCloseAccesser = false;
				}

				if (!jobProcessRet)
					break;
			} // end of: for (int i = 0; i < entryNum; i++) {

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
			// accessor = null;
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
		LOGGER.debug("AdaptiveStreamJob线程结束，taskId=" + rightTaskId);
		return jobFutrue;
	}

	/**
	 * 开始工作：解析，启动warehouse，向warehouse传送数据
	 * 
	 * @param accessOutObject
	 * @return true:处理正常结束 false:处理过程中发生了异常
	 */
	@Override
	public boolean extractRecordToWareHouse(AccessOutObject accessOutObject) {
		if (!this.isStreamAccessor)
			return super.extractRecordToWareHouse(accessOutObject);

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
		gatherObjStatus = objStatusInitialize(gatherObjStatus, pathEntry);
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
