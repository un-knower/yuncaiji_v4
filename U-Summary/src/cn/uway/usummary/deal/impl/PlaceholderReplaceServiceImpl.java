package cn.uway.usummary.deal.impl;

import java.util.Map;
import java.util.Set;

import cn.uway.usummary.deal.ParamReplaceService;

public class PlaceholderReplaceServiceImpl extends ParamReplaceService{

	@Override
	public String replace(String sql, Map<String, String> map) {
		if(map == null || map.size() == 0){
			return sql;
		}
		Set<String> set  = map.keySet();
		for(String key: set){
			sql = sql.replaceAll("\\{"+key.toUpperCase()+"\\}", map.get(key));	
		}
		return sql;
	}

	@Override
	public Integer storageType() {
		return 2;
	}

}

