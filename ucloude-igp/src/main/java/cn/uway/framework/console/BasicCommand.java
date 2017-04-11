package cn.uway.framework.console;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.uway.console.command.CommandAction;
import cn.uway.console.io.CommandIO;

/**
 * 控制台命令基础类。
 * 
 * @author ChenSijiang 2012-11-8
 */
public abstract class BasicCommand implements CommandAction {

	/* 输入命令时的提示符。 */
	private static final String CMD_PREFIX = ">";

	protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public String getDateString(Date date) {
		if (date == null)
			return "";
		return this.dateFormat.format(date);
	}

	@Override
	public boolean handleCommand(String[] args, CommandIO io) throws Exception {
		io.setPrefix(CMD_PREFIX);
		return doCommand(args, io);
	}

	public abstract boolean doCommand(String[] args, CommandIO io) throws Exception;

}
