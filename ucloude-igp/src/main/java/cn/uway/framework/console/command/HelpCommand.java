package cn.uway.framework.console.command;

import cn.uway.console.io.CommandIO;
import cn.uway.framework.console.BasicCommand;

public class HelpCommand extends BasicCommand {

	@Override
	public boolean doCommand(String[] args, CommandIO io) throws Exception {
		io.println(" help          显示此控制台的帮助信息。");
		io.println(" list          列出当前正在运行的任务信息。");
		io.println(" date          获取服务器当前时间。");
		io.println(" disk          获取磁盘信息。");
		io.println(" error         获取采集系统标准错误端信息。");
		io.println(" host          获取服务器机器名。");
		io.println(" jvm           获取JVM的版本信息及JVM内存消耗情况。");
		//io.println(" kill          强行终止指定任务(id为任务编号或者补采编号)。");
		io.println(" os            获取操作系统信息。");
		io.println(" sys           获取系统信息。");
		io.println(" thread        获取采集系统内部线程个数。");
		io.println(" ver           获取采集系统版本信息。");
		io.println(" stop          等待当前任务结束，并退出IGP程序。");
		io.println(" exit          退出此控制台。");
		return true;
	}

}
