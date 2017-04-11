package cn.uway.framework.task.worker;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * 缓存已在状态表判断过的文件名，目的是减少对状态表的select次数。
 * 
 * @author ChensSijiang 20130306
 */
public final class FileNamesCache {

	private static final ILogger log = LoggerManager.getLogger(FileNamesCache.class);

	/* map中以任务ID作为key，set中存储该任务已经判断过的文件名。 */
	private Map<Long, Set<String>> filenames = new HashMap<Long, Set<String>>();

	/* 调用者所使用的锁，同步操作使用这个对象作为锁，方面与对象内部清理操作同步。 */
	private Object useLock = this;

	/* 负责定时清空缓存。 */
	private Timer cleaner;

	/* 单实例。 */
	private static FileNamesCache instance = new FileNamesCache();

	public static FileNamesCache getInstance() {
		return instance;
	}

	/**
	 * 判断某个任务的某个文件是否已经采集过。
	 * 
	 * @param filename
	 *            文件名。
	 * @param taskId
	 *            任务编号。
	 * @return 是否已经采集过。
	 */
	public boolean isAlreadyGather(String filename, long taskId) {
		if (filename == null)
			return false;

		Set<String> taskNames = this.filenames.get(taskId);
		if (taskNames == null || taskNames.isEmpty())
			return false;

		return taskNames.contains(filename);
	}
	
	/**
	 * 获取任务采集记录在缓存中的数量
	 * @param taskId 任务编号
	 * @return
	 */
	public int getTaskGatherCacheCount(long taskId) {
		Set<String> taskNames = this.filenames.get(taskId);
		if (taskNames == null || taskNames.isEmpty())
			return 0;
		
		return taskNames.size();
	}
	
	protected Set<String> getTaskFileNameCache(long taskId) {
		Set<String> taskNames = this.filenames.get(taskId);
		return taskNames;
	}

	/**
	 * 新增一个缓存内容。
	 * 
	 * @param filename
	 *            文件名。
	 * @param taskId
	 *            任务编号。
	 */
	public void putToCache(String filename, long taskId) {
		if (filename == null)
			return;
		Set<String> taskNames = this.filenames.get(taskId);
		if (taskNames == null) {
			taskNames = new HashSet<String>();
			this.filenames.put(taskId, taskNames);
		}
		taskNames.add(filename);
	}

	/**
	 * 调用者所使用的锁，同步操作使用这个对象作为锁，方面与对象内部清理操作同步。
	 * 
	 * @return 调用者所使用的锁。
	 */
	public Object getUseLock() {
		return useLock;
	}

	private FileNamesCache() {
		super();
		this.cleaner = new Timer("文件名缓存清理线程", true);
		// 每天0点清空。
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		this.cleaner.schedule(new ClearTask(), date.getTime(), 24 * 60 * 60 * 1000);
		log.debug("文件名缓存清理线程将于'{}'首次执行。", date.getTime());
	}

	private final class ClearTask extends TimerTask {

		private final ILogger log = LoggerManager.getLogger(ClearTask.class);

		@Override
		public void run() {
			synchronized (FileNamesCache.this.getUseLock()) {
				log.debug("准备清空文件名缓存，目前缓存的任务数量：{}", FileNamesCache.this.filenames.size());
				Iterator<Entry<Long, Set<String>>> it = FileNamesCache.this.filenames.entrySet().iterator();
				while (it.hasNext()) {
					Entry<Long, Set<String>> entry = it.next();
					log.debug("任务{}，缓存了{}个文件名。", new Object[]{entry.getKey(), entry.getValue() != null ? entry.getValue().size() : "<null>"});
				}
				FileNamesCache.this.filenames.clear();
				log.debug("文件名缓存已被清空。");
			}
		}
	}

	public static void main(String[] args) {
		FileNamesCache.getInstance().putToCache("aa", 123);
		System.out.println(FileNamesCache.getInstance().isAlreadyGather("aa", 121));
		System.out.println(FileNamesCache.getInstance().isAlreadyGather("aa", 123));
	}
}
