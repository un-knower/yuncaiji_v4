package cn.uway.framework.console.command;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import cn.uway.console.io.CommandIO;
import cn.uway.framework.console.BasicCommand;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.framework.task.TaskFuture;
import cn.uway.framework.task.TaskTrigger;
import cn.uway.framework.task.worker.TaskWorker;

/**
 * 命令行强行终止指定任务(id为任务编号或者补采编号)
 * 
 * @author zhangp 2015-10-21
 * @since 3.8.5.0
 */
public class KillCommand extends BasicCommand {
	
	/* 任务触发器，从此对象中获取正在运行的任务的信息。 */
	private TaskTrigger taskTrigger;
	
	/**
	 * 设置任务触发器。
	 * 
	 * @param taskTrigger
	 *            任务触发器。
	 */
	public void setTaskTrigger(TaskTrigger taskTrigger) {
		this.taskTrigger = taskTrigger;
	}

	@Override
	public boolean doCommand(String[] args, CommandIO io) throws Exception {
		if (args == null || args.length < 1) {
			io.println("kill语法错误,缺少任务编号. 输入help/?获取命令帮助");
			return true;
		}

		// 检查任务编号合法性
		String strTaskID = args[0];
		long taskID = -1L;
		try {
			taskID = Long.parseLong(strTaskID);
		} catch (NumberFormatException e) {
		}
		if (taskID == -1L) {
			io.println("kill语法错误,任务编号输入有误. 输入help/?获取命令帮助");
			return true;
		}
		boolean notFound = true;
		Task task = null;
		Set<Task> tasks = taskTrigger.getWorkingTaskList();
		Iterator<Task> taskIterator = tasks.iterator();
		while (notFound && taskIterator.hasNext()) {
			Task task_temp = taskIterator.next();
			if(taskID == taskTrigger.getRightTaskId(task_temp)){
				notFound = false;
				task = task_temp;
			}
		}

		if (true == notFound) {
			io.println("指定的任务编号当前不在运行状态或者任务编号不存在");
			return true;
		}

		// 询问用户是否杀死指定任务
		String des = null;
		String time = " N/A";
		if(null!=task){
		des = task.getDescription();
		if (task.getDataTime() != null && (task instanceof PeriodTask))
			time = getDateString(task.getDataTime());
		}
		String strLine = io.readLine("是否要杀死任务(" + taskID + ", " + time + ", " + des + " )?   [y|n]  ");

		// 放弃停止
		if (strLine.equalsIgnoreCase("n") || strLine.equalsIgnoreCase("no"))
			return true;
		// 输入非法
		if (!strLine.equalsIgnoreCase("n") && !strLine.equalsIgnoreCase("no") && !strLine.equalsIgnoreCase("y") && !strLine.equalsIgnoreCase("yes")) {
			io.println("非法输入,放弃操作.");
			return true;
		}

//		 杀死指定任务
//		taskTrigger.kill(taskID);
//		taskTrigger.setTaskID(taskID);
//		TaskWorker thrd = taskTrigger.getWorkingThread(taskID);
//		Callable<TaskFuture> th= taskTrigger.getWorkingThread(taskID);
//		FutureTask<TaskFuture> futureTask = new FutureTask<TaskFuture>(th);
//		futureTask.cancel(true);
	    taskTrigger.removeTask(task);
		return true;
	}

}
