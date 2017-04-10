package cn.uway.ucloude.serialize;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Uway-M3
 *
 */
public interface IJsonSerialize {
	
	/**
	 * 序列化
	 * @param obj
	 * @return
	 */
	String serialize(Object obj);
	
	/** 
	 * 反序列化普通对象
	 * @param json
	 * @param sourceClass
	 * @return
	 */
	<T> T deSerialize(String json, Class<T> sourceClass);
	
	
	/** 
	 * 反序列化普通对象
	 * @param json
	 * @param sourceClass
	 * @return
	 */
	<T> T deSerialize(String json, Type t);
	
	/**
	 * 反序列化数组
	 * @param json
	 * @param sourceClass
	 * @return
	 */
	<T> List<T> deSerializeArray(String json, Class<T> sourceClass);
}
