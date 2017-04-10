package cn.uway.ucloude.uts.core.queue.implement.test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.uway.ucloude.query.Pagination;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.domain.JobType;
import cn.uway.ucloude.uts.core.queue.ExecutableJobQueue;
import cn.uway.ucloude.uts.core.queue.domain.JobPo;
import cn.uway.ucloude.uts.core.queue.domain.JobQueueReq;
import cn.uway.ucloude.uts.core.queue.implement.DbExecutableJobQueue;
import cn.uway.ucloude.uts.core.queue.implement.DbExecutingJobQueue;

public class ExecutingJobQueueInDBTest {
	@org.junit.Test
	public void Test() {
		JobPo po = new JobPo();
		/*INSERT INTO uts_excuting_job(job_id, 
				job_type, priority, retry_times,
				max_retry_times, rely_on_prev_cycle, task_id,
				real_task_id, CREATED_TIME, MODIFIED_TIME, 
				submit_node_group, task_tracker_node_group, is_running, task_tracker_identity, 
				need_feedback, cron_expression, trigger_time, repeat_count, repeated_count, repeat_interval) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
		["A28F3A9338844630A5E934B02FE8CE42",0,100,0,0,true,"111","111",1487661774028,1487661774028,
		"igp_jobclient","igp_test_tasktracker",false,null,true,null,1487661774028,0,0,null], UCloude version: 0.0.1-SNAPSHOT, current host: 192.168.15.161
		*/
		po.setJobId("A28F3A9338844630A5E934B02FE8CE42");
		po.setJobType(JobType.REAL_TIME);
		po.setPriority(100);
		po.setRetryTimes(0);
		po.setMaxRetryTimes(0);
		po.setRelyOnPrevCycle(true);
		po.setTaskId("111");
		po.setRealTaskId("111");
		po.setGmtCreated(1487661774028L);
		po.setSubmitNodeGroup("igp_jobclient");
		po.setTaskTrackerNodeGroup("igp_test_tasktracker");
		po.setRunning(false);
		po.setNeedFeedback(true);
		po.setTriggerTime(1487661774028L);
		po.setGmtModified(1487661774028L);
		po.setRepeatCount(0);
		po.setRepeatedCount(0);
		ExecutableJobQueue db = new DbExecutableJobQueue(ExtConfigKeys.CONNECTION_KEY);
		db.add(po);
//		String taskTrackerNodeGroup = "igpV1-192";
//		String taskTrackerIdentity="";
//		String jobId = UUID.randomUUID().toString();
//		DbExecutingJobQueue db = new DbExecutingJobQueue();
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
//		//---
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
//		//--
//		boolean re1 = db.add(jobPo);
//		System.out.println("add:" + re1);
//		JobPo re2 = db.getJob(jobId);
//		System.out.println("getJob-jobId:" + re2.getJobName());
//		JobPo re3 = db.getJob(taskTrackerNodeGroup, "1");
//		System.out.println("getJob-task:" + re3.getJobName());
//		List<JobPo> re4 = db.getJobs(taskTrackerIdentity);
//		System.out.println("getJobs:" + re4.size());
//		List<JobPo> re5 = db.getDeadJobs(new Date().getTime());
//		System.out.println("getDeadJobs:" + re5);
//		boolean re6 = db.selectiveUpdateByJobId(request);
//		System.out.println("selectiveUpdateByJobId:" + re6);
//		//boolean re7 = db.remove(jobId);
//		//System.out.println("remove:" + re7);
//		Pagination<JobPo> re8 = db.pageSelect(request);
//		System.out.println("pageSelect:" + re8.getTotal());
//		boolean re9 = db.selectiveUpdateByTaskId(request);
//		System.out.println("selectiveUpdateByTaskId:" + re9);
	}
}
