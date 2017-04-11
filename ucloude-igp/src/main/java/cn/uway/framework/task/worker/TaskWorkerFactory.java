package cn.uway.framework.task.worker;

import cn.uway.framework.task.Task;

/**
 * Task worker构造工厂 目前worker type没有存在价值 建议通过 conn_type实例化不同的task worker
 * 
 * @desc 根据不同的task worker type来实例化不同类型的task worker<br>
 * @author chenrongqiang
 * @date 2011/11/19
 * @version 1.0
 */
public class TaskWorkerFactory {

	/**
	 * FTP工作类型<br>
	 */
	private static final int WORKER_TYPE_FTP = 2;

	/**
	 * Telnet工作类型
	 */
	private static final int WORKER_TYPE_TELNET = 3;

	/**
	 * FTP分组类型
	 */
	private static final int WORKER_TYPE_FTP_GROUP = 4;

	/**
	 * 朗讯DO话单类型
	 */
	private static final int WORKER_TYPE_LUCDO = 5;

	/**
	 * 本地磁盘扫描类型
	 */
	private static final int WORKER_TYPE_LOCAL = 6;

	/**
	 * SFTP工作类型
	 */
	private static final int WORKER_TYPE_SFTP = 7;
	
	/**
	 * SFTP 分组采集
	 */
	private static final int WORKER_TYPE_SFTP_GROUP = 8;

	/**
	 * 类似IGP1方式的处理，FTP文件下载到本地，下载到一个文件，进行解析入库。
	 */
	private static final int WORKER_TYPE_LOCAL_FTP_EACH = 9;

	/**
	 * 适配IGP1方式的数据库采集。
	 * */
	private static final int WORK_TYPE_DB_IGP1 = 60;

	/**
	 * 组文件批量周期性采集（烽火性能）。
	 * */
	private static final int WORK_TYPE_GROUP_PERIOD_FTP = 10;
	
	/**
	 * http方式 采集
	 */
	private static final int WORK_TYPE_HTTP = 11;

	/**
	 * 类似IGP1方式的处理，FTP文件下载到本地，下载到一个文件，进行解析，一个任务一个周期的文件都解析完后，再调入库。 典型的例子是WCDMA的爱立信性能。
	 */
	// private static final int WORKER_TYPE_LOCAL_FTP_FULL = 8;

	public TaskWorkerFactory() {
		super();
	}

	public static AbstractTaskWorker getTaskWorkerFactory(Task task) {
		// 并发多个JOB处理
		if (task.getWorkerType() == WORKER_TYPE_FTP)
			return new FTPTaskWorker(task);
		if (task.getWorkerType() == WORKER_TYPE_TELNET)
			return new TelnetTaskWorker(task);
		if (task.getWorkerType() == WORKER_TYPE_FTP_GROUP)
			return new GroupingFTPTaskWorker(task);
		if (task.getWorkerType() == WORKER_TYPE_LUCDO)
			return new LucDoTaskWorker(task);
		if (task.getWorkerType() == WORKER_TYPE_LOCAL)
			return new LocalTaskWorker(task);
		if (task.getWorkerType() == WORKER_TYPE_SFTP)
			return new SFTPTaskWorker(task);
		if (task.getWorkerType() == WORKER_TYPE_SFTP_GROUP)
			return new SFTPGroupingTaskWorker(task);
		if (task.getWorkerType() == WORK_TYPE_DB_IGP1)
			return new DbTaskWorker(task);
		if(task.getWorkerType() == WORK_TYPE_HTTP){
			return new HttpTaskWorker(task);
		}
		// 其他情况返回默认的TaskWorker
		return new DefaultTaskWorker(task);
	}

	public static final boolean isLogCltInsert(int workType) {
		return (workType == WORKER_TYPE_LOCAL_FTP_EACH || workType == WORK_TYPE_DB_IGP1 || workType == WORK_TYPE_GROUP_PERIOD_FTP);
	}
}
