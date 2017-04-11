package cn.uway.framework.task.worker.luc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.IoUtil;
import cn.uway.util.StringUtil;

/**
 * EVDOPCMD文件切割线程<br>
 * 
 * @author chenrongqiang @ 2013-9-5
 */
public class LucEvdoCutter implements Runnable {

	/**
	 * 朗讯EVDOPCMD文件切割器参数<br>
	 */
	private EvdoCutterParam param;

	/**
	 * 文件开始断点<br>
	 */
	private long breakpoint = 0;

	/**
	 * 是否历史文件
	 */
	private boolean historyFlag = false;

	/**
	 * 文件流转换后的bufferdReader<br>
	 */
	private BufferedReader reader;

	/**
	 * 当前文件生成读取的行数<br>
	 */
	private long readLineNum = 0;

	private String currFileName = null;

	private String headerLine = null;

	private String evdoName = null;

	/**
	 * 单个文件最大的行数限制<br>
	 * 当读取到300W行后，无论是否满足超时规则，都会强制的生成一个文件<br>
	 */
	private final int MAX_LINE_PER_FILE = 3000000;// 3000000;

	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(LucEvdoCutter.class);

	private static final String LucDoTmpFileDir = AppContext.getBean("LucDoTmpFileDir", String.class);

	private String idxExtentName = ".idx";

	private File idxfile = null;

	private PrintWriter pw = null;

	/**
	 * 构造方法<br>
	 * 
	 * @param param
	 */
	public LucEvdoCutter(EvdoCutterParam param) {
		this.param = param;
		validate();
		idxFileInit();
		IdxInfo idxInfo = getIdxInfo(idxfile);
		this.breakpoint = idxInfo.breakPoint;
		this.headerLine = idxInfo.headerLine;
		this.historyFlag = param.isHistoryFlag();
		this.reader = new BufferedReader(new InputStreamReader(param.getInputstream()), 4 * 1024);
		this.evdoName = param.getEvdoName();
	}

	/**
	 * idx文件初始化
	 */
	public void idxFileInit() {
		File splitFileDir = new File(getLocalDir());
		File[] files = splitFileDir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				name = name.toUpperCase();
				return name.endsWith(idxExtentName.toUpperCase());
			}
		});
		String fileName = param.getEvdoName().substring(param.getEvdoName().lastIndexOf("/") + 1, param.getEvdoName().length());
		String name = fileName.substring(0, fileName.indexOf("."));
		if (files != null && files.length != 0) {
			for (File f : files) {
				if (f.getName().contains(name)) {
					idxfile = f;
				}
			}
		}
		if (idxfile == null) {
			idxfile = new File(splitFileDir + File.separator + name + idxExtentName);
			if (!idxfile.exists())
				try {
					idxfile.createNewFile();
				} catch (IOException e) {
					LOGGER.error("[" + param.getTaskId() + "]，创建idx文件失败", e);
				}
		}
		try {
			FileOutputStream out = new FileOutputStream(idxfile, true);
			pw = new PrintWriter(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("[" + param.getTaskId() + "]，idx文件不存在或者不是文件", e);
		}
	}

	/**
	 * 返回断点
	 * 
	 * @return
	 */
	public long getBreakPoint() {
		return this.breakpoint;
	}

	/**
	 * 验证param
	 */
	private void validate() {
		if (param.getInputstream() == null)
			throw new IllegalArgumentException("参数错误,传入的流为空");
		if (param.getTaskId() < 0L)
			throw new IllegalArgumentException("参数错误,任务ID非法");
		if (StringUtil.isEmpty(param.getEvdoName()))
			throw new IllegalArgumentException("参数错误,EVDO文件名为空");
	}

	@Override
	public void run() {
		Thread.currentThread().setName("【" + evdoName + "切割线程】");
		LOGGER.debug("任务{}，文件{}开始进行切割", param.getTaskId(), evdoName);
		cutEvdoFile(historyFlag);
	}

	/**
	 * 切割实时文件<br>
	 */
	private void cutEvdoFile(boolean historyFlag) {
		long totalSize = 0;
		long singleFileSize = 0;
		long tmpSize = 0;
		// 历史文件
		if (historyFlag) {
			totalSize = param.getSize();
			singleFileSize = totalSize / 12;// 单位：字节
		}
		EvdoFileProducer evdoFileProducer = null;
		String nextFileName = null;
		long startTime = 0;
		try {
			// 如果断点为0，则需要先读取第一行<br>
			if (breakpoint == 0) {
				headerLine = reader.readLine();
				mark(breakpoint);
				tmpSize += headerLine.getBytes().length;
				readLineNum++;
			}
			String lineInfo = null;
			// 标记读取的开始时间<br>
			long start = System.currentTimeMillis();
			startTime = start;
			nextFileName = getNextFileName();
			if (nextFileName == null)
				return;
			// 需要保存当前的文件名，否则最后一个文件时会有空指针问题
			currFileName = nextFileName;
			evdoFileProducer = new EvdoFileProducer(getLocalDir() + File.separator + nextFileName);
			evdoFileProducer.write(headerLine);
			while ((lineInfo = reader.readLine()) != null) {
				if (readLineNum <= breakpoint) {
					readLineNum++;
					continue;
				}

				// 判断结束条件<br>
				if (historyFlag ? tmpSize >= singleFileSize : finish(start) && !currFileName.toUpperCase().endsWith("55.EVDOPCMD")) {
					// 提交文件并且创建一个新的文件<br>
					// 完成上一个文件的处理
					finishLastSplitFile(evdoFileProducer);
					if ((nextFileName = getNextFileName()) != null) {
						evdoFileProducer = new EvdoFileProducer(getLocalDir() + File.separator + nextFileName);
						evdoFileProducer.write(headerLine);
						currFileName = nextFileName;
						// 重置大小和时间
						tmpSize = 0;
						start = System.currentTimeMillis();
					}
				}
				evdoFileProducer.write(lineInfo);
				// 大小 +
				tmpSize += lineInfo.getBytes().length;
				// 读取条数+1
				readLineNum++;
			}
			// 完成最后一个文件的处理
			finishLastSplitFile(evdoFileProducer);
		} catch (SocketTimeoutException e) {
			LOGGER.error("文件" + evdoName + "拆分超时，详细原因", e);
			// 完成最后一个文件的处理
			finishLastSplitFile(evdoFileProducer);
		} catch (Exception e) {
			LOGGER.error("文件" + evdoName + "拆分失败，详细原因", e);
		} finally {
			// 生成空文件
			try {
				while ((nextFileName = getNextFileName()) != null) {
					evdoFileProducer = new EvdoFileProducer(getLocalDir() + File.separator + nextFileName);
					evdoFileProducer.write(headerLine);
					currFileName = nextFileName;
					// 重置大小
					tmpSize = 0;
					finishLastSplitFile(evdoFileProducer);
				}
				long stampTime = (System.currentTimeMillis() - startTime) / 1000;
				LOGGER.debug("[" + param.getTaskId() + "]，文件" + evdoName + "拆分成功，共读取" + readLineNum + "行，大小" + totalSize + "字节，耗时" + stampTime + "秒");
			} catch (Exception e) {
				LOGGER.error("[" + param.getTaskId() + "]，文件" + evdoName + "拆分空文件失败，详细原因", e);
			}
			// 关闭流
			closeStream();
			// 必须关闭 释放文件句柄
			if (evdoFileProducer != null)
				evdoFileProducer.close();
		}
	}

	/**
	 * 完成上一个（最后一个）文件的处理：add、commit、mark
	 * 
	 * @param evdoFileProducer
	 */
	private void finishLastSplitFile(EvdoFileProducer evdoFileProducer) {
		evdoFileProducer.commit();
		addEntry(currFileName);
		breakpoint = readLineNum;
		mark(breakpoint);
	}

	/**
	 * 关闭流
	 */
	private void closeStream() {
		IoUtil.closeQuietly(reader);
		IoUtil.closeQuietly(param.getInputstream());
		IoUtil.closeQuietly(pw);
	}

	/**
	 * 将切割后的文件添加到队列中<br>
	 * 
	 * @param cuttedFileName
	 */
	private void addEntry(String cuttedFileName) {
		cuttedFileName = getLocalDir() + File.separator + cuttedFileName;
		GatherPathEntry entry = new GatherPathEntry(cuttedFileName);
		param.getPathEntries().add(entry);
		if (currFileName.toUpperCase().endsWith("55.EVDOPCMD")) {
			entry.setLast(true);
		} else {
			entry.setLast(false);
		}
	}

	/**
	 * 判断是否满足结束条件<br>
	 * 
	 * @param start
	 * @return 是否达到结束条件<br>
	 */
	private boolean finish(long start) {
		if (readLineNum >= MAX_LINE_PER_FILE)
			return true;
		// 1000条才判断一次超时机制
		if (readLineNum % 1000 == 0)
			return timeout(start);
		return false;
	}

	/**
	 * 判断是否读取超时<br>
	 * 
	 * @param start
	 * @return 是否已经读取5分钟<br>
	 */
	private boolean timeout(long start) {
		return System.currentTimeMillis() - start >= 300000;
	}

	/**
	 * 获取下一个需要切割的文件的名字<br>
	 * 如果已经获取过最后一个文件，则调用该方法直接返回null<br>
	 * 
	 * @param taskId
	 * @param evdoName
	 * @return 下一个切割文件的名字
	 * @throws Exception
	 */
	public String getNextFileName() throws Exception {
		if (breakpoint == 0) {
			String fileName = evdoName.substring(evdoName.lastIndexOf("/") + 1, evdoName.length());
			String name = fileName.substring(0, fileName.indexOf("."));
			return name + "00" + fileName.substring(fileName.indexOf("."));
		}
		if (currFileName != null && currFileName.toUpperCase().endsWith("55.EVDOPCMD")) {
			return null;
		}
		if (currFileName == null)
			return null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
		Date date = sdf.parse(currFileName.substring(0, currFileName.indexOf(".")));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, 5);
		return sdf.format(calendar.getTime()) + currFileName.substring(currFileName.indexOf("."));
	}

	/**
	 * 标记方法<br>
	 * 向idx文件中写入断点信息<br>
	 * 
	 * @param taskId
	 * @param evdoName
	 * @return 是否标记成功
	 */
	public boolean mark(long breakpoint) {
		try {
			// 第一次，只写入文件头
			if (breakpoint == 0) {
				pw.append(headerLine + "\r\n");
			} else {
				pw.append(this.currFileName + ":" + breakpoint + "\r\n");
			}
			pw.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("[" + param.getTaskId() + "]，写入idx文件异常", e);
			return false;
		}
		return true;
	}

	/**
	 * 返回本地目录 存放拆分文件的目录
	 * 
	 * @return
	 */
	public String getLocalDir() {
		return LucDoTmpFileDir + File.separator + param.getBscId();
	}

	/**
	 * 获取指定的任务对应的文件的断点信息<br>
	 * 
	 * @return 指定文件断点信息<br>
	 */
	public IdxInfo getIdxInfo(File idxFile) {
		IdxInfo idxInfo = new IdxInfo();
		if (idxFile.length() == 0) {
			idxInfo.breakPoint = 0;
			return idxInfo;
		}
		InputStream in = null;
		BufferedReader reader = null;
		try {
			in = new FileInputStream(idxFile);
			reader = new BufferedReader(new InputStreamReader(in));
			String lineStr = null;
			String tmpLineStr = null;
			String tmpHeadStr = null;
			int n = 0;
			while ((lineStr = reader.readLine()) != null) {
				if (n == 0)
					tmpHeadStr = lineStr;
				tmpLineStr = lineStr;
				n++;
			}
			// 只有文件头
			if (n == 1) {
				idxInfo.breakPoint = 0;
				idxInfo.headerLine = tmpHeadStr;
				return idxInfo;
			}
			// 取断点
			currFileName = tmpLineStr.trim().substring(0, tmpLineStr.trim().indexOf(":"));
			idxInfo.breakPoint = Integer.parseInt(tmpLineStr.trim().substring(tmpLineStr.trim().indexOf(":") + 1));
			idxInfo.headerLine = tmpHeadStr;
			return idxInfo;

		} catch (Exception e) {
			LOGGER.error("idx文件找不到 ", e);
			return idxInfo;
		} finally {
			IoUtil.closeQuietly(in);
			IoUtil.closeQuietly(reader);
		}
	}

	/**
	 * @author zxk 记录idx文件
	 */
	public class IdxInfo {

		public String headerLine;

		public int breakPoint;
	}
}
