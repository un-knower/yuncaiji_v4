package cn.uway.framework.console;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.uway.console.Console;
import cn.uway.console.command.Command;
import cn.uway.console.command.CommandAction;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * IGP3控制台，以telnet方式连接，查询任务运行情况等信息。
 * 
 * @author ChenSijiang 2012-11-8
 */
public class IGP3Console {

	private static final ILogger log = LoggerManager.getLogger(IGP3Console.class);

	private static final int DEFAULT_CLIENT_TIMEOUT_MILLS = 3 * 60 * 1000;

	/** 侦听端口。 */
	protected int port;

	/** 控制台欢迎消息。 */
	protected String welcomeMessage;

	/** 命令未找到时的动作。 */
	protected CommandAction cmdNotFoundAction;

	/** 命令定义列表。以命令为key，命令动作为value. */
	protected Map<String, CommandAction> commands;

	/* 内部使用的commons_console. */
	private Console _console;

	/* 客户端超时（秒）。 */
	private int clientTimeoutSecond;

	/**
	 * 设置侦听端口。
	 * 
	 * @param port
	 *            侦听端口。
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * 设置控制台欢迎消息。
	 * 
	 * @param welcomeMessage
	 *            控制台欢迎消息。
	 */
	public void setWelcomeMessage(String welcomeMessage) {
		this.welcomeMessage = welcomeMessage;
	}

	/**
	 * 设置命令未找到时的动作。
	 * 
	 * @param cmdNotFoundAction
	 *            命令未找到时的动作。
	 */
	public void setCmdNotFoundAction(CommandAction cmdNotFoundAction) {
		this.cmdNotFoundAction = cmdNotFoundAction;
	}

	/**
	 * 设置命令定义列表。以命令为key，命令动作为value.
	 * 
	 * @param commands
	 *            命令定义列表。以命令为key，命令动作为value.
	 */
	public void setCommands(Map<String, CommandAction> commands) {
		this.commands = commands;
	}

	public void setClientTimeoutSecond(int clientTimeoutSecond) {
		this.clientTimeoutSecond = clientTimeoutSecond;
	}

	/**
	 * 启动控制台。
	 */
	public synchronized void startConsole() {
		try {
			checkPort(port);
			Map<String, Command> cmds = new HashMap<String, Command>();
			Iterator<String> keys = this.commands.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				cmds.put(key, new Command(key, this.commands.get(key)));
			}
			this._console = new Console(this.port, cmds, this.cmdNotFoundAction, this.welcomeMessage + "\r\n>", this.clientTimeoutSecond > 0
					? this.clientTimeoutSecond * 1000
					: DEFAULT_CLIENT_TIMEOUT_MILLS);
			this._console.start();
			log.debug("控制台已成功启动，端口：{}", this.port);
		} catch (Exception e) {
			log.warn("控制台功能启动时异常。", e);
		}
	}

	/**
	 * 停止控制台。
	 */
	public synchronized void stopConsole() {
		if (this._console != null) {
			this._console.stop();
		}
	}

	static void checkPort(int port) {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
		} catch (Exception e) {
			log.error(
					"【注意】*************由于端口{}（config.ini中的system.console.port配置项）已被占用，为防止程序重复开启，此程序退出。错误消息：{}**************************************",
					new Object[]{port, e.getMessage()});
			System.exit(1);
		} finally {
			if (ss != null && !ss.isClosed()) {
				try {
					ss.close();
				} catch (IOException e) {}
			}
		}
	}
}
