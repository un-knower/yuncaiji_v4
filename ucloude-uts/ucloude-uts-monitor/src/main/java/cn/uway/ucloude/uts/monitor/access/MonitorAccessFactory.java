package cn.uway.ucloude.uts.monitor.access;

import cn.uway.ucloude.container.SPI;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.UtsConfiguration;
import cn.uway.ucloude.uts.monitor.access.face.JVMGCAccess;
import cn.uway.ucloude.uts.monitor.access.face.JVMMemoryAccess;
import cn.uway.ucloude.uts.monitor.access.face.JVMThreadAccess;
import cn.uway.ucloude.uts.monitor.access.face.JobClientMAccess;
import cn.uway.ucloude.uts.monitor.access.face.JobTrackerMAccess;
import cn.uway.ucloude.uts.monitor.access.face.TaskTrackerMAccess;

/**
 * @author uway
 */
@SPI(key = ExtConfigKeys.ACCESS_DB, dftValue = "db")
public interface MonitorAccessFactory {

    JobTrackerMAccess getJobTrackerMAccess();

    TaskTrackerMAccess getTaskTrackerMAccess();

    JVMGCAccess getJVMGCAccess();

    JVMMemoryAccess getJVMMemoryAccess();

    JVMThreadAccess getJVMThreadAccess();

    JobClientMAccess getJobClientMAccess();
}
