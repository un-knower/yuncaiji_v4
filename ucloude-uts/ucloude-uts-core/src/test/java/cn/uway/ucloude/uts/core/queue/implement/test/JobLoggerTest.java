package cn.uway.ucloude.uts.core.queue.implement.test;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.uts.biz.logger.JobLogger;
import cn.uway.ucloude.uts.biz.logger.db.DbJobLogger;
import cn.uway.ucloude.uts.biz.logger.domain.JobLogPo;
import cn.uway.ucloude.uts.biz.logger.domain.JobLoggerRequest;
import cn.uway.ucloude.uts.core.ExtConfigKeys;

public class JobLoggerTest {
	@Test
	public void testLogger(){
		JobLogger logger = new DbJobLogger(ExtConfigKeys.CONNECTION_KEY);
		JobLoggerRequest request = new JobLoggerRequest();
		request.setStartLogTime(DateUtils.addDays(new Date(), -1).getTime());
		request.setEndLogTime(new Date().getTime());
		System.out.println("xxx1");
		Pagination<JobLogPo> page  = logger.search(request);
		System.out.println("xxx");
		System.out.println(page.getTotal() > 0);
	}
}
