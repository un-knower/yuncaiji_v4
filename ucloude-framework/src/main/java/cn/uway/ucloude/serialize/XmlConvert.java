package cn.uway.ucloude.serialize;

/**
 * Xml 序列化 反序列化
 * 
 * @author Uway-M3
 */
public class XmlConvert {

	private static IXmlSerialize _normalHandel = new NormalXmlSerialize();

	private static IXmlSerialize getHandel(XmlSerializeType xmlType) {
		IXmlSerialize handel = _normalHandel;
		// TODO适配
		return handel;
	}

	/**
	 * @param obj
	 *            需要序列化的对象
	 * @param xmlType
	 *            序列化方式
	 * @return xml字符串
	 * @throws JAXBException
	 */
	public static String serialize(Object obj, XmlSerializeType xmlType) {
		return getHandel(xmlType).serialize(obj);
	}

	/**
	 * @param obj
	 *            需要序列化的对象
	 * @return xml字符串
	 */
	public static String serialize(Object obj) {
		return serialize(obj, XmlSerializeType.Normal);
	}

	/**
	 * @param xml
	 *            xml字符串
	 * @return 指定类型的对象
	 */
	public static <T> T deSerialize(String xml, Class<T> sourceClass, XmlSerializeType xmlType) {
		return getHandel(xmlType).deSerialize(xml, sourceClass);
	}

	/**
	 * @param xml
	 *            xml字符串
	 * @return 指定类型的对象
	 */
	public static <T> T deSerialize(String xml, Class<T> sourceClass) {
		return deSerialize(xml, sourceClass, XmlSerializeType.Normal);
	}
}
