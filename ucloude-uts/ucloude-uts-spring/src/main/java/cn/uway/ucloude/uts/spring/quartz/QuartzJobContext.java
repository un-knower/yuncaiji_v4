package cn.uway.ucloude.uts.spring.quartz;

import cn.uway.ucloude.uts.spring.quartz.invoke.JobExecution;
import org.quartz.Trigger;

import java.util.Map;

/**
 * @author magic.s.g.xie
 */
public class QuartzJobContext {

    private String name;

    private QuartzJobType type;

    private Trigger trigger;

    private JobExecution jobExecution;

    private Map<String, Object> jobDataMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QuartzJobType getType() {
        return type;
    }

    public void setType(QuartzJobType type) {
        this.type = type;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public JobExecution getJobExecution() {
        return jobExecution;
    }

    public void setJobExecution(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    public Map<String, Object> getJobDataMap() {
        return jobDataMap;
    }

    public void setJobDataMap(Map<String, Object> jobDataMap) {
        this.jobDataMap = jobDataMap;
    }
}
