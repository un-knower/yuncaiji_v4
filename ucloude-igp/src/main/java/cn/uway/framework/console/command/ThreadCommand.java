package cn.uway.framework.console.command;

import cn.uway.console.io.CommandIO;
import cn.uway.framework.console.BasicCommand;

/**
 * 命令行获取采集系统内部线程个数
 * 
 * @author zhangp 2015-10-21
 * @since 3.8.5.0
 */
public class ThreadCommand extends BasicCommand {

	@Override
	public boolean doCommand(String[] args, CommandIO io) throws Exception {
		if (args == null || args.length < 1 || !args[0].trim().toLowerCase().equals("-c")) {
			io.println("thread语法错误. 输入help/?获取命令帮助");
			return true;
		}
		io.println("active thread count: " + Thread.activeCount());
		return true;
	}

}
