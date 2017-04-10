package cn.uway.ucloude.uts.biz.logger;

import cn.uway.ucloude.common.SystemClock;

import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.LogType;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.support.JobDomainConverter;

public class JobLogUtils {
	public static void log(LogType logType, JobPo jobPo, JobLogger jobLogger) {
        JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
        jobLogPo.setSuccess(true);
        jobLogPo.setLogType(logType);
        jobLogPo.setLogTime(SystemClock.now());
        jobLogPo.setLevel(Level.INFO);
        jobLogger.log(jobLogPo);
    }
}
