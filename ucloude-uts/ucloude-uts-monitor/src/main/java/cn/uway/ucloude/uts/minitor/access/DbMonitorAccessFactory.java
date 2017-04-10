package cn.uway.ucloude.uts.minitor.access;

import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.minitor.access.db.*;
import cn.uway.ucloude.uts.minitor.access.face.*;

public class DbMonitorAccessFactory implements MonitorAccessFactory {

	@Override
	public JobTrackerMAccess getJobTrackerMAccess() {
		// TODO Auto-generated method stub
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
