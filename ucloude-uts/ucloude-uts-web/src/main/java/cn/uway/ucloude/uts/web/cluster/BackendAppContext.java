package cn.uway.ucloude.uts.web.cluster;

import cn.uway.ucloude.uts.biz.logger.JobLogger;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.cluster.Node;
import cn.uway.ucloude.uts.core.queue.*;
import cn.uway.ucloude.uts.web.access.NodeMemCacheAccess;
import cn.uway.ucloude.uts.web.access.face.*;
import cn.uway.ucloude.uts.web.admin.support.NoRelyJobGenerator;


public class BackendAppContext extends UtsContext {
	 private CronJobQueue cronJobQueue;
	    private RepeatJobQueue repeatJobQueue;
	    private ExecutableJobQueue executableJobQueue;
	    private ExecutingJobQueue executingJobQueue;
	    private JobFeedbackQueue jobFeedbackQueue;
	    private SuspendJobQueue suspendJobQueue;
	    private NodeGroupStore nodeGroupStore;
	    private JobLogger jobLogger;
	    private Node node;
	    private NodeMemCacheAccess nodeMemCacheAccess;
	    
	    
	    private BackendJobClientMAccess backendJobClientMAccess;
	    

	    private NoRelyJobGenerator noRelyJobGenerator;

	    
	    public NoRelyJobGenerator getNoRelyJobGenerator() {
			return noRelyJobGenerator;
		}

		public void setNoRelyJobGenerator(NoRelyJobGenerator noRelyJobGenerator) {
			this.noRelyJobGenerator = noRelyJobGenerator;
		}

		public BackendJobClientMAccess getBackendJobClientMAccess() {
			return backendJobClientMAccess;
		}

		public void setBackendJobClientMAccess(BackendJobClientMAccess backendJobClientMAccess) {
			this.backendJobClientMAccess = backendJobClientMAccess;
		}

		public BackendJobTrackerMAccess getBackendJobTrackerMAccess() {
			return backendJobTrackerMAccess;
		}

		public void setBackendJobTrackerMAccess(BackendJobTrackerMAccess backendJobTrackerMAccess) {
			this.backendJobTrackerMAccess = backendJobTrackerMAccess;
		}

		public BackendTaskTrackerMAccess getBackendTaskTrackerMAccess() {
			return backendTaskTrackerMAccess;
		}

		public void setBackendTaskTrackerMAccess(BackendTaskTrackerMAccess backendTaskTrackerMAccess) {
			this.backendTaskTrackerMAccess = backendTaskTrackerMAccess;
		}

		public BackendJVMGCAccess getBackendJVMGCAccess() {
			return backendJVMGCAccess;
		}

		public void setBackendJVMGCAccess(BackendJVMGCAccess backendJVMGCAccess) {
			this.backendJVMGCAccess = backendJVMGCAccess;
		}

		public BackendJVMMemoryAccess getBackendJVMMemoryAccess() {
			return backendJVMMemoryAccess;
		}

		public void setBackendJVMMemoryAccess(BackendJVMMemoryAccess backendJVMMemoryAccess) {
			this.backendJVMMemoryAccess = backendJVMMemoryAccess;
		}

		public BackendJVMThreadAccess getBackendJVMThreadAccess() {
			return backendJVMThreadAccess;
		}

		public void setBackendJVMThreadAccess(BackendJVMThreadAccess backendJVMThreadAccess) {
			this.backendJVMThreadAccess = backendJVMThreadAccess;
		}

		private BackendJobTrackerMAccess backendJobTrackerMAccess;
	    private BackendTaskTrackerMAccess backendTaskTrackerMAccess;
	    private BackendJVMGCAccess backendJVMGCAccess;
	    private BackendJVMMemoryAccess backendJVMMemoryAccess;
	    private BackendJVMThreadAccess backendJVMThreadAccess;
	    private ShopIdAccess shopIdAccess;
//
//	    private NoRelyJobGenerator noRelyJobGenerator;
	    
	    private BackendNodeOnOfflineLogAccess backendNodeOnOfflineLogAccess;

	    public BackendNodeOnOfflineLogAccess getBackendNodeOnOfflineLogAccess() {
			return backendNodeOnOfflineLogAccess;
		}

		public void setBackendNodeOnOfflineLogAccess(BackendNodeOnOfflineLogAccess backendNodeOnOfflineLogAccess) {
			this.backendNodeOnOfflineLogAccess = backendNodeOnOfflineLogAccess;
		}

		public NodeMemCacheAccess getNodeMemCacheAccess() {
			return nodeMemCacheAccess;
		}

		public void setNodeMemCacheAccess(NodeMemCacheAccess nodeMemCacheAccess) {
			this.nodeMemCacheAccess = nodeMemCacheAccess;
		}

		private BackendRegistrySrv backendRegistrySrv;

		public CronJobQueue getCronJobQueue() {
			return cronJobQueue;
		}

		public void setCronJobQueue(CronJobQueue cronJobQueue) {
			this.cronJobQueue = cronJobQueue;
		}

		public RepeatJobQueue getRepeatJobQueue() {
			return repeatJobQueue;
		}

		public void setRepeatJobQueue(RepeatJobQueue repeatJobQueue) {
			this.repeatJobQueue = repeatJobQueue;
		}

		public ExecutableJobQueue getExecutableJobQueue() {
			return executableJobQueue;
		}

		public void setExecutableJobQueue(ExecutableJobQueue executableJobQueue) {
			this.executableJobQueue = executableJobQueue;
		}

		public ExecutingJobQueue getExecutingJobQueue() {
			return executingJobQueue;
		}

		public void setExecutingJobQueue(ExecutingJobQueue executingJobQueue) {
			this.executingJobQueue = executingJobQueue;
		}

		public JobFeedbackQueue getJobFeedbackQueue() {
			return jobFeedbackQueue;
		}

		public void setJobFeedbackQueue(JobFeedbackQueue jobFeedbackQueue) {
			this.jobFeedbackQueue = jobFeedbackQueue;
		}

		public SuspendJobQueue getSuspendJobQueue() {
			return suspendJobQueue;
		}

		public void setSuspendJobQueue(SuspendJobQueue suspendJobQueue) {
			this.suspendJobQueue = suspendJobQueue;
		}

		public NodeGroupStore getNodeGroupStore() {
			return nodeGroupStore;
		}

		public void setNodeGroupStore(NodeGroupStore nodeGroupStore) {
			this.nodeGroupStore = nodeGroupStore;
		}

		public JobLogger getJobLogger() {
			return jobLogger;
		}

		public void setJobLogger(JobLogger jobLogger) {
			this.jobLogger = jobLogger;
		}

		public Node getNode() {
			return node;
		}

		public void setNode(Node node) {
			this.node = node;
		}

		public BackendRegistrySrv getBackendRegistrySrv() {
			return backendRegistrySrv;
		}

		public void setBackendRegistrySrv(BackendRegistrySrv backendRegistrySrv) {
			this.backendRegistrySrv = backendRegistrySrv;
		}

		public ShopIdAccess getShopIdAccess() {
			return shopIdAccess;
		}

		public void setShopIdAccess(ShopIdAccess shopIdAccess) {
			this.shopIdAccess = shopIdAccess;
		}
}
