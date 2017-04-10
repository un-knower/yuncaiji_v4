package cn.uway.ucloude.uts.spring.quartz.invoke;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.core.jmx.JobDataMapSupport;

import cn.uway.ucloude.utils.CollectionUtil;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.spring.quartz.QuartzJobContext;

import java.util.Map;

/**
 * @author magic.s.g.xie
 */
public class JobDetailJobExecution implements JobExecution {

    private org.quartz.Job quartzJob;

    public JobDetailJobExecution(org.quartz.Job quartzJob) {
        this.quartzJob = quartzJob;
    }

    @Override
    public void execute(QuartzJobContext quartzJobContext, Job job) throws Throwable {

        JobDataMap jobDataMap = JobDataMapSupport.newJobDataMap(quartzJobContext.getJobDataMap());

        // 用lts的覆盖
        Map<String, String> map = job.getExtParams();
        if (CollectionUtil.isNotEmpty(map)) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                jobDataMap.put(entry.getKey(), entry.getValue());
            }
        }

        JobExecutionContext jobExecutionContext = new JobExecutionContextImpl(jobDataMap);
        quartzJob.execute(jobExecutionContext);
    }
}
