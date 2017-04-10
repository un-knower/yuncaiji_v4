package cn.uway.ucloude.uts.web.access;

import cn.uway.ucloude.container.SPI;
import cn.uway.ucloude.uts.core.UtsConfiguration;
import cn.uway.ucloude.uts.web.access.face.BackendJVMGCAccess;
import cn.uway.ucloude.uts.web.access.face.BackendJVMMemoryAccess;
import cn.uway.ucloude.uts.web.access.face.BackendJVMThreadAccess;
import cn.uway.ucloude.uts.web.access.face.BackendJobClientMAccess;
import cn.uway.ucloude.uts.web.access.face.BackendJobTrackerMAccess;
import cn.uway.ucloude.uts.web.access.face.BackendNodeOnOfflineLogAccess;
import cn.uway.ucloude.uts.web.access.face.BackendTaskTrackerMAccess;

@SPI(key = "uts.web.access.db", dftValue = "db")
public interface BackendAccessFactory {
	BackendJobTrackerMAccess getJobTrackerMAccess();

    BackendJobClientMAccess getBackendJobClientMAccess();

    BackendJVMGCAccess getBackendJVMGCAccess();

    BackendJVMMemoryAccess getBackendJVMMemoryAccess();

    BackendJVMThreadAccess getBackendJVMThreadAccess();

    BackendNodeOnOfflineLogAccess getBackendNodeOnOfflineLogAccess();

    BackendTaskTrackerMAccess getBackendTaskTrackerMAccess();
}
