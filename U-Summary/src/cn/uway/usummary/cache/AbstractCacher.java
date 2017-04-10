package cn.uway.usummary.cache;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.uway.usummary.context.AppContext;
import cn.uway.usummary.entity.ElementKeyProducer;

/**
 * @author chenrongqiang @ 2013-2-4
 */
public abstract class AbstractCacher implements Cacher {

	/**
	 * 存储器操作锁
	 */
	protected ReentrantLock lock = new ReentrantLock();

	/**
	 * 缓存中是否有Element存入
	 */
	protected Condition noElement = lock.newCondition();

	/**
	 * 提交标志
	 */
	protected boolean commitFlag = false;

	/**
	 * 常量 表示位置在内存中
	 */
	protected static final byte LOCATION_MEMOERY = 0;

	/**
	 * 常量 表示位置在寄存器内存中
	 */
	protected static final byte LOCATION_REGISTER = 1;

	/**
	 * 常量 表示位置在本地磁盘文件中
	 */
	protected static final byte LOCATION_DISK = 2;

	/**
	 * 系统内部维持的一个保证Element key唯一的自动生成器
	 */
	protected ElementKeyProducer keyProducer = new ElementKeyProducer();

	/**
	 * 缓存的大小
	 */
	protected int size = 0;

	/**
	 * 已经取出的记录数
	 */
	protected int exportNum = 0;

	/**
	 * 内存中保存的最大记录条数
	 */
	protected static final int MAX_QUEUE_SIZE = AppContext.getBean("elementsInMemery", java.lang.Integer.class);

	// 日志
	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractCacher.class);

	@Override
	public void commit() {
		lock.lock();
		try {
			this.commitFlag = true;
			this.noElement.signalAll();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isCommit() {
		lock.lock();
		try {
			return this.commitFlag;
		} finally {
			lock.unlock();
		}
	}
}
