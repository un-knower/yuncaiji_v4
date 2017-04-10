package cn.uway.ucloude.serialize;

import java.lang.reflect.Type;
import java.util.List;


import cn.uway.ucloude.utils.StringUtil;

/**
 * Json 序列化 反序列化
 * 
 * @author Uway-M3
 */
public class JsonConvert {

	 private static JsonAdapter adapter = JsonFactory.getJSONAdapter();

	

	/**
	 * @param obj
	 *            需要序列化的对象
	 * @return 返回Json字符串
	 */
	public static String serialize(Object obj) {
		return adapter.serialize(obj);
	}



	/**
	 * @param json
	 *            json字符串
	 * @return 返回指定类型的对象
	 */
	public static <T> T deserialize(String json, Class<T> sourceClass) {
		return adapter.deSerialize(json, sourceClass);
	}
	
    public static final <T> T deserialize(String json, TypeReference<T> typeReference) {
        try {
            if(StringUtil.isEmpty(json)){
                return null;
            }
            return adapter.deSerialize(json, typeReference.getType());
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }
	

    public static final <T> T deserialType(String json, Type type) {
        try {
            if (StringUtil.isEmpty(json)) {
                return null;
            }
            return adapter.deSerialize(json, type);
        } catch (Exception ex) {
            throw new JSONException(ex);
        }
    }

	
	/**
	 * @param json
	 *            json字符串
	 * @return 返回指定类型的对象
	 */
	public static <T> T deserialize(String json, Type t) {
		return adapter.deSerialize(json, t);
		
	}
	
	



	
	

	/**
	 * @param json
	 *            json字符串
	 * @return 返回指定类型的对象
	 */
	public static <T> List<T> deSerializeArray(String json, Class<T> sourceClass) {
		return adapter.deSerializeArray(json, sourceClass);
	}


}
