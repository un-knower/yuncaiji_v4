package cn.uway.ucloude.uts.tasktracker;

import cn.uway.ucloude.rpc.RpcProcessor;
import cn.uway.ucloude.uts.core.ExtConfigKeys;
import cn.uway.ucloude.uts.core.cluster.AbstractClientNode;
import cn.uway.ucloude.uts.core.domain.Level;
import cn.uway.ucloude.uts.tasktracker.cmd.JobTerminateCmd;
import cn.uway.ucloude.uts.tasktracker.cmd.TaskTrackerReadFileHttpCmd;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerContext;
import cn.uway.ucloude.uts.tasktracker.domain.TaskTrackerNode;
import cn.uway.ucloude.uts.tasktracker.monitor.StopWorkingMonitor;
import cn.uway.ucloude.uts.tasktracker.monitor.TaskTrackerMStatReporter;
import cn.uway.ucloude.uts.tasktracker.processor.RpcDispather;
import cn.uway.ucloude.uts.tasktracker.runner.JobRunner;
import cn.uway.ucloude.uts.tasktracker.runner.RunnerFactory;
import cn.uway.ucloude.uts.tasktracker.runner.RunnerPool;
import cn.uway.ucloude.uts.tasktracker.support.JobPullMachine;

public class TaskTracker extends AbstractClientNode<TaskTrackerNode, TaskTrackerContext> {

	@Override
	protected RpcProcessor getDefaultProcessor() {
		// TODO Auto-generated method stub
		return new RpcDispather(context);
	}

	@Override
	protected void beforeStart() {
		// TODO Auto-generated method stub
		context.setMStatReporter(new TaskTrackerMStatReporter(context));

		context.setRpcClient(rpcClient);
		// 设置 线程池
		context.setRunnerPool(new RunnerPool(context));
		context.getMStatReporter().start();
		context.setJobPullMachine(new JobPullMachine(context));
		context.setStopWorkingMonitor(new StopWorkingMonitor(context));

		context.getHttpCmdServer().registerCommands(new JobTerminateCmd(context),
				new TaskTrackerReadFileHttpCmd(context)); // 终止某个正在执行的任务
	}

	@Override
	protected void afterStart() {
		// TODO Auto-generated method stub
		if (configuration.getParameter(ExtConfigKeys.TASK_TRACKER_STOP_WORKING_ENABLE, false)) {
			context.getStopWorkingMonitor().start();
		}
	}

	@Override
	protected void afterStop() {
		// TODO Auto-generated method stub
		context.getMStatReporter().stop();
		context.getStopWorkingMonitor().stop();
		context.getRunnerPool().shutDown();
	}

	@Override
	protected void beforeStop() {
		// TODO Auto-generated method stub

	}

	/**
	 * 设置业务日志记录级别
	 */
	public void setBizLoggerLevel(Level level) {
		if (level != null) {
			context.setBizLogLevel(level);
		}
	}

	/**
	 * 设置JobRunner工场类，一般用户不用调用
	 */
	public void setRunnerFactory(RunnerFactory factory) {
		context.setRunnerFactory(factory);
	}

	public <JRC extends JobRunner> void setJobRunnerClass(Class<JRC> clazz) {
		context.setJobRunnerClass(clazz);
	}

	public void setWorkThreads(int workThreads) {
		configuration.setWorkThreads(workThreads);
	}

}
