package cn.uway.usummary.entity;

import java.io.Serializable;

/**
 * 缓存数据对象 以key-value形式存在<br>
 * Element为final类，不允许继承 elementKey和elementValue不允许修改
 * 
 */
public final class Element implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 缓存对象key
	 */
	private final String elementKey;

	/**
	 * 缓存对象value
	 */
	private final Object elementValue;

	/**
	 * @param elementKey
	 * @param element
	 */
	public Element(String elementKey, Object elementValue) {
		super();
		this.elementKey = elementKey;
		this.elementValue = elementValue;
	}

	public String getElementKey() {
		return elementKey;
	}

	public Object getElementValue() {
		return elementValue;
	}

	/**
	 * 覆写equals方法 保证缓存中对象唯一性
	 */
	@Override
	public final boolean equals(final Object object) {
		if (object == null || !(object instanceof Element))
			return false;
		Element element = (Element) object;
		if (elementKey == null || element.getElementKey() == null)
			return false;
		return elementKey.equals(element.getElementKey());
	}

	/**
	 * 覆写hashCode方法 保证缓存中对象唯一性
	 */
	@Override
	public final int hashCode() {
		return elementKey.hashCode();
	}
}
