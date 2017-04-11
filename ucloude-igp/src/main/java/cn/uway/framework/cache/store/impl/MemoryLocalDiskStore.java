package cn.uway.framework.cache.store.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;

import cn.uway.framework.cache.Element;
import cn.uway.framework.cache.store.AbstractStore;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;


public class MemoryLocalDiskStore extends AbstractStore {
	// 日志类
	protected static final ILogger LOGGER = LoggerManager.getLogger(MemoryLocalDiskStore.class); 
	
	// 缓存文件
	protected File storeFile;
	
	// 缓存文件名
	protected String cacheFileName;
	
	// 文件写入流对象
	protected OutputStream ofs;
	
	// 文件诗读入对象
	protected RandomAccessFile randomAccessFile;
	
	// 缓存读写锁
	protected ReentrantLock lock = new ReentrantLock();
	
	// 缓存文件长度
	protected int cacheFileLength = 0;
	
	// 文件读取位置
	protected int readPos = 0;

	// 文件写入时长
	protected long readTime = 0L;
	
	// 文件写入时长
	protected long writeTime = 0L;
	
	// 对象反序列化时长
	protected long reserObjTime = 0L;
	
	//protected int size = 0;

	// 对象读取个数
	protected int readNum = 0;
	
	// 是否在写入模式;
	protected boolean isWriteMode = true;
	
	public final static String CACHE_FILE_EXT = ".cache.local";
	
	/**
	 * 数据存数索引
	 */
	protected Map<String, ElementFileIndex> elementIndex = new ConcurrentHashMap<String, ElementFileIndex>();
	
	public MemoryLocalDiskStore(String fileName) {
		try {
			this.cacheFileName = MemoryMappintDiskStore.CACHE_DIR + File.separator + fileName + CACHE_FILE_EXT;
			this.storeFile = new File(this.cacheFileName);
			ofs = new FileOutputStream(this.storeFile);
			LOGGER.debug("创建缓存文件:{}", this.cacheFileName);
		} catch (IOException e) {
			LOGGER.error("初始化文件缓存失败", e);
		}
	}
	
	
	@Override
	public void addElement(Element element) throws Exception {
		throw new UnsupportedOperationException("不支持的操作");
	}

	@Override
	public void addElement(String elementName, byte[] bs) throws Exception {
		lock.lock();
		try {
			long start = System.currentTimeMillis();
			int len = bs.length;
			ElementFileIndex elemenFiletIndex = new ElementFileIndex(elementName, cacheFileLength, len);
			elementIndex.put(elementName, elemenFiletIndex);
						
			start = System.currentTimeMillis();
			//写入文件
			ofs.write(bs);
			
			//写成功才能更新尺寸和数量
			cacheFileLength += len;
			++size;
			this.writeTime += (System.currentTimeMillis() - start);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Element getElement(String elementName) throws Exception {
		ByteArrayInputStream bIn = null;
		ObjectInputStream objIn = null;
		lock.lock();
		ElementFileIndex elementFileIndex = null;
		try {
			long start = System.currentTimeMillis();
			elementFileIndex = this.elementIndex.get(elementName);
			if (elementFileIndex == null)
				throw new IllegalArgumentException("MemoryAutoMappingDiskStore.getElement()参数错误.给定ElementName=" + elementName + "在缓存文件中不存在.");
			
			if (randomAccessFile == null) {
				// 读取仍用文件直接读，用FileMapping经测试在此种情况下更慢。
				randomAccessFile = new RandomAccessFile(this.storeFile, "r");
			}
			
			byte[] bs = new byte[elementFileIndex.getElementSize()];
			int elementCachePos = elementFileIndex.getBeginPosition();
			// 文件读取是顺序的，一般情况下是不会有seek动作
			if (this.readPos != elementCachePos) {
				randomAccessFile.seek(elementCachePos);
				this.readPos = elementCachePos;
			}
			
			int offset = 0;
			while (offset < bs.length) {
				int readLength = randomAccessFile.read(bs, offset, bs.length-offset);
				if (readLength <=0 ) {
					throw new Exception("MemoryAutoMappingDiskStore.getElement() 读取超出文件边界, 文件名："+ elementName + ",位置:" + this.readPos);
				}
				
				offset += readLength;
				this.readPos += readLength;
			}
			
			this.readTime += (System.currentTimeMillis() - start);
			start = System.currentTimeMillis();
			
			bIn = new ByteArrayInputStream(bs);
			objIn = new ObjectInputStream(bIn);
			Object obj = objIn.readObject();
			this.reserObjTime += (System.currentTimeMillis() - start);
			++this.readNum;
			
			// 如果已经输出完成 则直接关闭当前store对象 不需要外部再关闭(因为本地存储文件，不支持同时读写，所以在读完时，可以直接关闭）
			if (size <= readNum)
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

	@Override
	public boolean ensureCapacity(long length) {
		lock.lock();
		try {
			// 如果已经写满 则返回true。
			if (cacheFileLength + length > MemoryMappintDiskStore.FILE_SIZE_BYTES) {
				// 如果已经输出完成 则直接关闭当前store对象 不需要外部再关闭
				if (size <= readNum)
					shutdown();
				else
					closeWriteStream();
				
				return false;
			}
			
			// 如果文件已关闭，同样返回true,因为文件不支持同时读写
			if (!isWriteMode)
				return false;
			
			return true;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void shutdown() {
		LOGGER.debug("FileName={},writeNum={},writeTime={},readNum={},readTime={},reserTime={}", new Object[]{this.storeFile.getAbsolutePath(), size,
				writeTime, readNum, readTime, reserObjTime});
		
		closeWriteStream();
		closeReadStream();
		
		if (storeFile.exists()) {
			LOGGER.debug("缓存文件{}已删除{}", new Object[]{storeFile.getName(), this.storeFile.delete() ? "成功" : "失败"});
		}
	}
	
	/**
	 * 关闭写入流
	 */
	protected void closeWriteStream() {
		if (this.ofs != null) {
			try {
				this.ofs.flush();
				this.ofs.close();
				this.ofs = null;
				isWriteMode = false;
			}
			catch (Exception e) {
				LOGGER.warn("关闭文件写入流发生了异常.", e);
			}
		}
	}
	
	/**
	 * 关闭读取流
	 */
	protected void closeReadStream() {
		if (this.randomAccessFile != null) {
			try {
				this.randomAccessFile.close();
				this.randomAccessFile = null;
			}
			catch (Exception e) {
				LOGGER.warn("关闭文件读取流发生了异常.", e);
			}
		}
	}

	@Override
	public boolean isAvailable() {
		lock.lock();
		try {
			if (this.ofs == null && this.randomAccessFile == null) {
				return false;
			}
			
			return true;
		} finally {
			lock.unlock();
		}
	}

}
