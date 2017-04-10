package cn.uway.ucloude.uts.jobtracker.complete.retry;

import cn.uway.ucloude.common.SystemClock;
import cn.uway.ucloude.uts.core.domain.Job;

/**
 * @author magic.s.g.xie
 */
public class DefaultJobRetryTimeGenerator implements JobRetryTimeGenerator {

    @Override
    public long getNextRetryTriggerTime(Job job, int retryTimes, int retryInterval) {
        return SystemClock.now() + retryInterval;
    }
}
