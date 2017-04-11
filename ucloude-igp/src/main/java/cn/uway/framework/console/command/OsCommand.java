package cn.uway.framework.console.command;

import java.util.Properties;

import cn.uway.console.io.CommandIO;
import cn.uway.framework.console.BasicCommand;

/**
 * 命令行获取操作系统信息
 * 
 * @author zhangp 2015-10-21
 * @since 3.8.5.0
 */
public class OsCommand extends BasicCommand {

	@Override
	public boolean doCommand(String[] args, CommandIO io) throws Exception {
		Properties props = System.getProperties();
		String osName = props.getProperty("os.name");
		String osArch = props.getProperty("os.arch");
		String osVersion = props.getProperty("os.version");
		io.println(osName + "  " + osArch + "  " + osVersion);
		return true;
	}

}
