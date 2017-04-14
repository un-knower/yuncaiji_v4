package cn.uway.ucloude.uts.monitor;

import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.monitor.access.face.*;
import cn.uway.ucloude.uts.monitor.cmd.MDataSrv;

/**
 * @author magic.s.g.xie
 */
public class MonitorAppContext extends UtsContext {

    private int httpCmdPort;

    private JobTrackerMAccess jobTrackerMAccess;
    private TaskTrackerMAccess taskTrackerMAccess;
    private JobClientMAccess jobClientMAccess;
    private JVMGCAccess jvmGCAccess;
    private JVMMemoryAccess jvmMemoryAccess;
    private JVMThreadAccess jvmThreadAccess;

    private MDataSrv mDataSrv;

    public int getHttpCmdPort() {
        return httpCmdPort;
    }

    public void setHttpCmdPort(int httpCmdPort) {
        this.httpCmdPort = httpCmdPort;
    }

    public JobTrackerMAccess getJobTrackerMAccess() {
        return jobTrackerMAccess;
    }

    public void setJobTrackerMAccess(JobTrackerMAccess jobTrackerMAccess) {
        this.jobTrackerMAccess = jobTrackerMAccess;
    }

    public TaskTrackerMAccess getTaskTrackerMAccess() {
        return taskTrackerMAccess;
    }

    public void setTaskTrackerMAccess(TaskTrackerMAccess taskTrackerMAccess) {
        this.taskTrackerMAccess = taskTrackerMAccess;
    }

    public JVMGCAccess getJvmGCAccess() {
        return jvmGCAccess;
    }

    public void setJvmGCAccess(JVMGCAccess jvmGCAccess) {
        this.jvmGCAccess = jvmGCAccess;
    }

    public JVMMemoryAccess getJvmMemoryAccess() {
        return jvmMemoryAccess;
    }

    public void setJvmMemoryAccess(JVMMemoryAccess jvmMemoryAccess) {
        this.jvmMemoryAccess = jvmMemoryAccess;
    }

    public JVMThreadAccess getJvmThreadAccess() {
        return jvmThreadAccess;
    }

    public void setJvmThreadAccess(JVMThreadAccess jvmThreadAccess) {
        this.jvmThreadAccess = jvmThreadAccess;
    }

    public MDataSrv getMDataSrv() {
        return mDataSrv;
    }

    public void setMDataSrv(MDataSrv mDataSrv) {
        this.mDataSrv = mDataSrv;
    }

    public JobClientMAccess getJobClientMAccess() {
        return jobClientMAccess;
    }

    public void setJobClientMAccess(JobClientMAccess jobClientMAccess) {
        this.jobClientMAccess = jobClientMAccess;
    }

    public MDataSrv getmDataSrv() {
        return mDataSrv;
    }

    public void setmDataSrv(MDataSrv mDataSrv) {
        this.mDataSrv = mDataSrv;
    }
}
