package cn.uway.ucloude.uts.spring.quartz;

import cn.uway.ucloude.configuration.auto.annotation.ConfigurationProperties;
import cn.uway.ucloude.utils.Assert;
import cn.uway.ucloude.uts.core.exception.ConfigPropertiesIllegalException;
import cn.uway.ucloude.uts.core.properties.JobClientProperties;
import cn.uway.ucloude.uts.core.properties.TaskTrackerProperties;

/**
 * @author magic.s.g.xie
 */
class QuartzUTSConfig {

    private JobProperties jobProperties;

    private JobClientProperties jobClientProperties;

    private TaskTrackerProperties taskTrackerProperties;

    public JobClientProperties getJobClientProperties() {
        return jobClientProperties;
    }

    public void setJobClientProperties(JobClientProperties jobClientProperties) {
        this.jobClientProperties = jobClientProperties;
    }

    public TaskTrackerProperties getTaskTrackerProperties() {
        return taskTrackerProperties;
    }

    public void setTaskTrackerProperties(TaskTrackerProperties taskTrackerProperties) {
        this.taskTrackerProperties = taskTrackerProperties;
    }

    public JobProperties getJobProperties() {
        return jobProperties;
    }

    public void setJobProperties(JobProperties jobProperties) {
        this.jobProperties = jobProperties;
    }

    @ConfigurationProperties(prefix = "lts.jobProp")
    public static class JobProperties {

        // 是否要反馈给客户端
        private Boolean needFeedback;
        // 该任务最大的重试次数
        private Integer maxRetryTimes;
        /**
         * 如果为true, 每次启动时, 则以本地为准, 覆盖lts上的
         * 如果为false,每次启动是, 则以lts为准, 如果lts上已经存在, 则不添加
         */
        private Boolean replaceOnExist;
        /**
         * 是否依赖上一个执行周期(对于周期性任务才起作用)
         */
        private Boolean relyOnPrevCycle;

        public Boolean getNeedFeedback() {
            return needFeedback;
        }

        public void setNeedFeedback(Boolean needFeedback) {
            this.needFeedback = needFeedback;
        }

        public Integer getMaxRetryTimes() {
            return maxRetryTimes;
        }

        public void setMaxRetryTimes(Integer maxRetryTimes) {
            this.maxRetryTimes = maxRetryTimes;
        }

        public Boolean getReplaceOnExist() {
            return replaceOnExist;
        }

        public void setReplaceOnExist(Boolean replaceOnExist) {
            this.replaceOnExist = replaceOnExist;
        }

        public Boolean getRelyOnPrevCycle() {
            return relyOnPrevCycle;
        }

        public void setRelyOnPrevCycle(Boolean relyOnPrevCycle) {
            this.relyOnPrevCycle = relyOnPrevCycle;
        }

        public void checkProperties() throws ConfigPropertiesIllegalException {
            Assert.isTrue(getMaxRetryTimes() >= 0, "maxRetryTimes must >= 0.");
        }
    }
}
