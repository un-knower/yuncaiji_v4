package cn.uway.ucloude.uts.tasktracker.domain;

import cn.uway.ucloude.uts.core.domain.Action;
import cn.uway.ucloude.uts.core.domain.JobMeta;

public class Response {
	private Action action;
	
	private String msg;
	
	private JobMeta jobMeta;
	
	/**
	 * 是否接收新的任务
	 */
	private boolean receiveNewJob;
	
	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public JobMeta getJobMeta() {
		return jobMeta;
	}

	public void setJobMeta(JobMeta jobMeta) {
		this.jobMeta = jobMeta;
	}



	public boolean isReceiveNewJob() {
		return receiveNewJob;
	}

	public void setReceiveNewJob(boolean receiveNewJob) {
		this.receiveNewJob = receiveNewJob;
	}
}
