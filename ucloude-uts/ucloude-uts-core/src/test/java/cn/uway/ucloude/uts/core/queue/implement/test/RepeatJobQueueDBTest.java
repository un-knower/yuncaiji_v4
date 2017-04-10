package cn.uway.ucloude.uts.core.queue.implement.test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.uts.core.domain.JobType;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.domain.JobQueueReq;
import cn.uway.ucloude.uts.core.queue.implement.DbRepeatJobQueue;

public class RepeatJobQueueDBTest {
	@org.junit.Test
	public void Test() {
//		DbRepeatJobQueue db = new DbRepeatJobQueue();
//		String taskTrackerNodeGroup = "igpV1-192";
//		String jobId = UUID.randomUUID().toString();
//		Map<String, String> extParams = new HashMap<String, String>();
//		extParams.put("Name", "测试哥");
//		extParams.put("slutionId", "解决方案ID");
//		JobPo jobPo = new JobPo();
//		jobPo.setJobId(jobId);
//		jobPo.setTaskId("1");
//		jobPo.setJobName("测试");
//		jobPo.setJobType(JobType.REPEAT);
//		jobPo.setJobDescription("单元测试");
//		jobPo.setExtParams(extParams);
//		jobPo.setRealTaskId("1");
//		jobPo.setInternalExtParam(extParams);
//		jobPo.setTaskTrackerNodeGroup(taskTrackerNodeGroup);
//		// ---
//		JobQueueReq request = new JobQueueReq();
//		request.setJobId(jobId);
//		request.setCronExpression("5/h");
//		request.setNeedFeedback(true);
//		request.setExtParams(extParams);
//		request.setTriggerTime(new Date());
//		request.setPriority(6);
//		request.setMaxRetryTimes(5);
//		request.setRepeatCount(5);
//		request.setRepeatInterval(15l);
//		request.setRelyOnPrevCycle(true);
//		request.setModifiedTime(new Date());
//		request.setTaskTrackerNodeGroup(taskTrackerNodeGroup);
//		// --
//		boolean re1 = db.add(jobPo);
//		System.out.println("add:" + re1);
//
//		JobPo re2 = db.getJob(jobId);
//		System.out.println("getJob-jobId:" + re2.getJobName());
//		JobPo re3 = db.getJob(taskTrackerNodeGroup, "1");
//		System.out.println("getJob-task:" + re3.getJobName());
//		List<JobPo> re4 = db.getNeedGenerateJobPos(new Date().getTime(), 10);
//		System.out.println("getNeedGenerateJobPos:" + re4.size());
//		Pagination<JobPo> re5 = db.pageSelect(request);
//		System.out.println("pageSelect:" + re5.getTotal());
//
//		boolean re6 = db.selectiveUpdateByJobId(request);
//		System.out.println("selectiveUpdateByJobId:" + re6);
//		boolean re7 = db.selectiveUpdateByTaskId(request);
//		System.out.println("selectiveUpdateByTaskId:" + re7);
//		int re8 = db.incRepeatedCount(jobId);
//		System.out.println("incRepeatedCount:" + re8);
//		boolean re9 = db.updateLastGenerateTriggerTime(jobId, new Date().getTime());
//		System.out.println("updateLastGenerateTriggerTime:" + re9);
//
//		boolean re10 = db.remove(jobId);
//		System.out.println("remove:" + re10);

	}
}
