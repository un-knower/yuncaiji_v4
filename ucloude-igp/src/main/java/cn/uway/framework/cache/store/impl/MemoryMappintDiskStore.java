package cn.uway.framework.cache.store.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;

import cn.uway.framework.cache.Element;
import cn.uway.framework.cache.store.AbstractStore;
import cn.uway.framework.context.AppContext;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.IoUtil;

/**
 * 文件存储器 使用RandomAccessFile将文件序列化到本地<br>
 * 目前
 * 
 * @author chenrongqiang @ 2013-1-26
 */
public class MemoryMappintDiskStore extends AbstractStore {

	/**
	 * 数据存储文件
	 */
	protected File storeFile;

	protected RandomAccessFile randomAccessFile;

	protected FileChannel fileChannel;

	protected ByteBuffer byteBuffer;

	protected ReentrantLock lock = new ReentrantLock();

	protected int readNum = 0;

	protected long writeTime = 0L;

	protected long reserObjTime = 0L;

	protected long readTime = 0L;
	
	/**
	 * 容量是否已经满
	 */
	protected volatile boolean capacityFull = false;
	
	/**
	 * 缓存文件扩展名
	 */
	public final static String CACHE_FILE_EXT = ".cache.mapping";

	/**
	 * 数据存数索引
	 */
	protected Map<String, ElementFileIndex> elementIndex = new ConcurrentHashMap<String, ElementFileIndex>();

	protected int lastWritePosition = 0;

	protected static final long FILE_SIZE_BYTES = AppContext.getBean("cacheFileSizeMB", java.lang.Integer.class) * 1024 * 1024;

	protected static final String CACHE_DIR = AppContext.getBean("cacheFileDir", String.class);

	protected static final ILogger LOGGER = LoggerManager.getLogger(MemoryMappintDiskStore.class); // 日志

	static {
		File dir = new File(CACHE_DIR);
		if (!dir.exists() || !dir.isDirectory())
			dir.mkdirs();
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				f.delete();
			}
		}
	}

	public MemoryMappintDiskStore(String cacheFileName) {
		try {
			String mappingFile = CACHE_DIR + File.separator + cacheFileName + CACHE_FILE_EXT;
			this.storeFile = new File(mappingFile);
			LOGGER.debug("创建缓存文件:{}", mappingFile);
			this.randomAccessFile = new RandomAccessFile(this.storeFile, "rw");
			this.fileChannel = randomAccessFile.getChannel();
			this.byteBuffer = this.fileChannel.map(MapMode.READ_WRITE, 0, FILE_SIZE_BYTES);
		} catch (IOException e) {
			LOGGER.error("初始化文件缓存失败", e);
		}
	}

	@Override
	public void addElement(String elementName, byte[] bs) throws Exception {
		lock.lock();
		ByteArrayOutputStream byteOut = null;
		ObjectOutputStream objOut = null;
		// if(this.storeFile.getName().equals("512_93_1_CHR_2_20130702012500.dat.gz_22_4")
		// && size == 10)
		// throw new IOException("测试线程错误");
		try {
			long start = System.currentTimeMillis();
			int len = bs.length;
			ElementFileIndex elemenFiletIndex = new ElementFileIndex(elementName, lastWritePosition, len);
			elementIndex.put(elementName, elemenFiletIndex);
			if (lastWritePosition + len <= this.byteBuffer.capacity()) {
				start = System.currentTimeMillis();
				this.byteBuffer.position(lastWritePosition);
				this.byteBuffer.put(bs);
				
				lastWritePosition += len;
				size++;
				this.writeTime += (System.currentTimeMillis() - start);
				return;
			}
			LOGGER.warn("文件已写满,无法再将Element加入到缓存中");
		} finally {
			IOUtils.closeQuietly(byteOut);
			IOUtils.closeQuietly(objOut);
			lock.unlock();
		}
	}

	@Override
	public void addElement(Element element) throws IOException {
		throw new UnsupportedOperationException("不支持的操作");

	}

	@Override
	public Element getElement(String elementName) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bIn = null;
		ObjectInputStream objIn = null;
		lock.lock();
		ElementFileIndex elementFileIndex = null;
		// if(this.storeFile.getName().equals("512_93_1_CHR_2_20130702012500.dat.gz_22_4")
		// && readNum == 10)
		// throw new IOException("测试线程错误");
		try {
			long start = System.currentTimeMillis();
			elementFileIndex = this.elementIndex.get(elementName);
			if (elementFileIndex == null)
				throw new IllegalArgumentException("MemoryMappintDiskStore.getElement()参数错误.给定ElementName=" + elementName + "在缓存文件中不存在.");
			byte[] bs = new byte[elementFileIndex.getElementSize()];
			this.byteBuffer.position(elementFileIndex.getBeginPosition());
			this.byteBuffer.get(bs, 0, bs.length);
			this.readTime += (System.currentTimeMillis() - start);
			start = System.currentTimeMillis();
			bIn = new ByteArrayInputStream(bs);
			objIn = new ObjectInputStream(bIn);
			Object obj = objIn.readObject();
			this.reserObjTime += (System.currentTimeMillis() - start);
			this.readNum++;
			// 如果已经输出完成, 且缓存已经用完则直接关闭当前store对象 不需要外部再关闭
			if (size <= readNum && capacityFull)
				shutdown();
			return (Element) obj;
		} finally {
			IOUtils.closeQuietly(bIn);
			IOUtils.closeQuietly(objIn);
			lock.unlock();
		}
	}

	/**
	 * 文件存储目前不会删除数据 直接返回getElement的结果
	 */
	@Override
	public Element removeElement(String elementKey) throws Exception {
		return getElement(elementKey);
	}

	/**
	 * 判断文件是否还可以继续写入数据
	 */
	@Override
	public boolean ensureCapacity(long length) {
		lock.lock();
		try {
			// 如果已经写满 则返回true。并且将isFull设置为true
			if (lastWritePosition + length > this.byteBuffer.capacity()) {
				capacityFull = true;
				// 如果已经输出完成 则直接关闭当前store对象 不需要外部再关闭
				if (size <= readNum)
					shutdown();
				
				return false;
			}
			
			return true;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void shutdown() {
		LOGGER.debug("FileName={},writeNum={},writeTime={},readNum={},readTime={},reserTime={}", new Object[]{this.storeFile.getAbsolutePath(), size,
				writeTime, readNum, readTime, reserObjTime});
		clean(byteBuffer);
		IoUtil.closeQuietly(this.fileChannel);
		IoUtil.closeQuietly(randomAccessFile);
		
		byteBuffer = null;
		this.fileChannel = null;
		randomAccessFile = null;
		if (storeFile.exists())
			LOGGER.debug("缓存文件{}已删除{}", new Object[]{storeFile.getName(), this.storeFile.delete() ? "成功" : "失败"});
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void clean(final Object buffer) {
		AccessController.doPrivileged(new PrivilegedAction() {

			public Object run() {
				try {
					Method getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);
					getCleanerMethod.setAccessible(true);
					sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(buffer, new Object[0]);
					cleaner.clean();
				} catch (Exception e) {
				}
				return null;
			}
		});
	}

	@Override
	public boolean isAvailable() {
		lock.lock();
		try {
			if (size <= readNum && capacityFull)
				return false;
			
			if (byteBuffer == null)
				return false;
			
			return true;
		} finally {
			lock.unlock();
		}
	}
}
