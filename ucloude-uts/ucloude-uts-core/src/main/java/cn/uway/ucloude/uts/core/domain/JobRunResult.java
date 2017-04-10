package cn.uway.ucloude.uts.core.domain;

import java.io.Serializable;

import cn.uway.ucloude.serialize.JsonConvert;

/**
 * TaskTracker 任务执行结果
 * @author uway
 *
 */
public class JobRunResult implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7450500402730425111L;
	private JobMeta jobMeta;

    private Action action;

    private String msg;
    // 任务完成时间
    private Long time;

    public JobMeta getJobMeta() {
        return jobMeta;
    }

    public void setJobMeta(JobMeta jobMeta) {
        this.jobMeta = jobMeta;
    }

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

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return JsonConvert.serialize(this);
    }
}
