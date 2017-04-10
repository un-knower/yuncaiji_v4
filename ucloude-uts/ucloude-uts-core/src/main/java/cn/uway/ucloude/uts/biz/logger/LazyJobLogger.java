package cn.uway.ucloude.uts.biz.logger;

import java.util.List;

import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.JobLoggerRequest;
import cn.uway.ucloude.uts.core.UtsContext;

public class LazyJobLogger implements JobLogger {

	public LazyJobLogger(UtsContext context, JobLogger jobLogger) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void log(JobLogPo jobLogPo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void log(List<JobLogPo> jobLogPos) {
		// TODO Auto-generated method stub

	}

	@Override
	public Pagination<JobLogPo> search(JobLoggerRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
