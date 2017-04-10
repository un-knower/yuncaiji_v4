package cn.uway.ucloude.uts.jobtracker.support;

import java.util.Date;
import java.util.List;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.data.dataaccess.exception.DupEntryException;
import cn.uway.ucloude.log.ILogger;

import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.Assert;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.LogType;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.exception.JobReceiveException;
import cn.uway.ucloude.uts.core.protocol.command.JobSubmitRequest;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.support.CronExpressionUtils;
import cn.uway.ucloude.uts.core.support.JobDomainConverter;
import cn.uway.ucloude.uts.core.support.JobUtils;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;
import cn.uway.ucloude.uts.jobtracker.monitor.JobTrackerMStatReporter;
import cn.uway.ucloude.uts.core.domain.Level;

/**
 * 任务处理器
 * 
 * @author uway
 *
 */
public class JobReceiver {
	private static ILogger logger = LoggerManager.getLogger(JobReceiver.class);

	private JobTrackerContext context;

	private JobTrackerMStatReporter stat;

	public JobReceiver(JobTrackerContext context) {
		this.context = context;
		this.stat = (JobTrackerMStatReporter) context.getMStatReporter();
	}

	public void receive(JobSubmitRequest request) throws JobReceiveException {
		List<Job> jobs = request.getJobs();
		if (CollectionUtil.isEmpty(jobs)) {
			return;
		}
		JobReceiveException exception = null;
		for (Job job : jobs) {
			try {
				addToQueue(job, request);
			} catch (Exception e) {
				if (exception == null) {
					exception = new JobReceiveException(e);
				}
				exception.addJob(job);
			}
		}

		if (exception != null) {
			throw exception;
		}
	}

	private JobPo addToQueue(Job job, JobSubmitRequest request) {

		JobPo jobPo = null;
		boolean success = false;
		BizLogCode code = null;
		try {
			jobPo = JobDomainConverter.convert(job);
			if (jobPo == null) {
				logger.warn("Job can not be null。{}", job);
				return null;
			}
			if (StringUtil.isEmpty(jobPo.getSubmitNodeGroup())) {
				jobPo.setSubmitNodeGroup(request.getNodeGroup());
			}
			// 设置 jobId
			jobPo.setJobId(JobUtils.generateJobId());

			// 添加任务
			addJob(job, jobPo);

			success = true;
			code = BizLogCode.SUCCESS;

		} catch (DupEntryException e) {
			// 已经存在
			if (job.isReplaceOnExist()) {
				Assert.notNull(jobPo);
				success = replaceOnExist(job, jobPo);
				code = success ? BizLogCode.DUP_REPLACE : BizLogCode.DUP_FAILED;
			} else {
				code = BizLogCode.DUP_IGNORE;
				logger.info("Job already exist And ignore. nodeGroup={}, {}", request.getNodeGroup(), job);
			}
		} finally {
			if (success) {
				stat.incReceiveJobNum();
				if (logger.isDebugEnabled()) {
					logger.debug("Receive Job success. {}", job);
				}
			}
		}

		// 记录日志
		jobBizLog(jobPo, code);

		return jobPo;
	}

	/**
	 * 添加任务
	 */
	private void addJob(Job job, JobPo jobPo) throws DupEntryException {
		if (job.isCron()) {
			addCronJob(jobPo);
		} else if (job.isRepeatable()) {
			addRepeatJob(jobPo);
		} else {
			addTriggerTimeJob(jobPo);
		}
	}

	private void addTriggerTimeJob(JobPo jobPo) {
		boolean needAdd2ExecutableJobQueue = true;
		String ignoreAddOnExecuting = CollectionUtil.getValue(jobPo.getInternalExtParams(),
				"__UTS_ignoreAddOnExecuting");
		if (ignoreAddOnExecuting != null && "true".equals(ignoreAddOnExecuting)) {
			if (context.getExecutingJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) != null) {
				needAdd2ExecutableJobQueue = false;
			}
		}
		if (needAdd2ExecutableJobQueue) {
			jobPo.setInternalExtParam(ExtConfigKeys.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
			context.getExecutableJobQueue().add(jobPo);
		}
	}

	/**
	 * 更新任务
	 **/
	private boolean replaceOnExist(Job job, JobPo jobPo) {

		// 得到老的job
		JobPo existJobPo = context.getExecutableJobQueue().getJob(job.getTaskTrackerNodeGroup(), jobPo.getTaskId());
		if (existJobPo == null) {
			existJobPo = context.getCronJobQueue().getJob(job.getTaskTrackerNodeGroup(), job.getTaskId());
			if (existJobPo == null) {
				existJobPo = context.getRepeatJobQueue().getJob(job.getTaskTrackerNodeGroup(), job.getTaskId());
			}
		}
		if (existJobPo != null) {
			String jobId = existJobPo.getJobId();
			// 1. 3个都删除下
			context.getExecutableJobQueue().removeBatch(jobPo.getRealTaskId(), jobPo.getTaskTrackerNodeGroup());
			context.getCronJobQueue().remove(jobId);
			context.getRepeatJobQueue().remove(jobId);

			jobPo.setJobId(jobId);
		}

		// 2. 重新添加任务
		try {
			addJob(job, jobPo);
		} catch (DupEntryException e) {
			// 一般不会走到这里
			logger.warn("Job already exist twice. {}", job);
			return false;
		}
		return true;
	}

	/**
	 * 添加Cron 任务
	 */
	private void addCronJob(JobPo jobPo) throws DupEntryException {
		Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCronExpression());
		if (nextTriggerTime != null) {

			if (context.getRepeatJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) != null) {
				// 这种情况是 由repeat 任务变为了 Cron任务
				throw new DupEntryException();
			}

			// 1.add to cron job queue
			context.getCronJobQueue().add(jobPo);

			if (JobUtils.isRelyOnPrevCycle(jobPo)) {
				// 没有正在执行, 则添加
				if (context.getExecutingJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(),
						jobPo.getTaskId()) == null) {
					// 2. add to executable queue
					jobPo.setTriggerTime(nextTriggerTime.getTime());
					try {
						jobPo.setInternalExtParam(ExtConfigKeys.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
						context.getExecutableJobQueue().add(jobPo);
					} catch (DupEntryException e) {
						context.getCronJobQueue().remove(jobPo.getJobId());
						throw e;
					}
				}
			} else {
				// 对于不需要依赖上一周期的,采取批量生成的方式
				context.getNonRelyOnPrevCycleJobScheduler().addScheduleJobForOneHour(jobPo);
			}
		}
	}

	/**
	 * 添加Repeat 任务
	 */
	private void addRepeatJob(JobPo jobPo) throws DupEntryException {

		if (context.getCronJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) != null) {
			// 这种情况是 由cron 任务变为了 repeat 任务
			throw new DupEntryException();
		}

		// 1.add to repeat job queue
		context.getRepeatJobQueue().add(jobPo);

		if (JobUtils.isRelyOnPrevCycle(jobPo)) {
			// 没有正在执行, 则添加
			if (context.getExecutingJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) == null) {
				// 2. add to executable queue
				try {
					jobPo.setInternalExtParam(ExtConfigKeys.EXE_SEQ_ID, JobUtils.generateExeSeqId(jobPo));
					context.getExecutableJobQueue().add(jobPo);
				} catch (DupEntryException e) {
					context.getRepeatJobQueue().remove(jobPo.getJobId());
					throw e;
				}
			}
		} else {
			// 对于不需要依赖上一周期的,采取批量生成的方式
			context.getNonRelyOnPrevCycleJobScheduler().addScheduleJobForOneHour(jobPo);
		}
	}

	/**
	 * 记录任务日志
	 */
	private void jobBizLog(JobPo jobPo, BizLogCode code) {
		if (jobPo == null) {
			return;
		}

		try {
			// 记录日志
			JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
			jobLogPo.setSuccess(true);
			jobLogPo.setLogType(LogType.RECEIVE);
			jobLogPo.setLogTime(SystemClock.now());

			switch (code) {
			case SUCCESS:
				jobLogPo.setLevel(Level.INFO);
				jobLogPo.setMsg("Receive Success");
				break;
			case DUP_IGNORE:
				jobLogPo.setLevel(Level.WARN);
				jobLogPo.setMsg("Already Exist And Ignored");
				break;
			case DUP_FAILED:
				jobLogPo.setLevel(Level.ERROR);
				jobLogPo.setMsg("Already Exist And Update Failed");
				break;
			case DUP_REPLACE:
				jobLogPo.setLevel(Level.INFO);
				jobLogPo.setMsg("Already Exist And Update Success");
				break;
			}

			context.getJobLogger().log(jobLogPo);
		} catch (Throwable t) { // 日志记录失败不影响正常运行
			logger.error("Receive Job Log error ", t);
		}
	}

	private enum BizLogCode {
		DUP_IGNORE, // 添加重复并忽略
		DUP_REPLACE, // 添加时重复并覆盖更新
		DUP_FAILED, // 添加时重复再次添加失败
		SUCCESS, // 添加成功
	}

}
