package cn.uway.ucloude.uts.jobclient.cmd;

import cn.uway.ucloude.uts.core.cmd.ReadFileHttpCmd;
import cn.uway.ucloude.uts.jobclient.domain.JobClientContext;

public class JobClientReadFileHttpCmd extends ReadFileHttpCmd {
	public JobClientReadFileHttpCmd(JobClientContext context){
		super(context);
		this.logFilePath="../logs/jobclient.out";
	}
}
