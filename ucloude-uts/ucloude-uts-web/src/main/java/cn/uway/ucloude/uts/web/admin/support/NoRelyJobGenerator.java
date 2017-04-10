package cn.uway.ucloude.uts.web.admin.support;

import java.util.Date;

import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.support.NonRelyJobUtils;
import cn.uway.ucloude.uts.web.cluster.BackendAppContext;

public class NoRelyJobGenerator {
	private BackendAppContext appContext;
    private int scheduleIntervalMinute;

    public NoRelyJobGenerator(BackendAppContext appContext) {
        this.appContext = appContext;
        this.scheduleIntervalMinute = this.appContext.getConfiguration().getParameter(ExtConfigKeys.JOB_TRACKER_NON_RELYON_PREV_CYCLE_JOB_SCHEDULER_INTERVAL_MINUTE, 10);

    }

    public void generateCronJobForInterval(final JobPo jobPo, Date lastGenerateTime) {
        NonRelyJobUtils.addCronJobForInterval(appContext.getExecutableJobQueue(), appContext.getCronJobQueue(),
                scheduleIntervalMinute, jobPo, lastGenerateTime);
    }

    public void generateRepeatJobForInterval(final JobPo jobPo, Date lastGenerateTime) {
        NonRelyJobUtils.addRepeatJobForInterval(appContext.getExecutableJobQueue(), appContext.getRepeatJobQueue(),
                scheduleIntervalMinute, jobPo, lastGenerateTime);
    }
}
