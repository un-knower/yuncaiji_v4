package cn.uway.ucloude.uts.spring.quartz.invoke;

import org.springframework.util.MethodInvoker;

import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.spring.quartz.QuartzJobContext;

/**
 * @author magic.s.g.xie
 */
public class MethodInvokeJobExecution implements JobExecution {

    private MethodInvoker methodInvoker;

    public MethodInvokeJobExecution(MethodInvoker methodInvoker) {
        this.methodInvoker = methodInvoker;
    }

    @Override
    public void execute(QuartzJobContext quartzJobContext, Job job) throws Throwable {
        methodInvoker.invoke();
    }
}
