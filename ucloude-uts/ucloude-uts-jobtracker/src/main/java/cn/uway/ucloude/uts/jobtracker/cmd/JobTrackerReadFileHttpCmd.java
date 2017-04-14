package cn.uway.ucloude.uts.jobtracker.cmd;

import cn.uway.ucloude.uts.core.cmd.ReadFileHttpCmd;
import cn.uway.ucloude.uts.jobtracker.domain.JobTrackerContext;

public class JobTrackerReadFileHttpCmd extends ReadFileHttpCmd {
	public JobTrackerReadFileHttpCmd(JobTrackerContext context) {
		super(context);
		this.logFilePath = "../logs/jobtracker-" + context.getNodeName() + ".out";
	}
}
