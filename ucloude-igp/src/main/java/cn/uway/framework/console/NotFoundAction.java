package cn.uway.framework.console;

import cn.uway.console.command.CommandAction;
import cn.uway.console.io.CommandIO;

/**
 * 命令未找到时的动作。
 * 
 * @author ChenSijiang 2012-11-8
 */
public class NotFoundAction implements CommandAction {

	@Override
	public boolean handleCommand(String[] args, CommandIO io) throws Exception {
		io.setPrefix(">");
		io.println(" 您输入的命令不存在，请输入help或?获取帮助");
		return true;
	}

}