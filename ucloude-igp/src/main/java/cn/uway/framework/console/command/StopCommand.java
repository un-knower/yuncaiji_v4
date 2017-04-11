package cn.uway.framework.console.command;

import java.util.Iterator;
import java.util.Set;

import cn.uway.console.io.CommandIO;
import cn.uway.framework.console.BasicCommand;
import cn.uway.framework.task.Task;
import cn.uway.framework.task.TaskTrigger;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class StopCommand extends BasicCommand {

	private static final ILogger log = LoggerManager.getLogger(StopCommand.class);

	/* 任务触发器，从此对象中获取正在运行的任务的信息。 */
	private TaskTrigger taskTrigger;

	@Override
	public boolean doCommand(String[] args, CommandIO io) throws Exception {
		log.warn("收到stop命令，发起者：{}", io.getClientSocket());
		taskTrigger.setTriggerFalse();
		while (true) {
			Set<Task> tasks = taskTrigger.getWorkingTaskList();
			if (tasks.size() > 0) {
				String fmt = "%10s%22s%22s%12s";
				io.println(String.format(fmt, " TASK_ID", "DATA_TIME", "START_TIME", "COST_TIME"));
				io.println("------------------------------------------------------------------");
				Iterator<Task> taskIterator = tasks.iterator();
				while (taskIterator.hasNext()) {
					Task task = taskIterator.next();
					String time = " N/A";
					if (task.getDataTime() != null)
						time = getDateString(task.getDataTime());

					String cost = CMDUtil.costConvert(System.currentTimeMillis() - task.getBeginRuntime().getTime());

					String str = String.format(fmt, " " + task.getId(), time, getDateString(task.getBeginRuntime()), cost);
					io.println(str);
				}
				io.println(" 当前正等待" + tasks.size() + "个任务结束。\r\n");
				log.warn("IGP准备退出，因为收到stop命令，正等待{}个任务结束，发起者：{}", new Object[]{tasks.size(), io.getClientSocket()});
			} else {
				log.warn("IGP退出，因为收到stop命令，发起者：{}", io.getClientSocket());
				io.println(" 已无任务运行，IGP3退出。");
				System.exit(0);
			}
			Thread.sleep(5000);
		}

	}

	public void setTaskTrigger(TaskTrigger taskTrigger) {
		this.taskTrigger = taskTrigger;
	}
}
