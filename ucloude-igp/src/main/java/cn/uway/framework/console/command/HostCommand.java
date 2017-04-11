package cn.uway.framework.console.command;

import cn.uway.console.io.CommandIO;
import cn.uway.framework.console.BasicCommand;
import cn.uway.util.Util;

/**
 * 命令行获取服务器机器名
 * 
 * @author zhangp 2015-10-21
 * @since 3.8.5.0
 */
public class HostCommand extends BasicCommand {

	@Override
	public boolean doCommand(String[] args, CommandIO io) throws Exception {
		io.println(Util.getHostName());
		return true;
	}

}
