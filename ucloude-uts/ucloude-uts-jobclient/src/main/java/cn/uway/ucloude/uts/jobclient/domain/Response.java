package cn.uway.ucloude.uts.jobclient.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.uts.core.domain.Job;

public class Response implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8237539982754608952L;
	
	private boolean success;
    private String msg;
    private String code;

    // 如果success 为false, 这个才会有值
    private List<Job> failedJobs;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<Job> getFailedJobs() {
		return failedJobs;
	}

	public void setFailedJobs(List<Job> failedJobs) {
		this.failedJobs = failedJobs;
	}
	
    public void addFailedJobs(List<Job> jobs){
        if(this.failedJobs == null){
            this.failedJobs = new ArrayList<Job>();
        }
        this.failedJobs.addAll(jobs);
    }
	
    public void addFailedJob(Job job){
        if(this.failedJobs == null){
            this.failedJobs = new ArrayList<Job>();
        }
        this.failedJobs.add(job);
    }

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return JsonConvert.serialize(this);
	}

}
