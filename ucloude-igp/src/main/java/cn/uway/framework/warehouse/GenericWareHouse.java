package cn.uway.framework.warehouse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.log.ImportantLogger;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.warehouse.exporter.ExporterArgs;
import cn.uway.framework.warehouse.repository.BufferedMultiExportRepository;
import cn.uway.framework.warehouse.repository.Repository;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

/**
 * GenericWareHouse 仓库的使用流程:<br>
 * 1、调用isWarehouseReady方法判断当前数据仓库是否有足够的空间 <br>
 * 2、调用applyNotice通知申请到仓库<br>
 * 3、调用shutdownNotice通知已经使用仓库完毕 <br>
 * 
 * @author chenrongqiang 2012-10-31
 * @version 1.0
 * @since 3.0
 */
public class GenericWareHouse implements WareHouse {

	private static final ILogger LOGGER = LoggerManager.getLogger(GenericWareHouse.class); // 日志

	// 单例实现 保证整个程序上下文只有一个数据仓库对象
	private static final GenericWareHouse warehouse = new GenericWareHouse();

	// 单个任务最多可以申请仓库限制
	private static final int MAX_TASK_PREPOSITORY = AppContext.getBean("maxWorkingTaskRepository", Integer.class);

	// 仓库缓存队列，使用ConcurrentHashMap保证并发
	private Map<Long, Repository> workingRepositories = new ConcurrentHashMap<Long, Repository>();

	// 整个程序可以申请的数据仓库实例限制
	private static final int maxWorkingRepository = AppContext.getBean("maxWorkingRepository", Integer.class);;

	private int currentWorkingRepositories = 0;

	// 仓库容量管理
	protected Map<Long, Integer> workingRepositoryNum = new HashMap<Long, Integer>();

	protected Object warehouseLock = new Object();

	private GenericWareHouse() {
	}

	public static GenericWareHouse getInstance() {
		return warehouse;
	}

	/**
	 * 判断是否还有仓库空间使用
	 */
	public synchronized boolean isWarehouseReady(Long taskId) {
		Integer currentNum = getTaskWorkingNum(taskId);
		// 如果当前任务还未申请过仓库 则为空
		if (currentNum >= MAX_TASK_PREPOSITORY) {
			return false;
		}
		if (currentWorkingRepositories >= maxWorkingRepository) {
			LOGGER.debug("当前程序已使用{}个warehouse，超过程序配置限制{}", new Object[]{currentWorkingRepositories, maxWorkingRepository});
			return false;
		}
		return true;
	}

	/**
	 * 通知warehouse 当前任务已经申请一个仓库
	 * 
	 * @param taskId
	 */
	public synchronized void applyNotice(Long taskId) {
		Integer currentNum = getTaskWorkingNum(taskId);
		if (currentNum >= MAX_TASK_PREPOSITORY) {
			ImportantLogger.getLogger().error("任务ID={}目前使用warehouse已经达到最大限制，仍然在申请仓库。请检查程序。", taskId);
		}
		currentNum++;
		currentWorkingRepositories++;
		workingRepositoryNum.put(taskId, currentNum);
		LOGGER.debug("任务ID={}目前warehouse实例数={}。warehouse总共运行实例数={}", new Object[]{taskId, currentNum, currentWorkingRepositories});
	}

	/**
	 * 通知warehouse 当前任务已经关闭一个仓库
	 */
	public synchronized void shutdownNotice(Long taskId) {
		Integer currentNum = getTaskWorkingNum(taskId);
		if (currentNum == 0) {
			ImportantLogger.getLogger().error("任务ID={}通知关闭warehouse异常，可能未在使用时通知。", taskId);
			return;
		}
		currentNum--;
		currentWorkingRepositories--;
		workingRepositoryNum.put(taskId, currentNum);
		LOGGER.debug("任务ID={}目前warehouse实例数={}。warehouse总共运行实例数={}", new Object[]{taskId, currentNum, currentWorkingRepositories});
	}

	private Integer getTaskWorkingNum(Long taskId) {
		Integer currentNum = workingRepositoryNum.get(taskId);
		return currentNum == null ? 0 : currentNum;
	}

	/**
	 * 申请warehouse仓库空间
	 */
	public long apply(ExporterArgs exporterArgs) {
		// TODO 判断传入进来的参数，如果之前申请过就返回之前申请的仓库编号
		Repository workingRepository = new BufferedMultiExportRepository(exporterArgs);
		long reposId = workingRepository.getReposId();
		// 创建新的仓库并且加入到仓库队列中时,加锁保证数据安全
		workingRepositories.put(reposId, workingRepository);
		LOGGER.debug("申请仓库结束,当前仓库容量={}", workingRepositories.size());
		return reposId;
	}

	/**
	 * 关闭数据仓库 同时将仓库从队列中移除
	 */
	public void close(long reposId) {
		if (!workingRepositories.containsKey(reposId))
			return;
		workingRepositories.remove(reposId);
	}

	/**
	 * 调用commit方法表示数据提交完成
	 */
	public void commit(long reposId, boolean exceptionFlag) {
		if (workingRepositories.containsKey(reposId)) {
			workingRepositories.get(reposId).commit(exceptionFlag);
		}
	}

	@Override
	public WarehouseReport getReport(long reposId) {
		if (workingRepositories.containsKey(reposId))
			return workingRepositories.get(reposId).getReport();
		return new WarehouseReport();
	}

	/**
	 * 数据回滚
	 */
	public void rollBack(long reposId) {
		throw new UnsupportedOperationException("此版本不支持.");
	}

	/**
	 * 关闭仓库 暂不实现
	 */
	public void shutdown() {
		// TODO 暂不实现
	}

	public int transport(long reposId, ParseOutRecord outRecords) {
		return workingRepositories.get(reposId).transport(outRecords);
	}
}
