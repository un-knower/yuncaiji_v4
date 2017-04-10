package cn.uway.ucloude.uts.jobtracker.support;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.time.DateUtils;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.utils.Callable;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.exception.UtsRuntimeException;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.support.NonRelyJobUtils;
import cn.uway.ucloude.uts.core.support.NodeShutdownHook;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

/**
 * 仅用于不依赖上一周期的任务生成器
 * @author uway
 *
 */
public class NonRelyOnPrevCycleJobScheduler {
	private static final ILogger LOGGER = LoggerManager.getLogger(NonRelyOnPrevCycleJobScheduler.class);
    private JobTrackerContext context;
    private int scheduleIntervalMinute;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean running = new AtomicBoolean(false);

    private AtomicBoolean start = new AtomicBoolean(false);

    public NonRelyOnPrevCycleJobScheduler(JobTrackerContext context) {
        this.context = context;
        this.scheduleIntervalMinute = this.context.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_NON_RELYON_PREV_CYCLE_JOB_SCHEDULER_INTERVAL_MINUTE, 10);

        NodeShutdownHook.registerHook(context.getEventCenter(), context.getConfiguration().getIdentity(), this.getClass().getSimpleName(), new Callable() {
            @Override
            public void call() throws Exception {
                stop();
            }
        });
    }

    public void start() {
        if (!start.compareAndSet(false, true)) {
            return;
        }
        try {
            executorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory(NonRelyOnPrevCycleJobScheduler.class.getSimpleName(), true));
            this.scheduledFuture = executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (running.compareAndSet(false, true)) {
                            try {
                                schedule();
                            } finally {
                                running.set(false);
                            }
                        }
                    } catch (Throwable t) {
                        LOGGER.error("Error On Schedule", t);
                    }
                }
            }, 10, (scheduleIntervalMinute - 1) * 60, TimeUnit.SECONDS);
        } catch (Throwable t) {
            LOGGER.error("Scheduler Start Error", t);
        }
    }

    public void stop() {
        if (!start.compareAndSet(true, false)) {
            return;
        }
        try {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
            if (executorService != null) {
                executorService.shutdownNow();
                executorService = null;
            }
        } catch (Throwable t) {
            LOGGER.error("Scheduler Stop Error", t);
        }
    }

    private void schedule() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("========= Scheduler start =========");
        }

        Date now = new Date();
        Date checkTime = DateUtils.addMinutes(now, 10);
        //  cron任务
        while (true) {
            List<JobPo> jobPos = context.getCronJobQueue().getNeedGenerateJobPos(checkTime.getTime(), 10);
            if (CollectionUtil.sizeOf(jobPos) == 0) {
                break;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("========= CronJob size[{}] =========", CollectionUtil.sizeOf(jobPos));
            }
            for (JobPo jobPo : jobPos) {
                Long lastGenerateTriggerTime = jobPo.getLastGenerateTriggerTime();
                if (lastGenerateTriggerTime == null || lastGenerateTriggerTime == 0) {
                    lastGenerateTriggerTime = new Date().getTime();
                }
                addCronJobForInterval(jobPo, new Date(lastGenerateTriggerTime));
            }
        }

        // repeat 任务
        while (true) {
            List<JobPo> jobPos = context.getRepeatJobQueue().getNeedGenerateJobPos(checkTime.getTime(), 10);
            if (CollectionUtil.sizeOf(jobPos) == 0) {
                break;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("========= Repeat size[{}] =========", CollectionUtil.sizeOf(jobPos));
            }
            for (JobPo jobPo : jobPos) {
                Long lastGenerateTriggerTime = jobPo.getLastGenerateTriggerTime();
                if (lastGenerateTriggerTime == null || lastGenerateTriggerTime == 0) {
                    lastGenerateTriggerTime = new Date().getTime();
                }
                addRepeatJobForInterval(jobPo, new Date(lastGenerateTriggerTime));
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("========= Scheduler End =========");
        }
    }

    public void addScheduleJobForOneHour(JobPo jobPo) {
        if (jobPo.isCron()) {
            addCronJobForInterval(jobPo, new Date());
        } else if (jobPo.isRepeatable()) {
            addRepeatJobForInterval(jobPo, new Date());
        } else {
            throw new UtsRuntimeException("Only For Cron Or Repeat Job Now");
        }
    }

    private void addCronJobForInterval(final JobPo finalJobPo, Date lastGenerateTime) {
        NonRelyJobUtils.addCronJobForInterval(context.getExecutableJobQueue(), context.getCronJobQueue(),
                scheduleIntervalMinute, finalJobPo, lastGenerateTime);
    }

    private void addRepeatJobForInterval(final JobPo finalJobPo, Date lastGenerateTime) {
        NonRelyJobUtils.addRepeatJobForInterval(context.getExecutableJobQueue(), context.getRepeatJobQueue(),
                scheduleIntervalMinute, finalJobPo, lastGenerateTime);
    }
}
