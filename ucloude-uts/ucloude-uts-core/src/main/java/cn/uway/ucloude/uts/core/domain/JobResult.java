package cn.uway.ucloude.uts.core.domain;

import java.io.Serializable;

import cn.uway.ucloude.serialize.JsonConvert;

/**
 * 发送给客户端的 任务执行结果
 * @author uway
 *
 */
public class JobResult implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4169137418408978267L;
	private Job job;

    // 执行成功还是失败
    private boolean success;

    private String msg;
    // 任务完成时间
    private Long time;
    /**
     * 执行的时序 (每个执行周期都不一样，但是修复死任务，重试等不会改变)
     */
    private String exeSeqId;

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

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

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getExeSeqId() {
        return exeSeqId;
    }

    public void setExeSeqId(String exeSeqId) {
        this.exeSeqId = exeSeqId;
    }

    @Override
    public String toString() {
        return JsonConvert.serialize(this);
    }
}
