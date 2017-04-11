package cn.uway.framework.task.worker;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.telnet.TelnetClient;

import cn.uway.framework.connection.TelnetConnectionInfo;
import cn.uway.framework.task.GatherPathDescriptor;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.framework.task.PeriodTask;
import cn.uway.framework.task.Task;

/**
 * Telnet方式 TaskWorker
 * 
 * @author chenrongqiang @ 2013-3-19
 */
public class TelnetTaskWorker extends AbstractTaskWorker {

	/**
	 * Telnet 读取流
	 */
	InputStream inputStream = null;

	/**
	 * Telnet 回写流
	 */
	PrintStream out = null;

	/**
	 * Telnet连接信息
	 */
	protected TelnetConnectionInfo sourceConnectionInfo;

	String prompt = "";

	public TelnetTaskWorker(Task task) {
		super(task);
		if (!(connInfo instanceof TelnetConnectionInfo))
			throw new IllegalArgumentException("连接信息不正确，错误的连接类型");
		sourceConnectionInfo = (TelnetConnectionInfo) connInfo;
	}

	/**
	 * 在Telnet模式下 在开始work前 先检查目录上的文件
	 */
	public void beforeWork() {
		TelnetClient telnetClient = null;
		GatherPathDescriptor gatherPath = task.getGatherPathDescriptor();

		// 分解前置命令
		String beforCommand = task.getShellBefore();
		List<String> commandList = new LinkedList<String>();
		if (beforCommand != null && beforCommand.trim().length() > 0) {
			beforCommand = beforCommand.replaceAll("[\\r\\n]", "");
			String[] strs = beforCommand.split(GatherPathDescriptor.SPLIT_SYMBOL);
			for (String command : strs) {
				command = command.trim();
				if (command.length() < 1)
					continue;

				commandList.add(command);
			}
		}

		try {
			List<GatherPathEntry> pathEntrys = gatherPath.getPaths();
			loginTelnet(telnetClient);
			out.println("ksh");
			out.flush();
			receiveUntilTimeout(prompt, 10);

			// 依次执行前置命令
			for (String command : commandList) {
				out.println(command);
				out.flush();
				LOGGER.debug("开始发送前置命令:{}", command);
				receiveUntilTimeout(prompt, 60);
				LOGGER.debug("前置命令:{}执行成功.", command);
			}

			// 先扫描服务器上匹配的文件
			for (GatherPathEntry pathEntry : pathEntrys) {
				// 查找配置的所有路径
				String path = pathEntry.getPath();
				if (task instanceof PeriodTask)
					path = pathEntry.getConvertedPath(task.getDataTime(), task.getWorkerType());
				// 登陆Telnet
				// out.print("export LANG=en");
				// out.flush();
				String lsCommand = createCommand(path);
				out.println(lsCommand);
				out.flush();
				LOGGER.debug("开始发送命令:{}", lsCommand);
				String remoteFiles = receiveUntilTimeout(prompt, 10).trim();
				if (remoteFiles.length() > 0 && remoteFiles.endsWith(prompt)) {
					// 去掉结尾的提示符。
					remoteFiles = remoteFiles.substring(0, remoteFiles.length() - prompt.length()).trim();
				}

				/**
				 * <pre>
				 * 	对telnet的异常信息处理
				 *  ls出来的结果，包含换行符，有可能第一行是命令回显，在这里要去掉.
				 * </pre>
				 */
				if (remoteFiles.indexOf('\n') >= 0) {
					int nBreaklinePos = remoteFiles.indexOf('\n');
					String firstLine = remoteFiles.substring(0, nBreaklinePos);
					// 包含ls命令符或8(退格键)或127(delete)键
					if (firstLine.startsWith("ls ") || firstLine.indexOf(8) >= 0 || firstLine.indexOf(127) >= 0) {
						remoteFiles = remoteFiles.substring(nBreaklinePos + 1);
					}
				}

				LOGGER.debug("REMOTE FILES ={}", remoteFiles);

				// String sp = (remoteFiles.con);
				String pathFilenameExtension = null;
				int index = path.lastIndexOf(".");
				if (index != -1)
					pathFilenameExtension = path.substring(index);
				File file = new File(path);
				String gatherDirectory = file.getParent().replace('/', '|').replace('\\', '|');
				List<String> splited = ls_split(remoteFiles);
				for (String split : splited) {
					if (split == null)
						continue;
					split = split.trim();
					if (split.isEmpty())
						continue;
					// 过滤掉“ls”出来的文件名的后缀不是要扫描的文件后缀，描文件配置是“.*”的除外。
					if (pathFilenameExtension != null && !".*".equals(pathFilenameExtension) && !split.endsWith(pathFilenameExtension)) {
						LOGGER.debug("过滤掉无效文件：{}", split);
						continue;
					}
					// 过滤掉“ls”结果出来的文件路径不是要扫描的文件路径的扫描结果集。TODO：这里暂时没有考虑扫描目录配置通配符的情况。
					if (gatherDirectory != null) {
						String str = split.replace('/', '|').replace('\\', '|');
						if (str.indexOf(gatherDirectory) == -1) {
							LOGGER.debug("过滤掉无效文件：{}", split);
							continue;
						}
					}
					GatherPathEntry entry = new GatherPathEntry(split);
					
					// 这里第二个参数设置为null, 是因为先前人家在本类中的checkBeginTime和checkEndTime，直接返回true
					if (this.checkGatherObject(entry.getPath(), null))
						this.pathEntries.add(entry);
					// 暂时去掉话单采集文件行数变化 目前检查文件行数变化就需要10分钟左右时间 占用大量话单采集解码的时间
					/*
					 * else { // 如果文件大小有增加的话，也要采。 // 也就是说，某个文件上次采过了，但这次扫描时，发现它变大了，这时要重新采，以保证数据完整。 GatherObjStatus example = new GatherObjStatus();
					 * example.setGatherObj (FilenameUtils.getName(entry.getPath())); example.setTaskId(task.getId()); GatherObjStatus status =
					 * this.gatherObjStatusDAO.searchGatherObjStatus(example); if ( status != null && status.getSubGatherObj() != null ) { // 取一个解析条数。
					 * int gatheredLineCount = NumberUtil.parseInt(status.getSubGatherObj(), -1); if ( gatheredLineCount > 0 ) { // 看看服务器上文件现在有多少行。
					 * String awk = "awk 'END{print NR}' " + entry.getPath(); LOGGER.debug("准备检查服务器上的文件行数：{}", awk); out.println(awk); out.flush();
					 * String strLineCount = null; try { strLineCount = receiveUntilTimeout(prompt, 5 * 60); if ( strLineCount != null ) strLineCount
					 * = strLineCount.trim(); // 去掉结尾的提示符。 strLineCount = strLineCount.substring(0, strLineCount.length() - prompt.length()).trim(); }
					 * catch (Exception e) { LOGGER.error("准备检查服务器上的文件行数失败：" + awk, e); } LOGGER.debug("准备检查服务器上的文件行数，结果：{}", strLineCount); int
					 * currServerLineCount = NumberUtil.parseInt(strLineCount, -1); if ( currServerLineCount > 0 ) { if ( currServerLineCount >
					 * gatheredLineCount ) { LOGGER.warn( "目前服务器上文件有增大，继续采集。文件：{}，当前服务器上文件行数：{}，上次采集时的行数：{}", new Object[] { entry.getPath(),
					 * currServerLineCount, gatheredLineCount }); this.pathEntries.add(entry); } else {
					 * LOGGER.debug("目前服务器上文件无变化。文件：{}，当前服务器上文件行数：{}，上次采集时的行数：{}" , new Object[] { entry.getPath(), currServerLineCount,
					 * gatheredLineCount }); } } }
					 * 
					 * } }
					 */
				}
			}
		} catch (Exception e) {
			LOGGER.debug("error", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * 创建ls命令
	 * 
	 * @param pathName
	 * @return Telnet ls命令
	 */
	String createCommand(String pathName) {
		return " ls " + pathName;
	}

	/**
	 * 登陆FTP 完成用户名、密码校验
	 * 
	 * @param telnetClient
	 * @throws Exception
	 */
	private void loginTelnet(TelnetClient telnetClient) throws Exception {
		try {
			this.prompt = sourceConnectionInfo.getLoginSign();
			telnetClient = new TelnetClient(sourceConnectionInfo.getTermType());
			telnetClient.setReaderThread(true);
			telnetClient.setReceiveBufferSize(1024 * 4);
			telnetClient.connect(sourceConnectionInfo.getIp(), sourceConnectionInfo.getPort());
			inputStream = telnetClient.getInputStream();
			out = new PrintStream(telnetClient.getOutputStream());
			if (inputStream == null)
				throw new Exception(task.getId() + " Telnet连接服务器异常");
			if (!waitBack("ogin", 5000))
				throw new Exception("Telnet登陆异常[input user name]");
			out.println(sourceConnectionInfo.getUserName());
			out.flush();
			if (!waitBack("assword", 5000))
				throw new Exception("Telnet登陆异常[input password] ");
			out.println(sourceConnectionInfo.getPassword());
			out.flush();
			if (!waitBack(sourceConnectionInfo.getLoginSign(), 5000))
				throw new Exception("Telnet登陆异常[login sign] ");
		} catch (Exception e) {
			// 发生异常时关闭流
			LOGGER.debug("Telnet 登陆异常", e);
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(out);
			throw e;
		}
	}

	// 等待TELNET命令提示符号返回相应的关键字
	public boolean waitBack(String keyWords, long timeout) throws Exception {
		return receiveUntilTimeout(keyWords, timeout).indexOf(keyWords) >= 0;
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
			if (inputStream.available() <= 0) {
				Thread.sleep(500);
				continue;
			}
			int redBytes = inputStream.read(buffer);
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
	protected boolean checkBeginTime(String gatherPath, Date timeEntry) {
		return true;
	}

	@Override
	protected boolean checkEndTime(String gatherPath, Date timeEntry) {
		return true;
	}

	@Override
	protected int getMaxConcurentJobThreadCount() {
		return Math.min(pathEntries.size(), systemMaxJobConcurrent);
	}

	// 对ls的结果进行分隔。
	static final List<String> ls_split(String raw) {
		if (raw == null || raw.trim().isEmpty())
			return Collections.emptyList();
		List<String> list = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(raw);
		while (st.hasMoreTokens()) {
			String tk = st.nextToken();
			if (tk != null && !tk.trim().isEmpty())
				list.add(tk.trim());
		}
		return list;
	}

}
