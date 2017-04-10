package cn.uway.ucloude.uts.biz.logger.db;

import cn.uway.ucloude.uts.biz.logger.JobLogger;
import cn.uway.ucloude.uts.biz.logger.JobLoggerFactory;
import cn.uway.ucloude.uts.core.ExtConfigKeys;

public class DbJobLoggerFactory implements JobLoggerFactory {

	@Override
	public JobLogger getJobLogger() {
		// TODO Auto-generated method stub
		return new DbJobLogger(ExtConfigKeys.CONNECTION_KEY);
	}

}
