package cn.uway.usummary.cache.store;

import java.util.List;

import cn.uway.usummary.entity.Element;

/**
 * 抽象存储器实现
 * 
 */
public abstract class AbstractStore implements Store {

	/**
	 * 存储器容量大小
	 */
	protected int maxElementNum;

	protected int size = 0;

	/**
	 * 当前存储器中为空的索引位置 主要在内存存储是使用<br>
	 * 文件存储目前不考虑磁盘空间问题、默认不删除.新增对象会往文件尾部追加<br>
	 * List使用LinkedList实现，快速判断空的索引位置
	 */
	protected List<Integer> emptyIndex;

	@Override
	public Element removeElement(Element element) throws Exception {
		if (element == null || element.getElementKey() == null)
			return null;
		return removeElement(element.getElementKey());
	}

}
