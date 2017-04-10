package cn.uway.ucloude.uts.jobtracker.complete.retry;

import cn.uway.ucloude.container.SPI;
import cn.uway.ucloude.uts.core.*;
import cn.uway.ucloude.uts.core.domain.Job;

/**
 * @author magic.s.g.xie
 */
@SPI(key = ExtConfigKeys.JOB_RETRY_TIME_GENERATOR, dftValue = "default")
public interface JobRetryTimeGenerator {

    /**
     * 得到任务重试的下一次时间
     *
     * @param retryTimes 已经重试的次数
     * @param retryInterval 重试间隔
     */
    long getNextRetryTriggerTime(Job job, int retryTimes, int retryInterval);
}
