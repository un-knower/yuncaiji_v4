package cn.uway.ucloude.serialize;

import java.lang.reflect.Type;
import java.util.List;

import com.alibaba.fastjson.JSON;

import cn.uway.ucloude.container.SPI;
@SPI(key = "ucloude.json", dftValue = "fastjson")
public class FastjsonAdapter implements JsonAdapter {
	/**
	 * @param obj
	 * @return
	 */
	@Override
	public String serialize(Object obj) {
		return JSON.toJSONString(obj,false);
	}
	
	@Override
	public <T> T deSerialize(String json, Type t){
		return JSON.parseObject(json, t);
	}

	/**
	 * @param json
	 * @param sourceClass
	 * @return
	 */
	@Override
	public <T> T deSerialize(String json, Class<T> sourceClass) {
		return JSON.parseObject(json, sourceClass);
	}

	@Override
	public <T> List<T> deSerializeArray(String json, Class<T> sourceClass) {
		return JSON.parseArray(json, sourceClass);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		 return "fastjson";
	}
}
