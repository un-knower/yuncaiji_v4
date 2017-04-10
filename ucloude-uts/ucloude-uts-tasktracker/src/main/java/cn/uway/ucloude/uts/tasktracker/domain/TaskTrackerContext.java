package cn.uway.ucloude.uts.tasktracker.domain;

import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.core.rpc.RpcClientDelegate;
import cn.uway.ucloude.uts.tasktracker.monitor.StopWorkingMonitor;
import cn.uway.ucloude.uts.tasktracker.runner.RunnerFactory;
import cn.uway.ucloude.uts.tasktracker.runner.RunnerPool;
import cn.uway.ucloude.uts.tasktracker.support.JobPullMachine;

public class TaskTrackerContext extends UtsContext {
	private RpcClientDelegate rpcClient;
	
	private RunnerPool runnerPool;
	
	private RunnerFactory runnerFactory;
	
	private JobPullMachine jobPullMachine;
	
	private StopWorkingMonitor stopWorkingMonitor;
	
	 /**
     * 业务日志记录级别
     */
    private Level bizLogLevel;
    /**
     * 执行任务的class
     */
    private Class<?> jobRunnerClass;

	public Level getBizLogLevel() {
		return bizLogLevel;
	}

	public void setBizLogLevel(Level bizLogLevel) {
		this.bizLogLevel = bizLogLevel;
	}

	public Class<?> getJobRunnerClass() {
		return jobRunnerClass;
	}

	public void setJobRunnerClass(Class<?> jobRunnerClass) {
		this.jobRunnerClass = jobRunnerClass;
	}

	public RpcClientDelegate getRpcClient() {
		return rpcClient;
	}

	public void setRpcClient(RpcClientDelegate rpcClient) {
		this.rpcClient = rpcClient;
	}

	public RunnerPool getRunnerPool() {
		return runnerPool;
	}

	public void setRunnerPool(RunnerPool runnerPool) {
		this.runnerPool = runnerPool;
	}

	public RunnerFactory getRunnerFactory() {
		return runnerFactory;
	}

	public void setRunnerFactory(RunnerFactory runnerFactory) {
		this.runnerFactory = runnerFactory;
	}

	public JobPullMachine getJobPullMachine() {
		return jobPullMachine;
	}

	public void setJobPullMachine(JobPullMachine jobPullMachine) {
		this.jobPullMachine = jobPullMachine;
	}

	public StopWorkingMonitor getStopWorkingMonitor() {
		return stopWorkingMonitor;
	}

	public void setStopWorkingMonitor(StopWorkingMonitor stopWorkingMonitor) {
		this.stopWorkingMonitor = stopWorkingMonitor;
	}
}
