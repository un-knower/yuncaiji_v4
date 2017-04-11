package cn.uway.framework.console.command;

import java.util.Iterator;
import java.util.Set;

import cn.uway.console.io.CommandIO;
import cn.uway.framework.console.BasicCommand;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;
import cn.uway.framework.task.TaskTrigger;

/**
 * List命令，查看当前运行的任务信息。
 * 
 * @author ChenSijiang 2012-11-8
 */
public class ListCommand extends BasicCommand {

	/* 任务触发器，从此对象中获取正在运行的任务的信息。 */
	private TaskTrigger taskTrigger;

	@Override
	public boolean doCommand(String[] args, CommandIO io) throws Exception {
		if (this.taskTrigger == null)
			return true;
		Set<Task> tasks = taskTrigger.getWorkingTaskList();
		if (tasks == null || tasks.isEmpty()) {
			io.println(" 当前没有正在运行的任务。");
		} else {
			String fmt = "%10s%22s%22s%12s";
			io.println(String.format(fmt, " TASK_ID", "DATA_TIME", "START_TIME", "COST_TIME"));
			io.println("------------------------------------------------------------------");
			Iterator<Task> taskIterator = tasks.iterator();
			while (taskIterator.hasNext()) {
				Task task = taskIterator.next();
				String time = " N/A";
				if (task.getDataTime() != null && (task instanceof PeriodTask))
					time = getDateString(task.getDataTime());
				String cost = CMDUtil.costConvert(System.currentTimeMillis() - task.getBeginRuntime().getTime());
				String str = String.format(fmt, " " + task.getId(), time, getDateString(task.getBeginRuntime()), cost);
				io.println(str);
			}
			io.println(" 共有" + tasks.size() + "个任务正在运行。\r\n");
		}
		return true;
	}

	/**
	 * 设置任务触发器。
	 * 
	 * @param taskTrigger
	 *            任务触发器。
	 */
	public void setTaskTrigger(TaskTrigger taskTrigger) {
		this.taskTrigger = taskTrigger;
	}

	static String makeSpace(int num) {
		if (num <= 0)
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < num; i++)
			sb.append(" ");
		return sb.toString();
	}

	public static void main(String[] args) {
	}
}
