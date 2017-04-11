package cn.uway.framework.console.command;

import cn.uway.console.io.CommandIO;
import cn.uway.framework.console.BasicCommand;

/**
 * 命令行获取JVM的版本信息及JVM内存消耗情况
 * 
 * @author zhangp 2015-10-21
 * @since 3.8.5.0
 */
public class JvmCommand extends BasicCommand {

	@Override
	public boolean doCommand(String[] args, CommandIO io) throws Exception {
		float maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
		float totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		float freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024);
		float usedMemory = totalMemory - freeMemory;
		freeMemory = maxMemory - usedMemory;
		
		io.println("jvm memory usage: ");
		io.println("已使用: " + usedMemory + "M  剩余: " + freeMemory + "M  最大内存: " + maxMemory + "M");
		return true;
	}

}
