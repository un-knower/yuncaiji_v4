package cn.uway.util;

import java.io.File;
import java.io.FileInputStream;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.loganalyzer.SqlLdrLogAnalyzer;

public class SqlldrRunner {
	
	private long taskId;
	
	private String tableId;

	/**
	 * 命令执行器
	 */
	private ExternalCmd executor;

	/**
	 * oracle服务名
	 */
	private String serviceName;

	/**
	 * oracle用户名
	 */
	private String userName;

	/**
	 * oracle密码
	 */
	private String password;

	/**
	 * sqlldr 控件文件路径
	 */
	private String cltPath;

	/**
	 * bad文件路径
	 */
	private String badpath;

	/**
	 * log文件路径
	 */
	private String logPath;

	/**
	 * 跳过的行数
	 */
	private int skip;
	
	/**
	 * sqlldr执行缓存条数
	 */
	private int dataCacheLine;

	/**
	 * 命令的执行结果
	 */
	private SqlldrResult result;
	
	/**
	 * 日志
	 */
	private static final ILogger LOGGER = LoggerManager.getLogger(SqlldrRunner.class); // 日志


	public SqlldrRunner() {
		super();
	}

	public SqlldrRunner(long taskId, String tableId, String serviceName,
			String userName, String password, String cltPath, String badpath,
			String logPath, int skip, int dataCacheLine) {
		this();
		this.taskId = taskId;
		this.tableId = tableId;
		this.serviceName = serviceName;
		this.userName = userName;
		this.password = password;
		this.cltPath = cltPath;
		this.badpath = badpath;
		this.logPath = logPath;
		this.skip = skip;
		this.dataCacheLine = dataCacheLine;
	}

	/**
	 * 开始执行sqlldr命令
	 */
	public SqlldrResult runSqlldr() {
		//10g的数据库有readszie不大于20MB的限定，11g没有限制
		String cmd = "sqlldr userid=%s/%s@%s skip=%s control=%s bad=%s log=%s errors=999999 bindsize=20000000 rows=5000 readsize=20000000 ";
		cmd = String.format(cmd, userName, password, serviceName, skip, cltPath, badpath, logPath);
		LOGGER.debug("要执行的sqlldr命令为：" + cmd.replace(userName, "*").replace(password, "*"));

		executor = new ExternalCmd();
		executor.setCmd(cmd);

		int retCode = -1; // 执行sqlldr后的返回码

		try {
			retCode = executor.execute();
			if (retCode == 0 || retCode == 2) {
				LOGGER.debug("Task-" + taskId + "-" + tableId + ": sqldr OK. retCode=" + retCode);
			} else if (retCode != 0 && retCode != 2) {
//				int maxTryTimes = 1;
//				int tryTimes = 0;
//				long waitTimeout = 30 * 1000;
//				while (tryTimes < maxTryTimes) {
//					retCode = executor.execute();
//					if (retCode == 0 || retCode == 2) {
//						break;
//					}
//
//					tryTimes++;
//					waitTimeout = 2 * waitTimeout;
//
//					LOGGER.error("Task-" + taskId + "-" + tableId + ": 第" + tryTimes + "次Sqlldr尝试入库失败. " + cmd + " retCode=" + retCode);
//
//					//Thread.sleep(waitTimeout);
//				}
//
//				// 如果重试超过 maxTryTimes 次还失败则记录日志
//				if (retCode == 0 || retCode == 2) {
//					LOGGER.info("Task-" + taskId + "-" + tableId + ": " + tryTimes + "次Sqlldr尝试入库后成功. retCode=" + retCode);
//				} else {
//					LOGGER.error("Task-" + taskId + "-" + tableId + " : " + tryTimes + "次Sqlldr尝试入库失败. " + cmd + " retCode=" + retCode);
//				}
			}
		} catch (Exception e) {
			LOGGER.error("执行sqlldr时发生异常，原因： " + e.getMessage());
		}

		File logFile = new File(logPath);
		if (!logFile.exists() || !logFile.isFile()) {
			LOGGER.info(logPath + "不存在，任务ID：" + taskId);
			return null;
		}
		SqlLdrLogAnalyzer analyzer = SqlLdrLogAnalyzer.getInstance();
		try {
			SqlldrResult result = analyzer.analysis(new FileInputStream(logPath));
			String txtFile = badpath.replace(".bad", ".txt");
			if (retCode == 0 || retCode == 2){
				removeFile();
			} else {
				LOGGER.debug("日志分析出错，本次未删除数据文件：{}", new Object[]{txtFile});
			}
			
			return result;
		} catch (Exception e) {
			LOGGER.error("Task-" + taskId + "-" + tableId + ": sqlldr日志分析失败，文件名：" + logPath + "，原因: ", e);
		}
		return null;
	}
	
	public void removeFile() {
		String txtFile = badpath.replace(".bad", ".txt");
		Boolean bRetDelBad = new File(badpath).delete();
		Boolean bRetDelClt = new File(cltPath).delete();
		Boolean bRetDelTxt = new File(txtFile).delete();
		Boolean bRetDelLog = new File(logPath).delete();
		LOGGER.debug("已删除数据文件:{},bad:{},clt:{},txt:{},log:{}", new Object[]{txtFile, 
				bRetDelBad,	bRetDelClt, bRetDelTxt, bRetDelLog
		});
	}

	public ExternalCmd getExecutor() {
		return executor;
	}

	public void setExecutor(ExternalCmd executor) {
		this.executor = executor;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCltPath() {
		return cltPath;
	}

	public void setCltPath(String cltPath) {
		this.cltPath = cltPath;
	}

	public SqlldrResult getResult() {
		return result;
	}

	public String getBadpath() {
		return badpath;
	}

	public void setBadpath(String badpath) {
		this.badpath = badpath;
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

	public int getDataCacheLine() {
		return dataCacheLine;
	}

	public void setDataCacheLine(int dataCacheLine) {
		this.dataCacheLine = dataCacheLine;
	}

	/**
	 * 删除txt,log,bad,ctl文件
	 */
	public void delLogs() {
		new File(logPath).delete();
		new File(badpath).delete();
		new File(cltPath).delete();
	}

	public void printResult(SqlldrResult result) {
		LOGGER.info("===============sqlldr结果分析=================");

		LOGGER.info("日志位置：" + logPath);
		LOGGER.info("表名：" + result.getTableName());
		LOGGER.info("载入成功的行数：" + result.getLoadSuccCount());
		LOGGER.info("因数据错误而没有加载的行数：" + result.getData());
		LOGGER.info("因when子句失败页没有加载的行数：" + result.getWhen());
		LOGGER.info("null字段行数：" + result.getNullField());
		LOGGER.info("跳过的逻辑记录总数：" + result.getSkip());
		LOGGER.info("读取的逻辑记录总数：" + result.getRead());
		LOGGER.info("拒绝的逻辑记录总数：" + result.getRefuse());
		LOGGER.info("废弃的逻辑记录总数：" + result.getAbandon());
		LOGGER.info("开始运行时间：" + result.getStartTime());
		LOGGER.info("结束运行时间：" + result.getEndTime());

		LOGGER.info("==============================================");
	}

}
