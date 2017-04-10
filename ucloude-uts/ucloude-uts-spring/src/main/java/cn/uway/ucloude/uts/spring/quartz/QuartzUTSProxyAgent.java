package cn.uway.ucloude.uts.spring.quartz;


import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.thread.NamedThreadFactory;
import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.utils.QuietUtils;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.core.properties.JobClientProperties;
import cn.uway.ucloude.uts.jobclient.JobClient;
import cn.uway.ucloude.uts.jobclient.JobClientBuilder;
import cn.uway.ucloude.uts.jobclient.domain.Response;
import cn.uway.ucloude.uts.tasktracker.TaskTracker;
import cn.uway.ucloude.uts.tasktracker.TaskTrackerBuilder;
import cn.uway.ucloude.uts.tasktracker.runner.JobRunner;
import cn.uway.ucloude.uts.tasktracker.runner.RunnerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author magic.s.g.xie
 */
class QuartzUTSProxyAgent {

    private static final ILogger LOGGER = LoggerManager.getLogger(QuartzUTSProxyAgent.class);
    private QuartzUTSConfig quartzUTSConfig;
    private List<QuartzJobContext> quartzJobContexts = new CopyOnWriteArrayList<QuartzJobContext>();
    private AtomicBoolean ready = new AtomicBoolean(false);

    public QuartzUTSProxyAgent(QuartzUTSConfig quartzUTSConfig) {
        this.quartzUTSConfig = quartzUTSConfig;
    }

    // 开始代理
    public void startProxy(List<QuartzJobContext> cronJobs) {
        if (CollectionUtil.isEmpty(cronJobs)) {
            return;
        }
        quartzJobContexts.addAll(cronJobs);

        if (!ready.compareAndSet(false, true)) {
            return;
        }
        new NamedThreadFactory(QuartzUTSProxyAgent.class.getSimpleName() + "_LazyStart").newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 3S之后启动 JobClient和TaskTracker, 为了防止有多个SchedulerFactoryBean, 从而这个方法被调用多次
                    QuietUtils.sleep(3000);
                    startProxy0();
                } catch (Throwable t) {
                    LOGGER.error("Error on start " + QuartzUTSProxyAgent.class.getSimpleName(), t);
                }
            }
        }).start();
    }

    private void startProxy0() {
        // 1. 先启动 TaskTracker
        startTaskTracker();

        // 2. 启动JobClient并提交任务
        JobClient jobClient = startJobClient();

        // 3. 提交任务
        submitJobs(jobClient);
    }

    private void startTaskTracker() {

        TaskTracker taskTracker = TaskTrackerBuilder.buildByProperties(quartzUTSConfig.getTaskTrackerProperties());
        taskTracker.setWorkThreads(quartzJobContexts.size());
        taskTracker.setJobRunnerClass(QuartzJobRunnerDispatcher.class);

        final QuartzJobRunnerDispatcher jobRunnerDispatcher = new QuartzJobRunnerDispatcher(quartzJobContexts);
        taskTracker.setRunnerFactory(new RunnerFactory() {
            @Override
            public JobRunner newRunner() {
                return jobRunnerDispatcher;
            }
        });

        taskTracker.start();
    }

    private JobClient startJobClient() {
        JobClientProperties jobClientProperties = quartzUTSConfig.getJobClientProperties();
        jobClientProperties.setUseRetryClient(false);
        JobClient jobClient = JobClientBuilder.buildByProperties(jobClientProperties);
        jobClient.start();
        return jobClient;
    }

    private void submitJobs(JobClient jobClient) {

        List<Job> jobs = new ArrayList<Job>(quartzJobContexts.size());
        for (QuartzJobContext quartzJobContext : quartzJobContexts) {

            if (QuartzJobType.CRON == quartzJobContext.getType()) {
                jobs.add(buildCronJob(quartzJobContext));
            } else if (QuartzJobType.SIMPLE_REPEAT == quartzJobContext.getType()) {
                jobs.add(buildSimpleJob(quartzJobContext));
            }
        }
        LOGGER.info("=============UTS=========== Submit start");
        submitJobs0(jobClient, jobs);
        LOGGER.info("=============UTS=========== Submit end");
    }

    private Job buildCronJob(QuartzJobContext quartzJobContext) {

        CronTriggerImpl cronTrigger = (CronTriggerImpl) quartzJobContext.getTrigger();
        String cronExpression = cronTrigger.getCronExpression();
        String description = cronTrigger.getDescription();
        int priority = cronTrigger.getPriority();
        String name = quartzJobContext.getName();

        Job job = new Job();
        job.setTaskId(name);
        job.setPriority(priority);
        job.setCronExpression(cronExpression);
        job.setSubmitNodeGroup(quartzUTSConfig.getJobClientProperties().getNodeGroup());
        job.setTaskTrackerNodeGroup(quartzUTSConfig.getTaskTrackerProperties().getNodeGroup());
        job.setParam("description", description);
        setJobProp(job);

        return job;
    }

    private Job buildSimpleJob(QuartzJobContext quartzJobContext) {

        SimpleTriggerImpl simpleTrigger = (SimpleTriggerImpl) quartzJobContext.getTrigger();

        String description = simpleTrigger.getDescription();
        int priority = simpleTrigger.getPriority();
        String name = quartzJobContext.getName();
        int repeatCount = simpleTrigger.getRepeatCount();
        long repeatInterval = simpleTrigger.getRepeatInterval();

        Job job = new Job();
        job.setTaskId(name);

        job.setTriggerDate(simpleTrigger.getNextFireTime());
        job.setRepeatCount(repeatCount);

        if (repeatCount != 0) {
            job.setRepeatInterval(repeatInterval);
        }
        job.setPriority(priority);
        job.setSubmitNodeGroup(quartzUTSConfig.getJobClientProperties().getNodeGroup());
        job.setTaskTrackerNodeGroup(quartzUTSConfig.getTaskTrackerProperties().getNodeGroup());
        job.setParam("description", description);
        setJobProp(job);

        return job;
    }

    private void setJobProp(Job job) {
        QuartzUTSConfig.JobProperties jobProperties = quartzUTSConfig.getJobProperties();
        if (jobProperties == null) {
            return;
        }
        if (jobProperties.getMaxRetryTimes() != null) {
            job.setMaxRetryTimes(jobProperties.getMaxRetryTimes());
        }
        if (jobProperties.getNeedFeedback() != null) {
            job.setNeedFeedback(jobProperties.getNeedFeedback());
        }
        if (jobProperties.getRelyOnPrevCycle() != null) {
            job.setRelyOnPrevCycle(jobProperties.getRelyOnPrevCycle());
        }
        if (jobProperties.getReplaceOnExist() != null) {
            job.setReplaceOnExist(jobProperties.getReplaceOnExist());
        }
    }

    private void submitJobs0(JobClient jobClient, List<Job> jobs) {
        List<Job> failedJobs = null;
        try {
            Response response = jobClient.submitJob(jobs);
            if (!response.isSuccess()) {
                LOGGER.warn("Submit Quartz Jobs to UTS failed: {}", JsonConvert.serialize(response));
                failedJobs = response.getFailedJobs();
            }
        } catch (Throwable e) {
            LOGGER.warn("Submit Quartz Jobs to UTS error", e);
            failedJobs = jobs;
        }

        if (CollectionUtil.isNotEmpty(failedJobs)) {
            // 没提交成功要重试 3S 之后重试
            LOGGER.info("=============UTS=========== Sleep 3 Seconds and retry");
            QuietUtils.sleep(3000);
            submitJobs0(jobClient, failedJobs);
            return;
        }

        // 如果成功了, 关闭jobClient
        jobClient.stop();
    }
}
