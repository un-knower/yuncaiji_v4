package cn.uway.ucloude.serialize;

/**
 * 二进制 序列化 反序列化
 * 
 * @author Uway-M3
 */
public class BinaryConvert {

	private static IBinarySerialize _normalHandel = new NormalBinarySerialize();

	private static IBinarySerialize getHandel(BinarySerializeType binaryType) {
		IBinarySerialize handel = _normalHandel;
		// TODO适配
		return handel;
	}

	/**
	 * @param obj
	 *            需要序列化的对象
	 * @return 二进制数据
	 */
	public static byte[] serialize(Object obj, BinarySerializeType type) {
		return getHandel(type).serialize(obj);
	}
	
	/**
	 * @param obj
	 *            需要序列化的对象
	 * @return 二进制数据
	 */
	public static byte[] serialize(Object obj) {
		return serialize(obj,BinarySerializeType.Normal);
	}

	/**
	 * @param bytes
	 *            二进制数据
	 * @return 指定类型的对象
	 */
	public static <T> T deSerialize(byte[] bytes, BinarySerializeType type) {
		return getHandel(type).deSerialize(bytes);
	}
	
	/**
	 * @param bytes
	 *            二进制数据
	 * @return 指定类型的对象
	 */
	public static <T> T deSerialize(byte[] bytes) {
		return deSerialize(bytes,BinarySerializeType.Normal);
	}
}
