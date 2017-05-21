package cn.uway.ucloude.uts.spring.jobclient;

import org.springframework.context.ApplicationContext;

import cn.uway.ucloude.uts.jobclient.JobClient;

public interface JobSubmitHandler {
	public void setJobClient(JobClient jobClient);
	
	
	public void start();
	
	public void runnerValidate();
}
