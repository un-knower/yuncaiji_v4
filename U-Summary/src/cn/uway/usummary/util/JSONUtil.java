package cn.uway.usummary.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jodd.util.StringUtil;

public class JSONUtil {
	
	/**
	 * 将String转换为map
	 * @param json
	 * @return
	 * @throws JSONException
	 */
	public static Map<String,String> covertStringToMap(String json) throws JSONException{		
		if(StringUtil.isEmpty(json)){
			return null;
		}
		Map<String,String> map = new HashMap<String,String>();
		JSONObject jsonObj = new JSONObject(json);  
        Iterator it = jsonObj.keys();  
        String key = null;
        while(it.hasNext()) {  
            key = it.next().toString();  
            map.put(key, jsonObj.getString(key));
        }  
		return map;
	}
}
