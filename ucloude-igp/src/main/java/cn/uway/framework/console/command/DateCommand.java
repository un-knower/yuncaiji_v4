package cn.uway.framework.console.command;

import java.util.Date;

import cn.uway.console.io.CommandIO;
import cn.uway.framework.console.BasicCommand;
import cn.uway.util.TimeUtil;

/**
 * 命令行获取服务器当前时间
 * 
 * @author zhangp 2015-10-21
 * @since 3.8.5.0
 */
public class DateCommand extends BasicCommand {

	@Override
	public boolean doCommand(String[] args, CommandIO io) throws Exception {
		io.println(TimeUtil.getDateString(new Date()));
		return true;
	}

}
