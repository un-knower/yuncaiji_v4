package cn.uway.ucloude.uts.spring.quartz.invoke;

import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.spring.quartz.QuartzJobContext;

/**
 * @author magic.s.g.xie
 */
public interface JobExecution {

    public void execute(QuartzJobContext quartzJobContext, Job job) throws Throwable;

}
