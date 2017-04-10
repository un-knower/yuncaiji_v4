package cn.uway.usummary.cache.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import cn.uway.usummary.cache.AbstractCacher;
import cn.uway.usummary.cache.store.Store;
import cn.uway.usummary.cache.store.impl.MemoryLocalDiskStore;
import cn.uway.usummary.cache.store.impl.MemoryMappintDiskStore;
import cn.uway.usummary.context.AppContext;
import cn.uway.usummary.entity.Element;
import cn.uway.usummary.entity.ElementIndex;
import cn.uway.usummary.warehouse.repository.Repository;

public class BlockingCacher extends AbstractCacher {

	protected List<ElementIndex> indexs = new LinkedList<ElementIndex>();

	/**
	 * 临时内存存储器使用BlockingQueue实现数据写入
	 */
	protected List<Element> queue = new LinkedList<Element>();

	/**
	 * 本地磁盘文件存储器列表 用于在cache关闭的时候全部关闭
	 */
	protected List<Store> diskStores = new LinkedList<Store>();

	protected int storeNums = 0;

	/**
	 * 缓存文件名字 如果内存不够用时会创建缓存文件
	 */
	protected String cacheFileName;
	
	//数据仓库
	protected Repository repository;
	
	//虚拟内存（映射文件)单个parser最在创建个数
	protected int MAX_MAPPING_STORE_NUM = 2;
	
	// mappingFileCount由config.ini中配置(路径：system.cache.mappingFileCoun)，在framework.xml中以bean name:"mappingFileCount"导入 
	protected volatile int mappingFileCount = 0;

	// 日志
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(BlockingCacher.class);

	public BlockingCacher(String cacheFileName, Repository repository) {
		this.cacheFileName = cacheFileName;
		this.repository = repository;
		
		String mappingFileCount = AppContext.getBean("mappingFileCount", java.lang.String.class);
		if (mappingFileCount != null && mappingFileCount.length() > 0 && mappingFileCount.indexOf('$') < 0) {
			MAX_MAPPING_STORE_NUM = Integer.parseInt(mappingFileCount);
		}
	}

	@Override
	public Element getElement(ElementIndex elementIndex) throws Exception {
		lock.lock();
		try {
			String elementName = elementIndex.getElementName();
			byte location = elementIndex.getLocation();
			if (location == LOCATION_MEMOERY)
				return queue.remove(0);
			// 获取从哪个缓存文件中读取内容
			Store store = elementIndex.getStore();
			if (store == null)
				throw new Exception("ElementName=" + elementIndex.getElementName() + ",所在的cache对象为空");
			Element element = store.getElement(elementName);
			
			// 如果store已不可用了，且是MemoryMappintDiskStore，则mappingFileCount数量要减1个
			if (store instanceof MemoryMappintDiskStore && !store.isAvailable()) {
				--mappingFileCount;
			}
			
			return element;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Element getNextElement() throws Exception {
		lock.lock();
		try {
			while (this.indexs.isEmpty()) {
				if (this.commitFlag && this.exportNum >= size)
					return null;
				// 线程挂起0.1秒
				this.noElement.awaitNanos(100000000L);
			}
			ElementIndex index = indexs.get(0);
			Element element = getElement(index);
			if (element != null) {
				this.exportNum++;
				indexs.remove(0);
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
	public void addElement(Object elementValue) throws Exception {
		lock.lock();
		try {
			if (elementValue == null)
				throw new NullPointerException("BlockingCacher.addElement() 传入参数Object为空.");
			Element element = new Element(keyProducer.getNextElementKey(), elementValue);
			addElement(element);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void addElement(Element element) throws Exception {
		if (element == null)
			throw new NullPointerException("BlockingCacher.addElement() 传入参数element为空.");
		lock.lock();
		try {
			ElementIndex index = new ElementIndex();
			// 如果内存未满 则直接将对象添加至内存中
			if (queue.size() < MAX_QUEUE_SIZE) {
				queue.add(element);
				index.setElementName(element.getElementKey());
				index.setLocation(LOCATION_MEMOERY);
				indexs.add(index);
				this.size++;
				return;
			}
			byte[] bs = getBytes(element);
			// 创建缓存文件
			Store store = this.getWiterDes(bs.length);
			store.addElement(element.getElementKey(), bs);
			size++;
			index.setElementName(element.getElementKey());
			index.setLocation(LOCATION_DISK);
			index.setStore(store);
			indexs.add(index);
		} finally {
			lock.unlock();
			noElement.signalAll();
		}
	}

	static byte[] getBytes(Element element) throws IOException {
		ByteArrayOutputStream byteOut = null;
		ObjectOutputStream objOut = null;
		try {
			byteOut = new ByteArrayOutputStream();
			objOut = new ObjectOutputStream(byteOut);
			objOut.writeObject(element);
			objOut.flush();
			byteOut.flush();
			return byteOut.toByteArray();
		} finally {
			IOUtils.closeQuietly(byteOut);
			IOUtils.closeQuietly(objOut);
		}
	}

	/**
	 * 获取写入的MemoryMappintDiskStore, 此函数在lock内部调用，不用担心线程安全
	 * 
	 * @param length
	 * @return Store
	 */
	private Store getWiterDes(long length) {
		Store store = null;
		if (this.diskStores.size() == 0) {
			store = new MemoryMappintDiskStore(createFileName());
			diskStores.add(store);
			return store;
		}
		store = diskStores.get(diskStores.size() - 1);
		// 如果store不为空 并且
		if (store != null && store.ensureCapacity(length))
			return store;
		
		
		// 如果store已不可用了，且是MemoryMappintDiskStore，则mappingFileCount数量要减1个
		if (store != null && store instanceof MemoryMappintDiskStore && !store.isAvailable()) {
			--mappingFileCount;
		}
		/**
		 * 	<pre>
		 *  	如果store的个数已经大于或等于最大镜像个数， 则创建MemoryAutoMappingDiskStore，数据直接缓存到硬盘，
		 *  	不再由MemoryMappintDiskStore去创建虚拟内存，等需要的时候，再创建虚拟内存，以免虚拟内存创建过大
		 *  </pre>
		 */
		if (mappingFileCount >= MAX_MAPPING_STORE_NUM) {
			store = new MemoryLocalDiskStore(createFileName());
		}
		else {
			store = new MemoryMappintDiskStore(createFileName());
			++mappingFileCount;
		}
		diskStores.add(store);
		
		return store;
	}

	String createFileName() {
		return new StringBuilder(cacheFileName).append("_").append(storeNums++).toString();
	}

	@Override
	public void shutdown() {
		lock.lock();
		try {
			this.queue.clear();
			this.queue = null;
			for (Store diskStore : this.diskStores) {
				if (diskStore != null)
					diskStore.shutdown();
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int size() {
		lock.lock();
		try {
			return this.size;
		} finally {
			lock.unlock();
		}
	}
}
