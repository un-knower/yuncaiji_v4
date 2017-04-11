package cn.uway.framework.task.worker;

import cn.uway.framework.job.DbTaskJob;
import cn.uway.framework.job.Job;
import cn.uway.framework.job.JobParam;
import cn.uway.framework.solution.GatherSolution;
import cn.uway.framework.solution.SolutionLoader;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.Task;


public class DbTaskWorker  extends DefaultTaskWorker{

	public DbTaskWorker(Task task) {
		super(task);
	}
	
	
	@Override
	protected Job createJob(GatherPathEntry pathEntry) {
		GatherSolution solution = SolutionLoader.getSolution(task);
		JobParam param = new JobParam(task, connInfo, solution, pathEntry);
		return new DbTaskJob(param);
	}


	@Override
	public void beforeWork() {
		super.beforeWork();
		// 数据库采集，采集路径默认随便配一个，让AbstractTaskWork.call()能正常运行
		if (pathEntries.size() < 1) {
			pathEntries.add(new GatherPathEntry("DB COLLECT"));
		}
	}
	
}
