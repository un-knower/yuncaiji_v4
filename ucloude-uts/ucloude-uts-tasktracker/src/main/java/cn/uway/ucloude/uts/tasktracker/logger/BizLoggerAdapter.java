package cn.uway.ucloude.uts.tasktracker.logger;

import cn.uway.ucloude.uts.core.domain.JobMeta;

/**
 * @author magic.s.g.xie
 */
public abstract class BizLoggerAdapter implements BizLogger {

    private final ThreadLocal<JobMeta> jobMetaThreadLocal;

    public BizLoggerAdapter() {
        this.jobMetaThreadLocal = new ThreadLocal<JobMeta>();
    }

    public void setJobMeta(JobMeta jobMeta) {
        jobMetaThreadLocal.set(jobMeta);
    }

    public void removeJobMeta() {
        jobMetaThreadLocal.remove();
    }

    protected JobMeta getJobMeta() {
        return jobMetaThreadLocal.get();
    }

}
