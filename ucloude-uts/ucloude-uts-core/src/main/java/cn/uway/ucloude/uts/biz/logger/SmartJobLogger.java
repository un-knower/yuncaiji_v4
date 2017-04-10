package cn.uway.ucloude.uts.biz.logger;

import java.util.List;

import cn.uway.ucloude.container.ServiceFactory;
import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.JobLoggerRequest;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.UtsConfiguration;
import cn.uway.ucloude.uts.core.UtsContext;

/**
 * 内部根据用户参数决定是否采用延迟批量刷盘的策略,来提高吞吐量
 * @author uway
 *
 */
public class SmartJobLogger implements JobLogger {
	
	private JobLogger delegate;
	
	 public SmartJobLogger(UtsContext context) {
	        UtsConfiguration config = context.getConfiguration();
	        JobLoggerFactory jobLoggerFactory = ServiceFactory.load(JobLoggerFactory.class, config);
	        JobLogger jobLogger = jobLoggerFactory.getJobLogger();
	        if (config.getParameter(ExtConfigKeys.LAZY_JOB_LOGGER, false)) {
	            this.delegate = new LazyJobLogger(context, jobLogger);
	        } else {
	            this.delegate = jobLogger;
	        }
	    }

	@Override
	public void log(JobLogPo jobLogPo) {
		// TODO Auto-generated method stub
		this.delegate.log(jobLogPo);
	}

	@Override
	public void log(List<JobLogPo> jobLogPos) {
		// TODO Auto-generated method stub
		this.delegate.log(jobLogPos);
	}

	@Override
	public Pagination<JobLogPo> search(JobLoggerRequest request) {
		// TODO Auto-generated method stub
		return this.delegate.search(request);
	}

}
