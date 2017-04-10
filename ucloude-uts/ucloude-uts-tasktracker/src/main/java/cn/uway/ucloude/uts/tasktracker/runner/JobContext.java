package cn.uway.ucloude.uts.tasktracker.runner;

import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.uts.core.domain.Job;
import cn.uway.ucloude.uts.tasktracker.logger.BizLogger;

public class JobContext {
	private Job job;
	
	private JobExtInfo jobExtInfo;
	
	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public JobExtInfo getJobExtInfo() {
		return jobExtInfo;
	}

	public void setJobExtInfo(JobExtInfo jobExtInfo) {
		this.jobExtInfo = jobExtInfo;
	}

	public BizLogger getBizLogger() {
		return bizLogger;
	}

	public void setBizLogger(BizLogger bizLogger) {
		this.bizLogger = bizLogger;
	}

	private BizLogger bizLogger;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return JsonConvert.serialize(this);
	}
	
	
}
