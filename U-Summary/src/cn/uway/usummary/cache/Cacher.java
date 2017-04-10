package cn.uway.usummary.cache;

import cn.uway.usummary.entity.Element;
import cn.uway.usummary.entity.ElementIndex;

/**
 * Cacher interface 支持缓存多种类型数据
 * 
 * @author chenrongqiang
 * @version 1.0
 * @since 3.0 2012-12-16
 */
public interface Cacher {

	/**
	 * 根据数据类型从Cache中查找缓存数据块 取值时会根据缓存加入顺序
	 * 
	 * @param dataType
	 * @return BlockData
	 */
	Element getElement(ElementIndex elementIndex) throws Exception;

	/**
	 * 获取下一个缓存的数据块
	 * 
	 * @param dataType
	 *            数据类型
	 * @param elementKey
	 *            上次从缓存中获取的Element key
	 * @return
	 */
	Element getNextElement() throws Exception;

	/**
	 * 将数据块写入缓存中
	 * 
	 * @param element
	 */
	void addElement(Element element) throws Exception;

	/**
	 * 将数据块写入缓存中
	 * 
	 * @param blockData
	 * @param dataType
	 * @return 是否添加成功
	 */
	void addElement(Object elementValue) throws Exception;

	/**
	 * commit提交，表示本次仓库写入数据完毕
	 */
	void commit();

	/**
	 * 外部调用该方法判断数据是否写入结束
	 * 
	 * @return 是否写入结束
	 */
	boolean isCommit();

	/**
	 * 关闭缓存
	 */
	void shutdown();

	/**
	 * 返回缓存的大小
	 * 
	 * @return
	 */
	int size();
}
