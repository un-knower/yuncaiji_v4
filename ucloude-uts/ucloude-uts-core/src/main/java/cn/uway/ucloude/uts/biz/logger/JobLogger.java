package cn.uway.ucloude.uts.biz.logger;

import java.util.List;

import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.JobLoggerRequest;

/**
 * 执行任务日志记录器
 * @author uway
 *
 */
public interface JobLogger {
	
	public void log(JobLogPo jobLogPo);

    public void log(List<JobLogPo> jobLogPos);

    public Pagination<JobLogPo> search(JobLoggerRequest request);
}
