package cn.uway.ucloude.uts.biz.logger.db.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class DbJobLoggerTest {
	// @org.junit.Test
	public void testAdd() {
//		DbJobLogger db = new DbJobLogger(ExtConfigKeys.CONNECTION_KEY);
//		JobLogPo log = new JobLogPo();
//		Date now = new Date();
//		log.setGmtCreated(System.currentTimeMillis());
//		log.setLogTime(System.currentTimeMillis());
//		log.setJobId(UUID.randomUUID().toString());
//		log.setLogType(LogType.BIZ);
//		log.setDepPreCycle(false);
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("abc", "abc");
//		params.put("abc1", "abc1");
//		params.put("abc2", "abc2");
//		log.setExtParams(params);
//		db.log(log);
//		List<JobLogPo> list = new ArrayList<JobLogPo>();
//		log.setGmtCreated(System.currentTimeMillis());
//		log.setLogTime(System.currentTimeMillis());
//		log.setJobId(UUID.randomUUID().toString());
//		list.add(log);
//		log.setGmtCreated(System.currentTimeMillis());
//		log.setLogTime(System.currentTimeMillis());
//		log.setJobId(UUID.randomUUID().toString());
//		list.add(log);
//		db.log(list);
//		System.out.println("finish");
	}

	@org.junit.Test
	public void testSearch() {
//		DbJobLogger db = new DbJobLogger(ExtConfigKeys.CONNECTION_KEY);
//		JobLoggerRequest request = new JobLoggerRequest();
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTime(new Date());
//		calendar.add(Calendar.DAY_OF_MONTH, -1);
//		Date date = calendar.getTime();
//		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
//		System.out.print(dateFormater.format(date));
//		request.setStartLogTime(calendar.getTime());
//		request.setEndLogTime(new Date());
//		Pagination<JobLogPo> result = db.search(request);
//		System.out.println(result.getTotal());
//		System.out.println(result.getData());
//		System.out.println("finish");
	}
}
