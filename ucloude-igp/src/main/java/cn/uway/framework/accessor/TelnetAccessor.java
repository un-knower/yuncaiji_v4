package cn.uway.framework.accessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.net.telnet.TelnetClient;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.connection.TelnetConnectionInfo;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.IoUtil;

public class TelnetAccessor extends AbstractAccessor {

	private static final ILogger LOGGER = LoggerManager
			.getLogger(TelnetAccessor.class);

	/**
	 * Telnet方式数据源连接信息
	 */
	protected TelnetConnectionInfo connInfo;

	/**
	 * telnetClient对象
	 */
	protected TelnetClient telnetClient = null;

	protected InputStream in = null;

	protected PrintStream out = null;

	@Override
	public void setConnectionInfo(ConnectionInfo connInfo) {
		if (!(connInfo instanceof TelnetConnectionInfo))
			throw new IllegalArgumentException("错误的连接信息.请配置有效的Telnet连接配置信息");
		this.connInfo = (TelnetConnectionInfo) connInfo;
	}

	@Override
	public boolean beforeAccess() {
		try {
			telnetClient = new TelnetClient(connInfo.getTermType());
			telnetClient.setReaderThread(true);
			telnetClient.setReceiveBufferSize(1024 * 4);
			telnetClient.connect(connInfo.getIp(), connInfo.getPort());
			int timeoutSec = connInfo.getTimeoutMinutes() * 60;
			timeoutSec = (timeoutSec > 0) ? timeoutSec : 60;
			telnetClient.setSoTimeout(timeoutSec * 1000);
			LOGGER.info("设置超时，{}s", timeoutSec);
			in = telnetClient.getInputStream();
			out = new PrintStream(telnetClient.getOutputStream());
			if (in == null)
				return false;
			LOGGER.debug(
					"telnet开始登录：{}:{}，用户名：{}，密码：{}",
					new Object[]{connInfo.getIp(), connInfo.getPort(),
							connInfo.getUserName(), connInfo.getPassword()});
			// 如果服务器返回login,则发送用户名
			if (waitForString("ogin:", 5000)) {
				out.println(connInfo.getUserName());
				out.flush();
			} else
				return false;
			// 如果服务器返回password,则发送用户密码
			if (waitForString("assword:", 5000)) {
				out.println(connInfo.getPassword());
				out.flush();
			} else {
				return false;
			}
			// 等待用户登陆成功信息
			if (!waitForString(connInfo.getLoginSign(), 5000)) {
				return false;
			}
		} catch (Exception e) {
			LOGGER.error("telnet 登陆失败", e);
			return false;
		}
		LOGGER.info("telnet 登陆成功");
		return super.beforeAccess();
	}

	// 等待TELNET命令提示符号返回
	public boolean waitForString(String end, long timeout) throws Exception {
		byte[] buffer = new byte[1024 * 10];

		long starttime = System.currentTimeMillis();

		try {
			String readbytes = new String();
			// 循环读取，直到超时或者遇到结束符号
			while ((readbytes.indexOf(end) < 0)
					&& ((System.currentTimeMillis() - starttime) < timeout * 1000)) {
				if (in.available() > 0) {
					int ret_read = in.read(buffer);
					if (ret_read != -1)
						readbytes = readbytes + new String(buffer, 0, ret_read);
				} else {
					// System.out.println(readbytes+" waitForString timeout
					// "+timeout);
					Thread.sleep(500);
				}
			}

			if (readbytes.indexOf(end) >= 0) {
				return (true);
			} else {
				return (false);
			}
		} catch (Exception e) {
			LOGGER.error("Telnet> waitForString error.", e);
			return false;
		}
	}

	String receiveUntilTimeout(String keyWords, long timeout) throws Exception {
		byte[] buffer = new byte[1024];
		long startTime = System.currentTimeMillis();
		String readbytes = "";
		// 循环读取直到超时或者遇到相应的关键字
		while ((readbytes.indexOf(keyWords) < 0)) {
			if (isTimeout(startTime, timeout * 1000L))
				throw new Exception("连接Telnet服务器超时,timeout=" + timeout);
			// 如果输入流没有内容 线程休眠0.5秒
			if (in.available() <= 0) {
				Thread.sleep(500);
				continue;
			}
			int redBytes = in.read(buffer);
			if (redBytes != -1)
				readbytes = readbytes + new String(buffer, 0, redBytes);
		}
		LOGGER.debug("keyWords={},back={}", new Object[]{keyWords, readbytes});
		return readbytes;
	}

	boolean isTimeout(long startTime, long timeout) {
		return System.currentTimeMillis() - startTime >= timeout;
	}

	@Override
	public AccessOutObject access(GatherPathEntry path) {
		String pathStr = path.getPath();
		// 是不是solaris，否则认为是linux.
		boolean isSunOs = false;
		LOGGER.debug("发送命令“uname”……");
		out.println("uname");
		out.flush();
		try {
			String recv = receiveUntilTimeout(connInfo.getLoginSign(), 5);
			LOGGER.debug("uname返回内容为：{}", recv);
			if (recv != null && recv.toLowerCase().contains("sunos"))
				isSunOs = true;
		} catch (Exception e) {
			LOGGER.error("接收uname的响应失败。", e);
			return null;
		}

		// int startLine = 2;
		// GatherObjStatus example = new GatherObjStatus();
		// example.setTaskId(task.getId());
		// example.setPcName(task.getPcName());
		// example.setGatherObj(FilenameUtils.getName(pathStr));
		// GatherObjStatus status =
		// gatherObjStatusDAO.searchGatherObjStatus(example);
		// if ( status != null && status.getSubGatherObj() != null
		// && !status.getSubGatherObj().trim().isEmpty() )
		// {
		// startLine = NumberUtil.parseInt(status.getSubGatherObj().trim(), -1);
		// if ( startLine < 2 )
		// {
		// startLine = 2;
		// }
		// else
		// {
		// log.debug("文件“{}”的读取行数断点：{}", new Object[] { pathStr, startLine });
		// }
		// }

		// solaris和linux的tail写法有些不同，以下命令都表示从第1行开始tail，并使用-f模式，一直监视文件的增加。
		// TODO从第几行开始读取，这个要根据断点来。
		String tailCmd = (isSunOs ? "tail +1f " : "tail -fn +1 ");
		pathStr = tailCmd + pathStr;

		LOGGER.info("发送命令:{}", pathStr);
		out.println(pathStr);
		out.flush();

		return new StreamAccessOutObject(pathStr, in, 0);
	}

	@Override
	public void close() {
		super.close();
		IoUtil.closeQuietly(in);
		IoUtil.closeQuietly(out);
		if (telnetClient != null) {
			try {
				telnetClient.disconnect();
			} catch (IOException e) {
			}
		}
	}

}
