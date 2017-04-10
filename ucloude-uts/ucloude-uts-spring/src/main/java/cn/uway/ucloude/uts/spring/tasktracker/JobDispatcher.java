package cn.uway.ucloude.uts.spring.tasktracker;

import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.tasktracker.Result;
import cn.uway.ucloude.uts.tasktracker.runner.JobContext;
import cn.uway.ucloude.uts.tasktracker.runner.JobRunner;

/**
 * @author magic.s.g.xie
 */
public class JobDispatcher implements JobRunner {

    private String shardField = "taskId";

    @Override
    public Result run(JobContext jobContext) throws Throwable {

        Job job = jobContext.getJob();

        String value;
        if (shardField.equals("taskId")) {
            value = job.getTaskId();
        } else {
            value = job.getParam(shardField);
        }

        JobRunner jobRunner = null;
        if (StringUtil.isNotEmpty(value)) {
            jobRunner = JobRunnerHolder.getJobRunner(value);
        }
        if (jobRunner == null) {
            jobRunner = JobRunnerHolder.getJobRunner("_UTS_DEFAULT");

            if (jobRunner == null) {
                throw new JobDispatchException("Can not find JobRunner by Shard Value : [" + value + "]");
            }
        }
        return jobRunner.run(jobContext);
    }

    public void setShardField(String shardField) {
        if (StringUtil.isNotEmpty(shardField)) {
            this.shardField = shardField;
        }
    }

}
