package cn.uway.ucloude.uts.core.queue.implement.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import cn.uway.ucloude.uts.core.domain.JobRunResult;
import cn.uway.ucloude.uts.core.queue.domain.JobFeedbackPo;
import cn.uway.ucloude.uts.core.queue.implement.DbJobFeedbackQueue;

public class JobFeedbackQueueInDBTest {
	@Test
	public void Test() {
		System.out.println("123");
//		String jobClientNodeGroup = "igp_client";
//		String jobId = UUID.randomUUID().toString();
//		DbJobFeedbackQueue db = new DbJobFeedbackQueue();
//		List<JobFeedbackPo> list = new ArrayList<JobFeedbackPo>();
//		JobFeedbackPo job = new JobFeedbackPo();
//		job.setId(jobId);
//		job.setGmtCreated(new Date().getTime());
//		JobRunResult jobRunResult = new JobRunResult();
//		jobRunResult.setMsg("测试");
//		jobRunResult.setTime(new Date().getTime());
//		job.setJobRunResult(jobRunResult);
//		list.add(job);
//		job = new JobFeedbackPo();
//		job.setId(jobId + "2");
//		job.setGmtCreated(new Date().getTime());
//		job.setJobRunResult(jobRunResult);
//		list.add(job);
//		// --
//		boolean re1 = false;
//		 re1 = db.createQueue(jobClientNodeGroup);
//		System.out.println("createQueue:" + re1);
//		re1 = db.add(jobClientNodeGroup, list);
//		System.out.println("add:" + re1);
//		long num = db.getCount(jobClientNodeGroup);
//		System.out.println("getCount:" + num);
//		list = db.fetchTop(jobClientNodeGroup, 10);
//		System.out.println("fetchTop:" + list.size());
//		re1 = db.remove(jobClientNodeGroup, jobId);
//		System.out.println("remove:" + re1);
//		re1 = db.removeQueue(jobClientNodeGroup);
//		System.out.println("removeQueue:" + re1);
	}
}
