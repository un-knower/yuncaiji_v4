package cn.uway.ucloude.serialize;

/**
 * xml序列化接口
 * 
 * @author Uway-M3
 *
 */
public interface IXmlSerialize {
	/**
	 * @param xml
	 *            XML字符串
	 * @param sourceClass
	 *            对象类型
	 * @return
	 */
	<T> T deSerialize(String xml, Class<T> sourceClass);

	/**
	 * @param obj
	 *            序列化对象
	 * @return
	 */
	String serialize(Object obj);
}
