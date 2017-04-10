package cn.uway.ucloude.uts.core.protocol.command;

public class JobCancelRequest extends AbstractRpcCommandBody {

	/**
	 * 
	 */
	private static final long serialVersionUID = 979911701556835707L;
	private String taskId;

    private String taskTrackerNodeGroup;

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
}
