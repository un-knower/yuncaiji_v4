package cn.uway.util.parquet;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import cn.uway.framework.warehouse.exporter.breakPoint.BpInfo;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.apache.parquet.hadoop.ParquetRecordWriter;

/**
 * ParquetWriter的对象池（不再被使用后，可自动关闭）
 * 
 * @author sunt
 *
 */
public class PWPool {
	private static ILogger LOG = LoggerManager.getLogger(PWPool.class);
	// wKey,w（Writer对象池）
	private static HashMap<String, ParquetRecordWriter> wPool = new HashMap<String, ParquetRecordWriter>();
	// wKey,taskfns（Writer与文件的映射池）
	private static HashMap<String, Set<String>> w2FilesPool = new HashMap<String, Set<String>>();
	// wKey,分钟数（Writer对象进入待清除池的分钟数）
	private static HashMap<String, Long> needClosePool = new HashMap<String, Long>();
	// Writer未引用后，停留多久关闭
	private static Integer TIME_OUT = ParqContext.getWaitForClose() * 60 * 1000;

	/**
	 * 根据wKey获取对应的writer对象，如果已经存在就返回，没有就创建(线程安全)
	 * 
	 * @param wKey Writer的唯一标识（例如：“taskid+输出表+分区字符串”表示任务间不共享writer，“输出表+分区字符串”表示任务间可以共享writer）
	 * @param taskfn 当前Writer对应的文件的唯一标识（例如：taskid+文件名）
	 * @param bpInfo 断点相关信息
	 * @param partStr 分区字符串
	 * @return
	 */
	public synchronized static ParquetRecordWriter getWriter(String wKey,
			String taskfn, BpInfo bpInfo, String partStr) {
		if (wPool.containsKey(wKey)) {
			ParquetRecordWriter w = wPool.get(wKey);
			// 整理writer与文件的关系
			Set<String> taskfns = w2FilesPool.get(wKey);
			if (!taskfns.contains(taskfn)) {
				taskfns.add(taskfn);
				w.addStatusId(bpInfo);
			}
			return w;
		}
		return createWriter(wKey, taskfn, bpInfo, partStr);
	}

	/**
	 * 根据标识符创建对应的writer对象（私有方法。如果要改为共有，需处理线程安全）
	 * 
	 * @param wKey Writer的唯一标识
	 * @param taskfn 当前Writer对应的文件的唯一标识
	 * @param bpInfo 断点相关信息
	 * @param partStr 分区字符串
	 * @return
	 */
	private static ParquetRecordWriter createWriter(String wKey,String taskfn, BpInfo bpInfo,
			String partStr) {
		ParquetRecordWriter w = new ParquetRecordWriter(wKey, bpInfo, partStr);
		wPool.put(wKey, w);

		Set<String> taskfns = new HashSet<String>();
		taskfns.add(taskfn);
		w2FilesPool.put(wKey, taskfns);
		return w;
	}

	/**
	 * 清理Writer对象对本taskfn的依赖
	 * 
	 * @param taskfn 文件唯一标识
	 */
	public synchronized static void removeAll(String taskfn) {
		LOG.debug("wKey.removeAll taskfn:{}", taskfn);
		for (String wKey : w2FilesPool.keySet()) {
			Set<String> taskfns = w2FilesPool.get(wKey);
			if (taskfns.remove(taskfn)) {
				LOG.debug("remove wKey:{}", wKey);
				if (taskfns.isEmpty()) {
					needClosePool.put(wKey, new Date().getTime());
				}
			}
		}
	}

	/**
	 * 检查一遍待关闭池，如果被复用，就移出，如果超时就关闭（线程安全）
	 */
	public synchronized static void checkClose() {
		Set<String> removeKeys = new HashSet<String>();
		for (String wKey : needClosePool.keySet()) {
			if (!w2FilesPool.get(wKey).isEmpty()) {
				LOG.debug("reuse wKey:{}", wKey);
				removeKeys.add(wKey);
				continue;
			}
			if ((new Date().getTime() - needClosePool.get(wKey)) >= TIME_OUT) {
				removeKeys.add(wKey);
				closeWriter(wKey);
				w2FilesPool.remove(wKey);
			}
		}
		for (String wKey : removeKeys) {
			needClosePool.remove(wKey);
		}
		LOG.debug("needClosePool.size:{}", needClosePool.size());
	}
	
	/**
	 * 用于数据库配置同步到IMPALA，写完文件立即关闭
	 * @param wKey
	 */
	public synchronized static void checkClose(String wKey) {	
		closeWriter(wKey);
		w2FilesPool.remove(wKey);
		needClosePool.remove(wKey);	
		LOG.debug("needClosePool.size:{}", needClosePool.size());
	}

	/**
	 * 关闭Writer对象，并从pool中移除（私有方法。如果要改为共有，需处理线程安全）
	 * 
	 * @param wKey Writer的唯一标识
	 */
	private static void closeWriter(String wKey) {
		ParquetRecordWriter w = wPool.remove(wKey);
		if (null != w) {
			try {
				LOG.debug("close wKey:{}", wKey);
				w.close();
			} catch (Exception e) {
				LOG.error("Close failed.wKey:{};msg:{};cause:{}", new Object[] { wKey,
						e.getMessage(), e.getCause() });
			}
		}else{
			LOG.warn("Closed more than once.wKey:{}", wKey);
		}
	}
	
	/**
	 * 强制刷新缓存到磁盘。(如果厂家都是几m的小文件，会非常恐怖)
	 * 应用场景：文件关闭时调用
	 * @param taskfn 文件唯一标识
	 * @param tblName 表名
	 */
	@Deprecated
	public static void flushWirter(String taskfn, String tblName){
		LOG.debug("wKey.flushWirter taskfn:{},tblName:{}", taskfn, tblName);
		for (String wKey : w2FilesPool.keySet()) {
			Set<String> taskfns = w2FilesPool.get(wKey);
			if (taskfns.contains(taskfn)&&wKey.contains(tblName)) {
				try {
					LOG.debug("flush wKey:{}", wKey);
					wPool.get(wKey).flush();
				} catch (Exception e) {
					LOG.error("flush failed.wKey:{};msg:{};cause:{}", new Object[] { wKey,
							e.getMessage(), e.getCause() });
				}
			}
		}
	}
	
	public synchronized static String getWKeys(){
		StringBuilder sb = new StringBuilder();
		for (String wKey : wPool.keySet()) {
			sb.append(wKey).append("\r\n");
		}
		return sb.toString();
	}
	
	public synchronized static String getW2Files(){
		StringBuilder sb = new StringBuilder();
		for (String wKey : w2FilesPool.keySet()) {
			Set<String> taskfns = w2FilesPool.get(wKey);
			sb.append(wKey).append(":");
			for (String taskfn : taskfns) {
				sb.append(taskfn).append(";");
			}
			sb.append("\r\n");
		}
		return sb.toString();
	}
	
	public synchronized static String getNeedClosePool(){
		StringBuilder sb = new StringBuilder();
		for (String wKey : needClosePool.keySet()) {
			sb.append(wKey).append("\r\n");
		}
		return sb.toString();
	}
	
	public synchronized static String getWriterStatistic(String wKey){
		ParquetRecordWriter w = wPool.get(wKey);
		if(null != w){
			return w.getStatistic();
		}
		return "none";
	}

}
