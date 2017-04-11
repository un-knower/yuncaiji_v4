package cn.uway.framework.warehouse.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import cn.uway.framework.context.AppContext;
import cn.uway.framework.parser.ParseOutRecord;
import cn.uway.framework.task.Task;
import cn.uway.framework.warehouse.WarehouseReport;
import cn.uway.framework.warehouse.WarehouseReport.TableReport;
import cn.uway.framework.warehouse.exporter.AbstractExporter;
import cn.uway.framework.warehouse.exporter.BlockData;
import cn.uway.framework.warehouse.exporter.BlockDataHelper;
import cn.uway.framework.warehouse.exporter.Exporter;
import cn.uway.framework.warehouse.exporter.ExporterArgs;
import cn.uway.framework.warehouse.exporter.ExporterGroupDispatcher;
import cn.uway.framework.warehouse.exporter.ExporterLauncher;
import cn.uway.framework.warehouse.exporter.template.ExportTemplateBean;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.ThreadUtil;

/**
 * 带缓存机制的多路数据分发仓库
 * 
 * @author chenrongqiang 2012-11-2
 * @version 1.0
 * @since 3.0
 */
public class BufferedMultiExportRepository implements Repository {

	private long id; // 仓库ID

	// 仓库暂存数据块大小
	private static final int REGITSTER_SIZE = AppContext.getBean("tempRegisterSize", Integer.class);

	private Map<Integer, List<ParseOutRecord>> tempRegisters = new HashMap<Integer, List<ParseOutRecord>>();

	private List<Exporter> exports;
	private List<ExporterGroupDispatcher> groupExporterDispatchers;

	private WarehouseReport warehouseReport;

	private Map<Integer, Integer> distributeNum = new HashMap<Integer, Integer>();

	private long total = 0L;

	private long succ = 0L;

	private long fail = 0L;

	private String cause;

	private ExporterLauncher exporterLaucher;

	private DistributeThread distributeThread = null;

	private Task task;

	// BlockData数据队列 用于缓存BlockData 用于解决解码和warehouse模块的速度差异
	private List<BlockData> dataQueue = new LinkedList<BlockData>();

	// BlockData对应的参数
	private BlockDataHelper blockDataHelper = new BlockDataHelper(this);

	private int maxNum = 30;

	// BufferedMultiExportRepository和异步输出线程 锁
	private ReentrantLock lock = new ReentrantLock();

	// BufferedMultiExportRepository和异步输出线程 锁condition 如果dataQueue为空 则异步输出线程挂起
	private Condition empty = lock.newCondition();

	private Condition full = lock.newCondition();

	// 数据提交标记
	private volatile boolean commitFlag = false;

	// 已经发生错误的exporter列表 如果已经发生错误 则从输出列表中清楚
	private Map<Integer, Exporter> errorExporters = new HashMap<Integer, Exporter>();

	/**
	 * 异步输出线程是否结束标记
	 */
	private volatile boolean distributedFlag = false;

	// 日志
	private static final ILogger LOGGER = LoggerManager.getLogger(BufferedMultiExportRepository.class);

	public BufferedMultiExportRepository(ExporterArgs exporterArgs) {
		super();
		exporterArgs.setRepository(this);
		this.id = RepositoryIDGenerator.generatId();
		this.task = exporterArgs.getTask();
		this.warehouseReport = new WarehouseReport();
		warehouseReport.setStartTime(new Date());
		initRegisters(exporterArgs);
		exporterLaucher = new ExporterLauncher(exporterArgs);
		
		// 启动输出线程
		exporterLaucher.start();
		
		this.exports = exporterLaucher.getExporters();
		this.groupExporterDispatchers = exporterLaucher.getGroupExporterDispatchers();
	}

	private void initRegisters(ExporterArgs exporterArgs) {
		List<ExportTemplateBean> exportTempletBeans = exporterArgs.getExportTempletBeans();
		Set<Integer> types = new HashSet<Integer>();
		getDataTypes(types, exportTempletBeans);
		for (Integer type : types) {
			tempRegisters.put(type, new ArrayList<ParseOutRecord>(REGITSTER_SIZE));
			distributeNum.put(type, 0);
		}
	}

	private void getDataTypes(Set<Integer> hashSet, List<ExportTemplateBean> exportTempletBean) {
		for (ExportTemplateBean templetBean : exportTempletBean) {
			hashSet.add(templetBean.getDataType());
		}
	}

	@Override
	public int transport(ParseOutRecord[] outRecords) {
		// TODO 批量插入暂时不实现
		return 0;
	}

	/**
	 * Repository获取一条解码数据方法
	 */
	public int transport(ParseOutRecord outRecords) {
		this.total++;
		int dataType = outRecords.getType();
		List<ParseOutRecord> dataList = tempRegisters.get(dataType);
		// 如果dataList没有进行初始化 则先初始化
		if (dataList == null) {
			dataList = new ArrayList<ParseOutRecord>(REGITSTER_SIZE);
			dataList.add(outRecords);
			tempRegisters.put(dataType, dataList);
			return 1;
		}
		int size = dataList.size();
		// 如果没有达到最大的限制 则直接放入到dataList中
		if (size < REGITSTER_SIZE) {
			dataList.add(outRecords);
			return 1;
		}
		// 如果dataList已经满了 则将BlockData添加至传输队列中 ，然后再重新创建一个缓存放入数据
		BlockData blockData = new BlockData(tempRegisters.get(dataType), dataType);
		addQueue(blockData);
		dataList = new ArrayList<ParseOutRecord>(REGITSTER_SIZE);
		dataList.add(outRecords);
		tempRegisters.put(dataType, dataList);
		return 1;
	}

	/**
	 * 将BlockData添加至传输队列中
	 * 
	 * @param blockData
	 */
	void addQueue(BlockData blockData) {
		// 加入到队列时，先调用processOnbeforAddToCacher作一些内存dump清理动作，节省内存
		blockData.processOnbeforeAddToCacher();

		// 分发线程延迟启动 避免未解码出数据 线程启动后不关闭
		// 1、即在解码线程解出第一个BlockData启动
		// 2、解码条数不足一个BlockData时则在commit时启动
		if (this.distributeThread == null) {
			distributeThread = new DistributeThread();
			distributeThread.start();
		}
		try {
			lock.lockInterruptibly();
			while (dataQueue.size() == this.maxNum)
				full.await();
			this.dataQueue.add(blockData);
			empty.signalAll();
		} catch (InterruptedException e) {
			full.signalAll();
			LOGGER.error("解码线程获取Lock异常", e);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 将写满的暂存器中数据分发至Exporter
	 */
	private void distribute(BlockData blockData) {
		if (this.groupExporterDispatchers == null || groupExporterDispatchers.size() < 1) {
			distributeDirect(blockData);
		} else {
			distributeGroupDispatcher(blockData);
		}
	}
	
	private void distributeDirect(BlockData blockData) {
		int dataType = blockData.getType();
		int blockDataNum = blockData.getData().size();
		if (exports.size() == 0 || exports.size() == errorExporters.size()) {
			LOGGER.warn("任务ID={},仓库ID={}没有开启输出线程或输出线程已全部失败.不在继续分发。", new Object[]{task.getId(), this.id});
			return;
		}
		int exporterId = -1;
		AbstractExporter exporter = null;
		Iterator<Exporter> iterator = exports.iterator();
		while (iterator.hasNext()) {
			try {
				exporter = (AbstractExporter)iterator.next();
				exporterId = exporter.getExportId();
				// 如果已经发生过错误的exporter则不用再往里对应的缓存中写入BlockData
				//if (errorExporters.containsKey(exporterId))
				if (exporter.breakProcessFlag)
					continue;
				// 1)如果dataType相同，则输出；
				// 2)如果不同，则判断是否汇总文件输出，如果是，并且包含dataType，则输出
				if (dataType == exporter.getType()) {
					exporter.getCacher().addElement(blockData);
				}
			} catch (Exception e) {
				// 如果已经发生过错误.则将当前Exporter加入到错误map中.在输入的时候不在写入
				LOGGER.warn("任务ID={},仓库ID={},输出模版ID={}输出器发生异常已关闭", new Object[]{task.getId(), this.id, exporterId}, e);
				errorExporters.put(exporterId, exporter);
				
				/**
				 * <pre>
				 * change:shig date:2014-5-07
				 * explain: 如果向export.cacher调用addElement出现异常，此处需要通知export线程终止工作；
				 * 			否则，因为exporterId已加入到了errorExporters中，但export线程仍在工作，
				 * 			在解码commit完成后，不会通知退出，导到ExporterLaucher，永久性等待而死锁；
				 * </pre>
				 */
				exporter.breakProcess(e.getMessage());
			}
		}
		// 更新已分发条数
		Integer distributedNum = distributeNum.get(dataType);
		if (distributedNum == null)
			distributedNum = 0;
		distributedNum += blockDataNum;
		distributeNum.put(dataType, distributedNum);
		this.succ += blockDataNum;
	}
	
	private void distributeGroupDispatcher(BlockData blockData) {
		int dataType = blockData.getType();
		int blockDataNum = blockData.getData().size();
		if (groupExporterDispatchers.size() == 0 || groupExporterDispatchers.size() == errorExporters.size()) {
			LOGGER.warn("任务ID={},仓库ID={}没有开启输出线程或输出线程已全部失败.不在继续分发。", new Object[]{task.getId(), this.id});
			return;
		}
		int exporterId = -1;
		ExporterGroupDispatcher groupDispatcher = null;
		Iterator<ExporterGroupDispatcher> iterator = this.groupExporterDispatchers.iterator();
		while (iterator.hasNext()) {
			try {
				groupDispatcher = iterator.next();
				exporterId = groupDispatcher.getExportId();
				// 如果已经发生过错误的exporter则不用再往里对应的缓存中写入BlockData
				//if (errorExporters.containsKey(exporterId))
				if (groupDispatcher.breakProcessFlag)
					continue;
				
				// 1)如果dataType相同，则输出；
				// 2)如果不同，则判断是否汇总文件输出，如果是，并且包含dataType，则输出
				if (groupDispatcher.isIncludeDataType(dataType)) {
					groupDispatcher.getCacher().addElement(blockData);
				}
			} catch (Exception e) {
				// 如果已经发生过错误.则将当前Exporter加入到错误map中.在输入的时候不在写入
				LOGGER.warn("任务ID={},仓库ID={},输出器分组ID={}输出器发生异常已关闭", new Object[]{task.getId(), this.id, exporterId}, e);
				errorExporters.put(exporterId, groupDispatcher);
				
				/**
				 * <pre>
				 * change:shig date:2014-5-07
				 * explain: 如果向export.cacher调用addElement出现异常，此处需要通知export线程终止工作；
				 * 			否则，因为exporterId已加入到了errorExporters中，但export线程仍在工作，
				 * 			在解码commit完成后，不会通知退出，导到ExporterLaucher，永久性等待而死锁；
				 * </pre>
				 */
				groupDispatcher.breakProcess(e.getMessage());
			}
		}
		// 更新已分发条数
		Integer distributedNum = distributeNum.get(dataType);
		if (distributedNum == null)
			distributedNum = 0;
		distributedNum += blockDataNum;
		distributeNum.put(dataType, distributedNum);
		this.succ += blockDataNum;
	}

	/**
	 * 获取指定数据类型，所需要用到的字段属性
	 * 
	 * @param dataType
	 *            数据类型
	 * @return
	 */
	public Set<String> getExportUsesPropertys(int dataType) {
		Set<String> propertysSet = new HashSet<String>();
		if (exports.size() == 0) {
			LOGGER.warn("任务ID={},仓库ID={}没有开启输出线程或输出线程已全部失败.查找export使用的key失败。", new Object[]{task.getId(), this.id});
			return propertysSet;
		}

		Exporter exporter = null;
		Iterator<Exporter> iterator = exports.iterator();
		while (iterator.hasNext()) {
			exporter = iterator.next();
			
			if (dataType == exporter.getType()) {
				exporter.buildExportPropertysList(propertysSet);
			}
		}

		return propertysSet;
	}

	/**
	 * 数据写入完成 如果暂存器中还有数据 继续分发至Exporter<br>
	 * 注意：设置commitFlag=true和addQueue有先后顺序，先addQueue再commit,
	 * 保证最后一条blockData能够被分发线程获取到
	 */
	public void commit(boolean exceptionFlag) {
		// 通知输出线程解析发生异常,则输出线程不会将export_status修改为1
		if (exceptionFlag && exporterLaucher != null)
			exporterLaucher.notifyException();
		Set<Integer> types = tempRegisters.keySet();
		for (Integer type : types) {
			List<ParseOutRecord> dataList = tempRegisters.get(type);
			int size = dataList.size();
			if (size == 0)
				continue;
			if (exceptionFlag)
				continue;
			// 如果有数据 并且未发生异常 则将剩下的数据放入缓存用于输出
			BlockData blockData = new BlockData(tempRegisters.get(type), type);
			addQueue(blockData);
		}
		warehouseReport.setEndTime(new Date());
		// 不管是否有异常 都要调用commit操作和输出线程的commit
		this.commitFlag = true;
		// 设置commitFlag为true后 需要等待异步输出线程结束后
		/**
		 *  add:shig		date:2014-5-26 	
		 *  explain:
		 *  <pre>
		 *  	加上"this.distributeThread != null， 是为了
		 *  	防止当解码一个连50行都没有，就出错的文件时distributeThread线程还未启动，
		 *  	那么在此处，就不能傻等，一直挂死在这儿。
		 *  	ps.	distributeThread启动，目前代码是在addQueue中启动的，
		 *  		但当exceptionFlg为true时，就continue了。
		 *  </pre>
		 */
		while (!distributedFlag && this.distributeThread != null)
			ThreadUtil.sleep(500);
		
		if (this.groupExporterDispatchers != null) {
			for (Exporter groupDispatcher : this.groupExporterDispatchers) {
				// 只需要关闭没有报错的Exporter。报错的Exporter自动会关闭
				if (!errorExporters.containsKey(groupDispatcher.getExportId()))
					groupDispatcher.getCacher().commit();
			}			
		} else {
			for (Exporter export : this.exports) {
				// 只需要关闭没有报错的Exporter。报错的Exporter自动会关闭
				if (!errorExporters.containsKey(export.getExportId()))
					export.getCacher().commit();
			}
		}
	}

	@Override
	public long getReposId() {
		return id;
	}

	/**
	 * 数据回滚方法 目前暂不实现
	 */
	public void rollBack() {
		throw new UnsupportedOperationException("此版本不支持.");
	}

	public WarehouseReport getReport() {
		warehouseReport.setTotal(total);
		warehouseReport.setSucc(succ);
		warehouseReport.setFail(fail);
		warehouseReport.setCause(cause);
		warehouseReport.setDistributedNum(getDistributeNum());
		// 20151105 add by tyler.lee for TableReport begin
		Map<String, TableReport> tableInfo =new HashMap<String, TableReport>();
		for (Exporter export : this.exports) {
			AbstractExporter ae =(AbstractExporter)export;
			TableReport tr = new TableReport();
			tr.setDataType(ae.getType());
			tr.setTableName(ae.getDest());
			tr.setCause(ae.getCause());
			tr.setStartTime(ae.getStartTime());
			tr.setEndTime(ae.getEndTime());
			tr.setFail(ae.getFail());
			tr.setSucc(ae.getSucc());
			tr.setTotal(ae.getTotal());
			tableInfo.put(ae.getDest(), tr);
		}
		warehouseReport.setTableInfo(tableInfo);
		// 20151105 add by tyler.lee for TableReport end
		return warehouseReport;
	}

	/**
	 * 获取已经输出的条数
	 * 
	 * @return 已经分发至warehouse的条数
	 */
	protected String getDistributeNum() {
		if (distributeNum.isEmpty())
			return null;
		StringBuilder distributedNum = new StringBuilder();
		Set<Integer> keys = distributeNum.keySet();
		for (Integer num : keys) {
			distributedNum.append(num).append(":").append(distributeNum.get(num)).append(";");
		}
		return distributedNum.toString();
	}

	/**
	 * 异步分发线程
	 * 
	 * @author chenrongqiang @ 2013-4-14
	 */
	class DistributeThread extends Thread {

		public void run() {
			LOGGER.debug("TaskId={},BufferedMultiExportRepository异步分发线程启动。", task.getId());
			int distributedNum = 0;
			// 如果没有提交或者临时队列中仍然有数据 则线程一直运行
			BlockData blockData = null;
			while (true) {
				try {
					lock.lockInterruptibly();
					while (dataQueue.isEmpty() && !commitFlag) {
						empty.awaitNanos(100000000L);
					}
					if (!dataQueue.isEmpty()) {
						blockData = dataQueue.remove(0);
						full.signal();
					}
				} catch (InterruptedException e) {
					LOGGER.error("TaskId={},BufferedMultiExportRepository异步分发异常。", task.getId(), e);
				} finally {
					lock.unlock();
				}
				/**
				 * <pre>
				 * 修改说明：
				 * 	这个地方要检测一下 blockData是否为null，
				 * 	否则commitFlag=true且dataQueue.isEmpty()时将会出错.
				 * 	distribute完后，blockData要置为null，否则按原先模式会造成最后一个blockData重复入库(1-N次)
				 * </pre>
				 */
				if (blockData != null) {
					distribute(blockData);
					distributedNum++;
					blockData = null;
				}
				if (commitFlag && dataQueue.isEmpty())
					break;
			}
			// 将distributedFlag标记置为true，保证在输出完成后才commit 缓存
			distributedFlag = true;
			LOGGER.debug("TaskId={},BufferedMultiExportRepository异步分发线程完成,共分发数据块{}", new Object[]{task.getId(), distributedNum});
		}
	}

	public BlockDataHelper getBlockDataHelper() {
		return blockDataHelper;
	}

	public void setBlockDataHelper(BlockDataHelper blockDataExternalParam) {
		this.blockDataHelper = blockDataExternalParam;
	}
}
