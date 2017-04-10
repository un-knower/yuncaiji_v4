package cn.uway.ucloude.uts.jobclient.processor;

import cn.uway.ucloude.uts.jobclient.JobClient;

public interface JobSubmitHandler extends Runnable {
	public void setJobClient(JobClient jobClient);
}
