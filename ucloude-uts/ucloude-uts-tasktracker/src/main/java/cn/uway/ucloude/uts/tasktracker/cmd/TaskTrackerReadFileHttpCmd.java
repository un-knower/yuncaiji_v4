package cn.uway.ucloude.uts.tasktracker.cmd;

import cn.uway.ucloude.uts.core.cmd.ReadFileHttpCmd;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;

public class TaskTrackerReadFileHttpCmd extends ReadFileHttpCmd {
	public TaskTrackerReadFileHttpCmd(TaskTrackerContext context){
		super(context);
		this.logFilePath="../logs/tasktracker.out";
	}
}
