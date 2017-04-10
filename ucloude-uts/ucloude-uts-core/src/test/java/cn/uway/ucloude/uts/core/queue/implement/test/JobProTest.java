package cn.uway.ucloude.uts.core.queue.implement.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.utils.StringUtil;
import cn.uway.ucloude.uts.core.domain.JobType;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.domain.JobQueueReq;
import cn.uway.ucloude.uts.core.queue.implement.DbExecutableJobQueue;

public class JobProTest {

//	private DbExecutableJobQueue db = new DbExecutableJobQueue();
//	String taskTrackerNodeGroup = "igpV1-192";
	
	//@Test
	public void pageSelect() {
//		JobQueueReq request = new JobQueueReq();
//		request.setTaskId("1");
//		request.setTaskTrackerNodeGroup(taskTrackerNodeGroup);
//		request.setField("CREATED_TIME");
//		request.setPage(5);
//		request.setPageSize(4);
//		Pagination<JobPo> result = db.pageSelect(request);
//		System.out.println(result.getTotal());
	}

	// @Test
	public void selectiveUpdateByJobId() {
//		Map<String, String> extParams = new HashMap<String, String>();
//		extParams.put("Name", "测试哥");
//		extParams.put("slutionId", "解决方案ID");
//		JobQueueReq request = new JobQueueReq();
//		request.setJobId("430218a2-86ca-4ce6-a628-59efcb9bd340");
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
//		boolean result = db.selectiveUpdateByJobId(request);
//		System.out.println(result);
	}

	//@Test
	public void selectiveUpdateByTaskId() {
//		Map<String, String> extParams = new HashMap<String, String>();
//		extParams.put("Name", "测试哥");
//		extParams.put("slutionId", "解决方案ID");
//		JobQueueReq request = new JobQueueReq();
//		request.setTaskId("1");
//		request.setCronExpression("5/h");
//		request.setNeedFeedback(true);
//		request.setExtParams(extParams);
//		request.setTriggerTime(new Date());
//		request.setPriority(5);
//		request.setMaxRetryTimes(5);
//		request.setRepeatCount(5);
//		request.setRepeatInterval(15l);
//		request.setRelyOnPrevCycle(true);
//		request.setModifiedTime(new Date());
//		request.setTaskTrackerNodeGroup(taskTrackerNodeGroup);
//		boolean result = db.selectiveUpdateByTaskId(request);
//		System.out.println(result);
	}

	 @Test
	public void createQueue() {
//		boolean result = db.createQueue(taskTrackerNodeGroup);
//		System.out.println(result);
	}
	 
	 @Test
	public void testJobPo() {
		System.out.println((new JobPo()).toString());
	}

	// @Test
	public void removeQueue() {
//		boolean result = db.removeQueue(taskTrackerNodeGroup);
//		System.out.println(result);
	}

	 @Test
	public void add() {
//		Map<String, String> extParams = new HashMap<String, String>();
//		extParams.put("Name", "测试哥");
//		extParams.put("slutionId", "解决方案ID");
//		JobPo jobPo = new JobPo();
//		jobPo.setJobId(UUID.randomUUID().toString());
//		jobPo.setTaskId("2");
//		jobPo.setJobName("测试");
//		jobPo.setJobType(JobType.REPEAT);
//		jobPo.setJobDescription("单元测试");
//		jobPo.setExtParams(extParams);
//		jobPo.setRealTaskId("2");
//		jobPo.setInternalExtParam(extParams);
//		jobPo.setTaskTrackerNodeGroup(taskTrackerNodeGroup);
//		db.add(jobPo);
//		System.out.println(jobPo.getJobId());
	}

	 //@Test
	public void remove() {
//		boolean result = db.remove(taskTrackerNodeGroup, "8ee22a97-b0a7-451f-9f8b-a124acc4f9cc");
//		System.out.println(result);
	}

	 //@Test
	public void countJob() {
//		long result = db.countJob("1", taskTrackerNodeGroup);
//		System.out.println(result);
	}

	// @Test
	public void removeBatch() {
//		boolean result = db.removeBatch("2", taskTrackerNodeGroup);
//		System.out.println(result);
	}

	//@Test
	public void resume() {
//		db.resume("430218a2-86ca-4ce6-a628-59efcb9bd340", taskTrackerNodeGroup);
//		System.out.println("resume");
	}

	//@Test
	public void getDeadJob() {
//		List<JobPo> jobPos = db.getDeadJob(taskTrackerNodeGroup, new Date().getTime());
//		System.out.println(jobPos.size());
	}

	//@Test
	public void getJob() {
//		JobPo result = db.getJob(taskTrackerNodeGroup, "1");
//		System.out.println(result.getJobName());
	}

}
