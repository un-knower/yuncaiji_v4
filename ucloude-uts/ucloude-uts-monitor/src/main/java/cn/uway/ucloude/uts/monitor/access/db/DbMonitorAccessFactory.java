package cn.uway.ucloude.uts.monitor.access.db;

import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.monitor.access.MonitorAccessFactory;
import cn.uway.ucloude.uts.monitor.access.face.JVMGCAccess;
import cn.uway.ucloude.uts.monitor.access.face.JVMMemoryAccess;
import cn.uway.ucloude.uts.monitor.access.face.JVMThreadAccess;
import cn.uway.ucloude.uts.monitor.access.face.JobClientMAccess;
import cn.uway.ucloude.uts.monitor.access.face.JobTrackerMAccess;
import cn.uway.ucloude.uts.monitor.access.face.TaskTrackerMAccess;

public class DbMonitorAccessFactory  implements MonitorAccessFactory {

	@Override
	public JobTrackerMAccess getJobTrackerMAccess() {
		return new DbJobTrackerMAccess(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public TaskTrackerMAccess getTaskTrackerMAccess() {
		// TODO Auto-generated method stub
		return new DbTaskTrackerMAccess(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public JVMGCAccess getJVMGCAccess() {
		// TODO Auto-generated method stub
		return new DbJVMGCAccess(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public JVMMemoryAccess getJVMMemoryAccess() {
		// TODO Auto-generated method stub
		return new DbJVMMemoryAccess(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public JVMThreadAccess getJVMThreadAccess() {
		// TODO Auto-generated method stub
		return new DbJVMThreadAccess(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public JobClientMAccess getJobClientMAccess() {
		// TODO Auto-generated method stub
		return new DbJobClientMAccess(ExtConfigKeys.CONNECTION_KEY);
	}
}
