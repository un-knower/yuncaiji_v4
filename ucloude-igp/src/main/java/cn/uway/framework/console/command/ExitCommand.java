package cn.uway.framework.console.command;

import cn.uway.console.io.CommandIO;
import cn.uway.framework.console.BasicCommand;

public class ExitCommand extends BasicCommand {

	@Override
	public boolean doCommand(String[] args, CommandIO io) throws Exception {
		io.println(" Bye.");
		return false;
	}

}
