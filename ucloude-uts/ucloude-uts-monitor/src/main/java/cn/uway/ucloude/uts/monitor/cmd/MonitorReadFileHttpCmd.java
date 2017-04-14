package cn.uway.ucloude.uts.monitor.cmd;

import cn.uway.ucloude.uts.core.cmd.ReadFileHttpCmd;
import cn.uway.ucloude.uts.monitor.MonitorAppContext;

public class MonitorReadFileHttpCmd extends ReadFileHttpCmd {
	public MonitorReadFileHttpCmd(MonitorAppContext context, String cfgPath) {
		super(context);
		this.logFilePath = cfgPath + "../logs/ucloude-uts-admin.out";
	}
}
