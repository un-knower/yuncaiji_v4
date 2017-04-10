package cn.uway.usummary.cache.store;

import cn.uway.usummary.entity.Element;

/**
 * 存储器接口<br>
 * 定义缓存框架存储标准方法<br>
 * 常见操作 add、get、remove<br>
 * 缓存cacher调用store的isFull方法获取当前存储器的使用情况
 * 
 */
public interface Store {

	/**
	 * 向存储器中插入对象
	 * 
	 * @param element
	 * @throws Exception
	 */
	void addElement(Element element) throws Exception;

	/**
	 * 向存储器中插入对象
	 * 
	 * @param elementName
	 * @param bs
	 * @throws Exception
	 */
	void addElement(String elementName, byte[] bs) throws Exception;

	/**
	 * 通过elementKey获取存储器中对象
	 * 
	 * @param elementKey
	 * @return Element
	 * @throws Exception
	 */
	Element getElement(String elementKey) throws Exception;

	/**
	 * 从存储器中删除对象 并且返回被删除的对象
	 * 
	 * @param element
	 * @return Element
	 */
	Element removeElement(Element element) throws Exception;

	/**
	 * 从存储器中删除对象 并且返回被删除的对象
	 * 
	 * @param elementKey
	 * @return Element
	 * @throws Exception
	 */
	Element removeElement(String elementKey) throws Exception;

	/**
	 * 判断存储器是否仍然可以存放得下指定尺寸的容量
	 * 
	 * @return 如果可以存放得下指定的容量，返回true,否则返回false;
	 */
	boolean ensureCapacity(long length);
	
	/**
	 * 是否有效，如store已经被使用完了，就返回true,否则返回false
	 * @return
	 */
	boolean isAvailable();

	/**
	 * 关闭存储器
	 */
	void shutdown();

}
