package cn.uway.framework.task.worker;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.Task;


public interface ITaskDeliver {
	public boolean submit(Task task, ConnectionInfo connInfo, GatherPathEntry pathEntry, int fileIndex);

	void runnerValidate();
}
