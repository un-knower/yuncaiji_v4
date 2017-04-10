package cn.uway.ucloude.uts.biz.logger.domain;

import java.util.Date;

import cn.uway.ucloude.query.QueryRequest;

public class JobLoggerRequest extends QueryRequest {
	private String realTaskId;
    private String taskId;

    private String taskTrackerNodeGroup;

    private Long startLogTime;

    private Long endLogTime;

    public String getRealTaskId() {
        return realTaskId;
    }

    public void setRealTaskId(String realTaskId) {
        this.realTaskId = realTaskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public Long getStartLogTime() {
        return startLogTime;
    }

    public void setStartLogTime(Long startLogTime) {
        this.startLogTime = startLogTime;
    }

    public Long getEndLogTime() {
        return endLogTime;
    }

    public void setEndLogTime(Long endLogTime) {
        this.endLogTime = endLogTime;
    }
}
