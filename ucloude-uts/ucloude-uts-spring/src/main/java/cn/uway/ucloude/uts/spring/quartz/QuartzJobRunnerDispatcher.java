package cn.uway.ucloude.uts.spring.quartz;



import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.uway.ucloude.uts.core.domain.Action;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.tasktracker.Result;
import cn.uway.ucloude.uts.tasktracker.runner.JobContext;
import cn.uway.ucloude.uts.tasktracker.runner.JobRunner;

/**
 * @author magic.s.g.xie
 */
class QuartzJobRunnerDispatcher implements JobRunner {

    private ConcurrentMap<String, QuartzJobContext> JOB_MAP = new ConcurrentHashMap<String, QuartzJobContext>();

    public QuartzJobRunnerDispatcher(List<QuartzJobContext> quartzJobContexts) {
        for (QuartzJobContext quartzJobContext : quartzJobContexts) {
            String name = quartzJobContext.getName();
            JOB_MAP.put(name, quartzJobContext);
        }
    }

    @Override
    public Result run(JobContext jobContext) throws Throwable {
        Job job = jobContext.getJob();
        String taskId = job.getTaskId();

        QuartzJobContext quartzJobContext = JOB_MAP.get(taskId);
        if (quartzJobContext == null) {
            return new Result(Action.EXECUTE_FAILED, "Can't find the taskId[" + taskId + "]'s QuartzCronJob");
        }

        quartzJobContext.getJobExecution().execute(quartzJobContext, job);

        return new Result(Action.EXECUTE_SUCCESS);
    }
}
