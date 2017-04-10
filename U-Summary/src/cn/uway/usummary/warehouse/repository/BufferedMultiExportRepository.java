package cn.uway.usummary.warehouse.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.context.AppContext;
import cn.uway.usummary.entity.BlockData;
import cn.uway.usummary.entity.ExporterArgs;
import cn.uway.usummary.entity.WarehouseReport;
import cn.uway.usummary.export.AbstractExporter;
import cn.uway.usummary.warehouse.exporter.ExporterLauncher;
import jodd.util.ThreadUtil;


public class BufferedMultiExportRepository implements Repository {
	
	private long id; // 仓库ID

	// 仓库暂存数据块大小
	private static final int REGITSTER_SIZE = AppContext.getBean("tempRegisterSize", Integer.class);

	private List<Map<String,String>> tempRegisters = new ArrayList<Map<String,String>>(REGITSTER_SIZE);

	private AbstractExporter exporter;
	
	private AbstractExporter errorExporter;

	private WarehouseReport warehouseReport;

	private long total = 0L;

	private long succ = 0L;

	private long fail = 0L;
	
	private int errCode = 1;

	private String cause;

	private ExporterLauncher exporterLaucher;

	private DistributeThread distributeThread = null;

	// BlockData数据队列 用于缓存BlockData 用于解决解码和warehouse模块的速度差异
	private List<BlockData> dataQueue = new LinkedList<BlockData>();

	private int maxNum = 30;

	// BufferedMultiExportRepository和异步输出线程 锁
	private ReentrantLock lock = new ReentrantLock();

	// BufferedMultiExportRepository和异步输出线程 锁condition 如果dataQueue为空 则异步输出线程挂起
	private Condition empty = lock.newCondition();

	private Condition full = lock.newCondition();

	// 数据提交标记
	private volatile boolean commitFlag = false;
	
	private long sqlNum;

	/**
	 * 异步输出线程是否结束标记
	 */
	private volatile boolean distributedFlag = false;

	// 日志
	private static final Logger LOGGER = LoggerFactory.getLogger(BufferedMultiExportRepository.class);

	public BufferedMultiExportRepository(ExporterArgs exporterArgs) {
		super();
		exporterArgs.setRepository(this);
		this.sqlNum = exporterArgs.getSqlNum();
		this.id = RepositoryIDGenerator.generatId();
		this.warehouseReport = new WarehouseReport();
		warehouseReport.setStartTime(new Date());
		exporterLaucher = new ExporterLauncher(exporterArgs);
		
		// 启动输出线程
		exporterLaucher.start();
		
		this.exporter = exporterLaucher.getExporter();
	}

	/**
	 * Repository获取一条解码数据方法
	 */
	public int transport(Map<String,String> record) {
		this.total++;
		// 如果tempRegisters没有进行初始化 则先初始化
		if (tempRegisters == null) {
			tempRegisters = new ArrayList<Map<String,String>>(REGITSTER_SIZE);
			tempRegisters.add(record);
			return 1;
		}
		int size = tempRegisters.size();
		// 如果没有达到最大的限制 则直接放入到dataList中
		if (size < REGITSTER_SIZE) {
			tempRegisters.add(record);
			return 1;
		}
		// 如果dataList已经满了 则将BlockData添加至传输队列中 ，然后再重新创建一个缓存放入数据
		BlockData blockData = new BlockData(tempRegisters);
		addQueue(blockData);
		tempRegisters = new ArrayList<Map<String,String>>(REGITSTER_SIZE);
		tempRegisters.add(record);
		return 1;
	}

	/**
	 * 将BlockData添加至传输队列中
	 * 
	 * @param blockData
	 */
	void addQueue(BlockData blockData) {

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
			this.errCode = 0;
			this.cause = "解码线程获取Lock异常!";
			LOGGER.error("解码线程获取Lock异常", e);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 将写满的暂存器中数据分发至Exporter
	 */
	private void distribute(BlockData blockData) {
		int blockDataNum = blockData.getData().size();
		if (exporter == null || errorExporter != null) {
			LOGGER.warn("SQL编号={},仓库ID={}没有开启输出线程或输出线程已全部失败.不在继续分发。", new Object[]{this.sqlNum, this.id});
			this.fail += blockDataNum;
			return;
		}
		try {
			if (exporter.breakProcessFlag){
				this.fail += blockDataNum;
				return;
			}
			exporter.getCacher().addElement(blockData);
		} catch (Exception e) {
			LOGGER.error("仓库ID={},SQL编号={}输出器发生异常已关闭", new Object[]{this.id, this.sqlNum});
			LOGGER.error("失败原因：", e);
			errorExporter = exporter;
			this.errCode = 0;
			this.cause = "向缓存加入数据失败!";
			this.fail += blockDataNum;
			/**
			 * <pre>
			 * change:shig date:2014-5-07
			 * explain: 如果向export.cacher调用addElement出现异常，此处需要通知export线程终止工作；
			 * 			否则，因为exporterId已加入到了errorExporters中，但export线程仍在工作，
			 * 			在解码commit完成后，不会通知退出，导到ExporterLaucher，永久性等待而死锁；
			 * </pre>
			 */
			exporter.breakProcess(e.getMessage());
			return;
		}
		this.succ += blockDataNum;
	}

	/**
	 * 数据写入完成 如果暂存器中还有数据 继续分发至Exporter<br>
	 * 注意：设置commitFlag=true和addQueue有先后顺序，先addQueue再commit,
	 * 保证最后一条blockData能够被分发线程获取到
	 */
	public void commit(boolean exceptionFlag) {
		if(tempRegisters.size() > 0 && !exceptionFlag){
			// 如果有数据 并且未发生异常 则将剩下的数据放入缓存用于输出
			BlockData blockData = new BlockData(tempRegisters);
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

		exporter.getCacher().commit();
	}

	public long getReposId() {
		return id;
	}

	public WarehouseReport getReport() {
		warehouseReport.setTotal(total);
		warehouseReport.setSucc(succ);
		warehouseReport.setFail(fail);
		warehouseReport.setErrCode(errCode);
		warehouseReport.setCause(cause);
		warehouseReport.setDistributedNum(getDistributeNum());
		while(!exporterLaucher.isFinish()){
				ThreadUtil.sleep(500);
		}
		if(exporterLaucher.getErrCode() == 0){
			warehouseReport.setErrCode(exporterLaucher.getErrCode());
			warehouseReport.setCause(exporterLaucher.getCause());
		}
		return warehouseReport;
	}

	/**
	 * 获取已经输出的条数
	 * 
	 * @return 已经分发至warehouse的条数
	 */
	protected String getDistributeNum() {
		StringBuilder distributedNum = new StringBuilder();
		distributedNum.append(this.sqlNum).append(":").append(this.succ);
		return distributedNum.toString();
	}

	/**
	 * 异步分发线程
	 * 
	 * @author chenrongqiang @ 2013-4-14
	 */
	class DistributeThread extends Thread {

		public void run() {
			LOGGER.debug("SQL编号={},BufferedMultiExportRepository异步分发线程启动。", sqlNum);
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
					LOGGER.error("SQL编号={},BufferedMultiExportRepository异步分发异常。", sqlNum, e);
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
			LOGGER.debug("SQL编号={},BufferedMultiExportRepository异步分发线程完成,共分发数据块{}", new Object[]{sqlNum, distributedNum});
		}
	}

}
