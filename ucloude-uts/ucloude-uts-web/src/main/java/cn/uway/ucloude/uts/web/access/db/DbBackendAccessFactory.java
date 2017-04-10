package cn.uway.ucloude.uts.web.access.db;

import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.web.access.BackendAccessFactory;
import cn.uway.ucloude.uts.web.access.face.BackendJVMGCAccess;
import cn.uway.ucloude.uts.web.access.face.BackendJVMMemoryAccess;
import cn.uway.ucloude.uts.web.access.face.BackendJVMThreadAccess;
import cn.uway.ucloude.uts.web.access.face.BackendJobClientMAccess;
import cn.uway.ucloude.uts.web.access.face.BackendJobTrackerMAccess;
import cn.uway.ucloude.uts.web.access.face.BackendNodeOnOfflineLogAccess;
import cn.uway.ucloude.uts.web.access.face.BackendTaskTrackerMAccess;

public class DbBackendAccessFactory implements BackendAccessFactory {

	@Override
	public BackendJobTrackerMAccess getJobTrackerMAccess() {
		// TODO Auto-generated method stub
		return new DbBackendJobTrackerMAccess(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public BackendJobClientMAccess getBackendJobClientMAccess() {
		// TODO Auto-generated method stub
		return new DbBackendJobClientMAccess(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public BackendJVMGCAccess getBackendJVMGCAccess() {
		// TODO Auto-generated method stub
		return new DbBackendJVMGCAccess(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public BackendJVMMemoryAccess getBackendJVMMemoryAccess() {
		// TODO Auto-generated method stub
		return new DbBackendJVMMemoryAccess(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public BackendJVMThreadAccess getBackendJVMThreadAccess() {
		// TODO Auto-generated method stub
		return new DbBackendJVMThreadAccess(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public BackendNodeOnOfflineLogAccess getBackendNodeOnOfflineLogAccess() {
		// TODO Auto-generated method stub
		return new DbBackendNodeOnOfflineLogAccess(ExtConfigKeys.CONNECTION_KEY);
	}

	@Override
	public BackendTaskTrackerMAccess getBackendTaskTrackerMAccess() {
		// TODO Auto-generated method stub
		return new DbBackendTaskTrackerMAccess(ExtConfigKeys.CONNECTION_KEY);
	}

}
