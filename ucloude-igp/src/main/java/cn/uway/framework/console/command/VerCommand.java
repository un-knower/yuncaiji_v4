package cn.uway.framework.console.command;

import cn.uway.console.io.CommandIO;
import cn.uway.framework.console.BasicCommand;

/**
 * 命令行获取采集系统版本信息
 * 
 * @author zhangp 2015-10-21
 * @since 3.8.5.0
 */
public class VerCommand extends BasicCommand {

	@Override
	public boolean doCommand(String[] args, CommandIO io) throws Exception {
		String edition = null;
		String releaseTime = null;
		try {
			Class<?> runner = Class.forName("cn.uway.igp3.appRunner.Runner");
		    edition = (String) runner.getDeclaredField("APP_VERSION").get(null);
		    releaseTime = (String) runner.getDeclaredField("RELEASE_TIME").get(null);
		}catch(Exception e){
			io.println("错误,原因:" + e.getMessage());
		}

		io.println(edition + "  " + releaseTime);
		return true;
	}

}
