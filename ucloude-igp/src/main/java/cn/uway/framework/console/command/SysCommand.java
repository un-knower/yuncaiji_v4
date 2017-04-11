package cn.uway.framework.console.command;

import java.util.Date;
import cn.uway.console.io.CommandIO;
import cn.uway.framework.console.BasicCommand;
import cn.uway.util.TimeUtil;

/**
 * 命令行获取系统信息
 * 
 * @author zhangp 2015-10-21
 * @since 3.8.5.0
 */
public class SysCommand extends BasicCommand {

	@Override
	public boolean doCommand(String[] args, CommandIO io) throws Exception {
		Date sDate = null;
		try {
			sDate = (Date) Class.forName("cn.uway.igp3.appRunner.Runner").getDeclaredField("SYS_START_TIME").get(null);
		} catch (Exception e) {
			io.println("错误,原因:" + e.getMessage());
		}
		String sysStartTime = TimeUtil.getDateString(sDate);
		long fast = System.currentTimeMillis() - sDate.getTime();
		String cost = "";
		// 小于1小时的使用分钟为单位
		if (fast < (1000 * 60 * 60)) {
			cost = Math.round(fast / (1000 * 60)) + " 分钟";
		}
		// 其他使用小时为单位
		else {
			cost = Math.round(fast / (1000 * 60 * 60)) + " 小时";
		}
		io.println("系统启动时间: " + sysStartTime + "  已运行: " + cost);
		return true;
	}

}
