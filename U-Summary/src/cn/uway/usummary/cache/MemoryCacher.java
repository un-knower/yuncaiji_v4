package cn.uway.usummary.cache;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import cn.uway.usummary.context.AppContext;
import cn.uway.usummary.entity.Element;
import cn.uway.usummary.entity.ElementIndex;
import cn.uway.usummary.entity.ElementKeyProducer;

/**
 * 内存存储器<br>
 * 使用LinkedList保证存入和取出顺序 getNextElement(String keyName)实际上并不关心keyName
 * 
 */
public class MemoryCacher extends AbstractCacher {

	private ElementKeyProducer keyProducer = new ElementKeyProducer();

	protected List<Element> queue = new LinkedList<Element>();

	protected static final int MAX_QUEUE_SIZE = AppContext.getBean("elementsInMemery", java.lang.Integer.class);

	/**
	 * 存储器操作锁
	 */
	protected ReentrantLock lock = new ReentrantLock();

	/**
	 * 缓存中是否有Element存入
	 */
	Condition noElement = lock.newCondition();

	/**
	 * 缓存是否已满
	 */
	Condition queueFull = lock.newCondition();

	/**
	 * 暂不支持
	 */
	public Element getElement(ElementIndex elementIndex) {
		throw new UnsupportedOperationException("内存cacher不支持通过elementIndex获取Element");
	}

	public Element getNextElement() {
		lock.lock();
		try {
			while (queue.isEmpty()) {
				if (this.commitFlag && this.exportNum >= size)
					return null;
				this.noElement.awaitNanos(100000000L);
			}
			Element element = queue.remove(0);
			if (element != null) {
				this.queueFull.signal();
				exportNum++;
			}
			return element;
		} catch (InterruptedException e) {
			this.noElement.signal();
			return null;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void addElement(Element element) {
		lock.lock();
		try {
			if (element == null)
				throw new NullPointerException("MemoryCacher.addElement() 传入参数Element为空.");
			while (queue.size() >= MAX_QUEUE_SIZE)
				this.queueFull.await();
			boolean addFlag = queue.add(element);
			if (addFlag) {
				size++;
				this.noElement.signalAll();
			}
		} catch (InterruptedException e) {
			queueFull.signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void addElement(Object elementValue) {
		lock.lock();
		try {
			if (elementValue == null)
				throw new NullPointerException("MemoryCacher.addElement() 传入参数Object为空.");
			Element element = new Element(this.keyProducer.getNextElementKey(), elementValue);
			addElement(element);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void shutdown() {
		queue.clear();
		queue = null;
	}

	@Override
	public int size() {
		lock.lock();
		try {
			return size;
		} finally {
			lock.unlock();
		}
	}

}
