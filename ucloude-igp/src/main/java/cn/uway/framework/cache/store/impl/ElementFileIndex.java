package cn.uway.framework.cache.store.impl;

/**
 * 本地磁盘文件缓存索引对象<br>
 * 本地磁盘使用RandomAccessFile操作本地文件.读取对象时使用beginPosition+elementSize反序列化文件内容， 并且返回Element
 * 
 * @author chenrongqiang @ 2013-1-26
 */
public final class ElementFileIndex {

	/**
	 * 存储的Element对象的key
	 */
	private final String elementKey;

	/**
	 * Element对象的在文件中的开始位置
	 */
	private final int beginPosition;

	/**
	 * Element对象的在文件中的长度
	 */
	private final int elementSize;

	/**
	 * @param elementKey
	 * @param beginPosition
	 * @param elementSize
	 */
	public ElementFileIndex(String elementKey, int beginPosition, int elementSize) {
		super();
		this.elementKey = elementKey;
		this.beginPosition = beginPosition;
		this.elementSize = elementSize;
	}

	public String getElementKey() {
		return elementKey;
	}

	public int getBeginPosition() {
		return beginPosition;
	}

	public int getElementSize() {
		return elementSize;
	}
}
