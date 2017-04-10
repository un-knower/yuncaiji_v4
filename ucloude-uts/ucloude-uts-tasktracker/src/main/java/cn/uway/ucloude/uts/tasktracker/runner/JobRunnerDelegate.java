package cn.uway.ucloude.uts.tasktracker.runner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.log.DotLogUtils;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.domain.Action;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.domain.JobMeta;
import cn.uway.ucloude.uts.core.support.JobUtils;
import cn.uway.ucloude.uts.tasktracker.Result;
import cn.uway.ucloude.uts.tasktracker.domain.Response;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;
import cn.uway.ucloude.uts.tasktracker.logger.BizLoggerAdapter;
import cn.uway.ucloude.uts.tasktracker.logger.BizLoggerFactory;
import cn.uway.ucloude.uts.tasktracker.monitor.TaskTrackerMStatReporter;
import sun.nio.ch.Interruptible;

/**
 * Job Runner 的代理类,
 * 1. 做一些错误处理之类的事情
 * 2. 监控统计
 * 3. Context信息设置
 * @author uway
 *
 */
public class JobRunnerDelegate implements Runnable {
	private static final ILogger LOGGER = LoggerManager.getLogger(JobRunnerDelegate.class);

	private JobMeta jobMeta;
	private RunnerCallback callback;
	private BizLoggerAdapter logger;
	private TaskTrackerContext context;
	private TaskTrackerMStatReporter stat;
	private Interruptible interruptor;
	private JobRunner curJobRunner;
	private AtomicBoolean interrupted = new AtomicBoolean(false);
	private Thread thread;

	public JobRunnerDelegate(TaskTrackerContext context, JobMeta jobMeta, RunnerCallback callBack) {
		super();
		this.jobMeta = jobMeta;
		this.callback = callBack;
		this.context = context;
		this.logger = (BizLoggerAdapter) BizLoggerFactory.getLogger(context.getBizLogLevel(), context.getRpcClient(),
				context);
		stat = (TaskTrackerMStatReporter) context.getMStatReporter();
		this.interruptor = new InterruptibleAdapter() {
            public void interrupt() {
                JobRunnerDelegate.this.interrupt();
            }
        };
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		thread = Thread.currentThread();
		try {
			blockedOn(interruptor);
			if (Thread.currentThread().isInterrupted()) {
				((InterruptibleAdapter) interruptor).interrupt();
			}

			TaskLoggerFactory.setLogger(logger);

			while (jobMeta != null) {
				long startTime = SystemClock.now();
				// 设置当前context中的jobId
				logger.setJobMeta(jobMeta);
				Response response = new Response();
				response.setJobMeta(jobMeta);
				try {
					context.getRunnerPool().getRunningJobManager().in(jobMeta.getJobId(), this);
					this.curJobRunner = context.getRunnerPool().getRunnerFactory().newRunner();
					Result result = this.curJobRunner.run(buildJobContext(jobMeta));

					if (result == null) {
						response.setAction(Action.EXECUTE_SUCCESS);
					} else {
						if (result.getAction() == null) {
							response.setAction(Action.EXECUTE_SUCCESS);
						} else {
							response.setAction(result.getAction());
						}
						response.setMsg(result.getMsg());
					}

					long time = SystemClock.now() - startTime;
					stat.addRunningTime(time);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Job execute completed : {}, time:{} ms.", jobMeta.getJob(), time);
					}
				} catch (Throwable t) {
					StringWriter sw = new StringWriter();
					t.printStackTrace(new PrintWriter(sw));
					response.setAction(Action.EXECUTE_EXCEPTION);
					response.setMsg(sw.toString());
					long time = SystemClock.now() - startTime;
					stat.addRunningTime(time);
					LOGGER.error("Job execute error : {}, time: {}, {}", jobMeta.getJob(), time, t.getMessage(), t);
				} finally {
					checkInterrupted();
					logger.removeJobMeta();
					context.getRunnerPool().getRunningJobManager().out(jobMeta.getJobId());
				}
				// 统计数据
				stat(response.getAction());

				if (isStopToGetNewJob()) {
					response.setReceiveNewJob(false);
				}
				this.jobMeta = callback.runComplete(response);
				DotLogUtils.dot("JobRunnerDelegate.run get job " + (this.jobMeta == null ? "NULL" : "NOT_NULL"));
			}
		} finally {
			TaskLoggerFactory.remove();

			blockedOn(null);
		}
	}

	private JobContext buildJobContext(JobMeta jobMeta) {
		JobContext jobContext = new JobContext();
		// 采用deepopy的方式 防止用户修改任务数据
		Job job = JobUtils.copy(jobMeta.getJob());
		job.setTaskId(jobMeta.getRealTaskId()); // 这个对于用户需要转换为用户提交的taskId
		jobContext.setJob(job);

		JobExtInfo jobExtInfo = new JobExtInfo();
		jobExtInfo.setRepeatedCount(jobMeta.getRepeatedCount());
		jobExtInfo.setRetryTimes(jobMeta.getRetryTimes());
		jobExtInfo.setRetry(Boolean.TRUE.toString().equals(jobMeta.getInternalExtParam(ExtConfigKeys.IS_RETRY_JOB)));
		jobExtInfo.setJobType(jobMeta.getJobType());
		jobExtInfo.setSeqId(jobMeta.getInternalExtParam(ExtConfigKeys.EXE_SEQ_ID));

		jobContext.setJobExtInfo(jobExtInfo);

		jobContext.setBizLogger(TaskLoggerFactory.getBizLogger());
		return jobContext;
	}

	private void interrupt() {
		if (!interrupted.compareAndSet(false, true)) {
			return;
		}
		if (this.curJobRunner != null && this.curJobRunner instanceof InterruptibleJobRunner) {
			((InterruptibleJobRunner) this.curJobRunner).interrupt();
		}
	}

	private boolean isInterrupted() {
		return this.interrupted.get();
	}

	private void stat(Action action) {
		if (action == null) {
			return;
		}
		switch (action) {
		case EXECUTE_SUCCESS:
			stat.incSuccessNum();
			break;
		case EXECUTE_FAILED:
			stat.incFailedNum();
			break;
		case EXECUTE_LATER:
			stat.incExeLaterNum();
			break;
		case EXECUTE_EXCEPTION:
			stat.incExeExceptionNum();
			break;
		}
	}

	private static void blockedOn(Interruptible interruptible) {
		sun.misc.SharedSecrets.getJavaLangAccess().blockedOn(Thread.currentThread(), interruptible);
	}

	private abstract class InterruptibleAdapter implements Interruptible {
		// for > jdk7
		public void interrupt(Thread thread) {
			interrupt();
		}

		public abstract void interrupt();
	}

	private boolean isStopToGetNewJob() {
		if (isInterrupted()) {
			// 如果当前线程被阻断了,那么也就不接受新任务了
			return true;
		}
		// 机器资源是否充足
		return !context.getConfiguration().getInternalData(ExtConfigKeys.MACHINE_RES_ENOUGH, true);
	}

	private void checkInterrupted() {
		try {
			if (isInterrupted()) {
				logger.info("SYSTEM:Interrupted");
			}
		} catch (Throwable t) {
			LOGGER.warn("checkInterrupted error", t);
		}
	}

	public Thread currentThread() {
		return thread;
	}

	public JobMeta currentJob() {
		return jobMeta;
	}

}
